package io.github.mianalysis.mia;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptService;

import io.github.mianalysis.mia.module.LostAndFound;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.moduledependencies.Dependencies;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.process.analysishandling.Analysis;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.logging.BasicLogRenderer;
import io.github.mianalysis.mia.process.logging.Log;
import io.github.mianalysis.mia.process.logging.LogHistory;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import net.imagej.ImageJService;
import net.imagej.ops.OpService;

/**
 * Created by Stephen Cross on 14/07/2017.
 */
public class MIA {
    @Parameter
    protected static ImageJService ijService;

    @Parameter
    protected static OpService opService;

    @Parameter
    protected static PluginService pluginService;

    @Parameter
    protected static ScriptService scriptService;

    private static String version = null;
    protected static boolean debug = false;
    protected static LogRenderer mainRenderer = new BasicLogRenderer();
    protected static LogHistory logHistory = new LogHistory();
    protected static boolean headless = true; // Determines if there is a GUI
    protected static Preferences preferences;
    protected static Dependencies dependencies; // Maps module dependencies and reports if a
    // module's requirements aren't satisfied
    protected static LostAndFound lostAndFound; // Maps missing modules and parameters to
    // replacements (e.g. if a module was renamed)

    public static Log log = new Log(mainRenderer); // This is for testing and headless modes

    /*
     * Gearing up for the transition from ImagePlus to ImgLib2 formats. Modules can
     * use this to addRef compatibility.
     */
    protected static final boolean imagePlusMode = true;

    public static void main(String[] args) throws Exception {
        debug = true;

        try {
            if (args.length == 0) {
                System.err.println("No workflow file path specified as command line argument");
            } else if (args.length == 1) {
                Analysis analysis = AnalysisReader.loadAnalysis(new File(args[0]));
                new AnalysisRunner().run(analysis);
            } else if (args.length == 2) {
                Analysis analysis = AnalysisReader.loadAnalysis(new File(args[0]));
                analysis.getModules().getInputControl().updateParameterValue(InputControl.INPUT_PATH, args[1]);
                new AnalysisRunner().run(analysis);
            }

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
        if (version == null) {
            version = extractVersion();
        }

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

    public static ImageJService getIJService() {
        if (headless || ijService == null) {
            Context context = (Context) ij.IJ.runPlugIn("org.scijava.Context", "");
            ijService = (ImageJService) context.getService(ImageJService.class);
        }

        return ijService;

    }

    public static OpService getOpService() {
        if (headless || opService == null) {
            Context context = (Context) ij.IJ.runPlugIn("org.scijava.Context", "");
            opService = (OpService) context.getService(OpService.class);
        }

        return opService;

    }

    public static PluginService getPluginService() {
        if (headless || opService == null) {
            Context context = (Context) ij.IJ.runPlugIn("org.scijava.Context", "");
            pluginService = (PluginService) context.getService(PluginService.class);
        }

        return pluginService;

    }

    public static ScriptService getScriptService() {
        if (headless || scriptService == null) {
            Context context = (Context) ij.IJ.runPlugIn("org.scijava.Context", "");
            scriptService = (ScriptService) context.getService(ScriptService.class);
        }

        return scriptService;

    }

    public static void setIJService(ImageJService ijService) {
        MIA.ijService = ijService;
    }

    public static void setOpService(OpService opService) {
        MIA.opService = opService;
    }

    public static void setPluginService(PluginService pluginService) {
        MIA.pluginService = pluginService;
    }

    public static void setScriptService(ScriptService scriptService) {
        MIA.scriptService = scriptService;
    }


    // Checking if Kryo is greater than or equal to version 5.4.0.
    // If it isn't, the Memoizer will be disabled (an issue since Bio-Formats
    // 6.14.0)
    // Once Fiji's "Java8" update site uses kryo 5.4.0 this function can be disabled
    public static boolean kryoCheck() {
        try {
            Class.forName("com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy");
            return true;
        } catch (ClassNotFoundException e) {
            // Setting directly, so it doesn't change the saved Preference
            MIA.getPreferences().getAllParameters().updateValue(Preferences.USE_MEMOIZER, false);
            return false;
        }
    }
}