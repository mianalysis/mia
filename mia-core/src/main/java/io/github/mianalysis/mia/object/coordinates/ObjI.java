package io.github.mianalysis.mia.object.coordinates;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.poi.ss.formula.functions.T;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import io.github.mianalysis.mia.object.ImgPlusCoordinateIterator;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.measurements.MeasurementProvider;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface ObjI extends MeasurementProvider, VolumeI, SpatioTemporallyCalibrated {
    public default LinkedHashMap<String, ObjI> getParents(boolean useFullHierarchy) {
        if (!useFullHierarchy)
            return getAllParents();

        // Adding each parent and then the parent of that
        LinkedHashMap<String, ObjI> parentHierarchy = new LinkedHashMap<>(getAllParents());

        // Going through each parent, adding the parents of that.
        for (ObjI parent : getAllParents().values()) {
            if (parent == null)
                continue;

            LinkedHashMap<String, ObjI> currentParents = parent.getParents(true);
            if (currentParents == null)
                continue;

            parentHierarchy.putAll(currentParents);

        }

        return parentHierarchy;

    }

    public default ObjI getParent(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first parent
        ObjI parent = getAllParents().get(elements[0]);

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

    public default void addParent(ObjI parent) {
        getAllParents().put(parent.getName(), parent);
    }

    public default void removeParent(String name) {
        getAllParents().remove(name);
    }

    public default ObjsI getChildren(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first set of children
        ObjsI allChildren = getAllChildren().get(elements[0]);
        if (allChildren == null)
            return ObjsFactories.getDefaultFactory().createFromExample(elements[0], getObjectCollection());

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
        ObjsI outputChildren = ObjsFactories.getDefaultFactory().createFromExample(name, allChildren);
        for (ObjI child : allChildren.values()) {
            ObjsI currentChildren = child.getChildren(stringBuilder.toString());
            for (ObjI currentChild : currentChildren.values())
                outputChildren.add(currentChild);
        }

        return outputChildren;

    }

    public default void addChildren(ObjsI childSet) {
        getAllChildren().put(childSet.getName(), childSet);
    }

    public default void removeChildren(String name) {
        getAllChildren().remove(name);
    }

    public default void addChild(ObjI child) {
        String childName = child.getName();

        getAllChildren().putIfAbsent(childName, ObjsFactories.getDefaultFactory().createFromExample(childName, child.getObjectCollection()));
        getAllChildren().get(childName).add(child);

    }

    public default void removeChild(ObjI child) {
        String childName = child.getName();
        getAllChildren().get(childName).values().remove(child);

    }

    public default ObjsI getPartners(String name) {
        return getAllPartners().get(name);
    }

    public default void addPartners(ObjsI partnerSet) {
        getAllPartners().put(partnerSet.getName(), partnerSet);
    }

    public default void addPartner(ObjI partner) {
        String partnerName = partner.getName();

        getAllPartners().putIfAbsent(partnerName, ObjsFactories.getDefaultFactory().createFromExample(partnerName, partner.getObjectCollection()));
        getAllPartners().get(partnerName).add(partner);
    }

    public default void removePartner(ObjI partner) {
        String partnerName = partner.getName();
        getAllPartners().get(partnerName).values().remove(partner);

    }

    public default void removePartners(String name) {
        getAllPartners().remove(name);
    }

    /**
     * Returns any partners that happened in previous frames
     */
    public default ObjsI getPreviousPartners(String name) {
        ObjsI allPartners = getPartners(name);

        if (allPartners == null)
            return ObjsFactories.getDefaultFactory().createFromExample(name, getObjectCollection());

        ObjsI previousPartners = ObjsFactories.getDefaultFactory().createFromExample(name, allPartners);
        for (ObjI partner : allPartners.values())
            if (partner.getT() < getT())
                previousPartners.add(partner);

        return previousPartners;

    }

    /**
     * Returns any partners that happen in following frames
     */
    public default ObjsI getSimultaneousPartners(String name) {
        ObjsI allPartners = getPartners(name);

        if (allPartners == null)
            return ObjsFactories.getDefaultFactory().createFromExample(name, getObjectCollection());

        ObjsI simultaneousPartners = ObjsFactories.getDefaultFactory().createFromExample(name, allPartners);
        for (ObjI partner : allPartners.values())
            if (partner.getT() == getT())
                simultaneousPartners.add(partner);

        return simultaneousPartners;

    }

    /**
     * Returns any partners that happen in following frames
     */
    public default ObjsI getNextPartners(String name) {
        ObjsI allPartners = getPartners(name);

        if (allPartners == null)
            return ObjsFactories.getDefaultFactory().createFromExample(name, getObjectCollection());

        ObjsI nextPartners = ObjsFactories.getDefaultFactory().createFromExample(name, allPartners);
        for (ObjI partner : allPartners.values())
            if (partner.getT() > getT())
                nextPartners.add(partner);

        return nextPartners;

    }

    public default void addMetadataItem(ObjMetadata metadataItem) {
        if (metadataItem == null)
            return;
        getMetadata().put(metadataItem.getName(), metadataItem);

    }

    public default ObjMetadata getMetadataItem(String name) {
        LinkedHashMap<String, ObjMetadata> metadata = getMetadata();

        if (metadata.get(name) == null)
            return null;

        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        return metadata.get(name);

    }

    public default void removeMetadataItem(String name) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        getMetadata().remove(name);

    }

    public default void addMeasurement(Measurement measurement) {
        if (measurement == null)
            return;
        getMeasurements().put(measurement.getName(), measurement);

    }

    public default Measurement getMeasurement(String name) {
        HashMap<String, Measurement> measurements = getMeasurements();

        if (measurements.get(name) == null)
            return null;

        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        return measurements.get(name);

    }

    public default void removeMeasurement(String name) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);

        getMeasurements().remove(name);

    }

    public default ImageI getAsImage(String imageName, boolean singleTimepoint) {
        if (singleTimepoint)
            return getAsImage(imageName, 0, 1);
        else
            return getAsImage(imageName, getT(), getNFrames());
    }

    public default ImageI getCentroidAsImage(String imageName, boolean singleTimepoint) {
        int nFrames = singleTimepoint ? 1 : getNFrames();
        int t = singleTimepoint ? 0 : getT();

        ImagePlus ipl = IJ.createHyperStack(imageName, getWidth(), getHeight(), 1, getNSlices(), nFrames, 8);
        ipl.getCalibration().pixelWidth = getWidth();
        ipl.getCalibration().pixelHeight = getHeight();
        ipl.getCalibration().pixelDepth = getNSlices() == 1 ? 1 : getDppZ();
        ipl.getCalibration().setUnit(getSpatialUnits());
        
        Point<Double> centroid = getMeanCentroid(true, false);
        int x = (int) Math.round(centroid.getX());
        int y = (int) Math.round(centroid.getY());
        int z = (int) Math.round(centroid.getZ());

        int idx = ipl.getStackIndex(1, z + 1, t + 1);
        ipl.getStack().getProcessor(idx).set(x, y, 255);

        return ImageFactory.createImage(imageName, ipl);

    }

    public default void addToImage(ImageI image, float hue) {
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

    public default void addCentroidToImage(ImageI image, float hue) {
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

    public default void removeOutOfBoundsCoords() {
        int width = getWidth();
        int height = getHeight();
        int nSlices = getNSlices();

        getCoordinateSet().removeIf(point -> point.getX() < 0 || point.getX() >= width || point.getY() < 0
                || point.getY() >= height || point.getZ() < 0 || point.getZ() >= nSlices);

    }

    public default <T extends RealType<T> & NativeType<T>> Iterator<net.imglib2.Point> getImgPlusCoordinateIterator (
            ImgPlus<T> imgPlus, int c) {
        return new ImgPlusCoordinateIterator<>(getCoordinateIterator(), imgPlus, c, getT());
    }

    public static String getNameWithoutRelationship(String name) {
        if (name.contains("//"))
            name = name.substring(name.lastIndexOf("//") + 3);

        return name;

    }

    public ObjsI getObjectCollection();

    public void setObjectCollection(ObjsI objCollection);

    public String getName();

    public int getID();

    public ObjI setID(int ID);

    public int getT();

    public ObjI setT(int t);

    public LinkedHashMap<String, ObjI> getAllParents();

    public void setAllParents(LinkedHashMap<String, ObjI> parents);

    public LinkedHashMap<String, ObjsI> getAllChildren();

    public void setAllChildren(LinkedHashMap<String, ObjsI> children);

    public LinkedHashMap<String, ObjsI> getAllPartners();

    public void setAllPartners(LinkedHashMap<String, ObjsI> partners);

    public void removeRelationships();
    
    public LinkedHashMap<String, ObjMetadata> getMetadata();

    public void setMetadata(LinkedHashMap<String, ObjMetadata> metadata);

    public HashMap<Integer, Roi> getRois();

    public void clearROIs();

    public ObjI duplicate(ObjsI newCollection, boolean duplicateRelationships, boolean duplicateMeasurement,
            boolean duplicateMetadata);

    public boolean equalsIgnoreNameAndID(Object obj);

}
