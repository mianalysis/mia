package io.github.mianalysis.mia;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Dependencies;
import io.github.mianalysis.mia.module.LostAndFound;
import io.github.mianalysis.mia.object.Preferences;
import io.github.mianalysis.mia.process.DependencyValidator;
import io.github.mianalysis.mia.process.analysishandling.Analysis;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.logging.BasicLogRenderer;
import io.github.mianalysis.mia.process.logging.ConsoleRenderer;
import io.github.mianalysis.mia.process.logging.Log;
import io.github.mianalysis.mia.process.logging.LogHistory;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import net.imagej.ImageJ;
import net.imagej.ImageJService;


/**
 * Created by Stephen Cross on 14/07/2017.
 */
@Plugin(type = Command.class, menuPath = "Plugins>MIA>MIA (Modular Image Analysis)", visible=true)
public class MIA implements Command {
    private static ArrayList<String> pluginPackageNames = new ArrayList<>();
    private static String version = "";
    private static boolean debug = false;
    private static LogRenderer mainRenderer = new BasicLogRenderer();
    private static LogHistory logHistory = new LogHistory();
    private final static boolean headless = false; // Determines if there is a GUI

    public static Preferences preferences;
    public static Log log = new Log(mainRenderer); // This is for testing and headless modes
    public final static Dependencies dependencies = new Dependencies(); // Maps module dependencies and reports if a module's requirements aren't satisfied
    public final static LostAndFound lostAndFound = new LostAndFound(); // Maps missing modules and parameters to replacements (e.g. if a module was renamed)

    /*
        Gearing up for the transition from ImagePlus to ImgLib2 formats.  Modules can use this to addRef compatibility.
         */
    private static final boolean imagePlusMode = true;

    @Parameter
    public static ImageJService ijService;


    public static void main(String[] args) throws Exception {
        debug = true;

        try {
            if (args.length == 0) {
                ImageJ ij = new ImageJ();                
                ij.ui().showUI();
                ij.command().run("io.github.mianalysis.mia.MIA", false);
            } else {
                preferences = new Preferences(null);
                Analysis analysis = AnalysisReader.loadAnalysis(args[0]);
                new AnalysisRunner().run(analysis);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        preferences = new Preferences(null);
        
        setLookAndFeel();
        
        try {
            if (!headless) {
                // Before removing the old renderer we want to check the new one can be created
                UIService uiService = ijService.context().getService(UIService.class);
                LogRenderer newRenderer = new ConsoleRenderer(uiService);
                log.removeRenderer(mainRenderer);

                mainRenderer = newRenderer;
                mainRenderer.setWriteEnabled(LogRenderer.Level.DEBUG, debug);
                log.addRenderer(mainRenderer);

            }
        } catch (Exception e) {
            // If any exception was thrown, just don't apply the ConsoleRenderer.
        }

        log.addRenderer(logHistory);

        // Determining the version number from the pom file
        try {
            FileReader reader = new FileReader("pom.xml");
            Model model = new MavenXpp3Reader().read(reader);
            reader.close();
            version = new MavenProject(model).getVersion();
        } catch (XmlPullParserException | IOException e) {
            version = getClass().getPackage().getImplementationVersion();
        }

        // Run the dependency validator.  If updates were required, return.
        if (DependencyValidator.run())
            return;
                
        try {
            new GUI();
        } catch (InstantiationException | IllegalAccessException e) {
            MIA.log.writeError(e);
        }
    }

    public void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public static boolean isImagePlusMode() {
        return imagePlusMode;
    }

    public static void addPluginPackageName(String packageName) {
        pluginPackageNames.add(packageName);
    }

    public static ArrayList<String> getPluginPackages() {
        return pluginPackageNames;
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
}
