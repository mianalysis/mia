package io.github.mianalysis.mia.object.image;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.VolumeTypesInterface;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.common.mathfunc.CumStat;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by stephen on 30/04/2017.
 */
public abstract class Image <T extends RealType<T> & NativeType<T>> {
    protected String name;
    protected LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();

    // Abstract methods

    public abstract void showImage(String title, @Nullable LUT lut, boolean normalise, boolean composite);

    public abstract ImagePlus getImagePlus();

    public abstract void setImagePlus(ImagePlus imagePlus);

    public abstract ImgPlus<T> getImgPlus();

    public abstract void setImgPlus(ImgPlus<T> img);

    public abstract Objs convertImageToObjects(String type, String outputObjectsName, boolean singleObject);

    public abstract void addObject(Obj obj, float hue);

    public abstract void addObjectCentroid(Obj obj, float hue);


    // PUBLIC METHODS

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

    public void addMeasurement(Measurement measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        return measurements.get(name);

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

    public HashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, Measurement> singleMeasurements) {
        this.measurements = singleMeasurements;
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
            ImagePlus imagePlus = getImagePlus();
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