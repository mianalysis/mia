package wbif.sjx.ModularImageAnalysis.Object;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import net.imglib2.Cursor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeSet;

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

    public Image(String name, Img<T> img) {
        this.name = name;
        this.imagePlus = ImageJFunctions.wrap(img,name);

    }

    public ObjCollection convertImageToObjects(String outputObjectsName) {
        return convertImageToObjects(outputObjectsName,false);

    }

    public ObjCollection convertImageToObjects(String outputObjectsName, boolean singleObject) {
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
                                IDlink.computeIfAbsent(imageID, k -> outputObjects.getNextID());
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

    public Img<T> getImg() {
        return ImagePlusAdapter.wrap(new Duplicator().run(imagePlus));

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