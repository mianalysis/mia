package wbif.sjx.MIA.Object.Parameters.ChoiceInterfaces;

import ij.Prefs;

public interface BinaryLogicInterface {
    String BLACK_BACKGROUND = "Black (0) background";
    String WHITE_BACKGROUND = "White (255) background";

    String[] ALL = new String[] { BLACK_BACKGROUND, WHITE_BACKGROUND };
    
    public static void setBlackBackgroundPref(String logic) {
        switch (logic) {
            case BLACK_BACKGROUND:
                Prefs.blackBackground = true;
                break;
            case WHITE_BACKGROUND:
                Prefs.blackBackground = false;
                break;
        }
    }
}