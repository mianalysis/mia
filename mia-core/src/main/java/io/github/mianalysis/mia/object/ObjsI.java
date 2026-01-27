package io.github.mianalysis.mia.object;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.LUT;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.SpatioTemporallyCalibrated;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.metadata.DefaultObjMetadata;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.process.ColourFactory;

/**
 * Created by sc13967 on 12/05/2017.
 */
public interface ObjsI extends Map<Integer, ObjI>, SpatioTemporallyCalibrated {
    public ObjI createAndAddNewObject(CoordinateSetFactoryI coordinateSetFactory);

    public ObjI createAndAddNewObjectWithID(CoordinateSetFactoryI coordinateSetFactory, int ID);

    public Collection<ObjI> values();

    public int size();

    public String getName();

    public void add(ObjI object);

    public int getAndIncrementID();

    public void resetCollection();

    public void recalculateMaxID();

    public ObjI getAsSingleObject();

    public ObjsI getObjectsInFrame(String outputObjectsName, int frame);

    public void setCalibrationFromExample(SpatioTemporallyCalibrated example, boolean updateAllObjects);

    public ObjsI duplicate(String newObjectsName, boolean duplicateRelationships, boolean duplicateMeasurement,
            boolean duplicateMetadata, boolean addOriginalDuplicateRelationship);

    // Default methods

    public default ObjI getFirst() {
        if (size() == 0)
            return null;

        return values().iterator().next();

    }

    public default int[][] getSpatialExtents() {
        if (size() == 0)
            return null;

        int[][] extents = new int[][] { { Integer.MAX_VALUE, Integer.MIN_VALUE },
                { Integer.MAX_VALUE, Integer.MIN_VALUE }, { Integer.MAX_VALUE, Integer.MIN_VALUE } };

        for (ObjI obj : values()) {
            double[][] currExtents = obj.getExtents(true, false);
            extents[0][0] = (int) Math.round(Math.min(extents[0][0], currExtents[0][0]));
            extents[0][1] = (int) Math.round(Math.max(extents[0][1], currExtents[0][1]));
            extents[1][0] = (int) Math.round(Math.min(extents[1][0], currExtents[1][0]));
            extents[1][1] = (int) Math.round(Math.max(extents[1][1], currExtents[1][1]));
            extents[2][0] = (int) Math.round(Math.min(extents[2][0], currExtents[2][0]));
            extents[2][1] = (int) Math.round(Math.max(extents[2][1], currExtents[2][1]));
        }

        return extents;

    }

    public default int[][] getSpatialLimits() {
        // Taking limits from the first object, otherwise returning null
        if (size() == 0)
            return null;

        return new int[][] { { 0, getWidth() - 1 }, { 0, getHeight() - 1 },
                { 0, getNSlices() - 1 } };

    }

    public default int[] getTemporalLimits() {
        // Finding the first and last frame of all objects in the inputObjects set
        int[] limits = new int[2];
        limits[0] = Integer.MAX_VALUE;
        limits[1] = -Integer.MAX_VALUE;

        for (ObjI object : values()) {
            if (object.getT() < limits[0])
                limits[0] = object.getT();
            if (object.getT() > limits[1])
                limits[1] = object.getT();

        }

        return limits;

    }

    public default int getLargestID() {
        int largestID = 0;
        for (ObjI obj : values())
            if (obj.getID() > largestID)
                largestID = obj.getID();

        return largestID;

    }

    public default ImageI convertToImage(String outputName, HashMap<Integer, Float> hues, int bitDepth,
            boolean nanBackground, boolean verbose) {
        // Create output image
        ImageI image = createImage(outputName, bitDepth);

        // If it's a 32-bit image, set all background pixels to NaN
        if (bitDepth == 32 && nanBackground)
            setNaNBackground(image.getImagePlus());

        // Labelling pixels in image
        int count = 0;
        for (ObjI object : values()) {
            object.addToImage(image, hues.get(object.getID()));
            if (verbose)
                Module.writeProgressStatus(++count, size(), "objects", "Object collection");
        }

        // Assigning the spatial cal from the cal
        applySpatioTemporalCalibrationToImage(image.getImagePlus());

        return image;

    }

    public default ImageI convertToImageRandomColours() {
        HashMap<Integer, Float> hues = ColourFactory.getRandomHues(this);
        ImageI dispImage = convertToImage(getName(), hues, 8, false, false);

        if (dispImage == null)
            return null;
        if (dispImage.getImagePlus() == null)
            return null;

        ImagePlus dispIpl = dispImage.getImagePlus();
        dispIpl.setLut(LUTs.Random(true));
        dispIpl.setPosition(1, 1, 1);
        dispIpl.updateChannelAndDraw();

        return dispImage;

    }

    public default ImageI convertToImageBinary(String name) {
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(this, ColourFactory.SingleColours.WHITE);
        ImageI dispImage = convertToImage(name, hues, 8, false, false);

        if (dispImage == null)
            return null;
        if (dispImage.getImagePlus() == null)
            return null;

        ImagePlus dispIpl = dispImage.getImagePlus();
        dispIpl.setLut(LUT.createLutFromColor(Color.WHITE));
        dispIpl.setPosition(1, 1, 1);
        dispIpl.updateChannelAndDraw();

        return dispImage;

    }

    public default ImageI convertToImageIDColours() {
        HashMap<Integer, Float> hues = ColourFactory.getIDHues(this, false);
        ImageI dispImage = convertToImage(getName(), hues, 32, false, false);

        if (dispImage == null)
            return null;
        if (dispImage.getImagePlus() == null)
            return null;

        ImagePlus dispIpl = dispImage.getImagePlus();
        dispIpl.setLut(LUTs.Random(true, false));
        dispIpl.setDisplayRange(0, 255);
        dispIpl.setPosition(1, 1, 1);
        dispIpl.updateChannelAndDraw();

        return dispImage;

    }

    public default ImageI convertCentroidsToImage(String outputName, HashMap<Integer, Float> hues, int bitDepth,
            boolean nanBackground) {
        // Create output image
        ImageI image = createImage(outputName, bitDepth);

        // If it's a 32-bit image, set all background pixels to NaN
        if (bitDepth == 32 && nanBackground)
            setNaNBackground(image.getImagePlus());

        // Labelling pixels in image
        for (ObjI object : values())
            object.addCentroidToImage(image, hues.get(object.getID()));

        // Assigning the spatial cal from the cal
        applySpatioTemporalCalibrationToImage(image.getImagePlus());

        return image;

    }

    public default ImageI createImage(String outputName, int bitDepth) {
        int nFrames = getNFrames();

        // Creating a new image
        ImagePlus ipl = IJ.createHyperStack(outputName, getWidth(), getHeight(), 1, getNSlices(), nFrames, bitDepth);

        return ImageFactory.createImage(outputName, ipl);

    }

    public default void setNaNBackground(ImagePlus ipl) {
        for (int z = 1; z <= ipl.getNSlices(); z++) {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    for (int x = 0; x < ipl.getWidth(); x++) {
                        for (int y = 0; y < ipl.getHeight(); y++) {
                            ipl.setPosition(c, z, t);
                            ipl.getProcessor().putPixelValue(x, y, Double.NaN);
                        }
                    }
                }
            }
        }
    }

    /*
     * Returns the Obj with coordinates matching the Obj passed as an argument.
     * Useful for unit tests.
     */
    public default ObjI getByEqualsIgnoreNameAndID(ObjI referenceObj) {
        for (ObjI testObj : values())
            if (testObj.equalsIgnoreNameAndID(referenceObj))
                return testObj;

        return null;

    }

    /**
     * Displays measurement values from a specific Module
     * 
     * @param module  The module for which measurements will be displayed
     * @param modules The collection of modules in which this module resides
     */
    public default void showMeasurements(Module module, Modules modules) {
        // Getting MeasurementReferences
        ObjMeasurementRefs measRefs = module.updateAndGetObjectMeasurementRefs();
        if (measRefs == null)
            return;

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        LinkedHashSet<String> measNames = new LinkedHashSet<>();
        for (ObjMeasurementRef measRef : measRefs.values()) {
            if (measRef.getObjectsName().equals(getName()))
                measNames.add(measRef.getName());
        }

        // Iterating over each measurement, adding all the values
        int row = 0;
        for (ObjI obj : values()) {
            if (row != 0)
                rt.incrementCounter();

            // Setting some common values
            rt.setValue("ID", row, obj.getID());
            rt.setValue("X_CENTROID (PX)", row, obj.getXMean(true));
            rt.setValue("Y_CENTROID (PX)", row, obj.getYMean(true));
            rt.setValue("Z_CENTROID (SLICE)", row, obj.getZMean(true, false));
            rt.setValue("TIMEPOINT", row, obj.getT());

            // Setting the measurements from the Module
            for (String measName : measNames) {
                MeasurementI measurement = obj.getMeasurement(measName);
                double value = measurement == null ? Double.NaN : measurement.getValue();

                // Setting value
                rt.setValue(measName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("\"" + module.getName() + " \"measurements for \"" + getName() + "\"");

    }

    public default void showAllMeasurements() {
        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Iterating over each measurement, adding all the values
        int row = 0;
        for (ObjI obj : values()) {
            if (row != 0)
                rt.incrementCounter();

            // Setting some common values
            rt.setValue("ID", row, obj.getID());
            rt.setValue("X_CENTROID (PX)", row, obj.getXMean(true));
            rt.setValue("Y_CENTROID (PX)", row, obj.getYMean(true));
            rt.setValue("Z_CENTROID (SLICE)", row, obj.getZMean(true, false));
            rt.setValue("TIMEPOINT", row, obj.getT());

            // Setting the measurements from the Module
            Set<String> measNames = obj.getMeasurements().keySet();
            for (String measName : measNames) {
                MeasurementI measurement = obj.getMeasurement(measName);
                double value = measurement == null ? Double.NaN : measurement.getValue();

                // Setting value
                rt.setValue(measName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("All measurements for \"" + getName() + "\"");

    }

    /**
     * Displays metadata values from a specific Module
     * 
     * @param module  The module for which metadata will be displayed
     * @param modules The collection of modules in which this module resides
     */
    public default void showMetadata(Module module, Modules modules) {
        // Getting metadata references
        ObjMetadataRefs metadataRefs = module.updateAndGetObjectMetadataRefs();
        if (metadataRefs == null)
            return;

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all metadata relating to this object collection
        LinkedHashSet<String> metadataNames = new LinkedHashSet<>();
        for (ObjMetadataRef metadataRef : metadataRefs.values()) {
            if (metadataRef.getObjectsName().equals(getName()))
                metadataNames.add(metadataRef.getName());
        }

        // Iterating over each measurement, adding all the values
        int row = 0;
        for (ObjI obj : values()) {
            if (row != 0)
                rt.incrementCounter();

            // Setting some common values
            rt.setValue("ID", row, obj.getID());
            rt.setValue("X_CENTROID (PX)", row, obj.getXMean(true));
            rt.setValue("Y_CENTROID (PX)", row, obj.getYMean(true));
            rt.setValue("Z_CENTROID (SLICE)", row, obj.getZMean(true, false));
            rt.setValue("TIMEPOINT", row, obj.getT());

            // Setting the measurements from the Module
            for (String measName : metadataNames) {
                DefaultObjMetadata metadataItem = obj.getMetadataItem(measName);
                String value = metadataItem == null ? "" : metadataItem.getValue();

                // Setting value
                rt.setValue(measName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("\"" + module.getName() + " \"metadata for \"" + getName() + "\"");

    }

    public default void showAllMetadata() {
        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Iterating over each metadata, adding all the values
        int row = 0;
        for (ObjI obj : values()) {
            if (row != 0)
                rt.incrementCounter();

            // Setting some common values
            rt.setValue("ID", row, obj.getID());
            rt.setValue("X_CENTROID (PX)", row, obj.getXMean(true));
            rt.setValue("Y_CENTROID (PX)", row, obj.getYMean(true));
            rt.setValue("Z_CENTROID (SLICE)", row, obj.getZMean(true, false));
            rt.setValue("TIMEPOINT", row, obj.getT());

            // Setting the measurements from the Module
            Set<String> metadataNames = obj.getMetadata().keySet();
            for (String metadataName : metadataNames) {
                DefaultObjMetadata metadataItem = obj.getMetadataItem(metadataName);
                String value = metadataItem == null ? "" : metadataItem.getValue();

                // Setting value
                rt.setValue(metadataName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("All measurements for \"" + getName() + "\"");

    }

    public default void removeParents(String parentObjectsName) {
        for (ObjI obj : values())
            obj.removeParent(parentObjectsName);

    }

    public default void removeChildren(String childObjectsName) {
        for (ObjI obj : values())
            obj.removeChildren(childObjectsName);
    }

    public default void removePartners(String partnerObjectsName) {
        for (ObjI obj : values())
            obj.removePartners(partnerObjectsName);
    }

    public default boolean containsPoint(Point<Integer> point) {
        for (ObjI obj : values()) {
            if (obj.contains(point))
                return true;
        }

        return false;

    }

    public default boolean containsCoord(int x, int y, int z) {
        Point<Integer> point = new Point<>(x, y, z);

        return containsPoint(point);

    }

    public default ObjI getLargestObject(int t) {
        ObjI referenceObject = null;
        int objSize = Integer.MIN_VALUE;

        // Iterating over each object, checking its size against the current reference
        // values
        for (ObjI currReferenceObject : values()) {
            // Only check objects in the current frame (if required - if frame doesn't
            // matter, t = -1)
            if (t != -1 && currReferenceObject.getT() != t)
                continue;
            if (currReferenceObject.size() > objSize) {
                objSize = currReferenceObject.size();
                referenceObject = currReferenceObject;
            }
        }

        return referenceObject;

    }

    public default ObjI getSmallestObject(int t) {
        ObjI referenceObject = null;
        int objSize = Integer.MAX_VALUE;

        // Iterating over each object, checking its size against the current reference
        // values
        for (ObjI currReferenceObject : values()) {
            // Only check objects in the current frame (if required - if frame doesn't
            // matter, t = -1)
            if (t != -1 && currReferenceObject.getT() != t)
                continue;

            if (currReferenceObject.size() < objSize) {
                objSize = currReferenceObject.size();
                referenceObject = currReferenceObject;
            }
        }

        return referenceObject;

    }
}

// clear all
// print'HelloWorld'
// if{
// 'theSkyIsGreen'
// getMeasurements
// }
