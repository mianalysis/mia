package io.github.mianalysis.mia.object.image;

import java.util.HashMap;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.measure.Calibration;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.renderer.ImagePlusRenderer;
import io.github.mianalysis.mia.object.image.renderer.ImageRenderer;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import net.imagej.ImgPlus;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImagePlusImage<T extends RealType<T> & NativeType<T>> extends Image<T> {
    private static ImageRenderer renderer = new ImagePlusRenderer();
    private ImagePlus imagePlus;

    // CONSTRUCTORS

    public ImagePlusImage(String name, ImagePlus imagePlus) {
        this.name = name;
        this.imagePlus = imagePlus;

    }

    public ImagePlusImage(String name, ImgPlus<T> img) {
        this.name = name;

        // The ImgPlus is duplicated to ensure it's not a virtual stack
        this.imagePlus = ImageJFunctions.wrap(img, name).duplicate();
        ImgPlusTools.applyDimensions(img, this.imagePlus);
        ImgPlusTools.applyAxes(img, this.imagePlus);

    }

    // PUBLIC METHODS

    public Objs initialiseEmptyObjs(String outputObjectsName) {
        SpatCal cal = SpatCal.getFromImage(imagePlus);
        int nFrames = imagePlus.getNFrames();
        double frameInterval = imagePlus.getCalibration().frameInterval;

        return new Objs(outputObjectsName, cal, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());

    }

    @Override
    public Objs convertImageToSingleObjects(String type, String outputObjectsName, boolean blackBackground) {
        return convertImageToObjects(type, outputObjectsName, true, blackBackground);
    }

    @Override
    public Objs convertImageToObjects(String type, String outputObjectsName, boolean singleObject) {
        return convertImageToObjects(type, outputObjectsName, singleObject, true);
    }

    Objs convertImageToObjects(String type, String outputObjectsName, boolean singleObject, boolean blackBackground) {
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
                HashMap<Integer, Integer> links = new HashMap<>();

                for (int z = 0; z < nSlices; z++) {
                    imagePlus.setPosition(c + 1, z + 1, t + 1);
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            // Getting the ID of this object in the current stack.
                            int imageID = (int) ipr.getPixelValue(x, y);

                            // Checking for inversion
                            if (singleObject)
                                imageID = (blackBackground && imageID != 0) || (!blackBackground && imageID == 0) ? 1
                                        : 0;

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
                                        k -> new Obj(outputObjects, volumeType, outID).setT(finalT));
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

    @Override
    public void addObject(Obj obj, float hue) {
        int bitDepth = imagePlus.getBitDepth();

        int tPos = obj.getT();
        for (Point<Integer> point : obj.getCoordinateSet()) {
            int xPos = point.x;
            int yPos = point.y;
            int zPos = point.z;

            imagePlus.setPosition(1, zPos + 1, tPos + 1);

            switch (bitDepth) {
                case 8:
                case 16:
                    imagePlus.getProcessor().putPixel(xPos, yPos, Math.round(hue * 255));
                    break;
                case 32:
                    imagePlus.getProcessor().putPixelValue(xPos, yPos, hue);
                    break;
            }
        }
    }

    public void addObjectCentroid(Obj obj, float hue) {
        int bitDepth = imagePlus.getBitDepth();

        int tPos = obj.getT();
        int xPos = (int) Math.round(obj.getXMean(true));
        int yPos = (int) Math.round(obj.getYMean(true));
        int zPos = (int) Math.round(obj.getZMean(true, false));

        imagePlus.setPosition(1, zPos + 1, tPos + 1);

        switch (bitDepth) {
            case 8:
            case 16:
                imagePlus.getProcessor().putPixel(xPos, yPos, Math.round(hue * 255));
                break;
            case 32:
                imagePlus.getProcessor().putPixelValue(xPos, yPos, hue);
                break;
        }
    }

    public void show(String title, @Nullable LUT lut, boolean normalise, boolean composite) {
        // Show using this overlay
        show(title, lut, normalise, composite, imagePlus.getOverlay());
    }

    public void show(String title, @Nullable LUT lut, boolean normalise, boolean composite, Overlay overlay) {
        getRenderer().render(this, title, lut, normalise, composite, overlay);
    }

    public ImagePlusImage duplicate(String outputImageName) {
        return new ImagePlusImage<>(outputImageName, imagePlus.duplicate());

    }

    // GETTERS AND SETTERS

    public Overlay getOverlay() {
        if (imagePlus.getOverlay() == null)
            imagePlus.setOverlay(new ij.gui.Overlay());

        return imagePlus.getOverlay();

    }

    public void setOverlay(Overlay overlay) {
        imagePlus.setOverlay(overlay);
    }

    public ImagePlus getImagePlus() {
        return imagePlus;
    }

    public void setImagePlus(ImagePlus imagePlus) {
        this.imagePlus = imagePlus;
    }

    public ImgPlus<T> getImgPlus() {
        return ImagePlusAdapter.wrapImgPlus(imagePlus);
    }

    public void setImgPlus(ImgPlus<T> img) {
        if (img == null) {
            this.imagePlus = null;
            return;
        }

        // Duplicating ensures any cached images are moved to RAM
        imagePlus = ImageJFunctions.wrap(img, name).duplicate();
        ImgPlusTools.applyDimensions(img, imagePlus);

    }

    public synchronized static ImageStack getSetStack(ImagePlus inputImagePlus, int timepoint, int channel,
            @Nullable ImageStack toPut) {
        int nSlices = inputImagePlus.getNSlices();
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, "1-" + nSlices,
                    timepoint + "-" + timepoint).getStack();
        } else {
            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
                inputImagePlus.setPosition(channel, z, timepoint);
                inputImagePlus.setProcessor(toPut.getProcessor(z));
            }
            inputImagePlus.updateAndDraw();
            return null;
        }
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

        Image image2 = (Image) obj;
        ImagePlus imagePlus2 = image2.getImagePlus();

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
        return "ImagePlusImage (" + name + ")";
    }

    @Override
    public ImageRenderer getRenderer() {
        if (getUseGlobalImageRenderer())
            return getGlobalImageRenderer();
        else
            return renderer;
    }

    @Override
    public void setRenderer(ImageRenderer imageRenderer) {
        renderer = imageRenderer;
    }

    @Override
    public long getWidth() {
        return imagePlus.getWidth();
    }

    @Override
    public long getHeight() {
        return imagePlus.getHeight();
    }

    @Override
    public long getNChannels() {
        return imagePlus.getNChannels();
    }

    @Override
    public long getNSlices() {
        return imagePlus.getNSlices();
    }

    @Override
    public long getNFrames() {
        return imagePlus.getNFrames();
    }
}
