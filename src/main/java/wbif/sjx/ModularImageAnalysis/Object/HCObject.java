package wbif.sjx.ModularImageAnalysis.Object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by steph on 30/04/2017.
 */
public class HCObject {
    // Indices for dimensional coordinates.  Coordinates are all zero indexed
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int C = 3;
    public static final int T = 4;

    /**
     * Unique instance ID for this object
     */
    private int ID = 0;

    /**
     * ID number shared between linked objects (e.g. spots in a track)
     */
    private int groupID = 0;

    /**
     * 3D coordinates of this instance of the object.
     */
    private HashMap<Integer, ArrayList<Integer>> coordinates = new HashMap<>();

    /**
     * HashMap containing extra dimensions specifying the location of this instance
     */
    private HashMap<Integer, Integer> positions = new HashMap<>();
    private HCObject parent = null;
    private LinkedHashMap<HCName, HCObjectSet> children = new LinkedHashMap<>();
    private LinkedHashMap<String, HCMeasurement> measurements = new LinkedHashMap<>();
    private String calibratedUnits = "px";

    /**
     * Calibration for each dimension.  Stored as physical distance per pixel
     */
    private HashMap<Integer, Double> calibration = new HashMap<>();


    // CONSTRUCTORS

    public HCObject(int ID) {
        this.ID = ID;

        // Setting default values for C and T
        positions.put(C, 0);
        positions.put(T, 0);

    }


    // PUBLIC METHODS

    public void addCoordinate(int dim, int coordinate) {
        if (dim < 3) {
            coordinates.computeIfAbsent(dim, k -> new ArrayList<>());
            coordinates.get(dim).add(coordinate);
        } else {
            positions.put(dim, coordinate);
        }
    }

    public void removeCoordinate(int dim, double coordinate) {
        coordinates.get(dim).remove(coordinate);

    }

    public int[][] getCoordinateRange() {
        int[][] dimSize = new int[3][2];

        for (int dim : coordinates.keySet()) {
            if (coordinates.get(dim) != null) {
                ArrayList<Integer> vals = coordinates.get(dim);
                dimSize[dim][0] = Collections.min(vals);
                dimSize[dim][1] = Collections.max(vals);
            }
        }

        return dimSize;

    }

    public void addMeasurement(HCMeasurement measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public HCMeasurement getMeasurement(String name) {
        return measurements.get(name);

    }

    public void addCalibration(Integer dim, double cal) {
        calibration.put(dim, cal);

    }

    public double getCalibration(Integer dim) {
        // If no calibration has been set, return 1
        if (coordinates.get(dim) == null) return 1;

        return calibration.get(dim);

    }

    @Override
    public String toString() {
        return "HCObject with " + coordinates.size() + " coordinate points";

    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    /**
     * Setting one of the XYZ coordinates
     *
     * @param dim
     * @param coordinateList
     */
    public void setCoordinates(int dim, ArrayList<Integer> coordinateList) {
        assert dim < 3;
        coordinates.put(dim, coordinateList);

    }

    /**
     * Setting a single-valued coordinate (e.g. C or T)
     *
     * @param dim
     * @param coordinateList
     */
    public void setCoordinates(int dim, int coordinateList) {
        assert dim >= 3;
        positions.put(dim, coordinateList);

    }

    public <T> T getCoordinates(int dim) {
        // Returning one of the XYZ coordinates
        if (dim < 3) {
            return (T) coordinates.get(dim);
        }

        // Returning a single-valued coordinate
        return (T) positions.get(dim);

    }

    public HashMap<Integer, Integer> getPositions() {
        return positions;
    }

    public void setPositions(HashMap<Integer, Integer> positions) {
        this.positions = positions;
    }

    public Integer getPosition(int dim) {
        return positions.get(dim);

    }

    public void setPosition(int dim, int pos) {
        positions.put(dim, pos);

    }

    public HashMap<Integer, ArrayList<Integer>> getCoordinates() {
        return coordinates;

    }

    public void setCoordinates(HashMap<Integer, ArrayList<Integer>> coordinates) {
        this.coordinates = coordinates;

    }

    public int getExtraDimension(int dim) {
        return positions.get(dim) == null ? -1 : positions.get(dim);

    }

    public void setExtraDimensions(int dim, int value) {
        positions.put(dim, value);

    }

    public HCObject getParent() {
        return parent;
    }

    public void setParent(HCObject parent) {
        this.parent = parent;
    }

    public LinkedHashMap<HCName, HCObjectSet> getChildren() {
        return children;
    }

    public HCObjectSet getChildren(HCName name) {
        return children.get(name);
    }

    public void setChildren(LinkedHashMap<HCName, HCObjectSet> children) {
        this.children = children;
    }

    public void addChildren(HCObjectSet childSet) {
        children.put(childSet.getName(), childSet);
    }

    public void removeChildren(HCName name) {
        children.remove(name);
    }

    public void addChild(HCName name, HCObject childSet) {
        children.computeIfAbsent(name, k -> new HCObjectSet(name));
        children.get(name).put(childSet.getID(), childSet);

    }

    public void removeChild(HCName name, HCObject child) {
        children.get(name).values().remove(child);

    }

    /**
     * Removes itself from any other objects as a parent or child.
     * @param name Name of this object
     */
    public void removeRelationships(HCName name) {
        // Removing itself as a child from its parent
        if (parent != null) {
            parent.removeChild(name,this);

        }

        // Removing itself as a parent from any children
        if (children != null) {
            for (HCObjectSet childSet:children.values()) {
                for (HCObject child:childSet.values()) {
                    child.setParent(null);

                }
            }
        }
    }

    public LinkedHashMap<String, HCMeasurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, HCMeasurement> measurements) {
        this.measurements = measurements;

    }

    public HashMap<Integer, Double> getCalibration() {
        return calibration;
    }

    public void setCalibration(HashMap<Integer, Double> calibration) {
        this.calibration = calibration;
    }

    public String getCalibratedUnits() {
        return calibratedUnits;
    }

    public void setCalibratedUnits(String calibratedUnits) {
        this.calibratedUnits = calibratedUnits;
    }

}

