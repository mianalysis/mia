package io.github.mianalysis.mia.object;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.LUT;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.ObjFactories;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.ColourFactory;
import ome.units.UNITS;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class DefaultObjs extends LinkedHashMap<Integer, ObjI> implements ObjsI  {
    private static final long serialVersionUID = 7383226061156796558L;
    private String name;
    private int maxID = 0;
    private SpatCal spatCal;
    private int nFrames;
    private double frameInterval;
    private Unit<Time> temporalUnit;

    public DefaultObjs(String name, double dppXY, double dppZ, String units, int width, int height, int nSlices, int nFrames,
            double frameInterval, Unit<Time> temporalUnit) {
        this.name = name;
        this.spatCal = new SpatCal(dppXY, dppZ, units, width, height, nSlices);
        this.nFrames = nFrames;
        this.frameInterval = frameInterval;
        this.temporalUnit = temporalUnit;

    }

    public DefaultObjs(String name, SpatCal cal, int nFrames, double frameInterval, Unit<Time> temporalUnit) {
        this.name = name;
        this.spatCal = cal;
        this.nFrames = nFrames;
        this.frameInterval = frameInterval;
        this.temporalUnit = temporalUnit;

    }

    public DefaultObjs(String name, ObjsI exampleCollection) {
        this.name = name;
        this.spatCal = exampleCollection.getSpatialCalibration().duplicate();
        this.nFrames = exampleCollection.getNFrames();
        this.frameInterval = exampleCollection.getFrameInterval();
        this.temporalUnit = exampleCollection.getTemporalUnit();

    }

    public DefaultObjs(String name, ImagePlus imageForCalibration) {
        this.name = name;
        this.spatCal = SpatCal.getFromImage(imageForCalibration);
        this.nFrames = imageForCalibration.getNFrames();
        this.frameInterval = imageForCalibration.getCalibration().frameInterval;
        this.temporalUnit = TemporalUnit.getOMEUnit();

    }

    public ObjI createAndAddNewObject(CoordinateSetFactoryI factory) {
        ObjI newObject = ObjFactories.getDefaultFactory().createObj(this, factory, getAndIncrementID());
        add(newObject);

        return newObject;

    }

    public ObjI createAndAddNewObjectWithID(CoordinateSetFactoryI factory, int ID) {
        ObjI newObject = ObjFactories.getDefaultFactory().createObj(this, factory, ID);
        add(newObject);

        // Updating the maxID if necessary
        maxID = Math.max(maxID, ID);

        return newObject;

    }

    public String getName() {
        return name;
    }

    synchronized public void add(ObjI object) {
        maxID = Math.max(maxID, object.getID());
        put(object.getID(), object);

    }

    public SpatCal getSpatialCalibration() {
        return spatCal;
    }

    public int getWidth() {
        return spatCal.getWidth();
    }

    public int getHeight() {
        return spatCal.getHeight();
    }

    public int getNSlices() {
        return spatCal.getNSlices();
    }

    public double getDppXY() {
        return spatCal.getDppXY();
    }

    public double getDppZ() {
        return spatCal.getDppZ();
    }

    public String getSpatialUnits() {
        return spatCal.getUnits();
    }

    public void setSpatialCalibration(SpatCal spatCal, boolean updateAllObjects) {
        this.spatCal = spatCal;
        if (updateAllObjects) {
            for (ObjI obj : values()) {
                obj.setSpatialCalibration(spatCal);
            }
        }
    }

    public synchronized int getAndIncrementID() {
        maxID++;
        return maxID;
    }

    public ObjI getFirst() {
        if (size() == 0)
            return null;

        return values().iterator().next();

    }

    public int[][] getSpatialExtents() {
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

    public int[][] getSpatialLimits() {
        // Taking limits from the first object, otherwise returning null
        if (size() == 0)
            return null;

        return new int[][] { { 0, getWidth() - 1 }, { 0, getHeight() - 1 },
                { 0, getNSlices() - 1 } };

    }

    public int[] getTemporalLimits() {
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

    public int getLargestID() {
        int largestID = 0;
        for (ObjI obj : values())
            if (obj.getID() > largestID)
                largestID = obj.getID();

        return largestID;

    }

    public ObjI getAsSingleObject() {
        ObjsI newCollection = new DefaultObjs("Single", this);

        CoordinateSetFactoryI factory = new PointListFactory();
        ObjI firstObj = getFirst();
        if (firstObj != null)
            factory = firstObj.getCoordinateSetFactory();

        ObjI newObj = newCollection.createAndAddNewObject(factory);
        for (ObjI obj : values())
            for (Point<Integer> point : obj.getCoordinateSet())
                try {
                    newObj.addPoint(point.duplicate());
                } catch (PointOutOfRangeException e) {
                    // This shouldn't occur, as the points are from a collection with the same
                    // dimensions
                    e.printStackTrace();
                }

        return newObj;

    }

    public ImageI convertToImage(String outputName, HashMap<Integer, Float> hues, int bitDepth, boolean nanBackground) {
        return convertToImage(outputName, hues, bitDepth, nanBackground, false);
    }

    public ImageI convertToImage(String outputName, HashMap<Integer, Float> hues, int bitDepth, boolean nanBackground,
            boolean verbose) {
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
        spatCal.applyImageCalibration(image.getImagePlus());

        return image;

    }

    public ImageI convertToImageRandomColours() {
        HashMap<Integer, Float> hues = ColourFactory.getRandomHues(this);
        ImageI dispImage = convertToImage(name, hues, 8, false);

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

    public ImageI convertToImageBinary() {
            return convertToImageBinary(name);
    }

    public ImageI convertToImageBinary(String name) {
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(this, ColourFactory.SingleColours.WHITE);
        ImageI dispImage = convertToImage(name, hues, 8, false);

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

    public ImageI convertToImageIDColours() {
        HashMap<Integer, Float> hues = ColourFactory.getIDHues(this, false);
        ImageI dispImage = convertToImage(name, hues, 32, false);

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

    public ImageI convertCentroidsToImage(String outputName, HashMap<Integer, Float> hues, int bitDepth,
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
        spatCal.applyImageCalibration(image.getImagePlus());

        return image;

    }

    public void applyCalibration(ImageI image) {
        applyCalibrationFromImagePlus(image.getImagePlus());
    }

    public void applyCalibrationFromImagePlus(ImagePlus ipl) {
        ObjI obj = getFirst();
        if (obj == null)
            return;

        Calibration calibration = ipl.getCalibration();
        calibration.pixelWidth = obj.getDppXY();
        calibration.pixelHeight = obj.getDppXY();
        calibration.pixelDepth = obj.getDppZ();
        calibration.setUnit(obj.getUnits());

        calibration.frameInterval = frameInterval;
        calibration.fps = 1 / TemporalUnit.getOMEUnit().convertValue(frameInterval, UNITS.SECOND);

    }

    public ImageI createImage(String outputName, int bitDepth) {
        // Creating a new image
        ImagePlus ipl = IJ.createHyperStack(outputName, spatCal.getWidth(), spatCal.getHeight(), 1,
                spatCal.getNSlices(), nFrames, bitDepth);

        return ImageFactory.createImage(outputName, ipl);

    }

    public void setNaNBackground(ImagePlus ipl) {
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
    public ObjI getByEqualsIgnoreNameAndID(ObjI referenceObj) {
        for (ObjI testObj : values())
            if (testObj.equalsIgnoreNameAndID(referenceObj))
                return testObj;

        return null;

    }

    public void resetCollection() {
        clear();
        maxID = 0;
    }

    public void recalculateMaxID() {
        maxID = getLargestID();
    }

    /**
     * Displays measurement values from a specific Module
     * 
     * @param module  The module for which measurements will be displayed
     * @param modules The collection of modules in which this module resides
     */
    public void showMeasurements(Module module, Modules modules) {
        // Getting MeasurementReferences
        ObjMeasurementRefs measRefs = module.updateAndGetObjectMeasurementRefs();
        if (measRefs == null)
            return;

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        LinkedHashSet<String> measNames = new LinkedHashSet<>();
        for (ObjMeasurementRef measRef : measRefs.values()) {
            if (measRef.getObjectsName().equals(name))
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
                Measurement measurement = obj.getMeasurement(measName);
                double value = measurement == null ? Double.NaN : measurement.getValue();

                // Setting value
                rt.setValue(measName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("\"" + module.getName() + " \"measurements for \"" + name + "\"");

    }

    public void showAllMeasurements() {
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
                Measurement measurement = obj.getMeasurement(measName);
                double value = measurement == null ? Double.NaN : measurement.getValue();

                // Setting value
                rt.setValue(measName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("All measurements for \"" + name + "\"");

    }

    /**
     * Displays metadata values from a specific Module
     * 
     * @param module  The module for which metadata will be displayed
     * @param modules The collection of modules in which this module resides
     */
    public void showMetadata(Module module, Modules modules) {
        // Getting metadata references
        ObjMetadataRefs metadataRefs = module.updateAndGetObjectMetadataRefs();
        if (metadataRefs == null)
            return;

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all metadata relating to this object collection
        LinkedHashSet<String> metadataNames = new LinkedHashSet<>();
        for (ObjMetadataRef metadataRef : metadataRefs.values()) {
            if (metadataRef.getObjectsName().equals(name))
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
                ObjMetadata metadataItem = obj.getMetadataItem(measName);
                String value = metadataItem == null ? "" : metadataItem.getValue();

                // Setting value
                rt.setValue(measName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("\"" + module.getName() + " \"metadata for \"" + name + "\"");

    }

    public void showAllMetadata() {
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
                ObjMetadata metadataItem = obj.getMetadataItem(metadataName);
                String value = metadataItem == null ? "" : metadataItem.getValue();

                // Setting value
                rt.setValue(metadataName, row, value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("All measurements for \"" + name + "\"");

    }

    public void removeParents(String parentObjectsName) {
        for (ObjI obj : values())
            obj.removeParent(parentObjectsName);

    }

    public void removeChildren(String childObjectsName) {
        for (ObjI obj : values())
            obj.removeChildren(childObjectsName);
    }

    public void removePartners(String partnerObjectsName) {
        for (ObjI obj : values())
            obj.removePartners(partnerObjectsName);
    }

    public boolean containsPoint(Point<Integer> point) {
        for (ObjI obj : values()) {
            if (obj.contains(point))
                return true;
        }

        return false;

    }

    public boolean containsCoord(int x, int y, int z) {
        Point<Integer> point = new Point<>(x, y, z);

        return containsPoint(point);

    }

    public ObjI getLargestObject(int t) {
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

    public ObjI getSmallestObject(int t) {
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

    public ObjsI getObjectsInFrame(String outputObjectsName, int frame) {
        ObjsI outputObjects = new DefaultObjs(outputObjectsName, this);

        // Setting output objects collection to have one frame
        outputObjects.setNFrames(1);

        // Iterating over objects, getting those in this frame
        for (ObjI obj : values()) {
            if (obj.getT() == frame) {
                ObjI outputObject = ObjFactories.getDefaultFactory().createObj(outputObjects, obj.getCoordinateSetFactory(), obj.getID());
                outputObject.setCoordinateSet(obj.getCoordinateSet().duplicate());
                outputObject.setT(0);
                outputObjects.add(outputObject);
            }
        }

        return outputObjects;

    }

    public int getNFrames() {
        return nFrames;
    }

    public double getFrameInterval() {
        return frameInterval;
    }

    public Unit<Time> getTemporalUnit() {
        return temporalUnit;
    }

    public void setNFrames(int nFrames) {
        this.nFrames = nFrames;
    }

    public ObjsI duplicate(String newObjectsName, boolean duplicateRelationships, boolean duplicateMeasurement,
            boolean duplicateMetadata, boolean addOriginalDuplicateRelationship) {
        ObjsI newObjs = new DefaultObjs(newObjectsName, this);

        for (ObjI obj : values()) {
            ObjI newObj = obj.duplicate(newObjs, duplicateRelationships, duplicateMeasurement, duplicateMetadata);

            newObjs.add(newObj);

            if (addOriginalDuplicateRelationship) {
                newObj.addParent(obj);
                obj.addChild(newObj);
            }
        }

        newObjs.recalculateMaxID();

        return newObjs;

    }
}

// clear all
// print'HelloWorld'
// if{
// 'theSkyIsGreen'
// getMeasurements
// }
