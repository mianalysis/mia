package io.github.mianalysis.mia;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.Prefs;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.LostAndFound;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.moduledependencies.Dependencies;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.process.DependencyValidator;
import io.github.mianalysis.mia.process.analysishandling.Analysis;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.logging.BasicLogRenderer;
import io.github.mianalysis.mia.process.logging.HeadlessRenderer;
import io.github.mianalysis.mia.process.logging.Log;
import io.github.mianalysis.mia.process.logging.LogHistory;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import io.github.mianalysis.mia.process.logging.LogRenderer.Level;
import net.imagej.ImageJ;
import net.imagej.ImageJService;

/**
 * Created by Stephen Cross on 14/07/2017.
 */
@Plugin(type = Command.class, menuPath = "Plugins>ModularImageAnalysis (MIA)", visible = true)
public class MIA implements Command {
    private static String version = "";
    private static boolean debug = false;
    private static LogRenderer mainRenderer = new BasicLogRenderer();
    private static LogHistory logHistory = new LogHistory();
    private static boolean headless = false; // Determines if there is a GUI
    private static Preferences preferences;
    private static Dependencies dependencies; // Maps module dependencies and reports if a
    // module's requirements aren't satisfied
    private static LostAndFound lostAndFound; // Maps missing modules and parameters to
    // replacements (e.g. if a module was renamed)

    public static Log log = new Log(mainRenderer); // This is for testing and headless modes

    /*
     * Gearing up for the transition from ImagePlus to ImgLib2 formats. Modules can
     * use this to addRef compatibility.
     */
    private static final boolean imagePlusMode = true;

    @Parameter
    public static ImageJService ijService;

    @Parameter(label = "Workflow file path", required = true)
    public String workflowPath;

    @Parameter(label = "Input file path", required = false, persist = false)
    public String inputFilePath;

    @Parameter(label = "showDebug", required = false, persist = false)
    public boolean showDebug = false;

    @Parameter(label = "showMemory", required = false, persist = false)
    public boolean showMemory = false;

    @Parameter(label = "showMessage", required = false, persist = false)
    public boolean showMessage = true;

    @Parameter(label = "showStatus", required = false, persist = false)
    public boolean showStatus = true;

    @Parameter(label = "showWarning", required = false, persist = false)
    public boolean showWarning = true;

    @Parameter(label = "verbose", required = false, persist = false)
    public boolean verbose = false;

    @Parameter(label = "variables", required = false, persist = false)
    public String variables;

    public static void main(String[] args) throws Exception {
        debug = true;

        try {
            if (args.length == 0) {
                new ij.ImageJ();
                new ImageJ().command().run("io.github.mianalysis.mia.MIA", false);
            } else if (args.length == 1) {
                Analysis analysis = AnalysisReader.loadAnalysis(args[0]);
                new AnalysisRunner().run(analysis);
            } else if (args.length == 2) {
                Analysis analysis = AnalysisReader.loadAnalysis(args[0]);
                analysis.getModules().getInputControl().updateParameterValue(InputControl.INPUT_PATH, args[1]);
                new AnalysisRunner().run(analysis);
            }

        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    @Override
    public void run() {
        // If parameters are specified, running in headless mode
        if (workflowPath == null)
            runInteractive();
        else
            runHeadless();

    }

    public void runHeadless() {
        headless = true;

        try {
            // Before removing the old renderer we want to check the new one can be created
            HeadlessRenderer newRenderer = new HeadlessRenderer();
            HeadlessRenderer.setShowProgress(true);
            HeadlessRenderer.setProgress(0);

            newRenderer.setWriteEnabled(LogRenderer.Level.DEBUG, showDebug);
            newRenderer.setWriteEnabled(LogRenderer.Level.MEMORY, showMemory);
            newRenderer.setWriteEnabled(LogRenderer.Level.MESSAGE, showMessage);
            newRenderer.setWriteEnabled(LogRenderer.Level.STATUS, showStatus);
            newRenderer.setWriteEnabled(LogRenderer.Level.WARNING, showWarning);

            log.removeRenderer(mainRenderer);
            log.addRenderer(newRenderer);

            mainRenderer = newRenderer;

            version = extractVersion();
            Module.setVerbose(verbose);

            Analysis analysis;
            if (inputFilePath == null) {
                analysis = AnalysisReader.loadAnalysis(new File(workflowPath));
            } else {
                analysis = AnalysisReader.loadAnalysis(new File(workflowPath));
                analysis.getModules().getInputControl().updateParameterValue(InputControl.INPUT_PATH,
                        inputFilePath);
            }

            // Inserting variables
            if (variables != null)
                applyGlobalVariables(analysis.getModules(), variables);

            // Running analysis
            new AnalysisRunner().run(analysis);

        } catch (Exception e) {
            e.printStackTrace();
        }

        java.lang.System.exit(0);

    }

    private void applyGlobalVariables(Modules modules, String variables) {
        String[] variablesArray = variables.split(";");
        for (String variable : variablesArray) {
            String[] splitVariables = variable.split(":");
            String newVariableName = splitVariables[0];
            String newVariableValue = splitVariables[1];

            for (Module module : modules.values()) {
                if (module instanceof GlobalVariables && module.isEnabled()) {
                    ParameterGroup group = module.getAllParameters().getParameter(GlobalVariables.ADD_NEW_VARIABLE);
                    if (group == null)
                        continue;

                    LinkedHashMap<Integer, Parameters> collections = group.getCollections(false);
                    for (Parameters collection : collections.values()) {
                        String variableName = collection.getValue(GlobalVariables.VARIABLE_NAME, null);
                        if (!variableName.equals(newVariableName))
                            continue;

                        collection.getParameter(GlobalVariables.VARIABLE_VALUE).setValue(newVariableValue);

                    }
                }
            }
        }
    }

    public void runInteractive() {
        try {
            String theme = Prefs.get("MIA.GUI.theme", io.github.mianalysis.mia.gui.Themes.getDefaultTheme());
            UIManager.setLookAndFeel(io.github.mianalysis.mia.gui.Themes.getThemeClass(theme));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        try {
            // Before removing the old renderer we want to check the new one can be created
            // UIService uiService = ijService.context().getService(UIService.class);
            // LogRenderer newRenderer = new ConsoleRenderer(uiService);
            HeadlessRenderer newRenderer = new HeadlessRenderer();
            newRenderer.setShowProgress(true);
            newRenderer.setWriteEnabled(Level.STATUS, true);

            log.removeRenderer(mainRenderer);

            mainRenderer = newRenderer;
            mainRenderer.setWriteEnabled(LogRenderer.Level.DEBUG, debug);
            log.addRenderer(mainRenderer);
        } catch (Exception e) {
            // If any exception was thrown, just don't apply the ConsoleRenderer.
        }

        preferences = new Preferences(null);
        log.addRenderer(logHistory);

        version = extractVersion();

        // Run the dependency validator. If updates were required, return.
        if (DependencyValidator.run())
            return;

        try {
            new GUI();
        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    private static String extractVersion() {
        // Determining the version number from the pom file
        try {
            if (new File("pom.xml").exists()) {
                FileReader reader = new FileReader("pom.xml");
                Model model = new MavenXpp3Reader().read(reader);
                reader.close();
                return model.getVersion();
            } else {
                return MIA.class.getPackage().getImplementationVersion();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return "";

    }

    public static boolean isImagePlusMode() {
        return imagePlusMode;
    }

    public static String getVersion() {
        return version;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        MIA.debug = debug;
    }

    public static Log getLog() {
        return log;
    }

    public static LogRenderer getMainRenderer() {
        return mainRenderer;
    }

    public static LogHistory getLogHistory() {
        return logHistory;
    }

    public static void clearLogHistory() {
        logHistory.clearLogHistory();
    }

    public static void setLog(Log log) {
        MIA.log = log;
    }

    public static boolean isHeadless() {
        return headless;
    }

    public static Preferences getPreferences() {
        if (preferences == null)
            preferences = new Preferences(null);

        return preferences;
    }

    public static Dependencies getDependencies() {
        if (dependencies == null)
            dependencies = new Dependencies();

        return dependencies;

    }

    public static LostAndFound getLostAndFound() {
        if (lostAndFound == null)
            lostAndFound = new LostAndFound();

        return lostAndFound;

    }
}