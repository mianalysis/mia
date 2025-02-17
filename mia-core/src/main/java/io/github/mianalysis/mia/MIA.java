package io.github.mianalysis.mia;

import java.io.IOException;
import java.util.Properties;

import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogListener;
import org.scijava.log.LogMessage;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptService;

import io.github.mianalysis.mia.module.lostandfound.LostAndFound;
import io.github.mianalysis.mia.moduledependencies.Dependencies;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactories;
import io.github.mianalysis.mia.object.coordinates.volume.OctreeFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.process.logging.BasicLogRenderer;
import io.github.mianalysis.mia.process.logging.Log;
import io.github.mianalysis.mia.process.logging.LogHistory;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import io.github.mianalysis.mia.process.logging.LogRenderer.Level;
import net.imagej.ImageJService;
import net.imagej.ops.OpService;

/**
 * Created by Stephen Cross on 14/07/2017.
 */
public abstract class MIA {
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

    private static String extractVersion() {
        String versionNumber = MIA.class.getPackage().getImplementationVersion();
        if (versionNumber == null) {
            Properties properties = new Properties();
            try {
                properties.load(MIA.class.getClassLoader().getResourceAsStream("project.properties"));
                return properties.getProperty("version");
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }            
        } else {
            return versionNumber;
        }
    }

    protected static void registerCoordinateSetFactories() {
        CoordinateSetFactories.addFactory(new PointListFactory());
        CoordinateSetFactories.addFactory(new QuadtreeFactory());
        CoordinateSetFactories.addFactory(new OctreeFactory());
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

    public static void setPreferences(Preferences newPreferences) {
        preferences = newPreferences;
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

    public static void setLostAndFound(LostAndFound newLostAndFound) {
        lostAndFound = newLostAndFound;

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

    public static void linkLogServiceToLogHistory() {
        // Making sure errors written to the script service log are stored and output in
        // the Excel file
        MIA.getScriptService().log().addLogListener(new LogListener() {
            @Override
            public void messageLogged(LogMessage message) {
                if (message.level() == LogLevel.DEBUG && logHistory.isWriteEnabled(Level.DEBUG))
                    logHistory.write(message.toString(), Level.DEBUG);
                else if (message.level() == LogLevel.INFO && logHistory.isWriteEnabled(Level.MESSAGE))
                    logHistory.write(message.toString(), Level.MESSAGE);
                else if (message.level() == LogLevel.ERROR && logHistory.isWriteEnabled(Level.ERROR))
                    logHistory.write(message.toString(), Level.ERROR);
                else if (message.level() == LogLevel.WARN && logHistory.isWriteEnabled(Level.WARNING))
                    logHistory.write(message.toString(), Level.WARNING);
            }
        });
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