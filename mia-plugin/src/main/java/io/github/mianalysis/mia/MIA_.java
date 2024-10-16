package io.github.mianalysis.mia;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import ij.Prefs;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.parametercontrols.SwingParameterControlFactory;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.SwingPreferences;
import io.github.mianalysis.mia.process.DependencyValidator;
import io.github.mianalysis.mia.process.ParameterControlFactory;
import io.github.mianalysis.mia.process.logging.ConsoleRenderer;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

/**
 * Created by Stephen Cross on 14/07/2017.
 */
@Plugin(type = Command.class, menuPath = "Plugins>ModularImageAnalysis (MIA)>MIA", visible = true)
public class MIA_ extends MIA implements Command {
    /*
     * Gearing up for the transition from ImagePlus to ImgLib2 formats. Modules can
     * use this to add compatibility.
     */
    protected static final boolean imagePlusMode = true;

    static {
        LegacyInjector.preinit();
    }

    public static void main(String[] args) throws Exception {
        debug = true;

        try {
            new ij.ImageJ();
            new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    @Override
    public void run() {
        headless = false;

        // Adding LogService to LogHistory
        MIA.linkLogServiceToLogHistory();

        // Setting the ParameterControlFactory
        ParameterControlFactory.setActive(new SwingParameterControlFactory());

        // Replacing the default Preferences
        setPreferences(new SwingPreferences(null));
        getPreferences().updateAndGetParameters();

        try {
            String theme = Prefs.get("MIA.GUI.theme", io.github.mianalysis.mia.gui.Themes.getDefaultTheme());
            UIManager.setLookAndFeel(io.github.mianalysis.mia.gui.Themes.getThemeClass(theme));
            UIManager.put("TitlePane.showIconBesideTitle", true);
            UIManager.put("TabbedPane.selectedBackground", new Color(0, 0, 0, 0));
            System.setProperty("apple.awt.application.appearance", "system");
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

        log.addRenderer(logHistory);

        // Run the dependency validator. If updates were required, return.
        if (DependencyValidator.run())
            return;

        // Check Kryo version
        kryoCheck();

        try {
            new GUI();
        } catch (Exception e) {
            MIA.log.writeError(e);
        }
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