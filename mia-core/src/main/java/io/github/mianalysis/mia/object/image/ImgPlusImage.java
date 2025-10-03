package io.github.mianalysis.mia.object.image;

import java.util.HashMap;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.ObjFactories;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.image.renderer.ImageRenderer;
import io.github.mianalysis.mia.object.image.renderer.ImgPlusRenderer;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class ImgPlusImage<T extends RealType<T> & NativeType<T>> extends Image<T> {
    private static ImageRenderer renderer = new ImgPlusRenderer();
    private ImgPlus<T> img;
    private Overlay overlay = new Overlay();

    // CONSTRUCTORS

    public ImgPlusImage(String name, ImagePlus imagePlus) {
        this.name = name;
        this.img = ImagePlusAdapter.wrapImgPlus(imagePlus);
        this.overlay = imagePlus.getOverlay();

    }

    public ImgPlusImage(String name, ImgPlus<T> img) {
        this.name = name;
        this.img = img;

    }

    // PUBLIC METHODS

    public Objs initialiseEmptyObjs(String outputObjectsName) {
        SpatCal cal = SpatCal.getFromImage(img);
        int tIdx = img.dimensionIndex(Axes.TIME);
        int nFrames = (int) (tIdx == -1 ? 1 : img.dimension(tIdx));
        double frameInterval = tIdx == -1 ? 1 : img.axis(tIdx).calibratedValue(1);

        return new Objs(outputObjectsName, cal, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());

    }

    @Override
    public Objs convertImageToSingleObjects(CoordinateSetFactoryI factory, String outputObjectsName,
            boolean blackBackground) {
        return convertImageToObjects(factory, outputObjectsName, true, blackBackground);
    }

    @Override
    public Objs convertImageToObjects(CoordinateSetFactoryI factory, String outputObjectsName, boolean singleObject) {
        return convertImageToObjects(factory, outputObjectsName, singleObject, true);
    }

    Objs convertImageToObjects(CoordinateSetFactoryI factory, String outputObjectsName, boolean singleObject,
            boolean blackBackground) {
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

        for (int c = 0; c < nChannels; c++) {
            for (int t = 0; t < nFrames; t++) {
                // If using optimised type, determine types for each object, otherwise use a
                // blank map
                HashMap<Integer, Integer> links = new HashMap<>();

                for (int z = 0; z < nSlices; z++) {
                    long[][] interval = ImgPlusTools.getSliceInterval(img, c, z, t);

                    Cursor<T> cursor = (Cursor<T>) Views.interval(img, interval[0], interval[1])
                            .localizingCursor();
                    while (cursor.hasNext()) {
                        cursor.fwd();

                        int imageID = (int) cursor.get().getRealDouble();
                        int x = cursor.getIntPosition(xIdx);
                        int y = cursor.getIntPosition(yIdx);

                        // Checking for inversion
                        if (singleObject)
                            imageID = (blackBackground && imageID != 0) || (!blackBackground && imageID == 0) ? 1 : 0;

                        // If assigning a single object ID, this is the same value for all objects
                        if (singleObject && imageID != 0)
                            imageID = 1;

                        if (imageID != 0) {
                            // If not using optimised type, each link needs to be added here
                            if (!links.containsKey(imageID))
                                links.put(imageID, outputObjects.getAndIncrementID());

                            int outID = links.get(imageID);
                            int finalT = t;

                            outputObjects.computeIfAbsent(outID,
                                    k -> ObjFactories.getDefaultFactory().createObj(outputObjects, factory, outID)
                                            .setT(finalT));
                            try {
                                outputObjects.get(outID).add(x, y, z);
                            } catch (PointOutOfRangeException e) {
                            }
                        }
                    }

                    // Finalising the object store for this slice (this only does something for
                    // Quadtrees)
                    for (Obj obj : outputObjects.values())
                        obj.finaliseSlice(z);

                }
            }
        }

        for (Obj obj : outputObjects.values())
            obj.finalise();

        return outputObjects;

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
        T type = img.firstElement();

        int xIdx = img.dimensionIndex(Axes.X);
        int yIdx = img.dimensionIndex(Axes.Y);
        int zIdx = img.dimensionIndex(Axes.Z);
        int tIdx = img.dimensionIndex(Axes.TIME);

        int tPos = obj.getT();
        int xPos = (int) Math.round(obj.getXMean(true));
        int yPos = (int) Math.round(obj.getYMean(true));
        int zPos = (int) Math.round(obj.getZMean(true, false));

        RandomAccess<T> ra = img.randomAccess();

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

    public void show(String title, @Nullable LUT lut, boolean normalise, String displayMode,
            @Nullable Overlay overlayToUse) {
        // Show using this overlay
        if (overlayToUse == null)
            getRenderer().render(this, title, lut, normalise, displayMode, overlay);
        else
            getRenderer().render(this, title, lut, normalise, displayMode, overlay);getRenderer().render(this, title, lut, normalise, displayMode, overlayToUse);

    }

    public ImgPlusImage<T> duplicate(String outputImageName) {
        ImgPlus<T> outImg = ImgPlusTools.createNewImgPlus(img, img.firstElement());
        LoopBuilder.setImages(img, outImg).forEachPixel((i, o) -> o.set(i));

        ImgPlusImage<T> outImage = new ImgPlusImage<>(outputImageName, outImg);
        outImage.setOverlay(overlay.duplicate());

        return outImage;

    }

    // GETTERS AND SETTERS

    public Overlay getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }

    public ImagePlus getImagePlus() {
        RandomAccessibleInterval<T> rai = ImgPlusTools.forceImgPlusToXYCZT(img);

        ImagePlus ipl = ImageJFunctions.wrap(rai, name);
        setCalibration(ipl, img);
        ipl.setOverlay(overlay);

        return ipl.duplicate();

    }

    public void setImagePlus(ImagePlus imagePlus) {
        if (imagePlus == null) {
            this.img = null;
            this.overlay = null;
            return;
        }

        this.img = ImagePlusAdapter.wrapImgPlus(imagePlus);
        this.overlay = imagePlus.getOverlay();
    }

    public ImgPlus getImgPlus() {
        return img;
    }

    public void setImgPlus(ImgPlus img) {
        this.img = img;
    }

    public Object getRawImage() {
        return img;
    }

    public void setRawImage(Object image) {
        if (image instanceof ImgPlus)
            this.img = (ImgPlus) image;
        else
            MIA.log.writeError("Error in ImgPlusImage.setRawImage(Object image).  Image not instance of ImgPlus.");
    }

    @Override
    public void clear() {
        System.out.println(img.factory());
        img = null;
    }

    @Override
    public int hashCode() {
        int hash = 1;

        ImagePlus imagePlus = getImagePlus();
        Calibration calibration = imagePlus.getCalibration();

        hash = 31 * hash + ((Number) calibration.pixelWidth).hashCode();
        if (imagePlus.getNSlices() > 1)
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

        ImageI image = (Image) obj;
        ImagePlus imagePlus2 = image.getImagePlus();

        // Comparing calibrations
        ImagePlus imagePlus = getImagePlus();
        Calibration calibration1 = imagePlus.getCalibration();
        Calibration calibration2 = imagePlus2.getCalibration();

        if (calibration1.pixelWidth != calibration2.pixelWidth)
            return false;
        if (imagePlus.getNSlices() > 1 && calibration1.pixelDepth != calibration2.pixelDepth)
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

    @Override
    public ImageRenderer getRenderer() {
        if (ImageI.getUseGlobalImageRenderer())
            return ImageI.getGlobalImageRenderer();
        else
            return renderer;
    }

    @Override
    public void setRenderer(ImageRenderer imageRenderer) {
        renderer = imageRenderer;
    }

    public static DiskCachedCellImgOptions getCellImgOptions() {
        int[] cellSize = new int[] { 32, 32, 32 };

        DiskCachedCellImgOptions options = DiskCachedCellImgOptions.options();
        options.numIoThreads(2);
        options.cellDimensions(cellSize);

        // if (MIA.getPreferences().isSpecifyCacheDirectory())
        // options.cacheDirectory(Paths.get(MIA.getPreferences().getCacheDirectory()));

        return options;

    }

    @Override
    public long getWidth() {
        return img.dimension(img.dimensionIndex(Axes.X));
    }

    @Override
    public long getHeight() {
        return img.dimension(img.dimensionIndex(Axes.Y));
    }

    @Override
    public long getNChannels() {
        return img.dimension(img.dimensionIndex(Axes.CHANNEL));
    }

    @Override
    public long getNSlices() {
        return img.dimension(img.dimensionIndex(Axes.Z));
    }

    @Override
    public long getNFrames() {
        return img.dimension(img.dimensionIndex(Axes.TIME));
    }
}