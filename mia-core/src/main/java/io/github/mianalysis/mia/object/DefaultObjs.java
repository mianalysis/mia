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
import io.github.mianalysis.mia.object.coordinates.ObjFactories;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.SpatioTemporallyCalibrated;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
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
public class DefaultObjs extends LinkedHashMap<Integer, ObjI> implements ObjsI {
    private String name;
    private int maxID = 0;

    protected int width;
    protected int height;
    protected int nSlices;
    protected double dppXY;
    protected double dppZ;
    protected String spatialUnits;
    protected int nFrames;
    protected double frameInterval;
    protected Unit<Time> temporalUnit;

    public DefaultObjs(String name, int width, int height, int nSlices, double dppXY, double dppZ,
            String spatialUnits, int nFrames, double frameInterval, Unit<Time> temporalUnit) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.nSlices = nSlices;
        this.dppXY = dppXY;
        this.dppZ = dppZ;
        this.spatialUnits = spatialUnits;
        this.nFrames = nFrames;
        this.frameInterval = frameInterval;
        this.temporalUnit = temporalUnit;

    }

    public DefaultObjs(String name, SpatioTemporallyCalibrated example) {
        this.name = name;
        this.width = example.getWidth();
        this.height = example.getHeight();
        this.nSlices = example.getNSlices();
        this.dppXY = example.getDppXY();
        this.dppZ = example.getDppZ();
        this.spatialUnits = example.getSpatialUnits();
        this.nFrames = example.getNFrames();
        this.frameInterval = example.getFrameInterval();
        this.temporalUnit = example.getTemporalUnit();

    }

    public DefaultObjs(String name, ImageI imageForCalibration) {
        // Calibration calibration = imageForCalibration.getCalibration();

        this.name = name;
        this.width = imageForCalibration.getWidth();
        this.height = imageForCalibration.getHeight();
        this.nSlices = imageForCalibration.getNSlices();
        this.dppXY = imageForCalibration.getDppXY();
        this.dppZ = imageForCalibration.getDppZ();
        this.spatialUnits = imageForCalibration.getSpatialUnits();
        this.nFrames = imageForCalibration.getNFrames();
        this.frameInterval = imageForCalibration.getFrameInterval();
        this.temporalUnit = TemporalUnit.getOMEUnit();

    }

    public ObjI createAndAddNewObject(CoordinateSetFactoryI coordinateSetFactory) {
        ObjI newObject = ObjFactories.getDefaultFactory().createObjWithID(coordinateSetFactory, this,
                getAndIncrementID());
        add(newObject);

        return newObject;

    }

    public ObjI createAndAddNewObjectWithID(CoordinateSetFactoryI coordinateSetFactory, int ID) {
        ObjI newObject = ObjFactories.getDefaultFactory().createObjWithID(coordinateSetFactory, this, ID);
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

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getNSlices() {
        return nSlices;
    }

    @Override
    public void setNSlices(int nSlices) {
        this.nSlices = nSlices;
    }

    @Override
    public double getDppXY() {
        return dppXY;
    }

    @Override
    public void setDppXY(double dppXY) {
        this.dppXY = dppXY;
    }

    @Override
    public double getDppZ() {
        return dppZ;
    }

    @Override
    public void setDppZ(double dppZ) {
        this.dppZ = dppZ;
    }

    @Override
    public String getSpatialUnits() {
        return spatialUnits;
    }

    @Override
    public void setSpatialUnits(String spatialUnits) {
        this.spatialUnits = spatialUnits;
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
        ObjsI newCollection = new DefaultObjsFactory().createFromExample("Single", this);

        CoordinateSetFactoryI coordinateSetFactory = new PointListFactory();
        ObjI firstObj = getFirst();
        if (firstObj != null)
            coordinateSetFactory = firstObj.getCoordinateSetFactory();

        ObjI newObj = newCollection.createAndAddNewObject(coordinateSetFactory);
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
        applySpatioTemporalCalibrationToImage(image.getImagePlus());

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
        applySpatioTemporalCalibrationToImage(image.getImagePlus());

        return image;

    }

    public void applyCalibrationToImage(ImageI image) {
        applyCalibrationToImagePlus(image.getImagePlus());
    }

    public void applyCalibrationToImagePlus(ImagePlus ipl) {
        ObjI obj = getFirst();
        if (obj == null)
            return;

        Calibration calibration = ipl.getCalibration();
        calibration.pixelWidth = obj.getDppXY();
        calibration.pixelHeight = obj.getDppXY();
        calibration.pixelDepth = obj.getDppZ();
        calibration.setUnit(obj.getSpatialUnits());

        calibration.frameInterval = frameInterval;
        calibration.fps = 1 / TemporalUnit.getOMEUnit().convertValue(frameInterval, UNITS.SECOND);

    }

    public ImageI createImage(String outputName, int bitDepth) {
        // Creating a new image
        ImagePlus ipl = IJ.createHyperStack(outputName, getWidth(), getHeight(), 1, getNSlices(), nFrames, bitDepth);

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
                MeasurementI measurement = obj.getMeasurement(measName);
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
                MeasurementI measurement = obj.getMeasurement(measName);
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
        ObjsI outputObjects = new DefaultObjsFactory().createObjs(outputObjectsName, getWidth(), getHeight(),
                getNSlices(), getDppXY(), getDppZ(), getSpatialUnits(), 1, getFrameInterval(), getTemporalUnit());

        // Iterating over objects, getting those in this frame
        for (ObjI obj : values()) {
            if (obj.getT() == frame) {
                ObjI outputObject = ObjFactories.getDefaultFactory().createObjWithID(obj.getCoordinateSetFactory(),
                        outputObjects, obj.getID());
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

    @Override
    public void setNFrames(int nFrames) {
        this.nFrames = nFrames;
    }

    public double getFrameInterval() {
        return frameInterval;
    }

    @Override
    public void setFrameInterval(double frameInterval) {
        this.frameInterval = frameInterval;
    }

    public Unit<Time> getTemporalUnit() {
        return temporalUnit;
    }

    @Override
    public void setTemporalUnit(Unit<Time> temporalUnit) {
        this.temporalUnit = temporalUnit;
    }

    public ObjsI duplicate(String newObjectsName, boolean duplicateRelationships, boolean duplicateMeasurement,
            boolean duplicateMetadata, boolean addOriginalDuplicateRelationship) {
        ObjsI newObjs = new DefaultObjsFactory().createFromExample(newObjectsName, this);

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

    @Override
    public void setCalibrationFromExample(SpatioTemporallyCalibrated example, boolean updateAllObjects) {
        setSpatioTemporalCalibrationFromExample(example);
        
        if (updateAllObjects)
            for (ObjI obj : values())
                obj.setSpatialCalibrationFromExample(example);
            
    }
}

// clear all
// print'HelloWorld'
// if{
// 'theSkyIsGreen'
// getMeasurements
// }
