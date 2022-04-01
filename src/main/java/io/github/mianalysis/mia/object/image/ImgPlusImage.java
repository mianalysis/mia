package io.github.mianalysis.mia.object.image;

import java.nio.file.Paths;
import java.util.HashMap;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ImgPlusImage <T extends RealType<T> & NativeType<T>> extends Image<T> {
    private ImgPlus<T> img;

    public ImgPlusImage(String name, ImagePlus imagePlus) {
        this.name = name;
        this.img = ImagePlusAdapter.wrapImgPlus(imagePlus);
        
    }

    public ImgPlusImage(String name, ImgPlus<T> img) {
        this.name = name;
        this.img = img;
                
    }
    
    public Objs convertImageToObjects(String type, String outputObjectsName, boolean singleObject) {
        ImagePlus imagePlus = getImagePlus();

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
        ImagePlus imagePlus = getImagePlus();
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

    /*
    *   The following method is from John Bogovic via the image.sc forum (https://forum.image.sc/t/imglib2-force-wrapped-imageplus-rai-dimensions-to-xyczt/56461/2), accessed 2022-03-30
    */
    public RandomAccessibleInterval<T> forceImgPlusToXYCZT(ImgPlus<T> imgIn) {
        RandomAccessibleInterval<T> raiOut = imgIn;

        if (imgIn.dimensionIndex(Axes.CHANNEL) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.permute(Views.addDimension(raiOut, 0, 0), 2, nd);
        }
    
        if (imgIn.dimensionIndex(Axes.Z) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.permute(Views.addDimension(raiOut, 0, 0), 3, nd);
        }
    
        if (imgIn.dimensionIndex(Axes.TIME) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.permute(Views.addDimension(raiOut, 0, 0), 4, nd);
        }

        return raiOut;

    }

    public static <T extends RealType<T> & NativeType<T>> void setCalibration(ImagePlus targetIpl, ImgPlus<T> sourceImg) {
        Calibration calibration = targetIpl.getCalibration();

        int xIdx = sourceImg.dimensionIndex(Axes.X);
        int zIdx = sourceImg.dimensionIndex(Axes.Z);
        int tIdx = sourceImg.dimensionIndex(Axes.TIME);

        CalibratedAxis xAxis = sourceImg.axis(xIdx);
        calibration.pixelWidth = xAxis.calibratedValue(1);
        calibration.pixelHeight = xAxis.calibratedValue(1);
        calibration.setUnit(xAxis.unit());

        if (zIdx != -1)
            calibration.pixelDepth = sourceImg.axis(zIdx).calibratedValue(1);

        if (tIdx != -1) {
            CalibratedAxis tAxis = sourceImg.axis(tIdx);
            calibration.frameInterval = tAxis.calibratedValue(1);
            calibration.setTimeUnit(tAxis.unit());
        }        
    }

    public void showImage(String title, @Nullable LUT lut, boolean normalise, boolean composite) {
        RandomAccessibleInterval<T> rai = forceImgPlusToXYCZT(img);
        ImagePlus ipl = ImageJFunctions.show(rai);
        setCalibration(ipl,img);

    }


    // GETTERS AND SETTERS

    public ImagePlus getImagePlus() {
        RandomAccessibleInterval<T> rai = forceImgPlusToXYCZT(img);

        ImagePlus ipl = ImageJFunctions.wrap(rai, name);
        setCalibration(ipl, img);
        
        return ipl;

    }

    public void setImagePlus(ImagePlus imagePlus) {
        this.img = ImagePlusAdapter.wrapImgPlus(imagePlus);        
    }

    public ImgPlus getImgPlus() {
        return img;
    }

    public void setImgPlus(ImgPlus img) {
        this.img = img;
    }

    @Override
    public int hashCode() {
        int hash = 1;

        ImagePlus imagePlus = getImagePlus();
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

        Image image = (Image) obj;
        ImagePlus imagePlus2 = image.getImagePlus();

        // Comparing calibrations
        ImagePlus imagePlus = getImagePlus();
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

    @Override
    public String toString() {
        return "ImgPlusImage (" + name + ")";
    }
    
    public static DiskCachedCellImgOptions getCellImgOptions() {
        int[] cellSize = new int[] { 128, 128, 128 };
        
        DiskCachedCellImgOptions options = DiskCachedCellImgOptions.options();
        if (MIA.preferences.isSpecifyCacheDirectory())
            options.cacheDirectory(Paths.get(MIA.preferences.getCacheDirectory()));
        options.numIoThreads(2);
        options.cellDimensions(cellSize);

        return options;

    }
}