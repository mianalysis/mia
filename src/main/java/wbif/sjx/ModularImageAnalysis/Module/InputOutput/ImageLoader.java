// TODO: Load colours from LIF file (if possible).  Similarly, colour Flex files blue, green, red (currently RGB)

package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import fiji.stacks.Hyperstack_rearranger;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.measure.Calibration;
import ij.plugin.CompositeConverter;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.Calibrator;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.*;
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
import org.janelia.it.h5j.fiji.adapter.FijiAdapter;
import org.janelia.it.jacs.shared.ffmpeg.FFMpegLoader;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.ConvertStackToTimeseries;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.MetadataExtractors.CV7000FilenameExtractor;
import wbif.sjx.common.MetadataExtractors.IncuCyteShortFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.NameExtractor;
import wbif.sjx.common.Object.HCMetadata;

import javax.annotation.Nullable;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.ExtractSubstack.extendRangeToEnd;
import static wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.ExtractSubstack.interpretRange;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class ImageLoader < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String OUTPUT_MODE = "Output mode";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String IMPORT_MODE = "Import mode";
    public static final String NUMBER_OF_ZEROES = "Number of zeroes";
    public static final String STARTING_INDEX = "Starting index";
    public static final String LIMIT_FRAMES = "Limit frames";
    public static final String FINAL_INDEX = "Final index";
    public static final String NAME_FORMAT = "Name format";
    public static final String COMMENT = "Comment";
    public static final String PREFIX = "Prefix";
    public static final String SUFFIX = "Suffix";
    public static final String FILE_PATH = "File path";
    public static final String CHANNELS = "Channels";
    public static final String SLICES = "Slices";
    public static final String FRAMES = "Frames";
    public static final String CHANNEL = "Channel";
    public static final String CROP_IMAGE = "Crop image";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String SET_CAL = "Set manual spatial calibration";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";
    public static final String USE_IMAGEJ_READER = "Use ImageJ reader";
    public static final String THREE_D_MODE = "Load 3D stacks as";


    public interface OutputModes {
        String IMAGE = "Image";
        String OBJECTS = "Objects";

        String[] ALL = new String[]{IMAGE,OBJECTS};

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
        String INCUCYTE_SHORT = "Incucyte short filename";
        String YOKOGAWA = "Yokogowa";
        String INPUT_FILE_PREFIX = "Input filename with prefix";
        String INPUT_FILE_SUFFIX = "Input filename with suffix";

        String[] ALL = new String[]{INCUCYTE_SHORT,YOKOGAWA,INPUT_FILE_PREFIX,INPUT_FILE_SUFFIX};

    }

//    public Img<T> getImg(String path, int seriesNumber,int[][] dimRanges, boolean verbose) throws IOException, io.scif.FormatException {
//        File file = new File(path);
//
//        SCIFIO scifio = new SCIFIO();
//        final Reader reader = scifio.initializer().initializeReader(file.getAbsolutePath());
//        final Metadata meta = reader.getMetadata();
//
//        System.out.println("Image count "+meta.getImageCount());
//        for (int i=0;i<meta.getImageCount();i++) {
//            ImageMetadata iMeta = meta.get(i);
//            List<CalibratedAxis> axes = iMeta.getAxes();
//            for (CalibratedAxis axis:axes) {
//                System.out.println("    unit"+axis.unit());
//
//            }
//
//            long[] lengths = iMeta.getAxesLengths();
//            for (long len:lengths) System.out.println("    length "+len);
//
//        }
//
//        return null;
//
//    }


    public ImagePlus getBFImage(String path, int seriesNumber, @Nullable String[] dimRanges, @Nullable int[] crop, boolean localVerbose)
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

//        byte[][] lutt = reader.get8BitLookupTable();
//        for (int i=0;i<lutt[0].length;i++) {
//            System.out.println(lutt[0][i]+"_"+lutt[1][i]+"_"+lutt[2][i]);
//        }

        // Getting a list of colours
//        LUT[] luts = new LUT[reader.getSizeC()];
//        for (int i = 0;i<reader.getSizeC();i++) {
//            Color colour = meta.getChannelColor(seriesNumber - 1, i);
//
//            luts[i] = LUT.createLutFromColor(new java.awt.Color(colour.getRed(),colour.getGreen(),colour.getBlue()));
//        }

        int left = 0;
        int top = 0;
        int width = reader.getSizeX();
        int height = reader.getSizeY();

        int sizeC = reader.getSizeC();
        int sizeT = reader.getSizeT();
        int sizeZ = reader.getSizeZ();
        int bitDepth = reader.getBitsPerPixel();

        if (crop != null) {
            left = crop[0];
            top = crop[1];
            width = crop[2];
            height = crop[3];
        }

        int[] channelsList = interpretRange(dimRanges[0]);
        if (channelsList[channelsList.length-1] == Integer.MAX_VALUE) channelsList = extendRangeToEnd(channelsList,sizeC);
        int[] slicesList = interpretRange(dimRanges[1]);
        if (slicesList[slicesList.length-1] == Integer.MAX_VALUE) slicesList = extendRangeToEnd(slicesList,sizeZ);
        int[] framesList = interpretRange(dimRanges[2]);
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
                    int idx = reader.getIndex(z-1, c-1, t-1);
                    ImageProcessor ip = reader.openProcessors(idx,left,top,width,height)[0];
//                    ip.setLut(luts[c-1]);

//                    byte[][] lutt = reader.get8BitLookupTable();
//                    LUT lut = new LUT(lutt[0],lutt[1],lutt[2]);
//                    ip.setLut(lut);

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
        if (meta != null) {
            if (meta.getPixelsPhysicalSizeX(seriesNumber-1) != null) {
                Length physicalSizeX = meta.getPixelsPhysicalSizeX(seriesNumber-1);
                if (!unit.isConvertible(physicalSizeX.unit()))
                    System.err.println("Can't convert units for file \""+new File(path).getName()+"\".  Spatially calibrated values may be wrong");
                ipl.getCalibration().pixelWidth = (double) physicalSizeX.value(unit);
                ipl.getCalibration().setXUnit(unit.getSymbol());
            } else {
                System.err.println("Can't interpret units for file \""+new File(path).getName()+"\".  Spatial calibration set to pixel units.");
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
        } else {
            System.err.println("Can't interpret units for file \""+new File(path).getName()+"\".  Spatially calibrated values may be wrong");
        }

        reader.close();

        return ipl;

    }

    public ImagePlus getImageSequence(File rootFile, int numberOfZeroes, int startingIndex, int finalIndex, int[] crop)
            throws ServiceException, DependencyException, FormatException, IOException {
        // Number format
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<numberOfZeroes;i++) stringBuilder.append("0");
        DecimalFormat df = new DecimalFormat(stringBuilder.toString());

        // Getting fragments of the filepath
        String rootPath = rootFile.getParent()+MIA.slashes;
        String rootName = rootFile.getName();
        String startingNumber = df.format(startingIndex);
        int numStart = FilenameUtils.removeExtension(rootName).length()-numberOfZeroes;
        rootName = rootFile.getName().substring(0,numStart);
        String extension = FilenameUtils.getExtension(rootFile.getName());

        // Determining the number of images to load
        int count = 0;
        int idx = startingIndex;
        while (new File(rootPath+rootName+df.format(idx++)+"."+extension).exists()){
            count++;
            if (idx == finalIndex) break;
        }

        // Determining the dimensions of the input image
        String[] dimRanges = new String[]{"1","1","1"};
        ImagePlus rootIpl = getBFImage(rootFile.getAbsolutePath(),1,dimRanges,crop,false);
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
            String currentPath = rootPath+rootName+df.format(i+startingIndex)+"."+extension;
            ImagePlus tempIpl = getBFImage(currentPath,1,dimRanges,crop,false);

            outputIpl.setPosition(i+1);
            outputIpl.setProcessor(tempIpl.getProcessor());

        }

        outputIpl.setPosition(1);

        outputIpl.setCalibration(rootIpl.getCalibration());

        return outputIpl;

    }

    private ImagePlus getIncucyteShortNameImage(HCMetadata metadata, int seriesNumber, String[] dimRanges, int[] crop)
            throws ServiceException, DependencyException, FormatException, IOException {
        // First, running metadata extraction on the input file
        NameExtractor filenameExtractor = new IncuCyteShortFilenameExtractor();
        filenameExtractor.extract(metadata, metadata.getFile().getName());

        // Constructing a new name using the same name format
        String comment = metadata.getComment();
        String filename = metadata.getFile().getParent()+MIA.slashes+IncuCyteShortFilenameExtractor
                .generate(comment,metadata.getWell(),metadata.getAsString(HCMetadata.FIELD),metadata.getExt());

        return getBFImage(filename,seriesNumber,dimRanges,crop,true);

    }

    private ImagePlus getYokogawaNameImage(File templateFile, int seriesNumber, int[] crop)
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
        return getBFImage(listOfFiles[0].getAbsolutePath(),seriesNumber,dimRanges,crop,true);

    }

    private ImagePlus getPrefixNameImage(HCMetadata metadata, int seriesNumber, String[] dimRanges, int[] crop)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.getName(absolutePath);
        String comment = metadata.getComment();
        String filename = path+comment+name;

        return getBFImage(filename,seriesNumber,dimRanges,crop,true);

    }

    private ImagePlus getSuffixNameImage(HCMetadata metadata, int seriesNumber, String[] dimRanges, int[] crop)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String extension = FilenameUtils.getExtension(absolutePath);
        String comment = metadata.getComment();
        String filename = path+name+comment+"."+extension;

        return getBFImage(filename,seriesNumber,dimRanges,crop,true);

    }


    @Override
    public String getTitle() {
        return "Load image";

    }

    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) throws GenericMIAException {
        // Getting parameters
        String outputMode = parameters.getValue(OUTPUT_MODE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String importMode = parameters.getValue(IMPORT_MODE);
        String filePath = parameters.getValue(FILE_PATH);
        int numberOfZeroes = parameters.getValue(NUMBER_OF_ZEROES);
        int startingIndex = parameters.getValue(STARTING_INDEX);
        boolean limitFrames = parameters.getValue(LIMIT_FRAMES);
        int finalIndex = parameters.getValue(FINAL_INDEX);
        String nameFormat = parameters.getValue(NAME_FORMAT);
        String comment = parameters.getValue(COMMENT);
        String prefix = parameters.getValue(PREFIX);
        String suffix = parameters.getValue(SUFFIX);
        String channels = parameters.getValue(CHANNELS);
        String slices = parameters.getValue(SLICES);
        String frames = parameters.getValue(FRAMES);
        int channel = parameters.getValue(CHANNEL);
        boolean cropImage = parameters.getValue(CROP_IMAGE);
        int left = parameters.getValue(LEFT);
        int top = parameters.getValue(TOP);
        int width = parameters.getValue(WIDTH);
        int height = parameters.getValue(HEIGHT);
        boolean setCalibration = parameters.getValue(SET_CAL);
        double xyCal = parameters.getValue(XY_CAL);
        double zCal = parameters.getValue(Z_CAL);
        boolean useImageJReader = parameters.getValue(USE_IMAGEJ_READER);
        String threeDMode = parameters.getValue(THREE_D_MODE);

        // Series number comes from the Workspace
        int seriesNumber = workspace.getMetadata().getSeriesNumber();

        String[] dimRanges = new String[]{channels,slices,frames};

        int[] crop = null;
        if (cropImage) crop = new int[]{left,top,width,height};

        ImagePlus ipl = null;
        try {
            switch (importMode) {
                case ImportModes.CURRENT_FILE:
                    File file = workspace.getMetadata().getFile();
                    if (file == null)
                        throw new GenericMIAException("Set file in Input Control");
                    if (useImageJReader) {
                        ipl = IJ.openImage(workspace.getMetadata().getFile().getAbsolutePath());
                    } else {
                        ipl = getBFImage(workspace.getMetadata().getFile().getAbsolutePath(), seriesNumber, dimRanges, crop, true);
                    }
                    break;

                case ImportModes.IMAGEJ:
                    ipl = IJ.getImage();
                    break;

                case ImportModes.IMAGE_SEQUENCE:
                    if (!limitFrames) finalIndex = Integer.MAX_VALUE;
                    ipl = getImageSequence(workspace.getMetadata().getFile(),numberOfZeroes,startingIndex, finalIndex,crop);
                    break;

                case ImportModes.MATCHING_FORMAT:
                    switch (nameFormat) {
                        case NameFormats.INCUCYTE_SHORT:
                            HCMetadata metadata = (HCMetadata) workspace.getMetadata().clone();
                            metadata.setComment(comment);
                            ipl = getIncucyteShortNameImage(metadata, seriesNumber, dimRanges,crop);
                            break;

                        case NameFormats.YOKOGAWA:
                            ipl = getYokogawaNameImage(workspace.getMetadata().getFile(), seriesNumber, crop);
                            break;

                        case NameFormats.INPUT_FILE_PREFIX:
                            metadata = (HCMetadata) workspace.getMetadata().clone();
                            metadata.setComment(prefix);
                            ipl = getPrefixNameImage(metadata, seriesNumber, dimRanges,crop);
                            break;

                        case NameFormats.INPUT_FILE_SUFFIX:
                            metadata = (HCMetadata) workspace.getMetadata().clone();
                            metadata.setComment(suffix);
                            ipl = getSuffixNameImage(metadata, seriesNumber, dimRanges,crop);
                            break;
                    }
                    break;

                case ImportModes.SPECIFIC_FILE:
                    if (useImageJReader) {
                        ipl = IJ.openImage(filePath);
                    } else {
                        ipl = getBFImage(filePath, 1, dimRanges, crop, true);
                    }
                    break;
            }
        } catch (ServiceException | DependencyException | IOException | FormatException e) {
            e.printStackTrace();
        }

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
        switch (outputMode) {
            case OutputModes.IMAGE:
                writeMessage("Adding image (" + outputImageName + ") to workspace");
                workspace.addImage(new Image(outputImageName, ipl));
                break;

            case OutputModes.OBJECTS:
                Image outputImage = new Image(outputObjectsName, ipl);
                ObjCollection outputObjects = outputImage.convertImageToObjects(outputObjectsName);

                writeMessage("Adding objects (" + outputObjectsName + ") to workspace");
                workspace.addObjects(outputObjects);
                break;
        }

        // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
        if (showOutput && ipl != null) {
            ipl = new Duplicator().run(ipl);
            ipl.setTitle(outputImageName);
            ipl.show();
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(OUTPUT_MODE,Parameter.CHOICE_ARRAY, OutputModes.IMAGE, OutputModes.ALL));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(
                new Parameter(IMPORT_MODE, Parameter.CHOICE_ARRAY,ImportModes.CURRENT_FILE,ImportModes.ALL));
        parameters.add(new Parameter(NUMBER_OF_ZEROES,Parameter.INTEGER,4));
        parameters.add(new Parameter(STARTING_INDEX,Parameter.INTEGER,0));
        parameters.add(new Parameter(LIMIT_FRAMES,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(FINAL_INDEX,Parameter.INTEGER,1));
        parameters.add(
                new Parameter(NAME_FORMAT,Parameter.CHOICE_ARRAY,NameFormats.INCUCYTE_SHORT,NameFormats.ALL));
        parameters.add(new Parameter(COMMENT,Parameter.STRING,""));
        parameters.add(new Parameter(PREFIX,Parameter.STRING,""));
        parameters.add(new Parameter(SUFFIX,Parameter.STRING,""));
        parameters.add(new Parameter(FILE_PATH, Parameter.FILE_PATH,null));
        parameters.add(new Parameter(CHANNELS,Parameter.STRING,"1-end"));
        parameters.add(new Parameter(SLICES,Parameter.STRING,"1-end"));
        parameters.add(new Parameter(FRAMES,Parameter.STRING,"1-end"));
        parameters.add(new Parameter(CHANNEL,Parameter.INTEGER,1));
        parameters.add(new Parameter(CROP_IMAGE, Parameter.BOOLEAN, false));
        parameters.add(new Parameter(LEFT, Parameter.INTEGER,0));
        parameters.add(new Parameter(TOP, Parameter.INTEGER,0));
        parameters.add(new Parameter(WIDTH, Parameter.INTEGER,512));
        parameters.add(new Parameter(HEIGHT, Parameter.INTEGER,512));
        parameters.add(new Parameter(SET_CAL, Parameter.BOOLEAN, false));
        parameters.add(new Parameter(XY_CAL, Parameter.DOUBLE, 1.0));
        parameters.add(new Parameter(Z_CAL, Parameter.DOUBLE, 1.0));
        parameters.add(new Parameter(USE_IMAGEJ_READER, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(THREE_D_MODE,Parameter.CHOICE_ARRAY,ThreeDModes.ZSTACK,ThreeDModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(OUTPUT_MODE));
        switch ((String) parameters.getValue(OUTPUT_MODE)) {
            case OutputModes.IMAGE:
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                break;

            case OutputModes.OBJECTS:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(IMPORT_MODE));
        switch((String) parameters.getValue(IMPORT_MODE)) {
            case ImportModes.CURRENT_FILE:
            case ImportModes.IMAGEJ:
                break;

            case ImportModes.IMAGE_SEQUENCE:
                returnedParameters.add(parameters.getParameter(NUMBER_OF_ZEROES));
                returnedParameters.add(parameters.getParameter(STARTING_INDEX));
                returnedParameters.add(parameters.getParameter(LIMIT_FRAMES));
                if (parameters.getValue(LIMIT_FRAMES)) {
                    returnedParameters.add(parameters.getParameter(FINAL_INDEX));
                }
                break;

            case ImportModes.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(NAME_FORMAT));
                switch ((String) parameters.getValue(NAME_FORMAT)) {
                    case NameFormats.INCUCYTE_SHORT:
                        returnedParameters.add(parameters.getParameter(COMMENT));
                        break;
                    case NameFormats.YOKOGAWA:
                        returnedParameters.add(parameters.getParameter(CHANNEL));
                        break;
                    case NameFormats.INPUT_FILE_PREFIX:
                        returnedParameters.add(parameters.getParameter(PREFIX));
                        break;
                    case NameFormats.INPUT_FILE_SUFFIX:
                        returnedParameters.add(parameters.getParameter(SUFFIX));
                        break;
                }
                break;

            case ImportModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(FILE_PATH));
                break;
        }

        if (!parameters.getValue(IMPORT_MODE).equals(ImportModes.IMAGE_SEQUENCE) &&
                !(parameters.getValue(IMPORT_MODE).equals(ImportModes.MATCHING_FORMAT)
                        && parameters.getValue(NAME_FORMAT).equals(NameFormats.YOKOGAWA))) {
            returnedParameters.add(parameters.getParameter(CHANNELS));
            returnedParameters.add(parameters.getParameter(SLICES));
            returnedParameters.add(parameters.getParameter(FRAMES));
        }

        returnedParameters.add(parameters.getParameter(CROP_IMAGE));
        if (parameters.getValue(CROP_IMAGE)){
            returnedParameters.add(parameters.getParameter(LEFT));
            returnedParameters.add(parameters.getParameter(TOP));
            returnedParameters.add(parameters.getParameter(WIDTH));
            returnedParameters.add(parameters.getParameter(HEIGHT));
        }

        returnedParameters.add(parameters.getParameter(SET_CAL));
        if (parameters.getValue(SET_CAL)) {
            returnedParameters.add(parameters.getParameter(XY_CAL));
            returnedParameters.add(parameters.getParameter(Z_CAL));
        }

        if (parameters.getValue(IMPORT_MODE).equals(ImportModes.CURRENT_FILE)
                || parameters.getValue(IMPORT_MODE).equals(ImportModes.SPECIFIC_FILE)) {
            returnedParameters.add(parameters.getParameter(USE_IMAGEJ_READER));
        }

        returnedParameters.add(parameters.getParameter(THREE_D_MODE));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]