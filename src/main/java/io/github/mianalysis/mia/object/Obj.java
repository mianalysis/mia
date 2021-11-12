package io.github.mianalysis.mia.object;

import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.Point;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.Volume;
import io.github.sjcross.common.object.volume.VolumeType;

/**
 * Created by Stephen on 30/04/2017.
 */
public class Obj extends Volume {
    /**
     * Unique instance ID for this object
     */
    private int ID;

    private int T;

    private Objs objCollection;

    private LinkedHashMap<String, Obj> parents = new LinkedHashMap<>();
    private LinkedHashMap<String, Objs> children = new LinkedHashMap<>();
    private LinkedHashMap<String, Objs> partners = new LinkedHashMap<>();
    private LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();
    private HashMap<Integer, Roi> rois = new HashMap<>();

    // CONSTRUCTORS

    public Obj(Objs objCollection, VolumeType volumeType, int ID) {
        super(volumeType, objCollection.getSpatialCalibration());

        this.objCollection = objCollection;
        this.ID = ID;

    }

    public Obj(Objs objCollection, int ID, Volume exampleVolume) {
        super(exampleVolume.getVolumeType(), exampleVolume.getSpatialCalibration());

        this.objCollection = objCollection;
        this.ID = ID;

    }

    public Obj(Objs objCollection, int ID, Obj exampleObj) {
        super(exampleObj.getVolumeType(), exampleObj.getSpatialCalibration());

        this.objCollection = objCollection;
        this.ID = ID;

    }

    public Obj(Objs objCollection, VolumeType volumeType, int ID, Volume exampleVolume) {
        super(volumeType, exampleVolume.getSpatialCalibration());

        this.objCollection = objCollection;
        this.ID = ID;

    }

    public Obj(Objs objCollection, VolumeType volumeType, int ID, Obj exampleObj) {
        super(volumeType, exampleObj.getSpatialCalibration());

        this.objCollection = objCollection;
        this.ID = ID;

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

        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        return measurements.get(name);

    }

    public void removeMeasurement(String name) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        measurements.remove(name);

    }

    public Objs getObjectCollection() {
        return objCollection;
    }

    public void setObjectCollection(Objs objCollection) {
        this.objCollection = objCollection;
    }

    public String getName() {
        return objCollection.getName();
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

    public LinkedHashMap<String, Objs> getChildren() {
        return children;
    }

    public Objs getChildren(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first set of children
        Objs allChildren = children.get(elements[0]);
        if (allChildren == null)
            return new Objs(elements[0], spatCal, objCollection.getNFrames(), objCollection.getFrameInterval(),
                    objCollection.getTemporalUnit());

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
        Objs outputChildren = new Objs(name, allChildren.getSpatialCalibration(),
                objCollection.getNFrames(), objCollection.getFrameInterval(), objCollection.getTemporalUnit());
        for (Obj child : allChildren.values()) {
            Objs currentChildren = child.getChildren(stringBuilder.toString());
            for (Obj currentChild : currentChildren.values())
                outputChildren.add(currentChild);
        }

        return outputChildren;

    }

    public void setChildren(LinkedHashMap<String, Objs> children) {
        this.children = children;
    }

    public void addChildren(Objs childSet) {
        children.put(childSet.getName(), childSet);
    }

    public void removeChildren(String name) {
        children.remove(name);
    }

    public void addChild(Obj child) {
        String childName = child.getName();

        children.computeIfAbsent(childName, k -> new Objs(childName, child.getSpatialCalibration(),
                objCollection.getNFrames(), objCollection.getFrameInterval(), objCollection.getTemporalUnit()));
        children.get(childName).add(child);

    }

    public void removeChild(Obj child) {
        String childName = child.getName();
        children.get(childName).values().remove(child);

    }

    public LinkedHashMap<String, Objs> getPartners() {
        return partners;
    }

    public Objs getPartners(String name) {
        return partners.get(name);
    }

    public void setPartners(LinkedHashMap<String, Objs> partners) {
        this.partners = partners;
    }

    public void addPartners(Objs partnerSet) {
        partners.put(partnerSet.getName(), partnerSet);
    }

    public void removePartners(String name) {
        partners.remove(name);
    }

    public void addPartner(Obj partner) {
        String partnerName = partner.getName();

        partners.computeIfAbsent(partnerName, k -> new Objs(partnerName, partner.getSpatialCalibration(),
                objCollection.getNFrames(), objCollection.getFrameInterval(), objCollection.getTemporalUnit()));
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
            for (Objs childSet : children.values()) {
                for (Obj child : childSet.values()) {
                    if (child.getParent(getName()) == this) {
                        child.removeParent(getName());
                    }
                }
            }
        }

        // Removing itself as a partner from any partners
        if (partners != null) {
            for (Objs partnerSet : partners.values()) {
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

    @Override
    public Roi getRoi(int slice) {
        if (rois.containsKey(slice))
            return (Roi) rois.get(slice).clone();

        // // Getting the image corresponding to this slice
        // Volume sliceVol = getSlice(slice);

        // Objs objectCollection = new Objs("Slice", sliceVol.getSpatialCalibration(), 1, 1, null);
        // Obj sliceObj = objectCollection.createAndAddNewObject(sliceVol.getVolumeType(), ID);
        // sliceObj.setCoordinateSet(sliceVol.getCoordinateSet());

        // // Checking if the object exists in this slice
        // if (sliceVol.size() == 0)
        //     return null;

        // HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(objectCollection,
        //         ColourFactory.SingleColours.WHITE);
        // Image objectImage = objectCollection.convertToImage("Output", hues, 8, false);
        // IJ.run(objectImage.getImagePlus(), "Invert", "stack");

        // ImageProcessor ipr = objectImage.getImagePlus().getProcessor();
        // ipr.setThreshold(0, 0, ImageProcessor.NO_LUT_UPDATE);
        // ThresholdToSelection selection = new ThresholdToSelection();

        // Roi roi = selection.convert(objectImage.getImagePlus().getProcessor());
        Roi roi = super.getRoi(slice);
        rois.put(slice, roi);

        return (Roi) roi.clone();

    }

    public void addPointsFromRoi(Roi roi, int z) throws IntegerOverflowException {
        for (java.awt.Point point : roi.getContainedPoints()) {
            try {
                add((int) point.getX(), (int) point.getY(), z);
            } catch (PointOutOfRangeException e) {
            }
        }
    }

    public void addPointsFromPolygon(Polygon polygon, int z) throws PointOutOfRangeException {
        addPointsFromShape(polygon, z);
    }

    public void addPointsFromShape(Shape polygon, int z) throws PointOutOfRangeException {
        // Determining xy limits
        // int minX = Arrays.stream(polygon.xpoints).min().getAsInt();
        // int maxX = Arrays.stream(polygon.xpoints).max().getAsInt();
        // int minY = Arrays.stream(polygon.ypoints).min().getAsInt();
        // int maxY = Arrays.stream(polygon.ypoints).max().getAsInt();

        Rectangle bounds = polygon.getBounds();
        int minX = bounds.x;
        int maxX = minX + bounds.width;
        int minY = bounds.y;
        int maxY = minY + bounds.height;

        // Iterating over all possible points, checking if they're inside the polygon
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (polygon.contains(x, y))
                    add(x, y, z);
            }
        }
    }

    public Image getAsImage(String imageName, boolean singleTimepoint) {
        int nFrames = singleTimepoint ? 1 : objCollection.getNFrames();
        int t = singleTimepoint ? 0 : getT();

        ImagePlus ipl = IJ.createHyperStack(imageName, spatCal.width, spatCal.height, 1, spatCal.nSlices, nFrames, 8);
        spatCal.setImageCalibration(ipl);

        for (Point<Integer> point : getCoordinateSet()) {
            int idx = ipl.getStackIndex(1, point.getZ() + 1, t + 1);
            ipl.getStack().getProcessor(idx).set(point.getX(), point.getY(), 255);
            // ipl.setPosition(point.getZ() + 1);
            // ipl.getProcessor().putPixel(point.getX(), point.getY(), 255);
        }

        return new Image(imageName, ipl);

    }

    public void addToImage(Image image, float hue) {
        ImagePlus ipl = image.getImagePlus();
        int bitDepth = ipl.getBitDepth();

        int tPos = getT();
        for (Point<Integer> point : getCoordinateSet()) {
            int xPos = point.x;
            int yPos = point.y;
            int zPos = point.z;

            ipl.setPosition(1, zPos + 1, tPos + 1);

            switch (bitDepth) {
                case 8:
                case 16:
                    ipl.getProcessor().putPixel(xPos, yPos, Math.round(hue * 255));
                    break;
                case 32:
                    ipl.getProcessor().putPixelValue(xPos, yPos, hue);
                    break;
            }
        }
    }

    public void addCentroidToImage(Image image, float hue) {
        ImagePlus ipl = image.getImagePlus();
        int bitDepth = ipl.getBitDepth();

        int tPos = getT();
        int xPos = (int) Math.round(getXMean(true));
        int yPos = (int) Math.round(getYMean(true));
        int zPos = (int) Math.round(getZMean(true, false));

        ipl.setPosition(1, zPos + 1, tPos + 1);

        switch (bitDepth) {
            case 8:
            case 16:
                ipl.getProcessor().putPixel(xPos, yPos, Math.round(hue * 255));
                break;
            case 32:
                ipl.getProcessor().putPixelValue(xPos, yPos, hue);
                break;
        }
    }

    public Image getAsTightImage(String imageName) {
        int[][] borderWidths = new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 } };

        return getAsTightImage(imageName, borderWidths);

    }

    public Image getAsTightImage(String imageName, int[][] borderWidths) {
        double[][] extents = getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]) - borderWidths[0][0];
        int yOffs = (int) Math.round(extents[1][0]) - borderWidths[1][0];
        int zOffs = (int) Math.round(extents[2][0]) - borderWidths[2][0];

        int width = (int) Math.round(extents[0][1]) - (int) Math.round(extents[0][0]) + borderWidths[0][0]
                + borderWidths[0][1] + 1;
        int height = (int) Math.round(extents[1][1]) - (int) Math.round(extents[1][0]) + borderWidths[1][0]
                + borderWidths[1][1] + 1;
        int nSlices = (int) Math.round(extents[2][1]) - (int) Math.round(extents[2][0]) + borderWidths[2][0]
                + borderWidths[2][1] + 1;

        ImagePlus ipl = IJ.createImage(imageName, width, height, nSlices, 8);
        spatCal.setImageCalibration(ipl);

        // Populating ipl
        for (Point<Integer> point : getCoordinateSet()) {
            ipl.setPosition(point.z - zOffs + 1);
            ipl.getProcessor().putPixel(point.x - xOffs, point.y - yOffs, 255);

        }

        return new Image("Tight", ipl);

    }

    public void removeOutOfBoundsCoords() {
        int width = spatCal.getWidth();
        int height = spatCal.getHeight();
        int nSlices = spatCal.getNSlices();

        getCoordinateSet().removeIf(point -> point.getX() < 0 || point.getX() >= width || point.getY() < 0
                || point.getY() >= height || point.getZ() < 0 || point.getZ() >= nSlices);

    }

    public void clearROIs() {
        rois = new HashMap<>();

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
        return super.hashCode() * 31 + getT();
    }

    @Override
    public boolean equals(Object obj) {
        int T = getT();

        if (!super.equals(obj))
            return false;

        if (obj == this)
            return true;
        if (!(obj instanceof Obj))
            return false;

        return (T == ((Obj) obj).getT());

    }

    @Override
    public String toString() {
        return "Object \"" + getName() + "\", ID = " + ID + ", frame = " + getT();
    }

    public static String getNameWithoutRelationship(String name) {
        if (name.contains("//"))
            name = name.substring(name.lastIndexOf("//") + 3);

        return name;
        
    }
}
