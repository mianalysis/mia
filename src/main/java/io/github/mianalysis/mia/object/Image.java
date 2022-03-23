package io.github.mianalysis.mia.object;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.drew.lang.annotations.Nullable;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.LUT;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.common.mathfunc.CumStat;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;
import io.github.sjcross.common.process.IntensityMinMax;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by stephen on 30/04/2017.
 */
public class Image {
    private String name;
    private ImagePlus imagePlus;
    private LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();

    // CONSTRUCTORS

    public Image(String name, ImagePlus imagePlus) {
        this.name = name;
        this.imagePlus = imagePlus;

    }

    public <T extends RealType<T> & NativeType<T>> Image(String name, ImgPlus<T> img) {
        this.name = name;
        this.imagePlus = ImageJFunctions.wrap(img, name);

        // Calibrations don't always appear to transfer over, so doing this explicitly
        Calibration calibration = imagePlus.getCalibration();
        if (img.dimensionIndex(Axes.Z) != -1) {
            calibration.pixelDepth = ((CalibratedAxis) img.axis(img.dimensionIndex(Axes.Z))).calibratedValue(1);
        }
    }

    public Objs convertImageToObjects(String outputObjectsName) {
        String type = getVolumeType(VolumeType.POINTLIST);
        return convertImageToObjects(type, outputObjectsName);
    }

    public Objs convertImageToObjects(VolumeType volumeType, String outputObjectsName) {
        String type = getVolumeType(volumeType);
        return convertImageToObjects(type, outputObjectsName);
    }

    public Objs convertImageToObjects(VolumeType volumeType, String outputObjectsName, boolean singleObject) {
        String type = getVolumeType(volumeType);
        return convertImageToObjects(type, outputObjectsName, singleObject);
    }

    public Objs convertImageToObjects(String type, String outputObjectsName) {
        return convertImageToObjects(type, outputObjectsName, false);

    }

    public Objs convertImageToObjects(String type, String outputObjectsName, boolean singleObject) {
        // Getting spatial calibration
        double dppXY = imagePlus.getCalibration().pixelWidth;
        double dppZ = imagePlus.getCalibration().pixelDepth;
        String units = imagePlus.getCalibration().getUnits();
        ImageProcessor ipr = imagePlus.getProcessor();

        int h = imagePlus.getHeight();
        int w = imagePlus.getWidth();
        int nSlices = imagePlus.getNSlices();
        int nFrames = imagePlus.getNFrames();
        int nChannels = imagePlus.getNChannels();

        double frameInterval = imagePlus.getCalibration().frameInterval;

        // Need to get coordinates and convert to a HCObject
        SpatCal calibration = new SpatCal(dppXY, dppZ, units, w, h, nSlices);
        Objs outputObjects = new Objs(outputObjectsName, calibration, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());

        // Will return null if optimised
        VolumeType volumeType = getVolumeType(type);

        for (int c = 0; c < nChannels; c++) {
            for (int t = 0; t < nFrames; t++) {
                // If using optimised type, determine types for each object, otherwise use a
                // blank map
                HashMap<Integer, IDLink> links = volumeType == null
                        ? getOptimisedLinks(c, t, outputObjects, singleObject)
                        : new HashMap<>();

                for (int z = 0; z < nSlices; z++) {
                    imagePlus.setPosition(c + 1, z + 1, t + 1);
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            // Getting the ID of this object in the current stack.
                            int imageID = (int) ipr.getPixelValue(x, y);

                            // If assigning a single object ID, this is the same value for all objects
                            if (singleObject && imageID != 0)
                                imageID = 1;

                            if (imageID != 0) {
                                // If not using optimised type, each link needs to be added here
                                if (!links.containsKey(imageID))
                                    links.put(imageID, new IDLink(outputObjects.getAndIncrementID(), volumeType));

                                IDLink link = links.get(imageID);
                                int outID = link.getID();
                                VolumeType outType = link.getVolumeType();
                                int finalT = t;

                                outputObjects.computeIfAbsent(outID,
                                        k -> new Obj(outputObjects, outType, outID).setT(finalT));
                                try {
                                    outputObjects.get(outID).add(x, y, z);
                                } catch (PointOutOfRangeException e) {
                                }
                            }
                        }
                    }

                    // Finalising the object store for this slice (this only does something for
                    // QuadTrees)
                    for (Obj obj : outputObjects.values())
                        obj.finalise(z);

                }
            }
        }

        for (Obj obj : outputObjects.values())
            obj.finalise();

        return outputObjects;

    }

    HashMap<Integer, IDLink> getOptimisedLinks(int c, int t, Objs outputObjects, boolean singleObject) {
        int h = imagePlus.getHeight();
        int w = imagePlus.getWidth();
        int nSlices = imagePlus.getNSlices();

        // Looping over all pixels in this stack and adding to the relevant CumStat
        HashMap<Integer, IDLink> links = new HashMap<>();
        for (int z = 0; z < nSlices; z++) {
            imagePlus.setPosition(c, z + 1, t);
            ImageProcessor ipr = imagePlus.getProcessor();
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    // Getting the ID of this object in the current stack.
                    int imageID = (int) ipr.getPixelValue(x, y);

                    // If assigning a single object ID, this is the same value for all objects
                    if (singleObject && imageID != 0)
                        imageID = 1;

                    if (imageID == 0)
                        continue;
                    links.putIfAbsent(imageID, new IDLink(outputObjects.getAndIncrementID(), null));
                    links.get(imageID).addMeasurement(x, y, z);

                }
            }
        }

        return links;

    }

    // PUBLIC METHODS

    public void addMeasurement(Measurement measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        return measurements.get(name);

    }

    public void showImage(String title, @Nullable LUT lut, boolean normalise, boolean composite) {
        ImagePlus dispIpl = new Duplicator().run(imagePlus);
        dispIpl.setTitle(title);
        if (normalise) {
            ImageStatistics stats = dispIpl.getStatistics();
            if (stats.min != stats.max)
                IntensityMinMax.run(dispIpl, true);
        }

        dispIpl.setPosition(1, 1, 1);
        dispIpl.updateChannelAndDraw();
        if (lut != null && dispIpl.getBitDepth() != 24)
            dispIpl.setLut(lut);
        if (composite && dispIpl.getNChannels() > 1) {
            dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
        } else {
            dispIpl.setDisplayMode(CompositeImage.COLOR);
        }
        dispIpl.show();
    }

    public void showImage(String title, LUT lut) {
        showImage(title, lut, true, false);
    }

    public void showImage(String title) {
        showImage(title, LUT.createLutFromColor(Color.WHITE));
    }

    public void showImage(LUT lut) {
        showImage(name, lut);
    }

    public void showImage() {
        showImage(name, null);
    }

    /**
     * Displays measurement values from a specific Module
     *
     * @param module
     */
    public void showMeasurements(Module module) {
        // Getting MeasurementReferences
        ImageMeasurementRefs measRefs = module.updateAndGetImageMeasurementRefs();

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        LinkedHashSet<String> measNames = new LinkedHashSet<>();
        for (ImageMeasurementRef measRef : measRefs.values()) {
            if (measRef.getImageName().equals(name))
                measNames.add(measRef.getName());
        }

        // Setting the measurements from the Module
        for (String measName : measNames) {
            Measurement measurement = getMeasurement(measName);
            double value = measurement == null ? Double.NaN : measurement.getValue();

            // Setting value
            rt.setValue(measName, 0, value);

        }

        // Displaying the results table
        rt.show("\"" + module.getName() + " \"measurements for \"" + name + "\"");

    }

    public void showAllMeasurements() {
        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        Set<String> measNames = getMeasurements().keySet();

        // Setting the measurements from the Module
        int row = 0;
        for (String measName : measNames) {
            Measurement measurement = getMeasurement(measName);
            double value = measurement == null ? Double.NaN : measurement.getValue();

            // Setting value
            rt.setValue(measName, row, value);

        }

        // Displaying the results table
        rt.show("All measurements for \"" + name + "\"");

    }

    // PACKAGE PRIVATE METHODS

    VolumeType getVolumeType(String volumeType) {
        switch (volumeType) {
            case VolumeTypesInterface.OCTREE:
                return VolumeType.OCTREE;
            // case VolumeTypes.OPTIMISED:
            // return null;
            case VolumeTypesInterface.POINTLIST:
            default:
                return VolumeType.POINTLIST;
            case VolumeTypesInterface.QUADTREE:
                return VolumeType.QUADTREE;
        }
    }

    String getVolumeType(VolumeType volumeType) {
        switch (volumeType) {
            case OCTREE:
                return VolumeTypesInterface.OCTREE;
            case POINTLIST:
            default:
                return VolumeTypesInterface.POINTLIST;
            case QUADTREE:
                return VolumeTypesInterface.QUADTREE;
        }
    }

    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public ImagePlus getImagePlus() {
        return imagePlus;
    }

    public void setImagePlus(ImagePlus imagePlus) {
        this.imagePlus = imagePlus;
    }

    public <T extends RealType<T> & NativeType<T>> ImgPlus<T> getImgPlus() {
        return ImagePlusAdapter.wrapImgPlus(new Duplicator().run(imagePlus));
    }

    public <T extends RealType<T> & NativeType<T>> void setImgPlus(ImgPlus<T> img) {
        imagePlus = ImageJFunctions.wrap(img, name);
    }

    public HashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, Measurement> singleMeasurements) {
        this.measurements = singleMeasurements;
    }

    @Override
    public int hashCode() {
        int hash = 1;

        Calibration calibration = imagePlus.getCalibration();

        hash = 31 * hash + ((Number) calibration.pixelWidth).hashCode();
        hash = 31 * hash + ((Number) calibration.pixelDepth).hashCode();
        hash = 31 * hash + calibration.getUnits().toUpperCase().hashCode();

        hash = 31 * hash + imagePlus.getWidth();
        hash = 31 * hash + imagePlus.getHeight();
        hash = 31 * hash + imagePlus.getNChannels();
        hash = 31 * hash + imagePlus.getNSlices();
        hash = 31 * hash + imagePlus.getNFrames();
        hash = 31 * hash + imagePlus.getBitDepth();

        ImageStack ist = imagePlus.getImageStack();
        for (int z = 1; z <= imagePlus.getNSlices(); z++) {
            for (int c = 1; c <= imagePlus.getNChannels(); c++) {
                for (int t = 1; t <= imagePlus.getNFrames(); t++) {
                    int idx = imagePlus.getStackIndex(c, z, t);
                    ImageProcessor ipr = ist.getProcessor(idx);

                    for (int x = 0; x < ipr.getWidth(); x++) {
                        for (int y = 0; y < ipr.getHeight(); y++) {
                            hash = 31 * hash + ipr.getPixel(x, y);
                        }
                    }
                }
            }
        }

        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Image))
            return false;

        Image image2 = (Image) obj;
        ImagePlus imagePlus2 = image2.getImagePlus();

        // Comparing calibrations
        Calibration calibration1 = imagePlus.getCalibration();
        Calibration calibration2 = imagePlus2.getCalibration();

        if (calibration1.pixelWidth != calibration2.pixelWidth)
            return false;
        if (calibration1.pixelDepth != calibration2.pixelDepth)
            return false;
        if (!calibration1.getUnits().equals(calibration2.getUnits()))
            return false;

        // Comparing dimensions
        if (imagePlus.getWidth() != imagePlus2.getWidth())
            return false;
        if (imagePlus.getHeight() != imagePlus2.getHeight())
            return false;
        if (imagePlus.getNChannels() != imagePlus2.getNChannels())
            return false;
        if (imagePlus.getNSlices() != imagePlus2.getNSlices())
            return false;
        if (imagePlus.getNFrames() != imagePlus2.getNFrames())
            return false;
        if (imagePlus.getBitDepth() != imagePlus2.getBitDepth())
            return false;

        // Checking the individual image pixel values
        ImageStack ist = imagePlus.getImageStack();
        ImageStack ist2 = imagePlus2.getImageStack();
        for (int c = 1; c <= imagePlus.getNChannels(); c++) {
            for (int z = 1; z <= imagePlus.getNSlices(); z++) {
                for (int t = 1; t <= imagePlus.getNFrames(); t++) {
                    int idx = imagePlus.getStackIndex(c, z, t);
                    ImageProcessor imageProcessor1 = ist.getProcessor(idx);
                    int idx2 = imagePlus2.getStackIndex(c, z, t);
                    ImageProcessor imageProcessor2 = ist2.getProcessor(idx2);

                    for (int x = 0; x < imagePlus.getWidth(); x++) {
                        for (int y = 0; y < imagePlus.getHeight(); y++) {
                            if (imageProcessor1.getf(x, y) != imageProcessor2.getf(x, y))
                                return false;
                        }
                    }
                }
            }
        }

        return true;

    }

    class IDLink {
        private final int ID;
        private final CumStat csX;
        private final CumStat csY;
        private final CumStat csZ;
        private VolumeType volumeType = null;

        IDLink(int ID, @Nullable VolumeType volumeType) {
            this.ID = ID;
            this.volumeType = volumeType;

            this.csX = new CumStat();
            this.csY = new CumStat();
            this.csZ = new CumStat();

        }

        void addMeasurement(int x, int y, int z) {
            csX.addMeasure(x);
            csY.addMeasure(y);
            csZ.addMeasure(z);
        }

        VolumeType getVolumeType() {
            if (volumeType == null)
                volumeType = calculateVolumeType();
            return volumeType;
        }

        private VolumeType calculateVolumeType() {
            double N = csX.getN();

            // If this point is less than 50 voxels, use PointList
            if (N < 50)
                return VolumeType.POINTLIST;

            // Ratio of xy to z
            double xyToZ = imagePlus.getCalibration().pixelDepth / imagePlus.getCalibration().pixelWidth;
            MIA.log.writeMessage("        XY to Z " + xyToZ);

            // If distribution of points indicates a sparse object, use PointList. This is
            // calculated differently for 2D/3D
            if (csZ.getMax() - csZ.getMin() == 0) {
                // Assuming a circle of volume equal the number of coordinates, the expected
                // radius is
                double expectedRadius = Math.sqrt(N / Math.PI);
                double expectedStdev = 4.24 * expectedRadius;
                MIA.log.writeMessage("        2D, Exp stdev = " + expectedStdev + ", actual stdev = "
                        + ((csX.getStd() + csY.getStd()) / 2));
                if ((csX.getStd() + csY.getStd()) / 2 < expectedStdev * 2)
                    return VolumeType.QUADTREE;

            } else {
                // Assuming a sphere of volume equal the number of coordinates (corrected for
                // different XY and Z
                // sampling), the expected radius is
                double expectedRadius = Math.cbrt(N * xyToZ * 3d / (4d * Math.PI));
                double expectedStdev = 5.16 * expectedRadius;
                MIA.log.writeMessage("        3D, Exp stdev = " + expectedStdev + ", actual stdev = "
                        + ((csX.getStd() + csY.getStd() + csZ.getStd() * xyToZ) / 3));
                if ((csX.getStd() + csY.getStd() + csZ.getStd() * xyToZ) / 3 < expectedStdev * 2) {
                    if (xyToZ > 3)
                        return VolumeType.QUADTREE;
                    else
                        return VolumeType.OCTREE;
                }
            }

            // If distribution of points indicates an elongated object, use PointList
            if (csZ.getMax() - csZ.getMin() == 0) {
                CumStat cs = new CumStat();
                cs.addMeasure(csX.getStd());
                cs.addMeasure(csY.getStd());
                MIA.log.writeMessage("        2D, Ratio " + (cs.getStd() / cs.getMean()));
                if (cs.getStd() / cs.getMean() < 2)
                    return VolumeType.QUADTREE;
            } else {
                CumStat cs = new CumStat();
                cs.addMeasure(csX.getStd());
                cs.addMeasure(csY.getStd());
                cs.addMeasure(csZ.getStd());
                MIA.log.writeMessage("        3D, Ratio " + (cs.getStd() / cs.getMean()));
                if (cs.getStd() / cs.getMean() < 2) {
                    if (xyToZ > 3)
                        return VolumeType.QUADTREE;
                    else
                        return VolumeType.OCTREE;
                }
            }

            return VolumeType.POINTLIST;

        }

        int getID() {
            return ID;
        }
    }
}