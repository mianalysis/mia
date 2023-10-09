package io.github.mianalysis.mia.gui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
// import com.formdev.flatlaf.themes.FlatMacDarkLaf;
// import com.formdev.flatlaf.themes.FlatMacLightLaf;

public interface Themes {
    String FLAT_LAF_DARK = "Flat LAF (dark)";
    String FLAT_LAF_DARKULA = "Flat LAF (darkula)";
    String FLAT_LAF_INTELLIJ = "Flat LAF (IntelliJ)";
    String FLAT_LAF_LIGHT = "Flat LAF (light)";
    // String FLAT_LAF_MACOS_DARK = "Flat LAF (macOS dark)";
    // String FLAT_LAF_MACOS_LIGHT = "Flat LAF (macOS light)";
    String MATCH_IMAGEJ = "Match ImageJ";
    String SYSTEM_DEFAULT = "System default";

    // String[] ALL = new String[] { FLAT_LAF_DARK, FLAT_LAF_DARKULA, FLAT_LAF_INTELLIJ, FLAT_LAF_LIGHT,
    //         FLAT_LAF_MACOS_DARK, FLAT_LAF_MACOS_LIGHT, MATCH_IMAGEJ,
    //         SYSTEM_DEFAULT };
            String[] ALL = new String[] { FLAT_LAF_DARK, FLAT_LAF_DARKULA, FLAT_LAF_INTELLIJ, FLAT_LAF_LIGHT,
                MATCH_IMAGEJ,
                SYSTEM_DEFAULT };

    static String defaultTheme = FLAT_LAF_LIGHT;
    static LookAndFeel ijLAF = UIManager.getLookAndFeel();

    public static LookAndFeel getThemeClass(String theme)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        switch (theme) {
            case Themes.FLAT_LAF_DARK:
                return new FlatDarkLaf();
            case Themes.FLAT_LAF_DARKULA:
                return new FlatDarculaLaf();
            case Themes.FLAT_LAF_INTELLIJ:
                return new FlatIntelliJLaf();
            case Themes.FLAT_LAF_LIGHT:
                return new FlatLightLaf();
            // case Themes.FLAT_LAF_MACOS_DARK:
            //     return new FlatMacDarkLaf();
            // case Themes.FLAT_LAF_MACOS_LIGHT:
            //     return new FlatMacLightLaf();
            case Themes.MATCH_IMAGEJ:
                Class<?> clazz = Class.forName(ijLAF.getClass().getCanonicalName());
                Constructor<?> ctor = clazz.getConstructor();
                return (LookAndFeel) ctor.newInstance();
            case Themes.SYSTEM_DEFAULT:
            default:
                clazz = Class.forName(UIManager.getSystemLookAndFeelClassName());
                ctor = clazz.getConstructor();
                return (LookAndFeel) ctor.newInstance();
        }
    }

    public static boolean isDarkTheme(String theme) {
        switch (theme) {
            case Themes.FLAT_LAF_DARK:
            case Themes.FLAT_LAF_DARKULA:
                return true;
            case Themes.FLAT_LAF_INTELLIJ:
            case Themes.FLAT_LAF_LIGHT:
            case Themes.SYSTEM_DEFAULT:
            default:
                return false;
            case Themes.MATCH_IMAGEJ:
                return true;
        }
    }

    public static String getDefaultTheme() {
        return defaultTheme;
    }
}
