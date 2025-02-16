package io.github.mianalysis.mia.object;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.measurements.MeasurementProvider;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;

public interface ObjI extends MeasurementProvider, VolumeI {
    public default LinkedHashMap<String, ObjI> getParents(boolean useFullHierarchy) {
        if (!useFullHierarchy)
            return getParents();

        // Adding each parent and then the parent of that
        LinkedHashMap<String, ObjI> parentHierarchy = new LinkedHashMap<>(getParents());

        // Going through each parent, adding the parents of that.
        for (ObjI parent : getParents().values()) {
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
        ObjI parent = getParents().get(elements[0]);

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
        getParents().put(parent.getName(), parent);
    }

    public default void addParent(String name, ObjI parent) {
        getParents().put(name, parent);
    }

    public default void removeParent(String name) {
        getParents().remove(name);
    }

    public default void removeParent(ObjI parent) {
        getParents().remove(parent.getName());
    }

    public default Objs getChildren(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first set of children
        Objs allChildren = getChildren().get(elements[0]);
        if (allChildren == null)
            return new Objs(elements[0], getObjectCollection());

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
        for (ObjI child : allChildren.values()) {
            Objs currentChildren = child.getChildren(stringBuilder.toString());
            for (Obj currentChild : currentChildren.values())
                outputChildren.add(currentChild);
        }

        return outputChildren;

    }

    public default void addChildren(Objs childSet) {
        getChildren().put(childSet.getName(), childSet);
    }

    public default void removeChildren(String name) {
        getChildren().remove(name);
    }

    public default void addChild(Obj child) {
        String childName = child.getName();

        getChildren().computeIfAbsent(childName, k -> new Objs(childName, child.getObjectCollection()));
        getChildren().get(childName).add(child);

    }

    public default void removeChild(Obj child) {
        String childName = child.getName();
        getChildren().get(childName).values().remove(child);

    }

    public default Objs getPartners(String name) {
        return getPartners().get(name);
    }

    public default void addPartners(Objs partnerSet) {
        getPartners().put(partnerSet.getName(), partnerSet);
    }

    public default void removePartners(String name) {
        getPartners().remove(name);
    }

    public default void addPartner(Obj partner) {
        String partnerName = partner.getName();

        getPartners().computeIfAbsent(partnerName, k -> new Objs(partnerName, partner.getObjectCollection()));
        getPartners().get(partnerName).add(partner);
    }

    public default void removePartner(Obj partner) {
        String partnerName = partner.getName();
        getPartners().get(partnerName).values().remove(partner);

    }

    public default void removePartner(String name) {
        getPartners().remove(name);
    }

    /**
     * Returns any partners that happened in previous frames
     */
    public default Objs getPreviousPartners(String name) {
        Objs allPartners = getPartners(name);

        if (allPartners == null)
            return new Objs(name, getObjectCollection());

        Objs previousPartners = new Objs(name, allPartners);
        for (Obj partner : allPartners.values())
            if (partner.getT() < getT())
                previousPartners.add(partner);

        return previousPartners;

    }

    /**
     * Returns any partners that happen in following frames
     */
    public default Objs getSimultaneousPartners(String name) {
        Objs allPartners = getPartners(name);

        if (allPartners == null)
            return new Objs(name, getObjectCollection());

        Objs simultaneousPartners = new Objs(name, allPartners);
        for (Obj partner : allPartners.values())
            if (partner.getT() == getT())
                simultaneousPartners.add(partner);

        return simultaneousPartners;

    }

    /**
     * Returns any partners that happen in following frames
     */
    public default Objs getNextPartners(String name) {
        Objs allPartners = getPartners(name);

        if (allPartners == null)
            return new Objs(name, getObjectCollection());

        Objs nextPartners = new Objs(name, allPartners);
        for (Obj partner : allPartners.values())
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
            return getAsImage(imageName, getT(), getObjectCollection().getNFrames());
    }

    public default ImageI getCentroidAsImage(String imageName, boolean singleTimepoint) {
        SpatCal spatCal = getSpatialCalibration();

        int nFrames = singleTimepoint ? 1 : getObjectCollection().getNFrames();
        int t = singleTimepoint ? 0 : getT();

        ImagePlus ipl = IJ.createHyperStack(imageName, spatCal.width, spatCal.height, 1, spatCal.nSlices, nFrames, 8);
        spatCal.applyImageCalibration(ipl);

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
        SpatCal spatCal = getSpatialCalibration();

        int width = spatCal.getWidth();
        int height = spatCal.getHeight();
        int nSlices = spatCal.getNSlices();

        getCoordinateSet().removeIf(point -> point.getX() < 0 || point.getX() >= width || point.getY() < 0
                || point.getY() >= height || point.getZ() < 0 || point.getZ() >= nSlices);

    }

    public static String getNameWithoutRelationship(String name) {
        if (name.contains("//"))
            name = name.substring(name.lastIndexOf("//") + 3);

        return name;

    }

    public Objs getObjectCollection();

    public void setObjectCollection(Objs objCollection);

    public String getName();

    public int getID();

    public ObjI setID(int ID);

    public int getT();

    public ObjI setT(int t);

    public LinkedHashMap<String, ObjI> getParents();

    public void setParents(LinkedHashMap<String, ObjI> parents);

    public LinkedHashMap<String, Objs> getChildren();

    public void setChildren(LinkedHashMap<String, Objs> children);

    public LinkedHashMap<String, Objs> getPartners();

    public void setPartners(LinkedHashMap<String, Objs> partners);

    public void removeRelationships();
    
    public LinkedHashMap<String, ObjMetadata> getMetadata();

    public void setMetadata(LinkedHashMap<String, ObjMetadata> metadata);

    public HashMap<Integer, Roi> getRois();

    public void clearROIs();

    public Obj duplicate(Objs newCollection, boolean duplicateRelationships, boolean duplicateMeasurement,
            boolean duplicateMetadata);

    public boolean equalsIgnoreNameAndID(Object obj);

}
