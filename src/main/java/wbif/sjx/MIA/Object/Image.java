package wbif.sjx.MIA.Object;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import ij.process.LUT;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.References.Abstract.MeasurementRef;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Process.IntensityMinMax;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by stephen on 30/04/2017.
 */
public class Image < T extends RealType< T > & NativeType< T >> {
    private String name;
    private ImagePlus imagePlus;
    private LinkedHashMap<String,Measurement> measurements = new LinkedHashMap<>();


    // CONSTRUCTORS

    public Image(String name, ImagePlus imagePlus) {
        this.name = name;
        this.imagePlus = imagePlus;

    }

    public Image(String name, ImgPlus<T> img) {
        this.name = name;
        this.imagePlus = ImageJFunctions.wrap(img,name);

        // Calibrations don't always appear to transfer over, so doing this explicitly
        Calibration calibration = imagePlus.getCalibration();
        if (img.dimensionIndex(Axes.Z) != -1) {
            calibration.pixelDepth = ((CalibratedAxis) img.axis(img.dimensionIndex(Axes.Z))).calibratedValue(1);
        }
    }

    public ObjCollection convertImageToObjects(String outputObjectsName) throws IntegerOverflowException {
        return convertImageToObjects(outputObjectsName,false);

    }

    public ObjCollection convertImageToObjects(String outputObjectsName, boolean singleObject) throws IntegerOverflowException {
        // Need to get coordinates and convert to a HCObject
        ObjCollection outputObjects = new ObjCollection(outputObjectsName); //Local ArrayList of objects

        // Getting spatial calibration
        double dppXY = imagePlus.getCalibration().getX(1);
        double dppZ = imagePlus.getCalibration().getZ(1);
        String calibratedUnits = imagePlus.getCalibration().getUnits();
        boolean twoD = getImagePlus().getNSlices()==1;
        ImageProcessor ipr = imagePlus.getProcessor();

        int h = imagePlus.getHeight();
        int w = imagePlus.getWidth();
        int nSlices = imagePlus.getNSlices();
        int nFrames = imagePlus.getNFrames();
        int nChannels = imagePlus.getNChannels();

        for (int c=0;c<nChannels;c++) {
            for (int t = 0; t < nFrames; t++) {
                // HashMap linking the ID numbers in the present frame to those used to store the object (this means
                // each frame instance has different ID numbers)
                HashMap<Integer,Integer> IDlink = new HashMap<>();

                for (int z = 0; z < nSlices; z++) {
                    imagePlus.setPosition(c+1,z+1,t+1);
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            // Getting the ID of this object in the current stack.
                            int imageID = (int) ipr.getPixelValue(x, y);

                            // If assigning a single object ID, this is the same value for all objects
                            if (singleObject && imageID != 0) imageID = 1;

                            if (imageID != 0) {
                                IDlink.computeIfAbsent(imageID, k -> outputObjects.getAndIncrementID());
                                int outID = IDlink.get(imageID);

                                outputObjects.computeIfAbsent(outID, k ->
                                        new Obj(outputObjectsName, outID,dppXY,dppZ,calibratedUnits,twoD));

                                outputObjects.get(outID).addCoord(x,y,z);
                                outputObjects.get(outID).setT(t);

                            }
                        }
                    }
                }
            }
        }

        return outputObjects;

    }


    // PUBLIC METHODS

    public void addMeasurement(Measurement measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        return measurements.get(name);

    }

    public void showImage(String title, @Nullable LUT lut, boolean normalise) {
        ImagePlus dispIpl = new Duplicator().run(imagePlus);
        dispIpl.setTitle(title);
        if (normalise) IntensityMinMax.run(dispIpl,true);
        dispIpl.setPosition(1,1,1);
        dispIpl.updateChannelAndDraw();
        if (lut != null) dispIpl.setLut(lut);
        dispIpl.show();

    }

    public void showImage(String title, LUT lut) {
        showImage(title,lut,true);
    }

    public void showImage(String title) {
        showImage(title,LUT.createLutFromColor(Color.WHITE));
    }

    public void showImage(LUT lut) {
        showImage(name,lut);
    }

    public void showImage() {
        showImage(name,LUT.createLutFromColor(Color.WHITE));
    }

    /**
     * Displays measurement values from a specific Module
     * @param module
     */
    public void showMeasurements(Module module) {
        // Getting MeasurementReferences
        ImageMeasurementRefCollection measRefs = module.updateAndGetImageMeasurementRefs();

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        LinkedHashSet<String> measNames = new LinkedHashSet<>();
        for (MeasurementRef measRef:measRefs.values()) {
            if (measRef.getImageObjName().equals(name) && measRef.isAvailable()) measNames.add(measRef.getName());
        }

        // Iterating over each measurement, adding all the values
        int row = 0;

        // Setting the measurements from the Module
        for (String measName : measNames) {
            Measurement measurement = getMeasurement(measName);
            double value = measurement == null ? Double.NaN : measurement.getValue();

            // Setting value
            rt.setValue(measName,row,value);

        }

        // Displaying the results table
        rt.show("\""+module.getTitle()+" \"measurements for \""+name+"\"");

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
            rt.setValue(measName,row,value);

        }

        // Displaying the results table
        rt.show("All measurements for \""+name+"\"");

    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    @Deprecated
    public ImagePlus getImagePlus() {
        return imagePlus;
    }

    public void setImagePlus(ImagePlus imagePlus) {
        this.imagePlus = imagePlus;
    }

    public ImgPlus<T> getImgPlus() {
        return ImagePlusAdapter.wrapImgPlus(new Duplicator().run(imagePlus));
    }

    public void setImgPlus(ImgPlus<T> img) {
        imagePlus = ImageJFunctions.wrap(img,name);
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

        hash = 31*hash + ((Number) calibration.pixelWidth).hashCode();
        hash = 31*hash + ((Number) calibration.pixelDepth).hashCode();
        hash = 31*hash + calibration.getUnits().toUpperCase().hashCode();

        for (int z = 1; z <= imagePlus.getNSlices(); z++) {
            for (int c = 1; c <= imagePlus.getNChannels(); c++) {
                for (int t = 1; t <= imagePlus.getNFrames(); t++) {
                    imagePlus.setPosition(c,z,t);
                    ImageProcessor imageProcessor = imagePlus.getProcessor();
                    for (int x=0;x<imagePlus.getWidth();x++) {
                        for (int y=0;y<imagePlus.getHeight();y++) {
                            hash = 31*hash + ((Number) imageProcessor.getf(x,y)).hashCode();
                        }
                    }
                }
            }
        }

        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Image)) return false;

        Image image2 = (Image) obj;
        ImagePlus imagePlus2 = image2.getImagePlus();

        // Comparing calibrations
        Calibration calibration1 = imagePlus.getCalibration();
        Calibration calibration2 = imagePlus2.getCalibration();

        if (calibration1.pixelWidth != calibration2.pixelWidth) return false;
        if (calibration1.pixelDepth != calibration2.pixelDepth) return false;
        if (!calibration1.getUnits().equals(calibration2.getUnits())) return false;

        // Comparing dimensions
        if (imagePlus.getWidth() != imagePlus2.getWidth()) return false;
        if (imagePlus.getHeight() != imagePlus2.getHeight()) return false;
        if (imagePlus.getNChannels() != imagePlus2.getNChannels()) return false;
        if (imagePlus.getNSlices() != imagePlus2.getNSlices()) return false;
        if (imagePlus.getNFrames() != imagePlus2.getNFrames()) return false;
        if (imagePlus.getBitDepth() != imagePlus2.getBitDepth()) return false;

        // Checking the individual image pixel values
        for (int c=0;c<imagePlus.getNChannels();c++) {
            for (int z = 0; z < imagePlus.getNSlices(); z++) {
                for (int t = 0; t < imagePlus.getNFrames(); t++) {
                    imagePlus.setPosition(c+1, z + 1, t + 1);
                    imagePlus2.setPosition(c+1, z + 1, t + 1);

                    ImageProcessor imageProcessor1 = imagePlus.getProcessor();
                    ImageProcessor imageProcessor2 = imagePlus2.getProcessor();
                    for (int x=0;x<imagePlus.getWidth();x++) {
                        for (int y = 0; y < imagePlus.getHeight(); y++) {
                            if (imageProcessor1.getf(x,y) != imageProcessor2.getf(x,y)) return false;
                        }
                    }
                }
            }
        }

        return true;

    }
}