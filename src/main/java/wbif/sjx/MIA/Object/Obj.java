package wbif.sjx.MIA.Object;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.*;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Stephen on 30/04/2017.
 */
public class Obj extends Volume {
    private String name;

    /**
     * Unique instance ID for this object
     */
    private int ID;

    /**
     * Each instance of an object is only present in XYZ and a single timepoint. Any
     * other dimensionality (e.g. channel) must be added as a measurement.
     */
    private int T = 0;

    private final int nFrames;
    private LinkedHashMap<String, Obj> parents = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjCollection> children = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjCollection> partners = new LinkedHashMap<>();
    private LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();
    private HashMap<Integer, Roi> rois = new HashMap<>();

    // CONSTRUCTORS

    public Obj(VolumeType volumeType, String name, int ID, int width, int height, int nSlices, int nFrames,
            double dppXY, double dppZ, String units) {
        super(volumeType, width, height, nSlices, dppXY, dppZ, units);

        this.name = name;
        this.ID = ID;
        this.nFrames = nFrames;

    }

    public Obj(VolumeType volumeType, String name, int ID, SpatCal calibration, int nFrames) {
        super(volumeType, calibration);

        this.name = name;
        this.ID = ID;
        this.nFrames = nFrames;

    }

    public Obj(String name, int ID, Volume exampleVolume, int nFrames) {
        super(exampleVolume.getVolumeType(), exampleVolume.getSpatialCalibration());

        this.name = name;
        this.ID = ID;
        this.nFrames = nFrames;

    }

    public Obj(String name, int ID, Obj exampleObj) {
        super(exampleObj.getVolumeType(), exampleObj.getSpatialCalibration());

        this.name = name;
        this.ID = ID;
        this.nFrames = exampleObj.getNFrames();

    }

    public Obj(VolumeType volumeType, String name, int ID, Volume exampleVolume, int nFrames) {
        super(volumeType, exampleVolume.getSpatialCalibration());

        this.name = name;
        this.ID = ID;
        this.nFrames = nFrames;

    }

    public Obj(VolumeType volumeType, String name, int ID, Obj exampleObj) {
        super(volumeType, exampleObj.getSpatialCalibration());

        this.name = name;
        this.ID = ID;
        this.nFrames = exampleObj.getNFrames();

    }

    // PUBLIC METHODS

    public void addMeasurement(Measurement measurement) {
        if (measurement == null)
            return;
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        if (measurements.get(name) == null)
            return null;
        return measurements.get(name);

    }

    public void removeMeasurement(String name) {
        measurements.remove(name);

    }

    public String getName() {
        return name;
    }

    public Obj setName(String name) {
        this.name = name;
        return this;
    }

    public int getID() {
        return ID;
    }

    public Obj setID(int ID) {
        this.ID = ID;
        return this;
    }

    public int getT() {
        return T;
    }

    public Obj setT(int t) {
        this.T = t;
        return this;
    }

    public int getNFrames() {
        return nFrames;
    }

    public LinkedHashMap<String, Obj> getParents(boolean useFullHierarchy) {
        if (!useFullHierarchy)
            return parents;

        // Adding each parent and then the parent of that
        LinkedHashMap<String, Obj> parentHierarchy = new LinkedHashMap<>(parents);

        // Going through each parent, adding the parents of that.
        for (Obj parent : parents.values()) {
            if (parent == null)
                continue;

            LinkedHashMap<String, Obj> currentParents = parent.getParents(true);
            if (currentParents == null)
                continue;

            parentHierarchy.putAll(currentParents);

        }

        return parentHierarchy;

    }

    public void setParents(LinkedHashMap<String, Obj> parents) {
        this.parents = parents;
    }

    public Obj getParent(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first parent
        Obj parent = parents.get(elements[0]);

        // If the first parent was the only one listed, returning this
        if (elements.length == 1)
            return parent;

        // If there are additional parents listed, re-constructing the string and
        // running this method on the parent
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(elements[i]);
            if (i != elements.length - 1)
                stringBuilder.append(" // ");
        }

        if (parent == null)
            return null;

        return parent.getParent(stringBuilder.toString());

    }

    public void addParent(Obj parent) {
        parents.put(parent.getName(), parent);
    }

    public void addParent(String name, Obj parent) {
        parents.put(name, parent);
    }

    public void removeParent(String name) {
        parents.remove(name);

    }

    public LinkedHashMap<String, ObjCollection> getChildren() {
        return children;
    }

    public ObjCollection getChildren(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first set of children
        ObjCollection allChildren = children.get(elements[0]);
        if (allChildren == null)
            return new ObjCollection(elements[0], spatCal, nFrames);

        // If the first set of children was the only one listed, returning this
        if (elements.length == 1)
            return allChildren;

        // If there are additional parents listed, re-constructing the string and
        // running this method on the parent
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(elements[i]);
            if (i != elements.length - 1)
                stringBuilder.append(" // ");
        }

        // Going through each child in the current set, then adding all their children
        // to the output set
        ObjCollection outputChildren = new ObjCollection(name, allChildren.getSpatialCalibration(), nFrames);
        for (Obj child : allChildren.values()) {
            ObjCollection currentChildren = child.getChildren(stringBuilder.toString());
            for (Obj currentChild : currentChildren.values())
                outputChildren.add(currentChild);
        }

        return outputChildren;

    }

    public void setChildren(LinkedHashMap<String, ObjCollection> children) {
        this.children = children;
    }

    public void addChildren(ObjCollection childSet) {
        children.put(childSet.getName(), childSet);
    }

    public void removeChildren(String name) {
        children.remove(name);
    }

    public void addChild(Obj child) {
        String childName = child.getName();

        children.computeIfAbsent(childName, k -> new ObjCollection(childName, child.getSpatialCalibration(), nFrames));
        children.get(childName).add(child);

    }

    public void removeChild(Obj child) {
        String childName = child.getName();
        children.get(childName).values().remove(child);

    }

    public LinkedHashMap<String, ObjCollection> getPartners() {
        return partners;
    }

    public ObjCollection getPartners(String name) {
        return partners.get(name);
    }

    public void setPartners(LinkedHashMap<String, ObjCollection> partners) {
        this.partners = partners;
    }

    public void addPartners(ObjCollection partnerSet) {
        partners.put(partnerSet.getName(), partnerSet);
    }

    public void removePartners(String name) {
        partners.remove(name);
    }

    public void addPartner(Obj partner) {
        String partnerName = partner.getName();

        partners.computeIfAbsent(partnerName,
                k -> new ObjCollection(partnerName, partner.getSpatialCalibration(), nFrames));
        partners.get(partnerName).add(partner);
    }

    public void removePartner(Obj partner) {
        String partnerName = partner.getName();
        partners.get(partnerName).values().remove(partner);

    }

    public void removePartner(String name) {
        partners.remove(name);
    }

    /**
     * Removes itself from any other objects as a parent or child.
     */
    public void removeRelationships() {
        // Removing itself as a child from its parent
        if (parents != null) {
            for (Obj parent : parents.values()) {
                if (parent != null)
                    parent.removeChild(this);
            }
        }

        // Removing itself as a parent from any children
        if (children != null) {
            for (ObjCollection childSet : children.values()) {
                for (Obj child : childSet.values()) {
                    if (child.getParent(name) == this) {
                        child.removeParent(name);
                    }
                }
            }
        }

        // Removing itself as a partner from any partners
        if (partners != null) {
            for (ObjCollection partnerSet : partners.values()) {
                for (Obj partner : partnerSet.values()) {
                    partner.removePartner(this);
                }
            }
        }

        // Clearing children and parents
        children = new LinkedHashMap<>();
        parents = new LinkedHashMap<>();
        partners = new LinkedHashMap<>();

    }

    public LinkedHashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, Measurement> measurements) {
        this.measurements = measurements;

    }

    public Roi getRoi(int slice) {
        if (rois.containsKey(slice))
            return (Roi) rois.get(slice).clone();

        // Getting the image corresponding to this slice
        SpatCal newCal = new SpatCal(spatCal.getDppXY(), spatCal.getDppZ(), spatCal.getUnits(), spatCal.getWidth(),
                spatCal.getHeight(), 1);
        Obj sliceObj = new Obj(getVolumeType(), "Slice", ID, newCal, nFrames);
        setSlicePoints(sliceObj.coordinateSet, slice);

        // Checking if the object exists in this slice
        if (sliceObj.size() == 0)
            return null;

        ObjCollection objectCollection = new ObjCollection("SliceObjects", newCal, nFrames);
        objectCollection.add(sliceObj);

        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(objectCollection,
                ColourFactory.SingleColours.WHITE);
        Image objectImage = objectCollection.convertToImage("Output", hues, 8, false);
        IJ.run(objectImage.getImagePlus(), "Invert", "stack");

        ImageProcessor ipr = objectImage.getImagePlus().getProcessor();
        ipr.setThreshold(0, 0, ImageProcessor.NO_LUT_UPDATE);
        ThresholdToSelection selection = new ThresholdToSelection();

        Roi roi = selection.convert(objectImage.getImagePlus().getProcessor());
        rois.put(slice, roi);

        return (Roi) roi.clone();

    }

    public void addPointsFromRoi(Roi roi, int z) throws IntegerOverflowException, PointOutOfRangeException {
        for (java.awt.Point point : roi.getContainedPoints()) {
            add((int) point.getX(), (int) point.getY(), z);
        }
    }

    public void addPointsFromPolygon(Polygon polygon, int z) throws PointOutOfRangeException {
        // Determining xy limits
        int minX = Arrays.stream(polygon.xpoints).min().getAsInt();
        int maxX = Arrays.stream(polygon.xpoints).max().getAsInt();
        int minY = Arrays.stream(polygon.ypoints).min().getAsInt();
        int maxY = Arrays.stream(polygon.ypoints).max().getAsInt();

        // Iterating over all possible points, checking if they're inside the polygon
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (polygon.contains(x, y))
                    add(x, y, z);
            }
        }
    }

    public Image getAsImage(String imageName) {
        ImagePlus ipl = IJ.createImage(imageName, spatCal.width, spatCal.height, spatCal.nSlices, 8);

        for (Point<Integer> point : getPoints()) {
            ipl.setPosition(point.getZ() + 1);
            ipl.getProcessor().putPixel(point.getX(), point.getY(), 255);
        }

        return new Image(imageName, ipl);

    }

    public void setSlicePoints(CoordinateSet sliceCoordinateSet, int slice) {
        for (Point<Integer> point : coordinateSet)
            if (point.getZ() == slice)
                sliceCoordinateSet.add(point);
    }

    public Image convertObjToImage(String outputName) {
        // Creating an ObjCollection to hold this image
        ObjCollection tempObj = new ObjCollection(outputName, spatCal, nFrames);
        tempObj.add(this);

        // Getting the image
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(tempObj, ColourFactory.SingleColours.WHITE);
        return tempObj.convertToImage(outputName, hues, 8, false);

    }

    public void removeOutOfBoundsCoords() {
        int width = spatCal.getWidth();
        int height = spatCal.getHeight();
        int nSlices = spatCal.getNSlices();

        getPoints().removeIf(point -> point.getX() < 0 || point.getX() >= width || point.getY() < 0
                || point.getY() >= height || point.getZ() < 0 || point.getZ() >= nSlices);

    }

    @Override
    public void clearAllCoordinates() {
        super.clearAllCoordinates();
        rois = new HashMap<>();
    }

    @Override
    public int hashCode() {
        // Updating the hash for time-point. ID, measurements and relationships aren't
        // included; only spatial location.
        return super.hashCode() * 31 + T;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;

        if (obj == this)
            return true;
        if (!(obj instanceof Obj))
            return false;

        return (T == ((Obj) obj).T);

    }

    @Override
    public String toString() {
        return "Object " + name + ", ID = " + ID + ", frame = " + T;
    }
}
