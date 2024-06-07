package io.github.mianalysis.mia.object;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

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
    private LinkedHashMap<String, ObjMetadata> metadata = new LinkedHashMap<>();
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

    public void addMetadataItem(ObjMetadata metadataItem) {
        if (metadataItem == null)
            return;
        metadata.put(metadataItem.getName(), metadataItem);

    }

    public ObjMetadata getMetadataItem(String name) {
        if (metadata.get(name) == null)
            return null;

        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        return metadata.get(name);

    }

    public void removeMetadataItem(String name) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        metadata.remove(name);

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

    public void removeParent(Obj parent) {
        parents.remove(parent.getName());
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
            return new Objs(elements[0], objCollection);

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
        Objs outputChildren = new Objs(name, allChildren);
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

        children.computeIfAbsent(childName, k -> new Objs(childName, child.getObjectCollection()));
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

        partners.computeIfAbsent(partnerName, k -> new Objs(partnerName, partner.getObjectCollection()));
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
     * Returns any partners that happened in previous frames
     */
    public Objs getPreviousPartners(String name) {
        Objs allPartners = getPartners(name);

        if (allPartners == null)
            return new Objs(name, objCollection);

        Objs previousPartners = new Objs(name, allPartners);
        for (Obj partner : allPartners.values())
            if (partner.getT() < T)
                previousPartners.add(partner);

        return previousPartners;

    }

    /**
     * Returns any partners that happen in following frames
     */
    public Objs getSimultaneousPartners(String name) {
        Objs allPartners = getPartners(name);

        if (allPartners == null)
            return new Objs(name, objCollection);

        Objs simultaneousPartners = new Objs(name, allPartners);
        for (Obj partner : allPartners.values())
            if (partner.getT() == T)
                simultaneousPartners.add(partner);

        return simultaneousPartners;

    }

    /**
     * Returns any partners that happen in following frames
     */
    public Objs getNextPartners(String name) {
        Objs allPartners = getPartners(name);

        if (allPartners == null)
            return new Objs(name, objCollection);

        Objs nextPartners = new Objs(name, allPartners);
        for (Obj partner : allPartners.values())
            if (partner.getT() > T)
                nextPartners.add(partner);

        return nextPartners;

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

    public LinkedHashMap<String, ObjMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(LinkedHashMap<String, ObjMetadata> metadata) {
        this.metadata = metadata;

    }

    @Override
    public Roi getRoi(int slice) {
        if (rois.containsKey(slice))
            return (Roi) rois.get(slice).clone();

        Roi roi = super.getRoi(slice);

        if (roi == null)
            return null;

        rois.put(slice, roi);

        return (Roi) roi.clone();

    }

    /**
     * Accesses and generates all ROIs for the object
     * 
     * @return HashMap containing each ROI. Keys are integers with zero-based
     *         numbering, specifying Z-axis slice.
     */
    public HashMap<Integer, Roi> getRois() {
        // This will access and generate all ROIs for this object
        double[][] extents = getExtents(true, false);
        int minZ = (int) Math.round(extents[2][0]);
        int maxZ = (int) Math.round(extents[2][1]);
        for (int z = minZ; z <= maxZ; z++)
            getRoi(z);

        // For the sake of not wasting memory, this will output the original list of
        // ROIs
        return rois;

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
        if (singleTimepoint)
            return getAsImage(imageName, getT(), objCollection.getNFrames());
        else
            return getAsImage(imageName, 0, 1);
    }

    public Image getCentroidAsImage(String imageName, boolean singleTimepoint) {
        int nFrames = singleTimepoint ? 1 : objCollection.getNFrames();
        int t = singleTimepoint ? 0 : getT();

        ImagePlus ipl = IJ.createHyperStack(imageName, spatCal.width, spatCal.height, 1, spatCal.nSlices, nFrames, 8);
        spatCal.setImageCalibration(ipl);

        Point<Double> centroid = getMeanCentroid(true, false);
        int x = (int) Math.round(centroid.getX());
        int y = (int) Math.round(centroid.getY());
        int z = (int) Math.round(centroid.getZ());

        int idx = ipl.getStackIndex(1, z + 1, t + 1);
        ipl.getStack().getProcessor(idx).set(x, y, 255);

        return ImageFactory.createImage(imageName, ipl);

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

    public void removeOutOfBoundsCoords() {
        int width = spatCal.getWidth();
        int height = spatCal.getHeight();
        int nSlices = spatCal.getNSlices();

        getCoordinateSet().removeIf(point -> point.getX() < 0 || point.getX() >= width || point.getY() < 0
                || point.getY() >= height || point.getZ() < 0 || point.getZ() >= nSlices);

    }

    public <T extends RealType<T> & NativeType<T>> Iterator<net.imglib2.Point> getImgPlusCoordinateIterator(
            ImgPlus<T> imgPlus, int c) {
        return new ImgPlusCoordinateIterator<>(getCoordinateIterator(), imgPlus, c, T);
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
        // Updating the hash for time-point. Measurements and relationships aren't
        // included; only spatial location.
        return super.hashCode() * 31 + getID() * 31 + getT() * 31 + getName().hashCode();
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

        if (ID != ((Obj) obj).getID())
            return false;

        if (T != ((Obj) obj).getT())
            return false;

        return (getName().equals(((Obj) obj).getName()));

    }

    public boolean equalsIgnoreNameAndID(Object obj) {
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
