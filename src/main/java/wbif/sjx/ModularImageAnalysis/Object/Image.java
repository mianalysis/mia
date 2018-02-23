package wbif.sjx.ModularImageAnalysis.Object;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by steph on 30/04/2017.
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
        // Need to get coordinates and convert to a HCObject
        ObjCollection outputObjects = new ObjCollection(outputObjectsName); //Local ArrayList of objects

        // Getting spatial calibration
        double dppXY = imagePlus.getCalibration().getX(1);
        double dppZ = imagePlus.getCalibration().getZ(1);
        String calibratedUnits = imagePlus.getCalibration().getUnits();

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
                            int imageID = (int) ipr.getPixelValue(x, y); //Pixel value

                            if (imageID != 0) {
                                IDlink.computeIfAbsent(imageID, k -> outputObjects.getNextID());
                                int outID = IDlink.get(imageID);

                                outputObjects.computeIfAbsent(outID, k -> new Obj(outputObjectsName, outID,dppXY,dppZ,calibratedUnits));
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
        return ImagePlusAdapter.wrapImgPlus(imagePlus);
    }

    public HashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, Measurement> singleMeasurements) {
        this.measurements = singleMeasurements;
    }

}