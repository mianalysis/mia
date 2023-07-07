package io.github.mianalysis.mia.module.inputoutput;

import java.awt.Rectangle;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scijava.Priority;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.CanvasResizer;
import ij.plugin.CompositeConverter;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.sjcommon.metadataextractors.CV7000FilenameExtractor;
import io.github.sjcross.sjcommon.metadataextractors.IncuCyteShortFilenameExtractor;
import io.github.sjcross.sjcommon.metadataextractors.Metadata;
import io.github.sjcross.sjcommon.metadataextractors.NameExtractor;
import io.github.sjcross.sjcommon.process.CommaSeparatedStringInterpreter;
import io.github.sjcross.sjcommon.system.FileCrawler;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.SCIFIO;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.Memoizer;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.units.unit.Unit;
import ome.xml.meta.IMetadata;
import ome.xml.model.primitives.Color;
import ome.xml.model.primitives.PositiveInteger;

/**
 * Created by Stephen on 15/05/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ImageLoader<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String LOADER_SEPARATOR = "Core image loading controls";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String IMPORT_MODE = "Import mode";
    public static final String READER = "Reader";
    public static final String SEQUENCE_ROOT_NAME = "Sequence root name";
    public static final String NAME_FORMAT = "Name format";
    public static final String COMMENT = "Comment";
    public static final String EXTENSION = "Extension";
    public static final String GENERIC_FORMAT = "Generic format";
    public static final String CHANNEL = "Channel";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
    public static final String INCLUDE_SERIES_NUMBER = "Include series number";
    public static final String FILE_PATH = "File path";
    public static final String SERIES_MODE = "Series mode";
    public static final String SERIES_NUMBER = "Series number";

    public static final String RANGE_SEPARATOR = "Dimension ranges";
    public static final String CHANNELS = "Channels";
    public static final String SLICES = "Slices";
    public static final String FRAMES = "Frames";

    public static final String CROP_SEPARATOR = "Image cropping";
    public static final String CROP_MODE = "Crop mode";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String OBJECTS_FOR_LIMITS = "Objects for limits";

    public static final String SCALE_SEPARATOR = "Image scaling";
    public static final String SCALE_MODE = "Scale mode";
    public static final String SCALE_FACTOR_X = "X scale factor";
    public static final String SCALE_FACTOR_Y = "Y scale factor";
    public static final String DIMENSION_MISMATCH_MODE = "Dimension mismatch mode";
    public static final String PAD_INTENSITY_MODE = "Pad intensity mode";

    public static final String CALIBRATION_SEPARATOR = "Spatial and intensity calibration";
    public static final String SET_SPATIAL_CAL = "Set manual spatial calibration";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";
    public static final String SET_TEMPORAL_CAL = "Set manual temporal calibration";
    public static final String FRAME_INTERVAL = "Frame interval (time/frame)";
    public static final String FORCE_BIT_DEPTH = "Force bit depth";
    public static final String OUTPUT_BIT_DEPTH = "Output bit depth";
    public static final String MIN_INPUT_INTENSITY = "Minimum input intensity";
    public static final String MAX_INPUT_INTENSITY = "Maximum input intensity";

    public static void main(String[] args) throws io.scif.FormatException, IOException {
        SCIFIO scifio = new SCIFIO();
        String path = "C:\\Users\\steph\\Desktop\\lumen_area.tif";
        Reader reader = scifio.initializer().initializeReader(new FileLocation(path));
        Plane plane = reader.openPlane(0, 0);
        System.out.println(plane.getLengths()[0]);
    }

    public ImageLoader(Modules modules) {
        super("Load image", modules);
    }

    public interface ImportModes {
        String ALL_IN_FOLDER = "All in current folder";
        String CURRENT_FILE = "Current file";
        String IMAGEJ = "From ImageJ";
        String IMAGE_SEQUENCE_ALPHABETICAL = "Image sequence (alphabetical)";
        String IMAGE_SEQUENCE_ZEROS = "Image sequence (zero-based)";
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[] { ALL_IN_FOLDER, CURRENT_FILE, IMAGEJ, IMAGE_SEQUENCE_ALPHABETICAL,
                IMAGE_SEQUENCE_ZEROS, MATCHING_FORMAT, SPECIFIC_FILE };

    }

    public interface Readers {
        String BIOFORMATS = "Bio-Formats";
        String IMAGEJ = "ImageJ";

        String[] ALL = new String[] { BIOFORMATS, IMAGEJ };

    }

    public interface SeriesModes {
        String CURRENT_SERIES = "Current series";
        String SPECIFIC_SERIES = "Specific series";

        String[] ALL = new String[] { CURRENT_SERIES, SPECIFIC_SERIES };

    }

    public interface NameFormats {
        String GENERIC = "Generic (from metadata)";
        String HUYGENS = "Huygens";
        String INCUCYTE_SHORT = "Incucyte short filename";
        String YOKOGAWA = "Yokogowa";

        String[] ALL = new String[] { GENERIC, HUYGENS, INCUCYTE_SHORT, YOKOGAWA };

    }

    public interface CropModes {
        String NONE = "None";
        String FIXED = "Fixed";
        String FROM_REFERENCE = "From reference";
        String OBJECT_COLLECTION_LIMITS = "Object collection limits";

        String[] ALL = new String[] { NONE, FIXED, FROM_REFERENCE, OBJECT_COLLECTION_LIMITS };

    }

    public interface ScaleModes {
        String NONE = "No scaling";
        String NO_INTERPOLATION = "Scaling (no interpolation)";
        String BILINEAR = "Scaling (bilinear)";
        String BICUBIC = "Scaling (bicubic)";

        String[] ALL = new String[] { NONE, NO_INTERPOLATION, BILINEAR, BICUBIC };

    }

    public interface DimensionMismatchModes {
        String DISALLOW = "Disallow (fail upon mismatch)";
        String CENTRE_CROP = "Crop (centred)";
        String CENTRE_PAD = "Pad (centred)";

        String[] ALL = new String[] { DISALLOW, CENTRE_CROP, CENTRE_PAD };

    }

    public interface PadIntensityModes {
        String BLACK = "Black (0)";
        String WHITE = "White (bit-depth max)";

        String[] ALL = new String[] { BLACK, WHITE };

    }

    public interface OutputBitDepths {
        String EIGHT = "8";
        String SIXTEEN = "16";
        String THIRTY_TWO = "32";

        String[] ALL = new String[] { EIGHT, SIXTEEN, THIRTY_TWO };

    }

    public interface Measurements {
        String ROI_LEFT = "IMAGE_LOADING // ROI_LEFT (PX)";
        String ROI_TOP = "IMAGE_LOADING // ROI_TOP (PX)";
        String ROI_WIDTH = "IMAGE_LOADING // ROI_WIDTH (PX)";
        String ROI_HEIGHT = "IMAGE_LOADING // ROI_HEIGHT (PX)";

    }

    protected static int checkBitDepth(int bitDepth) {
        // Ensure bit depth is 8, 16 or 32
        if (bitDepth < 8)
            bitDepth = 8;
        else if (bitDepth > 8 && bitDepth < 16)
            bitDepth = 16;
        else if (bitDepth > 16 && bitDepth < 32)
            bitDepth = 32;
        else if (bitDepth > 32) {
            MIA.log.writeError("Input image bit depth exceeds maximum supported (32 bit).");
            return -1;
        }

        return bitDepth;

    }

    public HashMap<Integer, LUT> getLUTs(IMetadata meta, int series) {
        HashMap<Integer, LUT> luts = new HashMap<>();

        for (int c = 0; c < meta.getChannelCount(series); c++) {
            Color color = meta.getChannelColor(series, c);

            if (color == null) {
                luts.put(c, null);
            } else {
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                LUT lut = LUT.createLutFromColor(new java.awt.Color(red, green, blue));
                luts.put(c, lut);
            }
        }

        return luts;

    }

    public static int[] getCropROI(Image referenceImage) {
        int[] crop = null;
        // Displaying the image
        ImagePlus referenceIpl = referenceImage.getImagePlus().duplicate();
        referenceIpl.show();

        // Asking the user to draw a rectangular ROI
        IJ.runMacro("waitForUser(getArgument())", "Click \"OK\" once ROI selected");

        // Getting the ROI
        Roi roi = referenceIpl.getRoi();
        Rectangle bounds = roi.getBounds();
        crop = new int[] { bounds.x, bounds.y, bounds.width, bounds.height };

        // Closing the reference image
        referenceIpl.close();

        return crop;

    }

    public ImagePlus getBFImage(String path, int seriesNumber, @NotNull String[] dimRanges, @Nullable int[] crop,
            double[] scaleFactors, String scaleMode, @Nullable double[] intRange, boolean[] manualCal,
            boolean localVerbose) throws ServiceException, DependencyException, IOException, FormatException {
        DebugTools.enableLogging("off");
        DebugTools.setRootLevel("off");

        // Setting spatial calibration
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        IMetadata meta = service.createOMEXMLMetadata();

        IFormatReader reader = MIA.getPreferences()
                .getReader(new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader())));
        reader.setMetadataStore((MetadataStore) meta);
        reader.setGroupFiles(false);
        reader.setId(path);
        reader.setSeries(seriesNumber - 1);

        int left = 0;
        int top = 0;
        int width = reader.getSizeX();
        int height = reader.getSizeY();

        int sizeC = reader.getSizeC();
        int sizeT = reader.getSizeT();
        int sizeZ = reader.getSizeZ();
        int bitDepth = reader.getBitsPerPixel();

        // If a specific bit depth is to be used
        if (intRange != null)
            bitDepth = (int) intRange[0];

        bitDepth = checkBitDepth(bitDepth);
        if (bitDepth == -1)
            return null;

        if (crop != null) {
            left = crop[0];
            top = crop[1];
            width = crop[2];
            height = crop[3];
        }

        int[] channelsList = CommaSeparatedStringInterpreter.interpretIntegers(dimRanges[0], true, sizeC);
        int[] slicesList = CommaSeparatedStringInterpreter.interpretIntegers(dimRanges[1], true, sizeZ);
        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(dimRanges[2], true, sizeT);

        int nC = channelsList.length;
        int nZ = slicesList.length;
        int nT = framesList.length;

        int widthOut = width;
        int heightOut = height;

        // Applying scaling
        switch (scaleMode) {
            case ScaleModes.NONE:
                scaleFactors[0] = 1;
                scaleFactors[1] = 1;
                break;
            case ScaleModes.NO_INTERPOLATION:
            case ScaleModes.BILINEAR:
            case ScaleModes.BICUBIC:
                widthOut = (int) Math.round(width * scaleFactors[0]);
                heightOut = (int) Math.round(height * scaleFactors[1]);
                break;
        }

        // Creating the new ImagePlus
        ImagePlus ipl = IJ.createHyperStack("Image", widthOut, heightOut, nC, nZ, nT, bitDepth);

        // Iterating over all images in the stack, adding them to the output ImagePlus
        int nTotal = nC * nT * nZ;
        int count = 0;
        int countZ = 1;

        for (int z : slicesList) {
            int countC = 1;
            for (int c : channelsList) {
                int countT = 1;
                for (int t : framesList) {
                    int idx;
                    try {
                        idx = reader.getIndex(z - 1, c - 1, t - 1);
                    } catch (IllegalArgumentException e) {
                        MIA.log.writeWarning("Indices out of range for image \"" + path + "\" at c=" + (c - 1) + ", z="
                                + (z - 1) + ", t=" + (t - 1));
                        return null;
                    }

                    ImageProcessor ip = null;
                    if (reader instanceof Memoizer)
                        ip = ((ImageProcessorReader) ((Memoizer) reader).getReader()).openProcessors(idx, left, top,
                                width, height)[0];
                    else
                        ip = ((ImageProcessorReader) reader).openProcessors(idx, left, top,
                                width, height)[0];

                    // If forcing bit depth
                    if (intRange != null) {
                        ip.setMinAndMax(intRange[1], intRange[2]);
                        switch (bitDepth) {
                            case 8:
                                ip = ip.convertToByte(true);
                                break;
                            case 16:
                                ip = ip.convertToShort(true);
                                break;
                            case 32:
                                ip = ip.convertToFloat();
                                break;
                        }
                    }

                    // Applying scaling
                    switch (scaleMode) {
                        case ScaleModes.NO_INTERPOLATION:
                            ip.setInterpolationMethod(ImageProcessor.NONE);
                            ip = ip.resize(widthOut, heightOut);
                            break;
                        case ScaleModes.BILINEAR:
                            ip.setInterpolationMethod(ImageProcessor.BILINEAR);
                            ip = ip.resize(widthOut, heightOut);
                            break;
                        case ScaleModes.BICUBIC:
                            ip.setInterpolationMethod(ImageProcessor.BICUBIC);
                            ip = ip.resize(widthOut, heightOut);
                            break;
                    }

                    ipl.setPosition(countC, countZ, countT);
                    ipl.setProcessor(ip);

                    if (localVerbose)
                        writeProgressStatus(++count, nTotal, "images");

                    countT++;
                }
                countC++;
            }
            countZ++;
        }
        ipl.setPosition(1, 1, 1);
        ipl.updateAndDraw();

        // Applying LUTs
        HashMap<Integer, LUT> luts = getLUTs(meta, seriesNumber - 1);
        for (int i = 0; i < channelsList.length; i++) {
            int c = channelsList[i];
            LUT lut = luts.get(c - 1);
            if (lut == null)
                continue;
            if (ipl.isComposite()) {
                ((CompositeImage) ipl).setChannelLut(lut, i + 1);
            } else {
                ipl.setLut(lut);
            }
        }
        ipl.setPosition(1, 1, 1);
        ipl.updateAndDraw();

        // Adding spatial calibration
        if (!manualCal[0]) {
            if (!setSpatialCalibrationBF(ipl, meta, seriesNumber, scaleFactors)
                    && ipl.getCalibration().getUnit().equals("pixel")) {
                MIA.log.writeWarning("Can't apply spatial units for file \"" + new File(path).getName()
                        + "\".  Spatially calibrated values will be unavailable.");
                setDummySpatialCalibration(ipl);
            }
        }

        // Adding temporal calibration
        if (!manualCal[1]) {
            if (!setTemporalCalibrationBF(ipl, meta, seriesNumber)) {
                // Only display a warning if there's more than 1 frame. Otherwise this should't
                // be a problem.
                if (ipl.getNFrames() != 1)
                    MIA.log.writeWarning("Can't apply temporal units for file \"" + new File(path).getName()
                            + "\".  Temporally calibrated values will be unavailable.");

                // Either way, remove the temporal calibration
                setDummyTemporalCalibration(ipl);
            }
        }

        ipl.setPosition(1, 1, 1);
        ipl.updateAndDraw();

        reader.close();

        return ipl;

    }

    boolean setSpatialCalibrationBF(ImagePlus ipl, IMetadata meta, int seriesNumber, double[] scaleFactors) {
        // Add spatial calibration
        Unit<Length> spatialUnits = SpatialUnit.getOMEUnit();
        if (spatialUnits == null)
            return false;

        Calibration cal = ipl.getCalibration();
        cal.setXUnit(spatialUnits.getSymbol());
        cal.setYUnit(spatialUnits.getSymbol());
        cal.setZUnit(spatialUnits.getSymbol());

        if (meta == null)
            return false;

        if (meta.getPixelsPhysicalSizeX(seriesNumber - 1) == null) {
            return false;
        } else {
            Length physicalSizeX = meta.getPixelsPhysicalSizeX(seriesNumber - 1);
            if (physicalSizeX == null || physicalSizeX.value(spatialUnits) == null)
                return false;

            cal.pixelWidth = (double) physicalSizeX.value(spatialUnits) / scaleFactors[0];
        }

        if (meta.getPixelsPhysicalSizeY(seriesNumber - 1) == null) {
            return false;
        } else {
            Length physicalSizeY = meta.getPixelsPhysicalSizeY(seriesNumber - 1);
            if (physicalSizeY == null || physicalSizeY.value(spatialUnits) == null)
                return false;

            cal.pixelHeight = (double) physicalSizeY.value(spatialUnits) / scaleFactors[1];
        }

        if (ipl.getNSlices() > 1) {
            if (meta.getPixelsPhysicalSizeZ(seriesNumber - 1) == null) {
                return false;
            } else {
                Length physicalSizeZ = meta.getPixelsPhysicalSizeZ(seriesNumber - 1);
                if (physicalSizeZ == null || physicalSizeZ.value(spatialUnits) == null)
                    return false;

                cal.pixelDepth = (double) physicalSizeZ.value(spatialUnits);
            }
        }

        return true;

    }

    boolean setTemporalCalibrationBF(ImagePlus ipl, IMetadata meta, int seriesNumber) {
        // Add spatial calibration
        Unit<Time> temporalUnits = TemporalUnit.getOMEUnit();

        Calibration cal = ipl.getCalibration();
        cal.setTimeUnit(TemporalUnit.getOMEUnit().getSymbol());

        if (meta == null)
            return false;

        if (meta.getPixelsTimeIncrement(seriesNumber - 1) == null) {
            return false;
        } else {
            Time frameInterval = meta.getPixelsTimeIncrement(seriesNumber - 1);
            ipl.getCalibration().frameInterval = (double) frameInterval.value(temporalUnits);
            cal.fps = 1 / (double) frameInterval.value(UNITS.SECOND);
        }

        return true;

    }

    void setDummySpatialCalibration(ImagePlus ipl) {
        Calibration cal = ipl.getCalibration();

        cal.pixelWidth = Double.NaN;
        cal.pixelHeight = Double.NaN;
        cal.pixelDepth = Double.NaN;

    }

    void setDummyTemporalCalibration(ImagePlus ipl) {
        Calibration cal = ipl.getCalibration();

        cal.frameInterval = Double.NaN;
        cal.fps = Double.NaN;

    }

    void parseImageJSpatialCalibration(ImagePlus ipl, String path) {
        Calibration cal = ipl.getCalibration();

        // Checking if spatial units match those selected in InputControl
        String currUnit = cal.getUnit().toLowerCase();
        Unit<Length> currUnitOME = null;
        Unit<Length> targetUnitOME = SpatialUnit.getOMEUnit();

        if (currUnit.matches("um") || currUnit.matches("μm") || currUnit.contains("micron")
                || currUnit.contains("micrometer") || currUnit.contains("micrometre")) {
            currUnitOME = UNITS.MICROMETER;

        } else if (currUnit.matches("mm") || currUnit.contains("millimeter") || currUnit.contains("millimetre")) {
            currUnitOME = UNITS.MILLIMETER;

        } else if (currUnit.matches("cm") || currUnit.contains("centimeter") || currUnit.contains("centimetre")) {
            currUnitOME = UNITS.CENTIMETER;

        } else if (currUnit.matches("nm") || currUnit.contains("nanometer") || currUnit.contains("nanometre")) {
            currUnitOME = UNITS.NANOMETER;

        } else if (currUnit.matches("A") || currUnit.matches("Å") || currUnit.contains("angstrom")) {
            currUnitOME = UNITS.ANGSTROM;

        } else if (currUnit.matches("m") || currUnit.contains("meter") || currUnit.contains("metre")) {
            // THIS ONE HAS TO BE THE LAST ONE AS IT WILL PROBABLY MATCH
            currUnitOME = UNITS.METER;
        }

        if (currUnitOME == null) {
            if (path == null) {
                MIA.log.writeWarning(
                        "Can't apply spatial units for image loaded from ImageJ.  Spatially calibrated values will be unavailable.");
            } else {
                MIA.log.writeWarning("Can't apply spatial units for file \"" + new File(path).getName()
                        + "\".  Spatially calibrated values will be unavailable.");
            }
            setDummySpatialCalibration(ipl);

        } else if (currUnitOME != targetUnitOME) {
            cal.pixelWidth = currUnitOME.convertValue(cal.pixelWidth, targetUnitOME);
            cal.pixelHeight = currUnitOME.convertValue(cal.pixelHeight, targetUnitOME);
            cal.pixelDepth = currUnitOME.convertValue(cal.pixelDepth, targetUnitOME);
            cal.setUnit(targetUnitOME.getSymbol());
        }
    }

    void parseImageJTemporalCalibration(ImagePlus ipl, String path) {
        Calibration cal = ipl.getCalibration();

        // Checking if spatial units match those selected in InputControl
        String currUnit = cal.getTimeUnit().toLowerCase();
        Unit<Time> currUnitOME = null;
        Unit<Time> targetUnitOME = TemporalUnit.getOMEUnit();

        if (currUnit.matches("ns") || currUnit.contains("nanosecond")) {
            currUnitOME = UNITS.NANOSECOND;

        } else if (currUnit.matches("ms") || currUnit.contains("millisecond")) {
            currUnitOME = UNITS.MILLISECOND;

        } else if (currUnit.matches("m") || currUnit.contains("min") || currUnit.contains("minute")) {
            currUnitOME = UNITS.MINUTE;

        } else if (currUnit.matches("h") || currUnit.contains("hour")) {
            currUnitOME = UNITS.HOUR;

        } else if (currUnit.matches("d") || currUnit.matches("day")) {
            currUnitOME = UNITS.DAY;

        } else if (currUnit.matches("s") || currUnit.contains("sec") || currUnit.contains("second")) {
            // THIS ONE HAS TO BE THE LAST ONE AS IT WILL PROBABLY MATCH
            currUnitOME = UNITS.SECOND;
        }

        if (currUnitOME == null && ipl.getNFrames() != 1) {
            if (path == null) {
                MIA.log.writeWarning(
                        "Can't apply temporal units for image loaded from ImageJ.  Temporally calibrated values will be unavailable.");
            } else {
                MIA.log.writeWarning("Can't apply temporal units for file \"" + new File(path).getName()
                        + "\".  Temporally calibrated values will be unavailable.");
            }
            setDummyTemporalCalibration(ipl);

        } else if (currUnitOME != targetUnitOME) {
            cal.frameInterval = currUnitOME.convertValue(cal.frameInterval, targetUnitOME);
            cal.fps = 1 / targetUnitOME.convertValue(cal.frameInterval, UNITS.SECOND);
            cal.setTimeUnit(targetUnitOME.getSymbol());
        }
    }

    public ImagePlus getAlphabeticalImageSequence(String absolutePath, int seriesNumber, String channels, String slices,
            String frames, int[] crop, double[] scaleFactors, String scaleMode, String dimensionMismatchMode,
            String padIntensityMode, @Nullable double[] intRange, boolean[] manualCal, Metadata metadata)
            throws ServiceException, DependencyException, FormatException, IOException {

        // Getting list of all matching filenames
        String[] filenames = getGenericNames(metadata, absolutePath);

        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frames, true, filenames.length);

        // Determining the dimensions of the input image
        String[] dimRanges = new String[] { channels, slices, "1" };

        if (filenames.length == 0)
            return null;

        ImagePlus rootIpl = getBFImage(filenames[0], seriesNumber, dimRanges, crop, scaleFactors, scaleMode, intRange,
                manualCal, false);
        int width = rootIpl.getWidth();
        int height = rootIpl.getHeight();
        int bitDepth = rootIpl.getBitDepth();
        int nChannels = rootIpl.getNChannels();
        int nSlices = rootIpl.getNSlices();

        if (crop != null) {
            width = crop[2];
            height = crop[3];
        }

        // Creating the new image
        int count = 0;
        int total = framesList.length;
        ImagePlus outputIpl = IJ.createHyperStack("Image", width, height, nChannels, nSlices, total, bitDepth);

        for (int frame : framesList) {
            ImagePlus tempIpl = getBFImage(filenames[frame - 1], 1, dimRanges, crop, scaleFactors, scaleMode, intRange,
                    manualCal, false);

            // Checking and handling any XY dimension mismatches between the two stacks
            applyDimensionMatchingXY(outputIpl, tempIpl, dimensionMismatchMode, padIntensityMode);

            for (int c = 0; c < nChannels; c++) {
                for (int z = 0; z < nSlices; z++) {
                    tempIpl.setPosition(c + 1, z + 1, 1);
                    outputIpl.setPosition(c + 1, z + 1, count + 1);

                    outputIpl.getProcessor().setPixels(tempIpl.getProcessor().getPixels());

                }
            }

            writeProgressStatus(++count, total, "images");

        }

        outputIpl.setPosition(1, 1, 1);
        outputIpl.setCalibration(rootIpl.getCalibration());
        outputIpl.updateAndDraw();

        return outputIpl;

    }

    public ImagePlus getZeroBasedImageSequence(String absolutePath, int seriesNumber, String channels, String slices,
            String frames, int[] crop, double[] scaleFactors, String scaleMode, String dimensionMismatchMode,
            String padIntensityMode, @Nullable double[] intRange, boolean[] manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {

        // Number format
        Pattern pattern = Pattern.compile("Z\\{0+}");
        Matcher matcher = pattern.matcher(absolutePath);
        int numberOfZeroes = 0;
        String nameBefore = "";
        String nameAfter = "";
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            numberOfZeroes = end - start - 3; // Removing 3 for the "Z{}"
            nameBefore = absolutePath.substring(0, start);
            nameAfter = absolutePath.substring(end);
        } else {
            MIA.log.writeWarning("Zero location in sequence filename uncertain.");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numberOfZeroes; i++)
            stringBuilder.append("0");
        DecimalFormat df = new DecimalFormat(stringBuilder.toString());

        // This supports the special value "start", which will be the smallest value
        // starting at 0
        if (frames.contains("start")) {
            int minFrame = 0;
            while (!new File(nameBefore + df.format(minFrame) + nameAfter).exists())
                minFrame++;

            frames = frames.replace("start", String.valueOf(minFrame));
        }

        // Determining the number of images to load
        int maxFrame = 0;
        if (frames.contains("end")) {
            maxFrame = CommaSeparatedStringInterpreter.firstValue(frames);
            while (new File(nameBefore + df.format(maxFrame) + nameAfter).exists())
                maxFrame++;
        }

        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frames, true, maxFrame - 1);

        // Determining the dimensions of the input image
        String[] dimRanges = new String[] { channels, slices, "1" };

        if (framesList.length == 0)
            return null;

        ImagePlus rootIpl = getBFImage(nameBefore + df.format(framesList[0]) + nameAfter, seriesNumber, dimRanges, crop,
                scaleFactors, scaleMode, intRange, manualCal, false);
        int width = rootIpl.getWidth();
        int height = rootIpl.getHeight();
        int bitDepth = rootIpl.getBitDepth();
        int nChannels = rootIpl.getNChannels();
        int nSlices = rootIpl.getNSlices();

        if (crop != null) {
            width = crop[2];
            height = crop[3];
        }

        // Creating the new image
        int count = 0;
        int total = framesList.length;
        ImagePlus outputIpl = IJ.createHyperStack("Image", width, height, nChannels, nSlices, total, bitDepth);

        for (int frame : framesList) {
            String currentPath = nameBefore + df.format(frame) + nameAfter;

            ImagePlus tempIpl = getBFImage(currentPath, 1, dimRanges, crop, scaleFactors, scaleMode, intRange,
                    manualCal, false);

            // Checking and handling any XY dimension mismatches between the two stacks
            applyDimensionMatchingXY(outputIpl, tempIpl, dimensionMismatchMode, padIntensityMode);

            for (int c = 0; c < nChannels; c++) {
                for (int z = 0; z < nSlices; z++) {
                    tempIpl.setPosition(c + 1, z + 1, 1);
                    outputIpl.setPosition(c + 1, z + 1, count + 1);

                    outputIpl.getProcessor().setPixels(tempIpl.getProcessor().getPixels());

                }
            }

            writeProgressStatus(++count, total, "images");

        }

        outputIpl.setPosition(1, 1, 1);
        outputIpl.setCalibration(rootIpl.getCalibration());
        outputIpl.updateAndDraw();

        return outputIpl;

    }

    public static void applyDimensionMatchingXY(ImagePlus outputIpl, ImagePlus inputIpl, String dimensionMismatchMode,
            String padIntensityMode) {
        // Checking if dimensions are mismatches
        int widthIn = inputIpl.getWidth();
        int heightIn = inputIpl.getHeight();
        int widthOut = outputIpl.getWidth();
        int heightOut = outputIpl.getHeight();

        if (widthIn == widthOut && heightIn == heightOut)
            return;

        int padIntensity = getPadIntensity(inputIpl, padIntensityMode);
        IJ.setBackgroundColor(padIntensity, padIntensity, padIntensity);

        switch (dimensionMismatchMode) {
            case DimensionMismatchModes.CENTRE_PAD:
                int widthExpanded = Math.max(widthIn, widthOut);
                int heightExpanded = Math.max(heightIn, heightOut);

                if (widthIn < widthExpanded || heightIn < heightExpanded)
                    inputIpl.setStack(centreResizeStack(inputIpl.getStack(), widthExpanded, heightExpanded));

                if (widthOut < widthExpanded || heightOut < heightExpanded)
                    outputIpl.setStack(centreResizeStack(outputIpl.getStack(), widthExpanded, heightExpanded));

                break;

            case DimensionMismatchModes.CENTRE_CROP:
                widthExpanded = Math.min(widthIn, widthOut);
                heightExpanded = Math.min(heightIn, heightOut);

                if (widthIn > widthExpanded || heightIn > heightExpanded)
                    inputIpl.setStack(centreResizeStack(inputIpl.getStack(), widthExpanded, heightExpanded));

                if (widthOut > widthExpanded || heightOut > heightExpanded)
                    outputIpl.setStack(centreResizeStack(outputIpl.getStack(), widthExpanded, heightExpanded));

                break;
        }
    }

    public static int getPadIntensity(ImagePlus imagePlus, String padIntensityMode) {
        switch (padIntensityMode) {
            default:
            case PadIntensityModes.BLACK:
                return 0;
            case PadIntensityModes.WHITE:
                return (int) Math.round(Math.pow(imagePlus.getBitDepth(), 2) - 1);
        }
    }

    public static ImageStack centreResizeStack(ImageStack imageStack, int widthExpanded, int heightExpanded) {
        int xOff = (widthExpanded - imageStack.getWidth()) / 2;
        int yOff = (heightExpanded - imageStack.getHeight()) / 2;

        return new CanvasResizer().expandStack(imageStack, widthExpanded, heightExpanded, xOff, yOff);

    }

    public ImagePlus getAllInFolder(File rootFile, int[] crop, double[] scaleFactors, String scaleMode,
            @Nullable double[] intRange, boolean[] manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {

        // Creating a FileCrawler to get all the valid files in this folder
        FileCrawler fileCrawler = new FileCrawler(rootFile.getParentFile());
        modules.getInputControl().addFilenameFilters(fileCrawler);

        // Getting number of valid files in this folder
        File next = fileCrawler.getNextValidFileInFolder();
        int count = 0;
        while (next != null) {
            count++;
            next = fileCrawler.getNextValidFileInFolder();
        }

        // Determining the dimensions of the input image
        String[] dimRanges = new String[] { "1", "1", "1" };
        ImagePlus rootIpl = getBFImage(rootFile.getAbsolutePath(), 1, dimRanges, crop, scaleFactors, scaleMode,
                intRange, manualCal, false);
        int width = rootIpl.getWidth();
        int height = rootIpl.getHeight();
        int bitDepth = rootIpl.getBitDepth();

        if (crop != null) {
            width = crop[2];
            height = crop[3];
        }

        // Creating the new image
        ImagePlus outputIpl = IJ.createImage("Image", width, height, count, bitDepth);
        fileCrawler = new FileCrawler(rootFile.getParentFile());
        next = fileCrawler.getNextValidFileInFolder();
        int i = 0;
        while (next != null) {
            ImagePlus tempIpl = getBFImage(next.getAbsolutePath(), 1, dimRanges, crop, scaleFactors, scaleMode,
                    intRange, manualCal, false);

            outputIpl.setPosition(i + 1);
            outputIpl.setProcessor(tempIpl.getProcessor());

            next = fileCrawler.getNextValidFileInFolder();
            i++;

        }

        outputIpl.setPosition(1);
        outputIpl.updateAndDraw();
        outputIpl.setCalibration(rootIpl.getCalibration());

        return outputIpl;

    }

    public String getHuygensPath(Metadata metadata) {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String extension = FilenameUtils.getExtension(absolutePath);

        // The name will end with "_chxx" where "xx" is a two-digit number
        Pattern pattern = Pattern.compile("(.+)_ch([0-9]{2})");
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find())
            return null;

        return path + matcher.group(1) + "_ch" + metadata.getComment() + "." + extension;

    }

    private String getIncucyteShortName(Metadata metadata) {
        // First, running metadata extraction on the input file
        NameExtractor filenameExtractor = new IncuCyteShortFilenameExtractor();
        filenameExtractor.extract(metadata, metadata.getFile().getName());

        // Constructing a new name using the same name format
        String comment = metadata.getComment();
        return metadata.getFile().getParent() + File.separator + IncuCyteShortFilenameExtractor.generate(comment,
                metadata.getWell(), metadata.getAsString(Metadata.FIELD), metadata.getExt());

    }

    private String getYokogawaName(Metadata metadata, int channel)
            throws ServiceException, DependencyException, FormatException, IOException {

        // Creating metadata object
        Metadata tempMetadata = new Metadata();

        // First, running metadata extraction on the input file
        CV7000FilenameExtractor extractor = new CV7000FilenameExtractor();
        extractor.setToUseActionWildcard(true);
        extractor.extract(tempMetadata, metadata.getFile().getName());

        // Constructing a new name using the same name format
        tempMetadata.setChannel(channel);
        final String filename = extractor.construct(tempMetadata);

        // Running through files in this folder to find the one matching the pattern
        File parentFile = metadata.getFile().getParentFile();
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.compile(filename).matcher(name).find();
            }
        };

        File[] listOfFiles = parentFile.listFiles(filter);
        if (listOfFiles == null)
            return null;

        return listOfFiles[0].getAbsolutePath();

    }

    public static String getPrefixName(Metadata metadata, boolean includeSeries, String ext)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String series = includeSeries ? "_S" + metadata.getSeriesNumber() : "";

        return path + comment + name + series + "." + ext;

    }

    public static String getSuffixName(Metadata metadata, boolean includeSeries, String ext)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String series = includeSeries ? "_S" + metadata.getSeriesNumber() : "";

        return path + name + series + comment + "." + ext;

    }

    public static String getGenericName(Metadata metadata, String genericFormat)
            throws ServiceException, DependencyException, FormatException, IOException {
        // Returns the first generic name matching the specified format
        return getGenericNames(metadata, genericFormat)[0];

    }

    public static String[] getGenericNames(Metadata metadata, String genericFormat)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.insertMetadataValues(genericFormat);
        String filepath = FilenameUtils.getFullPath(absolutePath);
        String filename = FilenameUtils.getName(absolutePath);

        // If name includes "*" get first instance of wildcard
        if (filename.contains("*")) {
            String[] filenames = new File(filepath).list(new WildcardFileFilter(filename));

            // Appending the filepath to the start of each name
            return Arrays.stream(filenames).map(v -> filepath + v).sorted().toArray(s -> new String[s]);
        }

        return new String[] { filepath + filename };

    }

    private void addCropMeasurements(Image image, int[] crop) {
        image.addMeasurement(new Measurement(Measurements.ROI_LEFT, crop[0]));
        image.addMeasurement(new Measurement(Measurements.ROI_TOP, crop[1]));
        image.addMeasurement(new Measurement(Measurements.ROI_WIDTH, crop[2]));
        image.addMeasurement(new Measurement(Measurements.ROI_HEIGHT, crop[3]));
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "Load image into MIA workspace.  This module can be configured to import images from a variety of locations (selected using the \"Import mode\" control).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String importMode = parameters.getValue(IMPORT_MODE, workspace);
        String filePath = parameters.getValue(FILE_PATH, workspace);
        String sequenceRootName = parameters.getValue(SEQUENCE_ROOT_NAME, workspace);
        String nameFormat = parameters.getValue(NAME_FORMAT, workspace);
        String comment = parameters.getValue(COMMENT, workspace);
        String genericFormat = parameters.getValue(GENERIC_FORMAT, workspace);
        String seriesMode = parameters.getValue(SERIES_MODE, workspace);
        String channels = parameters.getValue(CHANNELS, workspace);
        String slices = parameters.getValue(SLICES, workspace);
        String frames = parameters.getValue(FRAMES, workspace);
        String cropMode = parameters.getValue(CROP_MODE, workspace);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
        int left = parameters.getValue(LEFT, workspace);
        int top = parameters.getValue(TOP, workspace);
        int width = parameters.getValue(WIDTH, workspace);
        int height = parameters.getValue(HEIGHT, workspace);
        String objectsForLimitsName = parameters.getValue(OBJECTS_FOR_LIMITS, workspace);
        String scaleMode = parameters.getValue(SCALE_MODE, workspace);
        double scaleFactorX = parameters.getValue(SCALE_FACTOR_X, workspace);
        double scaleFactorY = parameters.getValue(SCALE_FACTOR_Y, workspace);
        String dimensionMismatchMode = parameters.getValue(DIMENSION_MISMATCH_MODE, workspace);
        String padIntensityMode = parameters.getValue(PAD_INTENSITY_MODE, workspace);
        boolean setSpatialCalibration = parameters.getValue(SET_SPATIAL_CAL, workspace);
        double xyCal = parameters.getValue(XY_CAL, workspace);
        double zCal = parameters.getValue(Z_CAL, workspace);
        boolean setTemporalCalibration = parameters.getValue(SET_TEMPORAL_CAL, workspace);
        double frameInterval = parameters.getValue(FRAME_INTERVAL, workspace);
        boolean forceBitDepth = parameters.getValue(FORCE_BIT_DEPTH, workspace);
        String outputBitDepth = parameters.getValue(OUTPUT_BIT_DEPTH, workspace);
        double minIntensity = parameters.getValue(MIN_INPUT_INTENSITY, workspace);
        double maxIntensity = parameters.getValue(MAX_INPUT_INTENSITY, workspace);
        String reader = parameters.getValue(READER, workspace);

        // Series number comes from the Workspace
        int seriesNumber = 1;
        if (reader.equals(Readers.BIOFORMATS))
            switch (seriesMode) {
                case SeriesModes.CURRENT_SERIES:
                    seriesNumber = workspace.getMetadata().getSeriesNumber();
                    break;
                case SeriesModes.SPECIFIC_SERIES:
                    String seriesNumberText = parameters.getValue(SERIES_NUMBER, workspace);
                    Metadata metadata = workspace.getMetadata();
                    seriesNumber = (int) Math
                            .round(Double.parseDouble(metadata.insertMetadataValues(seriesNumberText)));
                    break;
            }

        // ImageJ reader can't use crop
        if (reader.equals(Readers.IMAGEJ))
            cropMode = CropModes.NONE;

        String[] dimRanges = new String[] { channels, slices, frames };

        int[] crop = null;
        switch (cropMode) {
            case CropModes.FIXED:
                crop = new int[] { left, top, width, height };
                break;
            case CropModes.FROM_REFERENCE:
                // Displaying the image
                Image referenceImage = workspace.getImage(referenceImageName);
                crop = getCropROI(referenceImage);
                break;
            case CropModes.OBJECT_COLLECTION_LIMITS:
                Objs objectsForLimits = workspace.getObjects(objectsForLimitsName);
                int[][] limits = objectsForLimits.getSpatialExtents();
                crop = new int[] { limits[0][0], limits[1][0], limits[0][1] - limits[0][0],
                        limits[1][1] - limits[1][0] };
                break;
        }

        if (scaleMode.equals(ScaleModes.NONE)) {
            scaleFactorX = 1;
            scaleFactorY = 1;
        }
        double[] scaleFactors = new double[] { scaleFactorX, scaleFactorY };

        double[] intRange = (forceBitDepth)
                ? new double[] { Double.parseDouble(outputBitDepth), minIntensity, maxIntensity }
                : null;

        boolean[] manualCalibration = new boolean[] { setSpatialCalibration, setTemporalCalibration };

        ImagePlus ipl = null;
        try {
            switch (importMode) {
                case ImportModes.ALL_IN_FOLDER:
                    File file = workspace.getMetadata().getFile();
                    ipl = getAllInFolder(file, crop, scaleFactors, scaleMode, intRange, manualCalibration);
                    break;
                case ImportModes.CURRENT_FILE:
                    file = workspace.getMetadata().getFile();
                    if (file == null) {
                        MIA.log.writeWarning("No input file/folder selected.");
                        return Status.FAIL;
                    }

                    if (!file.exists()) {
                        MIA.log.writeWarning("File \"" + file.getAbsolutePath() + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    switch (reader) {
                        case Readers.BIOFORMATS:
                            ipl = getBFImage(file.getAbsolutePath(), seriesNumber, dimRanges, crop, scaleFactors,
                                    scaleMode,
                                    intRange, manualCalibration, true);
                            break;
                        case Readers.IMAGEJ:
                            ipl = IJ.openImage(file.getAbsolutePath());
                            if (!setSpatialCalibration && ipl.getCalibration().getUnit().equals("pixel"))
                                parseImageJSpatialCalibration(ipl, file.getAbsolutePath());
                            if (!setTemporalCalibration)
                                parseImageJTemporalCalibration(ipl, file.getAbsolutePath());
                            break;
                    }
                    break;

                case ImportModes.IMAGEJ:
                    ipl = IJ.getImage().duplicate();
                    if (ipl == null) {
                        MIA.log.writeWarning("No image open in ImageJ.  Skipping.");
                        return Status.FAIL;
                    }
                    if (!setSpatialCalibration && ipl.getCalibration().getUnit().equals("pixel"))
                        parseImageJSpatialCalibration(ipl, null);
                    if (!setTemporalCalibration)
                        parseImageJTemporalCalibration(ipl, null);
                    break;

                case ImportModes.IMAGE_SEQUENCE_ALPHABETICAL:
                    Metadata metadata = (Metadata) workspace.getMetadata().clone();
                    ipl = getAlphabeticalImageSequence(sequenceRootName, seriesNumber, channels, slices, frames, crop,
                            scaleFactors, scaleMode, dimensionMismatchMode, padIntensityMode, intRange,
                            manualCalibration,
                            metadata);
                    break;

                case ImportModes.IMAGE_SEQUENCE_ZEROS:
                    metadata = (Metadata) workspace.getMetadata().clone();
                    String absolutePath = metadata.insertMetadataValues(sequenceRootName);
                    ipl = getZeroBasedImageSequence(absolutePath, seriesNumber, channels, slices, frames, crop,
                            scaleFactors, scaleMode, dimensionMismatchMode, padIntensityMode, intRange,
                            manualCalibration);

                    break;

                case ImportModes.MATCHING_FORMAT:
                    String path = null;
                    switch (nameFormat) {
                        case NameFormats.HUYGENS:
                            metadata = (Metadata) workspace.getMetadata().clone();
                            metadata.setComment(comment);
                            path = getHuygensPath(metadata);
                            break;

                        case NameFormats.INCUCYTE_SHORT:
                            metadata = (Metadata) workspace.getMetadata().clone();
                            metadata.setComment(comment);
                            path = getIncucyteShortName(metadata);
                            break;

                        case NameFormats.YOKOGAWA:
                            int channel = parameters.getValue(CHANNEL, workspace);
                            path = getYokogawaName(workspace.getMetadata(), channel);
                            break;

                        case NameFormats.GENERIC:
                            metadata = (Metadata) workspace.getMetadata().clone();
                            path = getGenericName(metadata, genericFormat);
                            break;
                    }

                    file = new File(path);

                    if (!file.exists()) {
                        MIA.log.writeWarning("File \"" + file.getAbsolutePath() + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    switch (reader) {
                        case Readers.BIOFORMATS:
                            ipl = getBFImage(file.getAbsolutePath(), seriesNumber, dimRanges, crop, scaleFactors,
                                    scaleMode,
                                    intRange, manualCalibration, true);
                            break;
                        case Readers.IMAGEJ:
                            ipl = IJ.openImage(file.getAbsolutePath());
                            if (!setSpatialCalibration && ipl.getCalibration().getUnit().equals("pixel"))
                                parseImageJSpatialCalibration(ipl, file.getAbsolutePath());
                            if (!setTemporalCalibration)
                                parseImageJTemporalCalibration(ipl, file.getAbsolutePath());
                            break;
                    }

                    break;

                case ImportModes.SPECIFIC_FILE:
                    if (!(new File(filePath)).exists()) {
                        MIA.log.writeWarning("File \"" + filePath + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    switch (reader) {
                        case Readers.BIOFORMATS:
                            ipl = getBFImage(filePath, seriesNumber, dimRanges, crop, scaleFactors, scaleMode, intRange,
                                    manualCalibration, true);
                            break;
                        case Readers.IMAGEJ:
                            ipl = IJ.openImage(filePath);
                            if (!setSpatialCalibration && ipl.getCalibration().getUnit().equals("pixel"))
                                parseImageJSpatialCalibration(ipl, filePath);
                            if (!setTemporalCalibration)
                                parseImageJTemporalCalibration(ipl, filePath);
                            break;
                    }
                    break;
            }
        } catch (SecurityException | DependencyException | IOException | FormatException | ServiceException e) {
            MIA.log.writeWarning(e);
            return Status.FAIL;
        }

        if (ipl == null)
            return Status.FAIL;

        // If necessary, setting the spatial calibration
        if (setSpatialCalibration) {
            writeStatus("Setting spatial calibration (XY = " + xyCal + ", Z = " + zCal +
                    ")");
            Calibration calibration = ipl.getCalibration();

            calibration.pixelHeight = xyCal / scaleFactorX;
            calibration.pixelWidth = xyCal / scaleFactorY;
            calibration.pixelDepth = zCal;
            calibration.setUnit(SpatialUnit.getOMEUnit().getSymbol());

            ipl.setCalibration(calibration);
            ipl.updateChannelAndDraw();

        }

        // If necessary, setting the spatial calibration
        if (setTemporalCalibration) {
            writeStatus("Setting temporal calibration (frame interval = " + frameInterval
                    + ")");
            Calibration calibration = ipl.getCalibration();

            calibration.frameInterval = frameInterval;
            calibration.fps = 1 / TemporalUnit.getOMEUnit().convertValue(frameInterval,
                    UNITS.SECOND);
            calibration.setTimeUnit(TemporalUnit.getOMEUnit().getSymbol());

            ipl.setCalibration(calibration);
            ipl.updateChannelAndDraw();

        }

        // Converting RGB to 3-channel
        if (ipl.getBitDepth() == 24)
            ipl = CompositeConverter.makeComposite(ipl);

        // Adding image to workspace
        writeStatus("Adding image (" + outputImageName + ") to workspace");
        Image outputImage = ImageFactory.createImage(outputImageName, ipl);
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        // If a crop was drawn, recording these coordinates as an image measurement
        switch (cropMode) {
            case CropModes.FROM_REFERENCE:
                addCropMeasurements(outputImage, crop);
                break;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        Preferences preferences = MIA.getPreferences();
        boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(IMPORT_MODE, this, ImportModes.CURRENT_FILE, ImportModes.ALL));
        parameters.add(new ChoiceP(READER, this, Readers.BIOFORMATS, Readers.ALL));
        parameters.add(new StringP(SEQUENCE_ROOT_NAME, this));
        parameters.add(new ChoiceP(NAME_FORMAT, this, NameFormats.GENERIC, NameFormats.ALL));
        parameters.add(new StringP(COMMENT, this));
        parameters.add(new StringP(EXTENSION, this));
        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, Colours.getDarkBlue(darkMode), 130));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER, this, true));
        parameters.add(new FilePathP(FILE_PATH, this));
        parameters.add(new ChoiceP(SERIES_MODE, this, SeriesModes.CURRENT_SERIES, SeriesModes.ALL));
        parameters.add(new StringP(SERIES_NUMBER, this, "1"));
        // parameters.get(SERIES_NUMBER).

        parameters.add(new SeparatorP(RANGE_SEPARATOR, this));
        parameters.add(new StringP(CHANNELS, this, "1-end"));
        parameters.add(new StringP(SLICES, this, "1-end"));
        parameters.add(new StringP(FRAMES, this, "1-end"));
        parameters.add(new IntegerP(CHANNEL, this, 1));

        parameters.add(new SeparatorP(CROP_SEPARATOR, this));
        parameters.add(new ChoiceP(CROP_MODE, this, CropModes.NONE, CropModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new IntegerP(LEFT, this, 0));
        parameters.add(new IntegerP(TOP, this, 0));
        parameters.add(new IntegerP(WIDTH, this, 512));
        parameters.add(new IntegerP(HEIGHT, this, 512));
        parameters.add(new InputObjectsP(OBJECTS_FOR_LIMITS, this));

        parameters.add(new SeparatorP(SCALE_SEPARATOR, this));
        parameters.add(new ChoiceP(SCALE_MODE, this, ScaleModes.NONE, ScaleModes.ALL));
        parameters.add(new DoubleP(SCALE_FACTOR_X, this, 1));
        parameters.add(new DoubleP(SCALE_FACTOR_Y, this, 1));
        parameters.add(new ChoiceP(DIMENSION_MISMATCH_MODE, this, DimensionMismatchModes.DISALLOW,
                DimensionMismatchModes.ALL));
        parameters.add(new ChoiceP(PAD_INTENSITY_MODE, this, PadIntensityModes.BLACK, PadIntensityModes.ALL));

        parameters.add(new SeparatorP(CALIBRATION_SEPARATOR, this));
        parameters.add(new BooleanP(SET_SPATIAL_CAL, this, false));
        parameters.add(new DoubleP(XY_CAL, this, 1d));
        parameters.add(new DoubleP(Z_CAL, this, 1d));
        parameters.add(new BooleanP(SET_TEMPORAL_CAL, this, false));
        parameters.add(new DoubleP(FRAME_INTERVAL, this, 1d));
        parameters.add(new BooleanP(FORCE_BIT_DEPTH, this, false));
        parameters.add(new ChoiceP(OUTPUT_BIT_DEPTH, this, OutputBitDepths.EIGHT, OutputBitDepths.ALL));
        parameters.add(new DoubleP(MIN_INPUT_INTENSITY, this, 0d));
        parameters.add(new DoubleP(MAX_INPUT_INTENSITY, this, 1d));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(IMPORT_MODE));
        switch ((String) parameters.getValue(IMPORT_MODE, workspace)) {
            case ImportModes.ALL_IN_FOLDER:
            case ImportModes.CURRENT_FILE:
            case ImportModes.IMAGEJ:
                break;

            case ImportModes.IMAGE_SEQUENCE_ALPHABETICAL:
            case ImportModes.IMAGE_SEQUENCE_ZEROS:
                returnedParameters.add(parameters.getParameter(SEQUENCE_ROOT_NAME));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                MetadataRefs metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;

            case ImportModes.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(NAME_FORMAT));
                switch ((String) parameters.getValue(NAME_FORMAT, workspace)) {
                    case NameFormats.HUYGENS:
                    case NameFormats.INCUCYTE_SHORT:
                        returnedParameters.add(parameters.getParameter(COMMENT));
                        break;
                    case NameFormats.YOKOGAWA:
                        returnedParameters.add(parameters.getParameter(CHANNEL));
                        break;
                    case NameFormats.GENERIC:
                        returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                        returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                        metadataRefs = modules.getMetadataRefs(this);
                        parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                        break;
                }
                break;

            case ImportModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(FILE_PATH));
                break;
        }

        if (parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.CURRENT_FILE)
                || parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ALPHABETICAL)
                || parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ZEROS)
                || parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.SPECIFIC_FILE)
                || parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.MATCHING_FORMAT)) {
            returnedParameters.add(parameters.getParameter(READER));
        }

        if (parameters.getValue(READER, workspace).equals(Readers.BIOFORMATS)
                & !parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGEJ)) {
            returnedParameters.add(parameters.getParameter(SERIES_MODE));
            if (parameters.getValue(SERIES_MODE, workspace).equals(SeriesModes.SPECIFIC_SERIES))
                returnedParameters.add(parameters.getParameter(SERIES_NUMBER));

            if (!(parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ALPHABETICAL)
                    || parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ZEROS))
                    && !(parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.MATCHING_FORMAT)
                            && parameters.getValue(NAME_FORMAT, workspace).equals(NameFormats.YOKOGAWA))) {
                returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
                returnedParameters.add(parameters.getParameter(CHANNELS));
                returnedParameters.add(parameters.getParameter(SLICES));
                returnedParameters.add(parameters.getParameter(FRAMES));
            }
        }

        if (parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ALPHABETICAL)
                || parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ZEROS)) {
            returnedParameters.add(parameters.getParameter(CHANNELS));
            returnedParameters.add(parameters.getParameter(SLICES));
            returnedParameters.add(parameters.getParameter(FRAMES));
        }

        if (parameters.getValue(READER, workspace).equals(Readers.BIOFORMATS)
                & !parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGEJ)) {
            returnedParameters.add(parameters.getParameter(CROP_SEPARATOR));
            returnedParameters.add(parameters.getParameter(CROP_MODE));
            switch ((String) parameters.getValue(CROP_MODE, workspace)) {
                case CropModes.FIXED:
                    returnedParameters.add(parameters.getParameter(LEFT));
                    returnedParameters.add(parameters.getParameter(TOP));
                    returnedParameters.add(parameters.getParameter(WIDTH));
                    returnedParameters.add(parameters.getParameter(HEIGHT));
                    break;
                case CropModes.FROM_REFERENCE:
                    returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                    break;
                case CropModes.OBJECT_COLLECTION_LIMITS:
                    returnedParameters.add(parameters.getParameter(OBJECTS_FOR_LIMITS));
                    break;
            }

            returnedParameters.add(parameters.getParameter(SCALE_SEPARATOR));
            returnedParameters.add(parameters.getParameter(SCALE_MODE));
            switch ((String) parameters.getValue(SCALE_MODE, workspace)) {
                case ScaleModes.NO_INTERPOLATION:
                case ScaleModes.BILINEAR:
                case ScaleModes.BICUBIC:
                    returnedParameters.add(parameters.getParameter(SCALE_FACTOR_X));
                    returnedParameters.add(parameters.getParameter(SCALE_FACTOR_Y));
                    break;
            }
        }

        if (parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ALPHABETICAL)
                || parameters.getValue(IMPORT_MODE, workspace).equals(ImportModes.IMAGE_SEQUENCE_ZEROS)) {
            returnedParameters.add(parameters.getParameter(DIMENSION_MISMATCH_MODE));

            if (parameters.getValue(DIMENSION_MISMATCH_MODE, workspace).equals(DimensionMismatchModes.CENTRE_PAD))
                returnedParameters.add(parameters.getParameter(PAD_INTENSITY_MODE));
        }

        returnedParameters.add(parameters.getParameter(CALIBRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SET_SPATIAL_CAL));
        if ((boolean) parameters.getValue(SET_SPATIAL_CAL, workspace)) {
            returnedParameters.add(parameters.getParameter(XY_CAL));
            returnedParameters.add(parameters.getParameter(Z_CAL));
        }

        returnedParameters.add(parameters.getParameter(SET_TEMPORAL_CAL));
        if ((boolean) parameters.getValue(SET_TEMPORAL_CAL, workspace))
            returnedParameters.add(parameters.getParameter(FRAME_INTERVAL));

        if (parameters.getValue(READER, workspace).equals(Readers.BIOFORMATS)) {
            returnedParameters.add(parameters.getParameter(FORCE_BIT_DEPTH));
            if ((boolean) parameters.getValue(FORCE_BIT_DEPTH, workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_BIT_DEPTH));
                if (!parameters.getValue(OUTPUT_BIT_DEPTH, workspace).equals(OutputBitDepths.THIRTY_TWO)) {
                    returnedParameters.add(parameters.getParameter(MIN_INPUT_INTENSITY));
                    returnedParameters.add(parameters.getParameter(MAX_INPUT_INTENSITY));
                }
            }
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        Workspace workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        switch ((String) parameters.getValue(CROP_MODE, workspace)) {
            case CropModes.FROM_REFERENCE:
                returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_LEFT).setImageName(outputImageName));
                returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_TOP).setImageName(outputImageName));
                returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_WIDTH).setImageName(outputImageName));
                returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_HEIGHT).setImageName(outputImageName));

                break;
        }

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        boolean valid = true;

        // If using the generic metadata extractor, check the values are available
        if (parameters.getValue(IMPORT_MODE, null).equals(ImportModes.MATCHING_FORMAT)) {
            MetadataRefs metadataRefs = modules.getMetadataRefs(this);

            switch ((String) parameters.getValue(NAME_FORMAT, null)) {
                case NameFormats.GENERIC:
                    String genericFormat = parameters.getValue(GENERIC_FORMAT, null);
                    valid = metadataRefs.hasRef(genericFormat);
                    parameters.getParameter(GENERIC_FORMAT).setValid(valid);
                    break;
            }
        }

        return valid;

    }

    void addParameterDescriptions() {
        parameters.get(OUTPUT_IMAGE).setDescription("Name assigned to the image.");

        parameters.get(IMPORT_MODE).setDescription("Controls where the image will be loaded from:<br><ul>"

                + "<li>\"" + ImportModes.CURRENT_FILE
                + "\" (default option) will import the current root-file for the workspace (this is the file specified in the \""
                + new InputControl(null).getName() + "\" module).</li>"

                + "<li>\"" + ImportModes.IMAGEJ + "\" will load the active image fromm ImageJ.</li>"

                + "<li>\"" + ImportModes.IMAGE_SEQUENCE_ALPHABETICAL
                + "\" will load a series of images matching a specified name format in alphabetical order.  The format of the names to be loaded is specified by the \""
                + SEQUENCE_ROOT_NAME + "\" parameter.</li>"

                + "<li>\"" + ImportModes.IMAGE_SEQUENCE_ZEROS
                + "\" will load a series of images with numbered elements.  The format of the names to be loaded is specified by the \""
                + SEQUENCE_ROOT_NAME + "\" parameter.</li>"

                + "<li>\"" + ImportModes.MATCHING_FORMAT
                + "\" will load the image matching a filename based on the root-file for the workspace and a series of rules.</li>"

                + "<li>\"" + ImportModes.SPECIFIC_FILE + "\" will load the image at the location specified by \""
                + FILE_PATH + "\".</li></ul>");

        parameters.get(READER).setDescription("Set the reader for importing the image:<br><ul>"

                + "<li>\"" + Readers.BIOFORMATS
                + "\" will use the Bio-Formats plugin.  This is best for most cases (especially proprietary formats).</li>"

                + "<li>\"" + Readers.IMAGEJ + "\" will use the stock ImageJ file reader.</li></ul>");

        parameters.get(SERIES_MODE).setDescription(
                "Controls which series should be loaded for multiseries files (e.g. Leica LIF files):<br><ul>"

                        + "<li>\"" + SeriesModes.CURRENT_SERIES
                        + "\" will load the same series as the current root file (i.e. that selected via \""
                        + new InputControl(null).getName() + "\").</li>"

                        + "<li>\"" + SeriesModes.SPECIFIC_SERIES + "\" will load a specific series specified by the \""
                        + SERIES_NUMBER + "\"parameter.</li></ul>");

        parameters.get(SERIES_NUMBER).setDescription("If a specific series is being loaded (\"" + SERIES_MODE
                + "\" set to \"" + SeriesModes.SPECIFIC_SERIES + "\"), this is the series that will be used.");

        parameters.get(SEQUENCE_ROOT_NAME).setDescription(
                "Template filename for loading multiple image sequence files (those with names in the format \"image0001.tif\", \"image0002.tif\", \"image0003.tif\",etc.).  Template filenames are constructed in a generic manner, whereby metadata values stored in the workspace can be inserted into the name using the notation  \"M{name}\".  This allows names to be created dynamically for each analysis run.  The location in the filenam of the variable image number is specified using the \"Z{0000}\" notation, where the number of \"0\" characters specifies the number of digits.  It is also necessary to specify the filepath (input file filepath stored as metadata value \"M{Filepath}\".   <br><br>For example, loading the sequence \"image0001.tif\", etc. from the same folder as the input file would require the format \"M{Filepath}\\\\imageZ{0000}.tif\".  Note: Backslash characters specifying the folder path need to be double typed (standard Java formatting).");

        parameters.get(NAME_FORMAT).setDescription("Method to use for generation of the input filename:<br><ul>"

                + "<li>\"" + NameFormats.GENERIC
                + "\" (default) will generate a name from metadata values stored in the current workspace.</li>"

                + "<li>\"" + NameFormats.HUYGENS
                + "\" will generate a name matching the SVI Huygens format, where channel numbers are specified as \"ch00\", \"ch01\", etc.</li>"

                + "<li>\"" + NameFormats.INCUCYTE_SHORT
                + "\" will generate a name matching the short Incucyte Zoom format.  The root name is specified as the parameter \"Comment\".</li>"

                + "<li>\"" + NameFormats.YOKOGAWA
                + "\" will generate a name matching the Yokogawa high content microscope name format.</li></ul>");

        parameters.get(COMMENT).setDescription(
                "Root name for generation of Incucyte Zoom filenames.  This will be added before the standard well and field values.");

        parameters.get(EXTENSION).setDescription("Extension for the generated filename.");

        parameters.get(GENERIC_FORMAT).setDescription(
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");

        parameters.get(INCLUDE_SERIES_NUMBER).setDescription(
                "Option to include the current series number when compiling filenames.  This may be necessary when working with multi-series files, as there will be multiple analyses completed for the same root file.");

        parameters.get(FILE_PATH).setDescription("Path to file to be loaded.");

        parameters.get(CHANNELS).setDescription(
                "Range of channels to be loaded for the current file.  These can be specified as a comma-separated list, using a range (e.g. \"4-7\" will load channels 4,5,6 and 7) or as a range loading every nth channel (e.g. \"4-10-2\" will load channels 4,6,8 and 10).  The \"end\" keyword will be converted to the total number of available channels at runtime.");

        parameters.get(SLICES).setDescription(
                "Range of slices to be loaded for the current file.  These can be specified as a comma-separated list, using a range (e.g. \"4-7\" will load slices 4,5,6 and 7) or as a range loading every nth slice (e.g. \"4-10-2\" will load slices 4,6,8 and 10).  The \"end\" keyword will be converted to the total number of available slices at runtime.");

        parameters.get(FRAMES).setDescription(
                "Range of frames to be loaded for the current file.  These can be specified as a comma-separated list, using a range (e.g. \"4-7\" will load frames 4,5,6 and 7) or as a range loading every nth frame (e.g. \"4-10-2\" will load frames 4,6,8 and 10).  The \"end\" keyword will be converted to the total number of available frames at runtime.");

        parameters.get(CHANNEL).setDescription("Channel to load when constructing a \"Yokogawa\" format name.");

        parameters.get(CROP_MODE).setDescription("Choice of loading the entire image, or cropping in XY:<br><ul>"

                + "<li>\"" + CropModes.NONE + "\" (default) will load the entire image in XY.</li>"

                + "<li>\"" + CropModes.FIXED
                + "\" will apply a pre-defined crop to the input image based on the parameters \"Left\", \"Top\",\"Width\" and \"Height\".</li>"

                + "<li>\"" + CropModes.FROM_REFERENCE
                + "\" will display a specified image and ask the user to select a region to crop the input image to.</li>"

                + "<li>\"" + CropModes.OBJECT_COLLECTION_LIMITS
                + "\" will crop to the limits of the objects in the collection specified by \"" + OBJECTS_FOR_LIMITS
                + "\".</li></ul>");

        parameters.get(REFERENCE_IMAGE).setDescription(
                "The image to be displayed for selection of the cropping region if the cropping mode is set to \""
                        + CropModes.FROM_REFERENCE + "\".");

        parameters.get(LEFT).setDescription("Left coordinate limit for image cropping (specified in pixel units).");

        parameters.get(TOP).setDescription("Top coordinate limit for image cropping (specified in pixel units).");

        parameters.get(WIDTH).setDescription("Width of the final cropped region (specified in pixel units).");

        parameters.get(HEIGHT).setDescription("Height of the final cropped region (specified in pixel units).");

        parameters.get(OBJECTS_FOR_LIMITS)
                .setDescription("If \"" + CROP_MODE + "\" is set to \"" + CropModes.OBJECT_COLLECTION_LIMITS
                        + "\", this is the object collection that will define the limits of the cropped region.");

        parameters.get(SCALE_MODE).setDescription(
                "Controls if the input image is scaled upon importing.  This only works for scaling in X and Y (magnitudes determined by the \""
                        + SCALE_FACTOR_X + "\" and \"" + SCALE_FACTOR_Y + "\" parameters):<br><ul>"

                        + "<li>\"" + ScaleModes.BICUBIC
                        + "\" Scales the input images using a bicubic filter.  This leads to smooth intensity transitions between interpolated pixels.</li>"

                        + "<li>\"" + ScaleModes.BILINEAR
                        + "\" Scales the input images using a bilinear filter.  This leads to smooth intensity transitions between interpolated pixels.</li>"

                        + "<li>\"" + ScaleModes.NONE
                        + "\" (default) Input images are not scaled.  They are loaded into the workspace at their native resolutions.</li>"

                        + "<li>\"" + ScaleModes.NO_INTERPOLATION
                        + "\" Scales the input images using a nearest-neighbour approach.  This leads to a \"blocky\" apppearance to scaled images.</li></ul>");

        parameters.get(SCALE_FACTOR_X).setDescription("Scale factor applied to X-axis of input images (if \""
                + SCALE_MODE
                + "\" is in an image scaling mode).  Values <1 will reduce image width, while values >1 will increase it.");

        parameters.get(SCALE_FACTOR_Y).setDescription("Scale factor applied to Y-axis of input images (if \""
                + SCALE_MODE
                + "\" is in an image scaling mode).  Values <1 will reduce image height, while values >1 will increase it.");

        parameters.get(DIMENSION_MISMATCH_MODE).setDescription(
                "If loading an image sequence from separate files, this parameter controls how dimensions mismatches between images are handled:<br><ul>"

                        + "<li>\"" + DimensionMismatchModes.CENTRE_CROP
                        + "\" The images will be centred-aligned and cropped to the smallest image dimensions.  This prevents empty borders around some images, but means information is lost from larger images.</li>"

                        + "<li>\"" + DimensionMismatchModes.CENTRE_PAD
                        + "\" The images will be centre-aligned and padded to the largest image dimensions.  This prevents any image infomation being lost at edges, but means smaller images will have borders.</li>"

                        + "<li>\"" + DimensionMismatchModes.DISALLOW
                        + "\" All images must have exactly the same dimensions.  If a dimension mismatch is detected, the image loading process will fail.</li></ul>");

        parameters.get(PAD_INTENSITY_MODE).setDescription("If loading an image series from separate files and \""
                + DIMENSION_MISMATCH_MODE + "\" is set to \"" + DimensionMismatchModes.CENTRE_PAD
                + "\" this parameter will control the intensity of the padding border around images smaller than the largest loaded image.");

        parameters.get(SET_SPATIAL_CAL).setDescription(
                "Option to use the automatically-applied spatial calibration or manually specify these values.");

        parameters.get(XY_CAL).setDescription(
                "Distance per pixel in the XY plane.  Units for this are specified in the main \"Input control\" module.");

        parameters.get(Z_CAL).setDescription(
                "Distance per slice (Z-axis).  Units for this are specified in the main \"Input control\" module.");

        parameters.get(SET_TEMPORAL_CAL).setDescription(
                "Option to use the automatically-applied temporal calibration or manually specify this value.");

        parameters.get(FRAME_INTERVAL).setDescription(
                "Time duration between the start of consecutive frames.  Units for this are specified in the main \"Input control\" module.");

        parameters.get(FORCE_BIT_DEPTH)
                .setDescription("Enable to force the output image to adopt a specific bit-depth.");

        parameters.get(OUTPUT_BIT_DEPTH).setDescription("Output bit depth of the loaded image.");

        parameters.get(MIN_INPUT_INTENSITY).setDescription(
                "Minimum intensity in the input image when in the native bit depth.  This is used for scaling intensities to the desired bit depth.");

        parameters.get(MAX_INPUT_INTENSITY).setDescription(
                "Maximum intensity in the input image when in the native bit depth.  This is used for scaling intensities to the desired bit depth.");

    }
}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]
