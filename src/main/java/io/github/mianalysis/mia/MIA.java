package io.github.mianalysis.mia;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import ij.Prefs;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.Themes;
import io.github.mianalysis.mia.module.LostAndFound;
import io.github.mianalysis.mia.moduledependencies.Dependencies;
import io.github.mianalysis.mia.object.system.Preferences;
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
@Plugin(type = Command.class, menuPath = "Plugins>ModularImageAnalysis (MIA)", visible = true)
public class MIA implements Command {
    private static String version = "";
    private static boolean debug = false;
    private static LogRenderer mainRenderer = new BasicLogRenderer();
    private static LogHistory logHistory = new LogHistory();
    private final static boolean headless = false; // Determines if there is a GUI

    public static Preferences preferences;
    public static Log log = new Log(mainRenderer); // This is for testing and headless modes
    public static Dependencies dependencies; // Maps module dependencies and reports if a
                                                                        // module's requirements aren't satisfied
    public static LostAndFound lostAndFound; // Maps missing modules and parameters to
                                                                        // replacements (e.g. if a module was renamed)

    /*
     * Gearing up for the transition from ImagePlus to ImgLib2 formats. Modules can
     * use this to addRef compatibility.
     */
    private static final boolean imagePlusMode = true;

    @Parameter
    public static ImageJService ijService;

    public static void main(String[] args) throws Exception {
        debug = true;

        try {
            if (args.length == 0) {
                new ij.ImageJ();
                new ImageJ().command().run("io.github.mianalysis.mia.MIA", false);
            } else {
                preferences = new Preferences(null);
                Analysis analysis = AnalysisReader.loadAnalysis(args[0]);
                new AnalysisRunner().run(analysis);
            }

        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    @Override
    public void run() {
        if (!headless) {
            try {
                String theme = Prefs.get("MIA.GUI.theme", Themes.getDefaultTheme());
                UIManager.setLookAndFeel(Themes.getThemeClass(theme));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
            try {

                // Before removing the old renderer we want to check the new one can be created
                UIService uiService = ijService.context().getService(UIService.class);
                LogRenderer newRenderer = new ConsoleRenderer(uiService);
                log.removeRenderer(mainRenderer);

                mainRenderer = newRenderer;
                mainRenderer.setWriteEnabled(LogRenderer.Level.DEBUG, debug);
                log.addRenderer(mainRenderer);
            } catch (Exception e) {
                // If any exception was thrown, just don't apply the ConsoleRenderer.
            }
        }

        preferences = new Preferences(null);
        dependencies = new Dependencies();
        lostAndFound = new LostAndFound();

        log.addRenderer(logHistory);

        // Determining the version number from the pom file
        try {
            if (new File("pom.xml").exists()) {
                FileReader reader = new FileReader("pom.xml");
                Model model = new MavenXpp3Reader().read(reader);
                reader.close();
                version = model.getVersion();
            } else {
                version = getClass().getPackage().getImplementationVersion();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        // Run the dependency validator. If updates were required, return.
        if (DependencyValidator.run())
            return;

        try {
            new GUI();
        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    // public void setLookAndFeel() {
    // try {
    // UIManager.put("TitlePane.showIconBesideTitle", true);
    // UIManager.setLookAndFeel(FlatLightLaf.class.getCanonicalName());
    // } catch (ClassNotFoundException | InstantiationException |
    // IllegalAccessException
    // | UnsupportedLookAndFeelException e) {
    // MIA.log.writeError(e);
    // }
    // }

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
}