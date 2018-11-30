package wbif.sjx.ModularImageAnalysis.Object;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.common.MathFunc.CumStat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class ObjCollection extends LinkedHashMap<Integer,Obj> {
    private String name;
    private int maxID = 0;

    public interface ColourModes {
        String SINGLE_COLOUR = "Single colour";
        String RANDOM_COLOUR = "Random colour";
        String MEASUREMENT_VALUE = "Measurement value";
        String ID = "ID";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";

        String[] ALL = new String[]{SINGLE_COLOUR, RANDOM_COLOUR, MEASUREMENT_VALUE, ID, PARENT_ID, PARENT_MEASUREMENT_VALUE};

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

        String[] ALL = new String[]{WHITE,BLACK,RED,ORANGE,YELLOW,GREEN,CYAN,BLUE,VIOLET,MAGENTA};

    }

    public interface LabelModes {
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";

        String[] ALL = new String[]{ID,MEASUREMENT_VALUE,PARENT_ID,PARENT_MEASUREMENT_VALUE};

    }

    public ObjCollection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Obj object) {
        put(object.getID(),object);

    }

    public int getNextID() {
        maxID++;
        return maxID;
    }

    public Obj getFirst() {
        if (size() == 0) return null;

        return values().iterator().next();

    }

    public int[][] getSpatialLimits() {
        int[][] limits = new int[][]{
                {Integer.MAX_VALUE,-Integer.MAX_VALUE},
                {Integer.MAX_VALUE,-Integer.MAX_VALUE},
                {Integer.MAX_VALUE,Integer.MIN_VALUE}};

        for (Obj object:values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            for (int i=0;i<x.size();i++) {
                limits[0][0] = Math.min(limits[0][0],x.get(i));
                limits[0][1] = Math.max(limits[0][1],x.get(i));
                limits[1][0] = Math.min(limits[1][0],y.get(i));
                limits[1][1] = Math.max(limits[1][1],y.get(i));
                limits[2][0] = Math.min(limits[2][0],z.get(i));
                limits[2][1] = Math.max(limits[2][1],z.get(i));

            }
        }

        return limits;

    }

    public int[] getTemporalLimits() {
        // Finding the first and last frame of all objects in the inputObjects set
        int[] limits = new int[2];
        limits[0] = Integer.MAX_VALUE;
        limits[1] = -Integer.MAX_VALUE;

        for (Obj object:values()) {
            if (object.getT() < limits[0]) limits[0] = object.getT();
            if (object.getT() > limits[1]) limits[1] = object.getT();

        }

        return limits;

    }

    public int getLargestID() {
        int largestID = 0;
        for (Obj obj:values()) {
            if (obj.getID() > largestID) largestID = obj.getID();
        }

        return largestID;

    }

    @Deprecated
    public Image convertObjectsToImageOld(String outputName, @Nullable ImagePlus templateIpl, String colourMode, HashMap<Integer,Float> hues) {
        Image templateImage = new Image("Template",templateIpl);
        return convertObjectsToImage(outputName,templateImage,colourMode,hues);

    }

    public Image convertObjectsToImage(String outputName, @Nullable Image templateImage, String colourMode, HashMap<Integer,Float> hues) {
        ImagePlus templateIpl = templateImage == null ? null : templateImage.getImagePlus();
        ImagePlus ipl;

        int bitDepth = 8;
        switch (colourMode){
            case ColourModes.RANDOM_COLOUR:
            case ColourModes.SINGLE_COLOUR:
                bitDepth = 8;
                break;

            case ColourModes.MEASUREMENT_VALUE:
            case ColourModes.ID:
            case ColourModes.PARENT_ID:
                bitDepth = 32;
                break;

        }

        if (templateIpl == null) {
            // Getting range of object pixels
            int[][] spatialLimits = getSpatialLimits();
            int[] temporalLimits = getTemporalLimits();

            // Creating a new image
            ipl = IJ.createHyperStack(outputName, spatialLimits[0][1] + 1,spatialLimits[1][1] + 1,
                    1, spatialLimits[2][1] + 1, temporalLimits[1] + 1,bitDepth);

        } else {
            ipl = IJ.createHyperStack(outputName,templateIpl.getWidth(),templateIpl.getHeight(),
                    templateIpl.getNChannels(),templateIpl.getNSlices(),templateIpl.getNFrames(),bitDepth);

        }

        // If it's a 32-bit image, set all background pixels to NaN
        if (colourMode.equals(ColourModes.MEASUREMENT_VALUE)) {
            for (int z = 1; z <= ipl.getNSlices(); z++) {
                for (int c = 1; c <= ipl.getNChannels(); c++) {
                    for (int t = 1; t <= ipl.getNFrames(); t++) {
                        for (int x=0;x<ipl.getWidth();x++) {
                            for (int y=0;y<ipl.getHeight();y++) {
                                ipl.setPosition(c,z,t);
                                ipl.getProcessor().putPixelValue(x,y,Double.NaN);
                            }
                        }
                    }
                }
            }
        }

        // Labelling pixels in image
        for (Obj object:values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();
            Integer tPos = object.getT();

            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);

                ipl.setPosition(1,zPos+1,tPos+1);

                if (colourMode.equals(ColourModes.SINGLE_COLOUR) | colourMode.equals(ColourModes.RANDOM_COLOUR)) {

                    ipl.getProcessor().putPixel(x.get(i), y.get(i), Math.round(hues.get(object.getID())*255));

                } else if (colourMode.equals(ColourModes.MEASUREMENT_VALUE) | colourMode.equals(ColourModes.ID)
                        | colourMode.equals(ColourModes.PARENT_ID)) {
                    ipl.getProcessor().putPixelValue(x.get(i), y.get(i), hues.get(object.getID()));

                }
            }
        }

        // Assigning the spatial calibration from the template image
        if (templateIpl != null) {
            ipl.getCalibration().pixelWidth = templateIpl.getCalibration().getX(1);
            ipl.getCalibration().pixelHeight = templateIpl.getCalibration().getY(1);
            ipl.getCalibration().pixelDepth = templateIpl.getCalibration().getZ(1);
            ipl.getCalibration().setUnit(templateIpl.getCalibration().getUnit());
        } else if (getFirst() != null) {
            Obj first = getFirst();
            ipl.getCalibration().pixelWidth = first.getDistPerPxXY();
            ipl.getCalibration().pixelHeight = first.getDistPerPxXY();
            ipl.getCalibration().pixelDepth = first.getDistPerPxZ();
            ipl.getCalibration().setUnit(first.getCalibratedUnits());
        }

        return new Image(outputName,ipl);

    }

    public HashMap<Integer,String> getIDs(String labelMode, String[] source, int nDecimalPlaces, boolean useScientific) {
        HashMap<Integer,String> IDs = new HashMap<>();

        DecimalFormat df;
        if (nDecimalPlaces == 0) {
            df = new DecimalFormat("0");
        } else {
            if (useScientific) {
                StringBuilder zeros = new StringBuilder("0.");
                for (int i = 0; i < nDecimalPlaces; i++) {
                    zeros.append("0");
                }
                zeros.append("E0");
                df = new DecimalFormat(zeros.toString());
            } else {
                StringBuilder zeros = new StringBuilder("0");
                if (nDecimalPlaces != 0) zeros.append(".");
                for (int i = 0;i <nDecimalPlaces; i++) {
                    zeros.append("0");
                }
                df = new DecimalFormat(zeros.toString());
            }
        }

        for (Obj object:values()) {
            switch (labelMode) {
                case LabelModes.ID:
                    IDs.put(object.getID(),df.format(object.getID()));
                    break;

                case LabelModes.MEASUREMENT_VALUE:
                    if (Double.isNaN(object.getMeasurement(source[0]).getValue())) {
                        IDs.put(object.getID(), "NA");
                    } else {
                        IDs.put(object.getID(), df.format(object.getMeasurement(source[0]).getValue()));
                    }
                    break;

                case LabelModes.PARENT_ID:
                    if (object.getParent(source[0]) == null) {
                        IDs.put(object.getID(), "NA");
                    } else {
                        IDs.put(object.getID(), df.format(object.getParent(source[0]).getID()));
                    }

                    break;

                case LabelModes.PARENT_MEASUREMENT_VALUE:
                    Obj parentObj = object.getParent(source[1]);
                    if (parentObj == null) break;

                    if (Double.isNaN(parentObj.getMeasurement(source[0]).getValue())) {
                        IDs.put(parentObj.getID(), "NA");
                    } else {
                        IDs.put(parentObj.getID(), df.format(parentObj.getMeasurement(source[0]).getValue()));
                    }
                    break;
            }
        }

        return IDs;

    }

    public HashMap<Integer,Float> getRandomHues() {
        HashMap<Integer,Float> hues = new HashMap<>();

        for (Obj object:values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = new Random().nextFloat();

            hues.put(ID,H);

        }

        return hues;

    }

    public HashMap<Integer,Float> getIDHues(boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();

        for (Obj object:values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = (float) object.getID();
            if (normalised) H = (H* 1048576 % 255) / 255;

            hues.put(ID,H);

        }

        return hues;

    }

    public HashMap<Integer,Float> getParentIDHues(String parentObjectsName, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();

        for (Obj object:values()) {
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

    public HashMap<Integer,Float> getSingleColourHues(String colour) {
        HashMap<Integer,Float> hues = new HashMap<>();

        for (Obj object:values()) {
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

    public HashMap<Integer,Float> getMeasurementValueHues(String measurementName, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat();
        for (Obj obj:values()) {
            if (obj.getMeasurement(measurementName) == null) break;
            cs.addMeasure(obj.getMeasurement(measurementName).getValue());
        }

        for (Obj object:values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Measurement measurement = object.getMeasurement(measurementName);
            if (measurement == null) break;
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

    public HashMap<Integer,Float> getParentMeasurementValueHues(String parentObjectsName, String measurementName, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat();
        for (Obj obj:values()) {
            Obj parentObj = obj.getParent(parentObjectsName);
            if (parentObj == null) break;
            if (parentObj.getMeasurement(measurementName) == null) break;
            cs.addMeasure(parentObj.getMeasurement(measurementName).getValue());
        }

        for (Obj object:values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            Obj parentObj = object.getParent(parentObjectsName);
            if (parentObj == null) break;
            if (parentObj.getMeasurement(measurementName) == null) break;

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

    public HashMap<Integer,Color> getColours(HashMap<Integer,Float> hues, @Nullable String specialColours) {
        HashMap<Integer,Color> colours = new HashMap<>();

        for (int key:hues.keySet()) {
            if (hues.get(key) == Float.MAX_VALUE) {
                colours.put(key,Color.getHSBColor(0f,0f,1f));
            } else if (hues.get(key) == Float.MIN_VALUE) {
                colours.put(key,Color.getHSBColor(0f,0f,0f));
            } else {
                // Have to add 1E-8 to prevent 0 values having a rounding error that makes them negative
                colours.put(key,Color.getHSBColor(hues.get(key)+1E-8f,1f,1f));
            }

            // If the hue was assigned as -1 (for example, no parent found), setting the colour to white
            if (hues.get(key) == -1) colours.put(key,Color.getHSBColor(0f,0f,1f));
        }

        return colours;

    }

    /**
     * Returns the Obj with coordinates matching the Obj passed as an argument.  Useful for unit tests.
     * @param referenceObj
     * @return
     */
    public Obj getByEquals(Obj referenceObj) {
        for (Obj testObj:values()) {
            if (testObj.equals(referenceObj)) return testObj;
        }

        return null;

    }

    public void resetCollection() {
        clear();
        maxID = 0;
    }
}
