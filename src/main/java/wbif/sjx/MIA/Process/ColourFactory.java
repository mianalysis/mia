package wbif.sjx.MIA.Process;

import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

public class ColourFactory {

    public interface SingleColours {
        String WHITE = "White";
        String BLACK = "Black";
        String RED = "Red";
        String ORANGE = "Orange";
        String YELLOW = "Yellow";
        String GREEN = "Green";
        String CYAN = "Cyan";
        String BLUE = "Blue";
        String VIOLET = "Violet";
        String MAGENTA = "Magenta";

        String[] ALL = new String[]{WHITE,BLACK,RED,ORANGE,YELLOW,GREEN,CYAN,BLUE,VIOLET,MAGENTA};

    }

    public static HashMap<Integer,Float> getRandomHues(ObjCollection objects) {
        HashMap<Integer,Float> hues = new HashMap<>();
        if (objects == null) return hues;

        for (Obj object:objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = new Random().nextFloat();

            hues.put(ID,H);

        }

        return hues;

    }

    public static HashMap<Integer,Float> getIDHues(ObjCollection objects, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();
        if (objects == null) return hues;

        for (Obj object:objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = (float) object.getID();
            if (normalised) H = (H* 1048576 % 255) / 255;

            hues.put(ID,H);

        }

        return hues;

    }

    public static HashMap<Integer,Float> getParentIDHues(ObjCollection objects, String parentObjectsName, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();
        if (objects == null) return hues;

        for (Obj object:objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            if (object.getParent(parentObjectsName) == null) {
                H = -1f;
            } else {
                H = (float) object.getParent(parentObjectsName).getID();
            }

            if (normalised & object.getParent(parentObjectsName) != null) H = (H* 1048576 % 255) / 255;

            hues.put(ID,H);

        }

        return hues;

    }

    public static HashMap<Integer,Float> getSingleColourHues(ObjCollection objects, String colour) {
        HashMap<Integer,Float> hues = new HashMap<>();
        if (objects == null) return hues;

        for (Obj object:objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            switch (colour) {
                case "":
                case SingleColours.WHITE:
                    H = Float.MAX_VALUE;
                    break;
                case SingleColours.BLACK:
                    H = Float.MIN_VALUE;
                    break;
                case SingleColours.RED:
                    H = 0f;
                    break;
                case SingleColours.ORANGE:
                    H = 0.078f;
                    break;
                case SingleColours.YELLOW:
                    H = 0.157f;
                    break;
                case SingleColours.GREEN:
                    H = 0.314f;
                    break;
                case SingleColours.CYAN:
                    H = 0.471f;
                    break;
                case SingleColours.BLUE:
                    H = 0.627f;
                    break;
                case SingleColours.VIOLET:
                    H = 0.706f;
                    break;
                case SingleColours.MAGENTA:
                    H = 0.784f;
                    break;
            }

            hues.put(ID,H);

        }

        return hues;

    }

    public static HashMap<Integer,Float> getMeasurementValueHues(ObjCollection objects, String measurementName, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();
        if (objects == null) return hues;

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat();
        for (Obj obj:objects.values()) {
            if (obj.getMeasurement(measurementName) == null) continue;
            cs.addMeasure(obj.getMeasurement(measurementName).getValue());
        }

        for (Obj object:objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Measurement measurement = object.getMeasurement(measurementName);
            if (measurement == null) {
                hues.put(ID,H);
                continue;
            }
            H = (float) object.getMeasurement(measurementName).getValue();
            if (normalised) {
                double startH = 0;
                double endH = 120d / 255d;
                H = (float) ((H - cs.getMin()) * (endH - startH) / (cs.getMax() - cs.getMin()) + startH);
            }

            hues.put(ID,H);

        }

        return hues;

    }

    public static HashMap<Integer,Float> getParentMeasurementValueHues(ObjCollection objects, String parentObjectsName, String measurementName, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();
        if (objects == null) return hues;

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat();
        for (Obj obj:objects.values()) {
            Obj parentObj = obj.getParent(parentObjectsName);
            if (parentObj == null) continue;
            if (parentObj.getMeasurement(measurementName) == null) continue;
            cs.addMeasure(parentObj.getMeasurement(measurementName).getValue());
        }

        for (Obj object:objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Obj parentObj = object.getParent(parentObjectsName);
            if (parentObj == null) {
                hues.put(ID,H);
                continue;
            }
            if (parentObj.getMeasurement(measurementName) == null) {
                hues.put(ID,H);
                continue;
            }

            H = (float) parentObj.getMeasurement(measurementName).getValue();
            if (normalised) {
                double startH = 0;
                double endH = 120d / 255d;
                H = (float) ((H - cs.getMin()) * (endH - startH) / (cs.getMax() - cs.getMin()) + startH);
            }

            hues.put(ID,H);

        }

        return hues;

    }

    public static Color getColour(float hue) {
        // If the hue was assigned as -1 (for example, no parent found), setting the colour to white
        if (hue == Float.MAX_VALUE || hue == -1) {
            return Color.getHSBColor(0f,0f,1f);
        } else if (hue == Float.MIN_VALUE) {
            return Color.getHSBColor(0f,0f,0f);
        } else {
            // Have to add 1E-8 to prevent 0 values having a rounding error that makes them negative
            return Color.getHSBColor(hue+1E-8f,1f,1f);
        }
    }

    public static HashMap<Integer,Color> getColours(HashMap<Integer,Float> hues) {
        HashMap<Integer,Color> colours = new HashMap<>();

        for (int key:hues.keySet()) colours.put(key,getColour(hues.get(key)));

        return colours;

    }
}
