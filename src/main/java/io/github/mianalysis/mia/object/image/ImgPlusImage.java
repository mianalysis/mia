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
import io.github.sjcross.common.object.Point;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class ImgPlusImage<T extends RealType<T> & NativeType<T>> extends Image<T> {
    private ImgPlus<T> img;

    // CONSTRUCTORS

    public ImgPlusImage(String name, ImagePlus imagePlus) {
        this.name = name;
        this.img = ImagePlusAdapter.wrapImgPlus(imagePlus);

    }

    public ImgPlusImage(String name, ImgPlus<T> img) {
        this.name = name;
        this.img = img;

    }

    // PUBLIC METHODS

    @Override
    public Objs convertImageToObjects(String type, String outputObjectsName, boolean singleObject) {
        // ImagePlus imagePlus = getImagePlus();

        int xIdx = img.dimensionIndex(Axes.X);
        int yIdx = img.dimensionIndex(Axes.Y);
        int cIdx = img.dimensionIndex(Axes.CHANNEL);
        int zIdx = img.dimensionIndex(Axes.Z);
        int tIdx = img.dimensionIndex(Axes.TIME);

        double dppXY = xIdx == -1 ? 1 : img.axis(xIdx).calibratedValue(1);
        double dppZ = zIdx == -1 ? 1 : img.axis(zIdx).calibratedValue(1);
        String units = xIdx == -1 ? "px" : img.axis(xIdx).unit();

        int w = (int) (xIdx == -1 ? 1 : img.dimension(xIdx));
        int h = (int) (yIdx == -1 ? 1 : img.dimension(yIdx));
        int nChannels = (int) (cIdx == -1 ? 1 : img.dimension(cIdx));
        int nSlices = (int) (zIdx == -1 ? 1 : img.dimension(zIdx));
        int nFrames = (int) (tIdx == -1 ? 1 : img.dimension(tIdx));

        double frameInterval = tIdx == -1 ? 1 : img.axis(tIdx).calibratedValue(1);

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
                    long[][] interval = ImgPlusTools2.getSliceInterval(img, c, z, t);

                    Cursor<IntegerType> cursor = (Cursor<IntegerType>) Views.interval(img, interval[0], interval[1])
                            .localizingCursor();
                    while (cursor.hasNext()) {
                        cursor.fwd();

                        int imageID = cursor.get().getInteger();
                        int x = cursor.getIntPosition(xIdx);
                        int y = cursor.getIntPosition(yIdx);

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
        int xIdx = img.dimensionIndex(Axes.X);
        int yIdx = img.dimensionIndex(Axes.Y);
        int zIdx = img.dimensionIndex(Axes.Z);

        int nSlices = (int) (zIdx == -1 ? 1 : img.dimension(zIdx));

        // Looping over all pixels in this stack and adding to the relevant CumStat
        HashMap<Integer, IDLink> links = new HashMap<>();
        for (int z = 0; z < nSlices; z++) {
            long[][] interval = ImgPlusTools2.getSliceInterval(img, 1, z, 1);

            Cursor<IntegerType> cursor = (Cursor<IntegerType>) Views.interval(img, interval[0], interval[1])
                    .localizingCursor();
            while (cursor.hasNext()) {
                cursor.fwd();

                int imageID = cursor.get().getInteger();
                int x = cursor.getIntPosition(xIdx);
                int y = cursor.getIntPosition(yIdx);

                // If assigning a single object ID, this is the same value for all objects
                if (singleObject && imageID != 0)
                    imageID = 1;

                if (imageID == 0)
                    continue;
                links.putIfAbsent(imageID, new IDLink(outputObjects.getAndIncrementID(), null));
                links.get(imageID).addMeasurement(x, y, z);

            }
        }

        return links;

    }

    @Override
    public void addObject(Obj obj, float hue) {
        T type = img.firstElement();

        int xIdx = img.dimensionIndex(Axes.X);
        int yIdx = img.dimensionIndex(Axes.Y);
        int zIdx = img.dimensionIndex(Axes.Z);
        int tIdx = img.dimensionIndex(Axes.TIME);
        
        int tPos = obj.getT();
        RandomAccess<T> ra = img.randomAccess();
        for (Point<Integer> point : obj.getCoordinateSet()) {
            int xPos = point.x;
            int yPos = point.y;
            int zPos = point.z;

            if (xIdx != -1)
                ra.setPosition(xPos, xIdx);
            if (yIdx != -1)
                ra.setPosition(yPos, yIdx);
            if (zIdx != -1)
                ra.setPosition(zPos, zIdx);
            if (tIdx != -1)
                ra.setPosition(tPos, tIdx);

            if (type instanceof UnsignedByteType) {
                ((UnsignedByteType) ra.get()).set(new UnsignedByteType(Math.round(hue * 255)));
            } else if (type instanceof UnsignedShortType) {
                ((UnsignedShortType) ra.get()).set(new UnsignedShortType(Math.round(hue * 65535)));
            } else if (type instanceof FloatType) {
                ((FloatType) ra.get()).set(new FloatType(hue));
            }
        }
    }

    @Override
    public void addObjectCentroid(Obj obj, float hue) {
        // TODO Auto-generated method stub

    }

    public static <T extends RealType<T> & NativeType<T>> void setCalibration(ImagePlus targetIpl,
            ImgPlus<T> sourceImg) {
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
        RandomAccessibleInterval<T> rai = ImgPlusTools2.forceImgPlusToXYCZT(img);
        ImagePlus ipl = ImageJFunctions.show(rai);
        setCalibration(ipl, img);

    }

    // GETTERS AND SETTERS

    public ImagePlus getImagePlus() {
        RandomAccessibleInterval<T> rai = ImgPlusTools2.forceImgPlusToXYCZT(img);

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