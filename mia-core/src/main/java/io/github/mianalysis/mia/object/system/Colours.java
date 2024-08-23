package io.github.mianalysis.mia.object.system;

import java.awt.*;

/**
 * Created by Stephen Cross on 01/11/2019.
 */
public class Colours {
    private static final Color LIGHT_BLUE = new Color(134, 208, 230); // #86d0e6
    private static final Color LIGHT_BLUE_DM = new Color(22, 87, 107); // #16576b

    private static final Color BLUE = new Color(66, 183, 217); // #42b7d9
    private static final Color BLUE_DM = new Color(66, 183, 217); // #42b7d9

    private static final Color DARK_BLUE = new Color(34, 140, 170); // #228caa
    private static final Color DARK_BLUE_DM = new Color(85, 191, 221); // #55bfdd

    private static final Color GREEN = new Color(89, 216, 68); // #59d844
    private static final Color GREEN_DM = new Color(124, 224, 107); // #7ce06b

    private static final Color ORANGE = new Color(233, 142, 50); // #e98e32
    private static final Color ORANGE_DM = new Color(237, 166, 94); // #eda65e

    private static final Color RED = new Color(217, 66, 66); // #d94242
    private static final Color RED_DM = new Color(225, 106, 106); // #e16a6a

    private static final Color DARK_GREY = new Color(66, 66, 66); // #424242
    private static final Color DARK_GREY_DM = new Color(66, 66, 66); // #424242

    public static Color getLightBlue(boolean darkMode) {
        if (darkMode)
            return LIGHT_BLUE_DM;
        else
            return LIGHT_BLUE;
    }

    public static Color getBlue(boolean darkMode) {
        if (darkMode)
            return BLUE_DM;
        else
            return BLUE;
    }

    public static Color getDarkBlue(boolean darkMode) {
        if (darkMode)
            return DARK_BLUE_DM;
        else
            return DARK_BLUE;
    }

    public static Color getGreen(boolean darkMode) {
        if (darkMode)
            return GREEN_DM;
        else
            return GREEN;
    }

    public static Color getOrange(boolean darkMode) {
        if (darkMode)
            return ORANGE_DM;
        else
            return ORANGE;
    }

    public static Color getRed(boolean darkMode) {
        if (darkMode)
            return RED_DM;
        else
            return RED;
    }

    public static Color getDarkGrey(boolean darkMode) {
        if (darkMode)
            return DARK_GREY_DM;
        else
            return DARK_GREY;
    }

    public static Color getBlack(boolean darkMode) {
        if (darkMode)
            return Color.WHITE;
        else
            return Color.BLACK;
    }
}
