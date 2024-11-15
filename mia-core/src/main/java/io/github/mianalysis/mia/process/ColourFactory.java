package io.github.mianalysis.mia.process;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.HashMap;
import java.util.Random;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.process.math.CumStat;

public class ColourFactory {
    private static long randomSeed = 1;

    public interface ColourModes {
        String CHILD_COUNT = "Child count";
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String OBJ_METADATA_ITEM = "Object metadata value";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";
        String PARTNER_COUNT = "Partner count";
        String RANDOM_COLOUR = "Random colour";
        String SINGLE_COLOUR = "Single colour";

        String[] ALL = new String[] { CHILD_COUNT, ID, MEASUREMENT_VALUE, OBJ_METADATA_ITEM, PARENT_ID,
                PARENT_MEASUREMENT_VALUE, PARTNER_COUNT, RANDOM_COLOUR, SINGLE_COLOUR };

    }

    public interface ColourMaps {
        String BLACK_FIRE = "Black fire";
        String ICE = "Ice";
        String JET = "Jet";
        String PHYSICS = "Physics";
        String RANDOM = "Random";
        String RANDOM_VIBRANT = "Random (vibrant)";
        String SPECTRUM = "Spectrum";
        String THERMAL = "Thermal";

        String[] ALL = new String[] { BLACK_FIRE, ICE, JET, PHYSICS, RANDOM, RANDOM_VIBRANT,
                SPECTRUM, THERMAL };

    }

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

        String[] ALL = new String[] { WHITE, BLACK, RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, VIOLET, MAGENTA };

    }

    public static HashMap<Integer, Float> getRandomHues(Objs objects) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        Random rand = new Random(randomSeed);
        for (Obj object : objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = rand.nextFloat();

            hues.put(ID, H);

        }

        return hues;

    }

    public static HashMap<Integer, Float> getIDHues(Objs objects, boolean normalised) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        for (Obj object : objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = (float) object.getID();
            if (normalised)
                H = (H * 1048576 % 255) / 255;

            hues.put(ID, H);

        }

        return hues;

    }

    public static HashMap<Integer, Float> getParentIDHues(Objs objects, String parentObjectsName, boolean normalised) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        for (Obj object : objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            if (object.getParent(parentObjectsName) == null) {
                H = -1f;
            } else {
                H = (float) object.getParent(parentObjectsName).getID();
            }

            if (normalised & object.getParent(parentObjectsName) != null)
                H = (H * 1048576 % 255) / 255;

            hues.put(ID, H);

        }

        return hues;

    }

    public static HashMap<Integer, Float> getSingleColourValues(Objs objects, String colour) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        for (Obj object : objects.values()) {
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

            hues.put(ID, H);

        }

        return hues;

    }

    public static Color hueToColour(float hue) {
        return Color.getHSBColor(hue + 1E-8f, 1f, 1f);
    }

    public static HashMap<Integer, Color> hueToColour(HashMap<Integer, Float> hues) {
        HashMap<Integer, Color> colours = new HashMap<>();
        for (int idx : hues.keySet())
            colours.put(idx, Color.getHSBColor(hues.get(idx) + 1E-8f, 1f, 1f));

        return colours;

    }

    public static HashMap<Integer, Color> valueToColour(HashMap<Integer, Float> hues) {
        HashMap<Integer, Color> colours = new HashMap<>();
        for (int idx : hues.keySet())
            colours.put(idx, Color.getHSBColor(hues.get(idx) + 1E-8f, 1f, 1f));

        return colours;

    }

    public static HashMap<Integer, Float> getChildCountHues(Objs objects, String childObjectsName, boolean normalised,
            double[] range) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        // Getting minimum and maximum values from measurement (if required)
        double min = range[0];
        double max = range[1];
        if (Double.isNaN(min) || Double.isNaN(max)) {
            CumStat cs = new CumStat();
            for (Obj obj : objects.values()) {
                if (obj.getChildren(childObjectsName) == null)
                    continue;
                cs.addMeasure(obj.getChildren(childObjectsName).size());
            }
            if (Double.isNaN(min))
                min = cs.getMin();
            if (Double.isNaN(max))
                max = cs.getMax();
        }

        for (Obj object : objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Objs childObjects = object.getChildren(childObjectsName);
            if (childObjects == null) {
                hues.put(ID, H);
                continue;
            }
            H = (float) object.getChildren(childObjectsName).size();
            if (normalised) {
                double startH = 0;
                double endH = 128d / 255d;
                double tempH = (H - min) * (endH - startH) / (max - min) + startH;
                H = (float) Math.min(endH, Math.max(startH, tempH));
            }

            hues.put(ID, H);

        }

        return hues;

    }

    public static HashMap<Integer, Float> getPartnerCountHues(Objs objects, String partnerObjectsName,
            boolean normalised, double[] range) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        // Getting minimum and maximum values from measurement (if required)
        double min = range[0];
        double max = range[1];
        if (Double.isNaN(min) || Double.isNaN(max)) {
            CumStat cs = new CumStat();
            for (Obj obj : objects.values()) {
                if (obj.getPartners(partnerObjectsName) == null)
                    continue;
                cs.addMeasure(obj.getPartners(partnerObjectsName).size());
            }
            if (Double.isNaN(min))
                min = cs.getMin();
            if (Double.isNaN(max))
                max = cs.getMax();
        }

        for (Obj object : objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Objs partnerObjects = object.getPartners(partnerObjectsName);
            if (partnerObjects == null) {
                hues.put(ID, H);
                continue;
            }
            H = (float) object.getPartners(partnerObjectsName).size();
            if (normalised) {
                double startH = 0;
                double endH = 120d / 255d;
                double tempH = (H - min) * (endH - startH) / (max - min) + startH;
                H = (float) Math.min(endH, Math.max(startH, tempH));
            }

            hues.put(ID, H);

        }

        return hues;

    }

    public static HashMap<Integer, Float> getMeasurementValueHues(Objs objects, String measurementName,
            boolean normalised, double[] range) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        // Getting minimum and maximum values from measurement (if required)
        double min = range[0];
        double max = range[1];
        if (Double.isNaN(min) || Double.isNaN(max)) {
            CumStat cs = new CumStat();
            for (Obj obj : objects.values()) {
                if (obj.getMeasurement(measurementName) == null)
                    continue;
                cs.addMeasure(obj.getMeasurement(measurementName).getValue());
            }
            if (Double.isNaN(min))
                min = cs.getMin();
            if (Double.isNaN(max))
                max = cs.getMax();
        }

        for (Obj object : objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Measurement measurement = object.getMeasurement(measurementName);
            if (measurement == null) {
                hues.put(ID, H);
                continue;
            }
            H = (float) object.getMeasurement(measurementName).getValue();
            if (normalised) {
                double startH = 0;
                double endH = 120d / 255d;
                double tempH = (H - min) * (endH - startH) / (max - min) + startH;
                H = (float) Math.min(endH, Math.max(startH, tempH));
            }

            hues.put(ID, H);

        }

        return hues;

    }

    public static HashMap<Integer, Float> getObjectMetadataHues(Objs objects, String metadataName) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        for (Obj object : objects.values()) {
            if (object == null || metadataName == null) {
                hues.put(object.getID(), 0f);
                continue;
            }

            ObjMetadata metadataItem = object.getMetadataItem(metadataName);
            if (metadataItem == null) {
                hues.put(object.getID(), 0f);
                continue;
            }

            hues.put(object.getID(), new Random(metadataItem.getValue().hashCode()*31).nextFloat());

        }

        return hues;

    }

    public static HashMap<Integer, Float> getParentMeasurementValueHues(Objs objects, String parentObjectsName,
            String measurementName, boolean normalised, double[] range) {
        HashMap<Integer, Float> hues = new HashMap<>();
        if (objects == null)
            return hues;

        // Getting minimum and maximum values from measurement (if required)
        double min = range[0];
        double max = range[1];
        if (Double.isNaN(min) || Double.isNaN(max)) {
            CumStat cs = new CumStat();
            for (Obj obj : objects.values()) {
                Obj parentObj = obj.getParent(parentObjectsName);
                if (parentObj == null)
                    continue;
                if (parentObj.getMeasurement(measurementName) == null)
                    continue;
                cs.addMeasure(parentObj.getMeasurement(measurementName).getValue());
            }
            if (Double.isNaN(min))
                min = cs.getMin();
            if (Double.isNaN(max))
                max = cs.getMax();
        }

        for (Obj object : objects.values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Obj parentObj = object.getParent(parentObjectsName);
            if (parentObj == null) {
                hues.put(ID, H);
                continue;
            }
            if (parentObj.getMeasurement(measurementName) == null) {
                hues.put(ID, H);
                continue;
            }

            H = (float) parentObj.getMeasurement(measurementName).getValue();
            if (normalised) {
                double startH = 0;
                double endH = 120d / 255d;
                double tempH = (H - min) * (endH - startH) / (max - min) + startH;
                H = (float) Math.min(endH, Math.max(startH, tempH));
            }

            hues.put(ID, H);

        }

        return hues;

    }

    public static Color getColour(String colour) {
        switch (colour) {
            default:
            case "":
            case SingleColours.WHITE:
                return Color.WHITE;
            case SingleColours.BLACK:
                return Color.BLACK;
            case SingleColours.RED:
                return Color.RED;
            case SingleColours.ORANGE:
                return Color.ORANGE;
            case SingleColours.YELLOW:
                return Color.YELLOW;
            case SingleColours.GREEN:
                return Color.GREEN;
            case SingleColours.CYAN:
                return Color.CYAN;
            case SingleColours.BLUE:
                return Color.BLUE;
            case SingleColours.VIOLET:
                return Color.getHSBColor(0.706f, 1, 1);
            case SingleColours.MAGENTA:
                return Color.MAGENTA;
        }
    }

    public static float getHue(String colour) {
        Color color = getColour(colour);

        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];

    }

    public static Color getColour(float hue) {
        return getColour(hue, ColourMaps.SPECTRUM, 0);
    }

    public static Color getColour(float value, double opacity) {
        return getColour(value, ColourMaps.PHYSICS, opacity);
    }

    public static Color getColour(float value, String colourMode, double opacity) {
        Color color;

        // If the hue was assigned as -1 (for example, no parent found), setting the
        // colour to white
        if (value == Float.MAX_VALUE || value == -1) {
            color = Color.getHSBColor(0f, 0f, 1f);
        } else if (value == Float.MIN_VALUE) {
            color = Color.getHSBColor(0f, 0f, 0f);
        } else if (colourMode.equals(ColourMaps.SPECTRUM)) {
            color = Color.getHSBColor(value + 1E-8f, 1f, 1f);
        } else {
            // Have to addRef 1E-8 to prevent 0 values having a rounding error that makes
            // them negative
            value = value + 1E-8f;

            IndexColorModel cm = null;
            switch (colourMode) {
                case ColourMaps.BLACK_FIRE:
                    cm = LUTs.BlackFire().getColorModel();
                    break;
                case ColourMaps.ICE:
                    cm = LUTs.Ice().getColorModel();
                    break;
                case ColourMaps.PHYSICS:
                    cm = LUTs.Physics().getColorModel();
                    break;
                case ColourMaps.RANDOM:
                    cm = LUTs.Random(false, false).getColorModel();
                    break;
                case ColourMaps.RANDOM_VIBRANT:
                    cm = LUTs.RandomVibrant(false, false).getColorModel();
                    break;
                case ColourMaps.JET:
                    cm = LUTs.Jet().getColorModel();
                    break;
                case ColourMaps.SPECTRUM:
                default:
                    cm = LUTs.Spectrum().getColorModel();
                    break;
                case ColourMaps.THERMAL:
                    cm = LUTs.Thermal().getColorModel();
                    break;
            }

            int idx = (int) Math.round(value * 255);
            color = new Color(cm.getRed(idx), cm.getGreen(idx), cm.getBlue(idx));

        }

        int alpha = (int) Math.round(opacity * 2.55);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

    }

    public static HashMap<Integer, Color> getColours(HashMap<Integer, Float> hues) {
        return getColours(hues, 100);
    }

    public static HashMap<Integer, Color> getColours(HashMap<Integer, Float> hues, double opacity) {
        return getColours(hues, ColourMaps.SPECTRUM, opacity);

    }

    public static HashMap<Integer, Color> getColours(HashMap<Integer, Float> hues, String colourMap, double opacity) {
        HashMap<Integer, Color> colours = new HashMap<>();

        for (int key : hues.keySet())
            colours.put(key, getColour(hues.get(key), colourMap, opacity));

        return colours;

    }
}
