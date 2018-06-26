package wbif.sjx.ModularImageAnalysis.Object;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.common.MathFunc.CumStat;

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

        String[] ALL = new String[]{SINGLE_COLOUR, RANDOM_COLOUR, MEASUREMENT_VALUE, ID, PARENT_ID};

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

        String[] ALL = new String[]{ID,MEASUREMENT_VALUE,PARENT_ID};

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

    public int[] getTimepointLimits() {
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

    public Image convertObjectsToImage(String outputName, ImagePlus templateIpl, String colourMode, HashMap<Integer,Float> hues) {
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
            int[][] coordinateRange = new int[4][2];

            for (Obj object : values()) {
                // Getting range of XYZ
                double[][] currCoordinateRange = object.getExtents(true,false);
                for (int dim = 0; dim < currCoordinateRange.length; dim++) {
                    if (currCoordinateRange[dim][0] < coordinateRange[dim][0]) {
                        coordinateRange[dim][0] = (int) currCoordinateRange[dim][0];
                    }

                    if (currCoordinateRange[dim][1] > coordinateRange[dim][1]) {
                        coordinateRange[dim][1] = (int) currCoordinateRange[dim][1];
                    }
                }

                // Getting range of timepoints
                int currTimepoint = object.getT();
                if (currTimepoint < coordinateRange[3][0]) {
                    coordinateRange[3][0] = currTimepoint;
                }

                if (currTimepoint > coordinateRange[3][1]) {
                    coordinateRange[3][1] = currTimepoint;
                }
            }

            // Creating a new image
            ipl = IJ.createHyperStack(outputName, coordinateRange[0][1] + 1,coordinateRange[1][1] + 1,
                    1, coordinateRange[2][1] + 1, coordinateRange[3][1] + 1,bitDepth);

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

        }

        return new Image(outputName,ipl);

    }

    public HashMap<Integer,String> getIDs(String labelMode, String source, int nDecimalPlaces, boolean useScientific) {
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
                    if (Double.isNaN(object.getMeasurement(source).getValue())) {
                        IDs.put(object.getID(), "NA");
                    } else {
                        IDs.put(object.getID(), df.format(object.getMeasurement(source).getValue()));
                    }
                    break;

                case LabelModes.PARENT_ID:
                    if (object.getParent(source) == null) {
                        IDs.put(object.getID(), "NA");
                    } else {
                        IDs.put(object.getID(), df.format(object.getParent(source).getID()));
                    }

                    break;
            }
        }

        return IDs;

    }

    public HashMap<Integer,Float> getHues(String colourMode, String source, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat();
        if (colourMode.equals(ColourModes.MEASUREMENT_VALUE)) {
            values().forEach(e -> cs.addMeasure(e.getMeasurement(source).getValue()));
        }

        for (Obj object:values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            switch (colourMode) {
                case ColourModes.SINGLE_COLOUR:
                    switch (source) {
                        case "":
                        case SingleColours.WHITE:
                            H = 1f;
                            break;
                        case SingleColours.BLACK:
                            H = 0f;
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

                    break;

                case ColourModes.RANDOM_COLOUR:
                    // Random colours
                    H = new Random().nextFloat();
                    break;

                case ColourModes.MEASUREMENT_VALUE:
                    H = (float) object.getMeasurement(source).getValue();
                    if (normalised) {
                        double startH = 0;
                        double endH = 120d / 255d;
                        H = (float) ((H - cs.getMin()) * (endH - startH) / (cs.getMax() - cs.getMin()) + startH);
                    }
                    break;

                case ColourModes.ID:
                    H = (float) object.getID();
                    if (normalised) H = (H* 1048576 % 255) / 255;
                    break;

                case ColourModes.PARENT_ID:
                    if (object.getParent(source) == null) {
                        H = 0.2f;
                    } else {
                        H = (float) object.getParent(source).getID();
                    }

                    if (normalised) H = (H* 1048576 % 255) / 255;

                    break;

            }

            hues.put(ID,H);

        }

        return hues;

    }

    public HashMap<Integer,Color> getColours(String colourMode, String source, boolean normalised) {
        HashMap<Integer,Float> hues = getHues(colourMode,source,normalised);
        HashMap<Integer,Color> colours = new HashMap<>();

        for (int key:hues.keySet()) {
            if (colourMode.equals(ColourModes.SINGLE_COLOUR) && source.equals(SingleColours.WHITE)) {
                colours.put(key,Color.getHSBColor(0f,0f,1f));
            } else if (colourMode.equals(ColourModes.SINGLE_COLOUR) && source.equals(SingleColours.BLACK)) {
                colours.put(key,Color.getHSBColor(0f,0f,0f));
            } else {
                // Have to add 1E-8 to prevent 0 values having a rounding error that makes them negative
                colours.put(key,Color.getHSBColor(hues.get(key)+1E-8f,1f,1f));
            }
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
}
