// TODO: Load colours from LIF file (if possible).  Similarly, colour Flex files blue, green, red (currently RGB)

package wbif.sjx.MIA.Module.InputOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.CompositeConverter;
import ij.process.ImageProcessor;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.units.unit.Unit;
import ome.xml.meta.IMetadata;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConvertStackToTimeseries;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;
import wbif.sjx.MIA.Process.Logging.Log;
import wbif.sjx.common.MetadataExtractors.CV7000FilenameExtractor;
import wbif.sjx.common.MetadataExtractors.IncuCyteShortFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.NameExtractor;
import wbif.sjx.common.Object.HCMetadata;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack.extendRangeToEnd;


/**
 * Created by Stephen on 15/05/2017.
 */
public class ImageLoader < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String LOADER_SEPARATOR = "Core image loading controls";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String IMPORT_MODE = "Import mode";
    public static final String USE_IMAGEJ_READER = "Use ImageJ reader";
    public static final String NUMBER_OF_ZEROES = "Number of zeroes";
    public static final String STARTING_INDEX = "Starting index";
    public static final String FRAME_INTERVAL = "Frame interval";
    public static final String LIMIT_FRAMES = "Limit frames";
    public static final String FINAL_INDEX = "Final index";
    public static final String NAME_FORMAT = "Name format";
    public static final String COMMENT = "Comment";
    public static final String PREFIX = "Prefix";
    public static final String SUFFIX = "Suffix";
    public static final String EXTENSION = "Extension";
    public static final String GENERIC_FORMAT = "Generic format";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
    public static final String INCLUDE_SERIES_NUMBER = "Include series number";
    public static final String FILE_PATH = "File path";

    public static final String RANGE_SEPARATOR = "Dimension ranges and cropping";
    public static final String CHANNELS = "Channels";
    public static final String SLICES = "Slices";
    public static final String FRAMES = "Frames";
    public static final String CHANNEL = "Channel";
    public static final String THREE_D_MODE = "Load 3D stacks as";
    public static final String CROP_MODE = "Crop mode";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";

    public static final String CALIBRATION_SEPARATOR = "Spatial and intensity calibration";
    public static final String SET_CAL = "Set manual spatial calibration";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";
    public static final String FORCE_BIT_DEPTH = "Force bit depth";
    public static final String OUTPUT_BIT_DEPTH = "Output bit depth";
    public static final String MIN_INPUT_INTENSITY = "Minimum input intensity";
    public static final String MAX_INPUT_INTENSITY = "Maximum input intensity";

    public ImageLoader(ModuleCollection modules) {
        super("Load image",modules);
    }


    public interface ImportModes {
        String CURRENT_FILE = "Current file";
        String IMAGEJ = "From ImageJ";
        String IMAGE_SEQUENCE = "Image sequence";
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[]{CURRENT_FILE, IMAGEJ, IMAGE_SEQUENCE, MATCHING_FORMAT, SPECIFIC_FILE};

    }

    public interface ThreeDModes {
        String TIMESERIES = "Timeseries";
        String ZSTACK = "Z-Stack";

        String[] ALL = new String[]{TIMESERIES,ZSTACK};

    }

    public interface NameFormats {
        String GENERIC = "Generic (from metadata)";
        String HUYGENS = "Huygens";
        String INCUCYTE_SHORT = "Incucyte short filename";
        String YOKOGAWA = "Yokogowa";
        String INPUT_FILE_PREFIX = "Input filename with prefix";
        String INPUT_FILE_SUFFIX = "Input filename with suffix";

        String[] ALL = new String[]{GENERIC, HUYGENS,INCUCYTE_SHORT,YOKOGAWA,INPUT_FILE_PREFIX,INPUT_FILE_SUFFIX};

    }

    public interface CropModes {
        String NONE = "None";
        String FIXED = "Fixed";
        String FROM_REFERENCE = "From reference";

        String[] ALL = new String[]{NONE,FIXED,FROM_REFERENCE};

    }

    public interface OutputBitDepths {
        String EIGHT = "8";
        String SIXTEEN = "16";
        String THIRTY_TWO = "32";

        String[] ALL = new String[]{EIGHT,SIXTEEN,THIRTY_TWO};

    }


    public interface Measurements {
        String ROI_LEFT = "IMAGE_LOADING // ROI_LEFT (PX)";
        String ROI_TOP = "IMAGE_LOADING // ROI_TOP (PX)";
        String ROI_WIDTH = "IMAGE_LOADING // ROI_WIDTH (PX)";
        String ROI_HEIGHT = "IMAGE_LOADING // ROI_HEIGHT (PX)";

    }


    public ImagePlus getBFImage(String path, int seriesNumber, @Nonnull String[] dimRanges, @Nullable int[] crop, @Nullable double[] intRange, boolean manualCal, boolean localVerbose)
            throws ServiceException, DependencyException, IOException, FormatException {
        DebugTools.enableLogging("off");
        DebugTools.setRootLevel("off");

        // Setting spatial calibration
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        IMetadata meta = service.createOMEXMLMetadata();
        ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
        reader.setMetadataStore((MetadataStore) meta);

        reader.setGroupFiles(false);
        reader.setId(path);
        reader.setSeries(seriesNumber-1);

        int left = 0;
        int top = 0;
        int width = reader.getSizeX();
        int height = reader.getSizeY();

        int sizeC = reader.getSizeC();
        int sizeT = reader.getSizeT();
        int sizeZ = reader.getSizeZ();
        int bitDepth = reader.getBitsPerPixel();

        // If a specific bit depth is to be used
        if (intRange != null) {
            bitDepth = (int) intRange[0];
        }

        if (crop != null) {
            left = crop[0];
            top = crop[1];
            width = crop[2];
            height = crop[3];
        }

        int[] channelsList = CommaSeparatedStringInterpreter.interpretIntegers(dimRanges[0],true);
        if (channelsList[channelsList.length-1] == Integer.MAX_VALUE) channelsList = extendRangeToEnd(channelsList,sizeC);
        int[] slicesList = CommaSeparatedStringInterpreter.interpretIntegers(dimRanges[1],true);
        if (slicesList[slicesList.length-1] == Integer.MAX_VALUE) slicesList = extendRangeToEnd(slicesList,sizeZ);
        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(dimRanges[2],true);
        if (framesList[framesList.length-1] == Integer.MAX_VALUE) framesList = extendRangeToEnd(framesList,sizeT);

        int nC = channelsList.length;
        int nZ = slicesList.length;
        int nT = framesList.length;

        // Creating the new ImagePlus
        ImagePlus ipl = IJ.createHyperStack("Image", width, height,nC,nZ,nT,bitDepth);

        // Iterating over all images in the stack, adding them to the output ImagePlus
        int nTotal = nC*nT*nZ;
        int count = 0;
        int countZ = 1;

        for (int z:slicesList) {
            int countC = 1;
            for (int c:channelsList) {
                int countT = 1;
                for (int t:framesList) {
                    int idx;
                    try {
                        idx = reader.getIndex(z - 1, c - 1, t - 1);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Indices out of range for image \""+path+"\" at c="+(c-1)+", z="+(z-1)+", t="+(t-1));
                        return null;
                    }

                    ImageProcessor ip = reader.openProcessors(idx,left,top,width,height)[0];

                    // If forcing bit depth
                    if (intRange != null) {
                        ip.setMinAndMax(intRange[1],intRange[2]);
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

                    ipl.setPosition(countC,countZ,countT);
                    ipl.setProcessor(ip);

                    if (localVerbose) writeMessage("Loaded image "+(++count)+" of "+nTotal);

                    countT++;
                }
                countC++;
            }
            countZ++;
        }

        ipl.setPosition(1, 1, 1);

        Unit<Length> unit = Units.getOMEUnits();

        // Add spatial calibration
        if (meta != null &! manualCal) {
            if (meta.getPixelsPhysicalSizeX(seriesNumber-1) != null) {
                Length physicalSizeX = meta.getPixelsPhysicalSizeX(seriesNumber-1);
                if (!unit.isConvertible(physicalSizeX.unit())) {
                    MIA.log.write("Can't convert units for file \"" + new File(path).getName() + "\".  Spatially calibrated values may be wrong",Log.Level.WARNING);
                    reader.close();
                    return ipl;
                }
                ipl.getCalibration().pixelWidth = (double) physicalSizeX.value(unit);
                ipl.getCalibration().setXUnit(unit.getSymbol());
            } else {
                MIA.log.write("Can't interpret units for file \""+new File(path).getName()+"\".  Spatial calibration set to pixel units.",Log.Level.WARNING);
                ipl.getCalibration().pixelWidth = 1.0;
                ipl.getCalibration().setXUnit("px");
            }

            if (meta.getPixelsPhysicalSizeY(seriesNumber-1) != null) {
                Length physicalSizeY = meta.getPixelsPhysicalSizeY(seriesNumber-1);
                ipl.getCalibration().pixelHeight = (double) physicalSizeY.value(unit);
                ipl.getCalibration().setYUnit(unit.getSymbol());
            } else {
                ipl.getCalibration().pixelHeight = 1.0;
                ipl.getCalibration().setYUnit("px");
            }

            if (ipl.getNSlices() > 1 && meta.getPixelsPhysicalSizeZ(seriesNumber-1) != null) {
                Length physicalSizeZ = meta.getPixelsPhysicalSizeZ(seriesNumber-1);
                ipl.getCalibration().pixelDepth = (double) physicalSizeZ.value(unit);
                ipl.getCalibration().setZUnit(unit.getSymbol());
            } else {
                ipl.getCalibration().pixelDepth = 1.0;
                ipl.getCalibration().setZUnit("px");
            }
        } else if (!manualCal) {
            MIA.log.write("Can't interpret units for file \""+new File(path).getName()+"\".  Spatially calibrated values may be wrong",Log.Level.WARNING);
        }

        reader.close();

        return ipl;

    }

    public ImagePlus getImageSequence(File rootFile, int numberOfZeroes, int startingIndex, int frameInterval, int finalIndex, int[] crop, @Nullable double[] intRange, boolean manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {
        // Number format
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<numberOfZeroes;i++) stringBuilder.append("0");
        DecimalFormat df = new DecimalFormat(stringBuilder.toString());

        // Getting fragments of the filepath
        String rootPath = rootFile.getParent()+MIA.getSlashes();
        String rootName = rootFile.getName();
        String startingNumber = df.format(startingIndex);
        int numStart = FilenameUtils.removeExtension(rootName).length()-numberOfZeroes;
        rootName = rootFile.getName().substring(0,numStart);
        String extension = FilenameUtils.getExtension(rootFile.getName());

        // Determining the number of images to load
        int count = 0;
        int idx = startingIndex;
        while (new File(rootPath+rootName+df.format(idx)+"."+extension).exists()){
            if (idx > finalIndex) break;
            count++;
            idx = idx + frameInterval;
        }

        // Determining the dimensions of the input image
        String[] dimRanges = new String[]{"1","1","1"};
        ImagePlus rootIpl = getBFImage(rootFile.getAbsolutePath(),1,dimRanges,crop,intRange,manualCal,false);
        int width = rootIpl.getWidth();
        int height = rootIpl.getHeight();
        int bitDepth = rootIpl.getBitDepth();

        if (crop != null) {
            width = crop[2];
            height = crop[3];
        }

        // Creating the new image
        ImagePlus outputIpl = IJ.createImage("Image",width,height,count,bitDepth);
        for (int i = 0;i<count;i++) {
            writeMessage("Loading image "+(i+1)+" of "+count);
            String currentPath = rootPath+rootName+df.format(i*frameInterval+startingIndex)+"."+extension;
            ImagePlus tempIpl = getBFImage(currentPath,1,dimRanges,crop,intRange,manualCal,false);

            outputIpl.setPosition(i+1);
            outputIpl.setProcessor(tempIpl.getProcessor());

        }

        outputIpl.setPosition(1);

        outputIpl.setCalibration(rootIpl.getCalibration());

        return outputIpl;

    }

    public ImagePlus getHuygensImage(HCMetadata metadata, String[] dimRanges, int[] crop, @Nullable double[] intRange, boolean manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String extension = FilenameUtils.getExtension(absolutePath);

        // The name will end with "_chxx" where "xx" is a two-digit number
        Pattern pattern = Pattern.compile("(.+)_ch([0-9]{2})");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            String comment = metadata.getComment();
            String filename = path+matcher.group(1)+"_ch"+comment+"."+extension;

            if (!new File(filename).exists()) {
                MIA.log.write("File \""+filename+"\" not found.  Skipping file.",Log.Level.WARNING);
                return null;
            }

            return getBFImage(filename,1,dimRanges,crop,intRange,manualCal,true);

        }

        return null;

    }

    private ImagePlus getIncucyteShortNameImage(HCMetadata metadata, int seriesNumber, String[] dimRanges, int[] crop, @Nullable double[] intRange, boolean manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {
        // First, running metadata extraction on the input file
        NameExtractor filenameExtractor = new IncuCyteShortFilenameExtractor();
        filenameExtractor.extract(metadata, metadata.getFile().getName());

        // Constructing a new name using the same name format
        String comment = metadata.getComment();
        String filename = metadata.getFile().getParent()+MIA.getSlashes()+IncuCyteShortFilenameExtractor
                .generate(comment,metadata.getWell(),metadata.getAsString(HCMetadata.FIELD),metadata.getExt());

        if (!new File(filename).exists()) {
            MIA.log.write("File \""+filename+"\" not found.  Skipping file.",Log.Level.WARNING);
            return null;
        }

        return getBFImage(filename,seriesNumber,dimRanges,crop,intRange,manualCal,true);

    }

    private ImagePlus getYokogawaNameImage(File templateFile, int seriesNumber, int[] crop, @Nullable double[] intRange, boolean manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {
        // Creating metadata object
        HCMetadata metadata = new HCMetadata();

        // First, running metadata extraction on the input file
        CV7000FilenameExtractor extractor = new CV7000FilenameExtractor();
        extractor.setToUseActionWildcard(true);
        extractor.extract(metadata,templateFile.getName());

        // Constructing a new name using the same name format
        metadata.setChannel(parameters.getValue(CHANNEL));
        final String filename = extractor.construct(metadata);

        // Running through files in this folder to find the one matching the pattern
        File parentFile = templateFile.getParentFile();
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.compile(filename).matcher(name).find();
            }
        };

        File[] listOfFiles = parentFile.listFiles(filter);
        String[] dimRanges = new String[]{"1","1","1"};

        if (!new File(listOfFiles[0].getAbsolutePath()).exists()) {
            MIA.log.write("File \""+listOfFiles[0].getAbsolutePath()+"\" not found.  Skipping file.",Log.Level.WARNING);
            return null;
        }

        return getBFImage(listOfFiles[0].getAbsolutePath(),seriesNumber,dimRanges,crop,intRange,manualCal,true);

    }

    private ImagePlus getPrefixNameImage(HCMetadata metadata, int seriesNumber, String[] dimRanges, int[] crop, @Nullable double[] intRange, boolean includeSeries, String ext, boolean manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String series = includeSeries ? "_S"+metadata.getSeriesNumber() : "";
        String filename = path+comment+name+series+"."+ext;

        if (!new File(filename).exists()) {
            MIA.log.write("File \""+filename+"\" not found.  Skipping file.",Log.Level.WARNING);
            return null;
        }

        return getBFImage(filename,seriesNumber,dimRanges,crop,intRange,manualCal,true);

    }

    private ImagePlus getSuffixNameImage(HCMetadata metadata, int seriesNumber, String[] dimRanges, int[] crop, @Nullable double[] intRange, boolean includeSeries, String ext, boolean manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String series = includeSeries ? "_S"+metadata.getSeriesNumber() : "";
        String filename = path+name+series+comment+"."+ext;

        if (!new File(filename).exists()) {
            MIA.log.write("File \""+filename+"\" not found.  Skipping file.",Log.Level.WARNING);
            return null;
        }

        return getBFImage(filename,seriesNumber,dimRanges,crop,intRange,manualCal,true);

    }

    private ImagePlus getGenericNameImage(HCMetadata metadata, int seriesNumber, String[] dimRanges, int[] crop, @Nullable double[] intRange, String genericFormat, boolean manualCal)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String filename = compileGenericFilename(genericFormat, metadata);
        filename = path + filename;

        return getBFImage(filename,seriesNumber,dimRanges,crop,intRange,manualCal,true);

    }

    private void addCropMeasurements(Image image, int[] crop) {
        image.addMeasurement(new Measurement(Measurements.ROI_LEFT,crop[0]));
        image.addMeasurement(new Measurement(Measurements.ROI_TOP,crop[1]));
        image.addMeasurement(new Measurement(Measurements.ROI_WIDTH,crop[2]));
        image.addMeasurement(new Measurement(Measurements.ROI_HEIGHT,crop[3]));
    }

    private void addCropMeasurements(ObjCollection objects, int[] crop) {
        for (Obj obj:objects.values()) {
            obj.addMeasurement(new Measurement(Measurements.ROI_LEFT, crop[0]));
            obj.addMeasurement(new Measurement(Measurements.ROI_TOP, crop[1]));
            obj.addMeasurement(new Measurement(Measurements.ROI_WIDTH, crop[2]));
            obj.addMeasurement(new Measurement(Measurements.ROI_HEIGHT, crop[3]));
        }
    }

    public static String getMetadataValues(MetadataRefCollection metadataRefs) {
        StringBuilder sb = new StringBuilder();

        sb.append("The following metadata values are available to use for generation of a filename string.  " +
                "Each metadata reference should include the \"${\" and \"}\".\r\n\r\n");

        for (MetadataRef ref:metadataRefs.values()) {
            sb.append("${");
            sb.append(ref.getName());
            sb.append("}");
            sb.append("\r\n");
        }

        return sb.toString();

    }

    public static String compileGenericFilename(String genericFormat, HCMetadata metadata) {
        String outputName = genericFormat;

        // Use regex to find instances of "${ }" and replace the contents with the appropriate metadata value
        Pattern pattern = Pattern.compile("\\$\\{([^${}]+)}");
        Matcher matcher = pattern.matcher(genericFormat);
        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);
            String metadataValue = metadata.getAsString(metadataName);

            outputName = outputName.replace(fullName,metadataValue);

        }

        return outputName;

    }


    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "Load image into MIA workspace.  This module can be configured to import images from a variety of locations (selected using the \"Import mode\" control).";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String importMode = parameters.getValue(IMPORT_MODE);
        String filePath = parameters.getValue(FILE_PATH);
        int numberOfZeroes = parameters.getValue(NUMBER_OF_ZEROES);
        int startingIndex = parameters.getValue(STARTING_INDEX);
        int frameInterval = parameters.getValue(FRAME_INTERVAL);
        boolean limitFrames = parameters.getValue(LIMIT_FRAMES);
        int finalIndex = parameters.getValue(FINAL_INDEX);
        String nameFormat = parameters.getValue(NAME_FORMAT);
        String comment = parameters.getValue(COMMENT);
        String prefix = parameters.getValue(PREFIX);
        String suffix = parameters.getValue(SUFFIX);
        String ext = parameters.getValue(EXTENSION);
        String genericFormat = parameters.getValue(GENERIC_FORMAT);
        boolean includeSeriesNumber = parameters.getValue(INCLUDE_SERIES_NUMBER);
        String channels = parameters.getValue(CHANNELS);
        String slices = parameters.getValue(SLICES);
        String frames = parameters.getValue(FRAMES);
        int channel = parameters.getValue(CHANNEL);
        String cropMode = parameters.getValue(CROP_MODE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        int left = parameters.getValue(LEFT);
        int top = parameters.getValue(TOP);
        int width = parameters.getValue(WIDTH);
        int height = parameters.getValue(HEIGHT);
        boolean setCalibration = parameters.getValue(SET_CAL);
        double xyCal = parameters.getValue(XY_CAL);
        double zCal = parameters.getValue(Z_CAL);
        boolean forceBitDepth = parameters.getValue(FORCE_BIT_DEPTH);
        String outputBitDepth = parameters.getValue(OUTPUT_BIT_DEPTH);
        double minIntensity = parameters.getValue(MIN_INPUT_INTENSITY);
        double maxIntensity = parameters.getValue(MAX_INPUT_INTENSITY);
        boolean useImageJReader = parameters.getValue(USE_IMAGEJ_READER);
        String threeDMode = parameters.getValue(THREE_D_MODE);

        // Series number comes from the Workspace
        int seriesNumber = workspace.getMetadata().getSeriesNumber();

        // ImageJ reader can't use crop
        if (useImageJReader) cropMode = CropModes.NONE;

        String[] dimRanges = new String[]{channels,slices,frames};

        int[] crop = null;
        switch (cropMode) {
            case CropModes.FIXED:
                crop = new int[]{left,top,width,height};
                break;
            case CropModes.FROM_REFERENCE:
                // Displaying the image
                Image referenceImage = workspace.getImage(referenceImageName);
                ImagePlus referenceIpl = referenceImage.getImagePlus().duplicate();
                referenceIpl.show();

                // Asking the user to draw a rectangular ROI
                IJ.runMacro("waitForUser(getArgument())","Click \"OK\" once ROI selected");

                // Getting the ROI
                Roi roi = referenceIpl.getRoi();
                Rectangle bounds = roi.getBounds();
                crop = new int[]{bounds.x,bounds.y,bounds.width,bounds.height};

                // Closing the reference image
                referenceIpl.close();
                break;

        }

        double[] intRange = (forceBitDepth) ? new double[]{Double.parseDouble(outputBitDepth),minIntensity,maxIntensity} : null;

        ImagePlus ipl = null;
        try {
            switch (importMode) {
                case ImportModes.CURRENT_FILE:
                    File file = workspace.getMetadata().getFile();
                    if (file == null) {
                        MIA.log.write("No input file/folder selected.",Log.Level.WARNING);
                        return false;
                    }

                    if (!file.exists()) {
                        MIA.log.write("File \""+file.getAbsolutePath()+"\" not found.  Skipping file.",Log.Level.WARNING);
                        return false;
                    }

                    if (useImageJReader) {
                        ipl = IJ.openImage(file.getAbsolutePath());
                    } else {
                        ipl = getBFImage(file.getAbsolutePath(), seriesNumber, dimRanges, crop, intRange, setCalibration, true);
                    }
                    break;

                case ImportModes.IMAGEJ:
                    ipl = IJ.getImage().duplicate();
                    if (ipl == null) {
                        MIA.log.write("No image open in ImageJ.  Skipping.",Log.Level.WARNING);
                        return false;
                    }
                    break;

                case ImportModes.IMAGE_SEQUENCE:
                    if (!limitFrames) finalIndex = Integer.MAX_VALUE;
                    file = workspace.getMetadata().getFile();
                    if (!file.exists()) {
                        MIA.log.write("File \""+file.getAbsolutePath()+"\" not found.  Skipping file.",Log.Level.WARNING);
                        return false;
                    }

                    ipl = getImageSequence(file,numberOfZeroes,startingIndex,frameInterval,finalIndex,crop,intRange,setCalibration);

                    break;

                case ImportModes.MATCHING_FORMAT:
                    switch (nameFormat) {
                        case NameFormats.HUYGENS:
                            HCMetadata metadata = (HCMetadata) workspace.getMetadata().clone();
                            metadata.setComment(comment);
                            ipl = getHuygensImage(metadata,dimRanges,crop,intRange,setCalibration);
                            break;

                        case NameFormats.INCUCYTE_SHORT:
                            metadata = (HCMetadata) workspace.getMetadata().clone();
                            metadata.setComment(comment);
                            ipl = getIncucyteShortNameImage(metadata,seriesNumber,dimRanges,crop,intRange,setCalibration);
                            break;

                        case NameFormats.YOKOGAWA:
                            ipl = getYokogawaNameImage(workspace.getMetadata().getFile(),seriesNumber,crop,intRange,setCalibration);
                            break;

                        case NameFormats.INPUT_FILE_PREFIX:
                            metadata = (HCMetadata) workspace.getMetadata().clone();
                            metadata.setComment(prefix);
                            ipl = getPrefixNameImage(metadata,seriesNumber,dimRanges,crop,intRange,includeSeriesNumber,ext,setCalibration);
                            break;

                        case NameFormats.INPUT_FILE_SUFFIX:
                            metadata = (HCMetadata) workspace.getMetadata().clone();
                            metadata.setComment(suffix);
                            ipl = getSuffixNameImage(metadata,seriesNumber,dimRanges,crop,intRange,includeSeriesNumber,ext,setCalibration);
                            break;

                        case NameFormats.GENERIC:
                            metadata = (HCMetadata) workspace.getMetadata().clone();
                            ipl = getGenericNameImage(metadata,seriesNumber,dimRanges,crop,intRange,genericFormat,setCalibration);
                            break;
                    }
                    break;

                case ImportModes.SPECIFIC_FILE:
                    if (!(new File(filePath)).exists()) {
                        MIA.log.write("File \""+filePath+"\" not found.  Skipping file.",Log.Level.WARNING);
                        return false;
                    }

                    if (useImageJReader) {
                        ipl = IJ.openImage(filePath);
                    } else {
                        ipl = getBFImage(filePath, 1, dimRanges, crop, intRange,setCalibration, true);
                    }
                    break;
            }
        } catch (ServiceException | DependencyException | IOException | FormatException e) {
            e.printStackTrace();
        }

        if (ipl == null) return false;

        // If necessary, setting the spatial calibration
        if (setCalibration) {
            writeMessage("Setting spatial calibration (XY = "+xyCal+", Z = "+zCal+")");
            Calibration calibration = new Calibration();

            calibration.pixelHeight = xyCal;
            calibration.pixelWidth = xyCal;
            calibration.pixelDepth = zCal;
            calibration.setUnit(Units.getOMEUnits().getSymbol());

            ipl.setCalibration(calibration);
            ipl.updateChannelAndDraw();

        }

        // Converting RGB to 3-channel
        boolean toConvert = ipl.getBitDepth() == 24;
        if (toConvert) ipl = CompositeConverter.makeComposite(ipl);

        // If either number of slices or timepoints is 1 check it's the right dimension
        if (threeDMode.equals(ThreeDModes.TIMESERIES) && ((ipl.getNFrames() == 1 && ipl.getNSlices() > 1) || (ipl.getNSlices() == 1 && ipl.getNFrames() > 1) )) {
            ConvertStackToTimeseries.process(ipl);
            ipl.getCalibration().pixelDepth = 1;
        }

        // Adding image to workspace
        writeMessage("Adding image (" + outputImageName + ") to workspace");
        Image outputImage = new Image(outputImageName, ipl);
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        // If a crop was drawn, recording these coordinates as an image measurement
        switch (cropMode) {
            case CropModes.FROM_REFERENCE:
                addCropMeasurements(outputImage, crop);
                break;
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(LOADER_SEPARATOR,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name assigned to the image."));
        parameters.add(new ChoiceP(IMPORT_MODE, this,ImportModes.CURRENT_FILE,ImportModes.ALL,"File reader mode to use." +
                "<br>- \"Current file\" (default option) will import the current root-file for the workspace." +
                "<br>- \"From ImageJ\" will load the active image fromm ImageJ." +
                "<br>- \"Image sequence\" will use the root-file for the workspace as the basis for loading a series of images with numbered suffixes.  " +
                "<br>- \"Matching format\" will load the image matching a filename based on the root-file for the workspace and a series of rules.  " +
                "<br>- \"Specific file\" will load the image at a specific location."));
        parameters.add(new BooleanP(USE_IMAGEJ_READER, this,false,"Use the stock ImageJ file reader, rather than the default BioFormats reader."));
        parameters.add(new IntegerP(NUMBER_OF_ZEROES,this,4,"Number of digits in image sequence suffix."));
        parameters.add(new IntegerP(STARTING_INDEX,this,0,"First number in sequence to load."));
        parameters.add(new IntegerP(FRAME_INTERVAL,this,1,"Frame interval to use for loading."));
        parameters.add(new BooleanP(LIMIT_FRAMES,this,false,"When \"true\" this will load a pre-determined number of frames.  When \"false\" it will load all the available images."));
        parameters.add(new IntegerP(FINAL_INDEX,this,1,"Final number in sequence to load."));
        parameters.add(new ChoiceP(NAME_FORMAT,this,NameFormats.GENERIC,NameFormats.ALL));
        parameters.add(new StringP(COMMENT,this));
        parameters.add(new StringP(PREFIX,this));
        parameters.add(new StringP(SUFFIX,this));
        parameters.add(new StringP(EXTENSION,this));
        parameters.add(new StringP(GENERIC_FORMAT,this));
        parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS,this,false));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER,this,true));
        parameters.add(new FilePathP(FILE_PATH, this));

        parameters.add(new ParamSeparatorP(RANGE_SEPARATOR,this));
        parameters.add(new StringP(CHANNELS,this,"1-end"));
        parameters.add(new StringP(SLICES,this,"1-end"));
        parameters.add(new StringP(FRAMES,this,"1-end"));
        parameters.add(new IntegerP(CHANNEL,this,1));
        parameters.add(new ChoiceP(THREE_D_MODE,this,ThreeDModes.ZSTACK,ThreeDModes.ALL));
        parameters.add(new ChoiceP(CROP_MODE,this,CropModes.NONE,CropModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE,this));
        parameters.add(new IntegerP(LEFT, this,0));
        parameters.add(new IntegerP(TOP, this,0));
        parameters.add(new IntegerP(WIDTH, this,512));
        parameters.add(new IntegerP(HEIGHT, this,512));

        parameters.add(new ParamSeparatorP(CALIBRATION_SEPARATOR,this));
        parameters.add(new BooleanP(SET_CAL, this, false));
        parameters.add(new DoubleP(XY_CAL, this, 1d));
        parameters.add(new DoubleP(Z_CAL, this, 1d));
        parameters.add(new BooleanP(FORCE_BIT_DEPTH, this, false));
        parameters.add(new ChoiceP(OUTPUT_BIT_DEPTH,this,OutputBitDepths.EIGHT,OutputBitDepths.ALL));
        parameters.add(new DoubleP(MIN_INPUT_INTENSITY, this, 0d));
        parameters.add(new DoubleP(MAX_INPUT_INTENSITY, this, 1d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(IMPORT_MODE));
        switch((String) parameters.getValue(IMPORT_MODE)) {
            case ImportModes.CURRENT_FILE:
            case ImportModes.IMAGEJ:
                break;

            case ImportModes.IMAGE_SEQUENCE:
                returnedParameters.add(parameters.getParameter(NUMBER_OF_ZEROES));
                returnedParameters.add(parameters.getParameter(STARTING_INDEX));
                returnedParameters.add(parameters.getParameter(FRAME_INTERVAL));
                returnedParameters.add(parameters.getParameter(LIMIT_FRAMES));
                if (parameters.getValue(LIMIT_FRAMES)) {
                    returnedParameters.add(parameters.getParameter(FINAL_INDEX));
                }
                break;

            case ImportModes.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(NAME_FORMAT));
                switch ((String) parameters.getValue(NAME_FORMAT)) {
                    case NameFormats.HUYGENS:
                    case NameFormats.INCUCYTE_SHORT:
                        returnedParameters.add(parameters.getParameter(COMMENT));
                        break;
                    case NameFormats.YOKOGAWA:
                        returnedParameters.add(parameters.getParameter(CHANNEL));
                        break;
                    case NameFormats.INPUT_FILE_PREFIX:
                        returnedParameters.add(parameters.getParameter(PREFIX));
                        returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
                        returnedParameters.add(parameters.getParameter(EXTENSION));
                        break;
                    case NameFormats.INPUT_FILE_SUFFIX:
                        returnedParameters.add(parameters.getParameter(SUFFIX));
                        returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
                        returnedParameters.add(parameters.getParameter(EXTENSION));
                        break;
                    case NameFormats.GENERIC:
                        returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                        returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                        MetadataRefCollection metadataRefs = modules.getMetadataRefs(this);
                        parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(getMetadataValues(metadataRefs));
                        break;
                }
                break;

            case ImportModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(FILE_PATH));
                break;
        }

        if (parameters.getValue(IMPORT_MODE).equals(ImportModes.CURRENT_FILE)
                || parameters.getValue(IMPORT_MODE).equals(ImportModes.SPECIFIC_FILE)) {
            returnedParameters.add(parameters.getParameter(USE_IMAGEJ_READER));
        }

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        if (!((boolean) parameters.getValue(USE_IMAGEJ_READER))) {
            if (!parameters.getValue(IMPORT_MODE).equals(ImportModes.IMAGE_SEQUENCE) &&
                    !(parameters.getValue(IMPORT_MODE).equals(ImportModes.MATCHING_FORMAT)
                            && parameters.getValue(NAME_FORMAT).equals(NameFormats.YOKOGAWA))) {
                returnedParameters.add(parameters.getParameter(CHANNELS));
                returnedParameters.add(parameters.getParameter(SLICES));
                returnedParameters.add(parameters.getParameter(FRAMES));
            }
        }

        returnedParameters.add(parameters.getParameter(THREE_D_MODE));

        if (!((boolean) parameters.getValue(USE_IMAGEJ_READER))) {
            returnedParameters.add(parameters.getParameter(CROP_MODE));
            switch ((String) parameters.getValue(CROP_MODE)) {
                case CropModes.FIXED:
                    returnedParameters.add(parameters.getParameter(LEFT));
                    returnedParameters.add(parameters.getParameter(TOP));
                    returnedParameters.add(parameters.getParameter(WIDTH));
                    returnedParameters.add(parameters.getParameter(HEIGHT));
                    break;
                case CropModes.FROM_REFERENCE:
                    returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                    break;
            }
        }

        returnedParameters.add(parameters.getParameter(CALIBRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SET_CAL));
        if (parameters.getValue(SET_CAL)) {
            returnedParameters.add(parameters.getParameter(XY_CAL));
            returnedParameters.add(parameters.getParameter(Z_CAL));
        }

        if (!((boolean) parameters.getValue(USE_IMAGEJ_READER))) {
            returnedParameters.add(parameters.getParameter(FORCE_BIT_DEPTH));
            if (parameters.getValue(FORCE_BIT_DEPTH)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_BIT_DEPTH));
                if (!parameters.getValue(OUTPUT_BIT_DEPTH).equals(OutputBitDepths.THIRTY_TWO)) {
                    returnedParameters.add(parameters.getParameter(MIN_INPUT_INTENSITY));
                    returnedParameters.add(parameters.getParameter(MAX_INPUT_INTENSITY));
                }
            }
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        switch ((String) parameters.getValue(CROP_MODE)) {
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
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]