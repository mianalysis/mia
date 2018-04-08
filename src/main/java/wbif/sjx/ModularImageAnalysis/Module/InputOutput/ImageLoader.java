// TODO: Load colours from LIF file (if possible).  Similarly, colour Flex files blue, green, red (currently RGB)

package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
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
import ome.xml.meta.IMetadata;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.ConvertStackToTimeseries;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.FileConditions.FileCondition;
import wbif.sjx.common.FileConditions.NameContainsString;
import wbif.sjx.common.MetadataExtractors.IncuCyteShortFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.NameExtractor;
import wbif.sjx.common.Object.HCMetadata;
import wbif.sjx.common.System.FileCrawler;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class ImageLoader < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String IMPORT_MODE = "Import mode";
    public static final String NUMBER_OF_ZEROES = "Number of zeroes";
    public static final String STARTING_INDEX = "Starting index";
    public static final String LIMIT_FRAMES = "Limit frames";
    public static final String FINAL_INDEX = "Final index";
    public static final String NAME_FORMAT = "Name format";
    public static final String COMMENT = "Comment";
    public static final String PREFIX = "Prefix";
    public static final String FILE_PATH = "File path";
    public static final String USE_ALL_C = "Use all channels";
    public static final String STARTING_C = "Starting channel";
    public static final String ENDING_C = "Ending channel";
    public static final String INTERVAL_C = "Channel interval";
    public static final String USE_ALL_Z = "Use all Z-slices";
    public static final String STARTING_Z = "Starting Z-slice";
    public static final String ENDING_Z = "Ending Z-slice";
    public static final String INTERVAL_Z = "Slice interval";
    public static final String USE_ALL_T = "Use all timepoints";
    public static final String STARTING_T = "Starting timepoint";
    public static final String ENDING_T = "Ending timepoint";
    public static final String INTERVAL_T = "Timepoint interval";
    public static final String SET_CAL = "Set manual spatial calibration";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";
    public static final String UNITS = "Units";
    public static final String USE_IMAGEJ_READER = "Use ImageJ reader";
    public static final String THREE_D_MODE = "Load 3D stacks as";
    public static final String SHOW_IMAGE = "Show image";

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
        String INPUT_FILE_PREFIX= "Input filename with prefix";

        String[] ALL = new String[]{INCUCYTE_SHORT,INPUT_FILE_PREFIX};

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


    public ImagePlus getBFImage(String path, int seriesNumber,int[][] dimRanges) throws ServiceException, DependencyException, IOException, FormatException {
        DebugTools.enableLogging("off");
        DebugTools.setRootLevel("off");

        // Setting spatial calibration
        IMetadata meta;
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        meta = service.createOMEXMLMetadata();
        ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
        reader.setMetadataStore((MetadataStore) meta);

        reader.setGroupFiles(false);
        reader.setId(path);
        reader.setSeries(seriesNumber-1);

        int width = reader.getSizeX();
        int height = reader.getSizeY();
        int sizeC = reader.getSizeC();
        int sizeT = reader.getSizeT();
        int sizeZ = reader.getSizeZ();
        int bitDepth = reader.getBitsPerPixel();

        int startingC = dimRanges[0][0];
        int endingC = dimRanges[0][1];
        int intervalC = dimRanges[0][2];
        int startingZ = dimRanges[1][0];
        int endingZ = dimRanges[1][1];
        int intervalZ = dimRanges[1][2];
        int startingT = dimRanges[2][0];
        int endingT = dimRanges[2][1];
        int intervalT = dimRanges[2][2];

        // Updating ranges for full import dimensions
        if (endingC == -1) endingC = sizeC;
        if (endingZ == -1) endingZ = sizeZ;
        if (endingT == -1) endingT = sizeT;

        int nC = Math.floorDiv((endingC-startingC+1),intervalC);
        int nZ = Math.floorDiv((endingZ-startingZ+1),intervalZ);
        int nT = Math.floorDiv((endingT-startingT+1),intervalT);

        // Creating the new ImagePlus
        ImagePlus ipl = IJ.createHyperStack("Image", width, height,nC,nZ,nT,bitDepth);

        // Iterating over all images in the stack, adding them to the output ImagePlus
        int nTotal = nC*nT*nZ;
        int count = 0;
        int countZ = 1;

        for (int z = startingZ; z <= endingZ; z=z+intervalZ) {
            int countC = 1;
            for (int c = startingC; c <= endingC; c=c+intervalC) {
                int countT = 1;
                for (int t = startingT; t <= endingT; t=t+intervalT) {
                    int idx = reader.getIndex(z-1, c-1, t-1);
                    ImageProcessor ip = reader.openProcessors(idx)[0];

                    ipl.setPosition(countC,countZ,countT);
                    ipl.setProcessor(ip);

                    writeMessage("Loaded image "+(++count)+" of "+nTotal);

                    countT++;
                }
                countC++;
            }
            countZ++;
        }

        ipl.setPosition(1, 1, 1);

        // Add spatial calibration
        if (meta != null) {
            if (meta.getPixelsPhysicalSizeX(0) != null) {
                ipl.getCalibration().pixelWidth = (double) meta.getPixelsPhysicalSizeX(0).value();
            } else {
                ipl.getCalibration().pixelWidth = 1.0;
            }

            if (meta.getPixelsPhysicalSizeX(0) != null) {
                ipl.getCalibration().pixelHeight = (double) meta.getPixelsPhysicalSizeX(0).value();
            } else {
                ipl.getCalibration().pixelHeight = 1.0;
            }

            if (ipl.getNSlices() > 1 && meta.getPixelsPhysicalSizeZ(0) != null) {
                ipl.getCalibration().pixelDepth = (double) meta.getPixelsPhysicalSizeZ(0).value();
            } else {
                ipl.getCalibration().pixelDepth = 1.0;
            }
        }

        reader.close();

        return ipl;

    }

    public ImagePlus getImageSequence(File rootFile, int numberOfZeroes, int startingIndex, int finalIndex) {
        // Number format
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<numberOfZeroes;i++) stringBuilder.append("0");
        DecimalFormat df = new DecimalFormat(stringBuilder.toString());
        
        // Getting fragments of the filepath
        String rootPath = rootFile.getParent()+"\\";
        String rootName = rootFile.getName();
        String startingNumber = df.format(startingIndex);
        int numStart = rootName.lastIndexOf(startingNumber);
        if (numStart == -1) return null; // Nothing was found at that number
        rootName = rootFile.getName().substring(0,numStart);
        String extension = FilenameUtils.getExtension(rootFile.getName());

        // Determining the number of images to load
        int count = 0;
        int idx = startingIndex;
        while (new File(rootPath+rootName+df.format(idx++)+"."+extension).exists()){
            if (idx == finalIndex) break;
            count++;
        }

        // Determining the dimensions of the input image
        ImagePlus rootIpl = IJ.openImage(rootFile.getAbsolutePath());
        int width = rootIpl.getWidth();
        int height = rootIpl.getHeight();
        int bitDepth = rootIpl.getBitDepth();

        // Creating the new image
        ImagePlus outputIpl = IJ.createImage("Image",width,height,count,bitDepth);
        for (int i = 0;i<count;i++) {
            writeMessage("Loading image "+i+" of "+count);
            String currentPath = rootPath+rootName+df.format(i+startingIndex)+"."+extension;
            ImagePlus tempIpl = IJ.openImage(currentPath);
            outputIpl.setPosition(i+1);
            outputIpl.setProcessor(tempIpl.getProcessor());

        }

        outputIpl.setPosition(1);

        return outputIpl;

    }

    private ImagePlus getFormattedNameImage(String nameFormat, HCMetadata metadata, String comment,
                                            int seriesNumber,int[][] dimRanges) throws ServiceException, DependencyException, FormatException, IOException {

        String filename = null;
        switch (nameFormat) {
            case NameFormats.INCUCYTE_SHORT:
                // First, running metadata extraction on the input file
                NameExtractor filenameExtractor = new IncuCyteShortFilenameExtractor();
                filenameExtractor.extract(metadata, metadata.getFile().getName());

                // Constructing a new name using the same name format
                filename = metadata.getFile().getParent()+"\\"+IncuCyteShortFilenameExtractor
                        .generate(comment,metadata.getWell(),metadata.getAsString(HCMetadata.FIELD),metadata.getExt());

                break;

            case NameFormats.INPUT_FILE_PREFIX:
                String absolutePath = metadata.getFile().getAbsolutePath();
                String path = FilenameUtils.getFullPath(absolutePath);
                String name = FilenameUtils.getName(absolutePath);
                filename = path+comment+name;
                break;
        }

        return getBFImage(filename,seriesNumber,dimRanges);

    }

    @Override
    public String getTitle() {
        return "Load image";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) throws GenericMIAException {
        // Getting parameters
        String importMode = parameters.getValue(IMPORT_MODE);
        String filePath = parameters.getValue(FILE_PATH);
        int numberOfZeroes = parameters.getValue(NUMBER_OF_ZEROES);
        int startingIndex = parameters.getValue(STARTING_INDEX);
        boolean limitFrames = parameters.getValue(LIMIT_FRAMES);
        int finalIndex = parameters.getValue(FINAL_INDEX);
        String nameFormat = parameters.getValue(NAME_FORMAT);
        String comment = parameters.getValue(COMMENT);
        String prefix = parameters.getValue(PREFIX);
        boolean useAllC = parameters.getValue(USE_ALL_C);
        int startingC = parameters.getValue(STARTING_C);
        int endingC = parameters.getValue(ENDING_C);
        int intervalC = parameters.getValue(INTERVAL_C);
        boolean useAllZ = parameters.getValue(USE_ALL_Z);
        int startingZ = parameters.getValue(STARTING_Z);
        int endingZ = parameters.getValue(ENDING_Z);
        int intervalZ = parameters.getValue(INTERVAL_Z);
        boolean useAllT = parameters.getValue(USE_ALL_T);
        int startingT = parameters.getValue(STARTING_T);
        int endingT = parameters.getValue(ENDING_T);
        int intervalT = parameters.getValue(INTERVAL_T);
        boolean setCalibration = parameters.getValue(SET_CAL);
        double xyCal = parameters.getValue(XY_CAL);
        double zCal = parameters.getValue(Z_CAL);
        String units = parameters.getValue(UNITS);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean useImageJReader = parameters.getValue(USE_IMAGEJ_READER);
        String threeDMode = parameters.getValue(THREE_D_MODE);
        boolean showImage = parameters.getValue(SHOW_IMAGE);

        // Series number comes from the Workspace
        int seriesNumber = workspace.getMetadata().getSeries();

        if (useAllC) endingC = -1;
        if (useAllZ) endingZ = -1;
        if (useAllT) endingT = -1;
        int[][] dimRanges = new int[][]{{startingC,endingC,intervalC},{startingZ,endingZ,intervalZ},{startingT,endingT,intervalT}};

        ImagePlus ipl = null;
        try {
            if (useImageJReader) {
                ipl = IJ.openImage(workspace.getMetadata().getFile().getAbsolutePath());

            } else {
                switch (importMode) {
                    case ImportModes.CURRENT_FILE:
                        File file = workspace.getMetadata().getFile();
                        if (file == null)
                            throw new GenericMIAException("Set file in Input Control");
                        ipl = getBFImage(workspace.getMetadata().getFile().getAbsolutePath(), seriesNumber, dimRanges);
                        break;

                    case ImportModes.IMAGEJ:
                        ipl = IJ.getImage();
                        break;

                    case ImportModes.IMAGE_SEQUENCE:
                        if (!limitFrames) finalIndex = Integer.MAX_VALUE;
                        ipl = getImageSequence(workspace.getMetadata().getFile(),numberOfZeroes,startingIndex, finalIndex);
                        break;

                    case ImportModes.MATCHING_FORMAT:
                        switch (nameFormat) {
                            case NameFormats.INCUCYTE_SHORT:
                                ipl = getFormattedNameImage(nameFormat, workspace.getMetadata(), comment, seriesNumber, dimRanges);
                                break;

                            case NameFormats.INPUT_FILE_PREFIX:
                                ipl = getFormattedNameImage(nameFormat, workspace.getMetadata(), prefix, seriesNumber, dimRanges);
                                break;
                        }
                        break;

                    case ImportModes.SPECIFIC_FILE:
                        ipl = getBFImage(filePath, seriesNumber, dimRanges);
                        break;
                }
            }
        } catch (ServiceException | DependencyException | IOException | FormatException e) {
            e.printStackTrace();
        }

        // If necessary, setting the spatial calibration
        if (setCalibration) {
            Calibration calibration = new Calibration();

            calibration.pixelHeight = xyCal;
            calibration.pixelWidth= xyCal;
            calibration.pixelDepth = zCal;
            calibration.setUnit(units);

            ipl.setCalibration(calibration);

        }

        if (threeDMode.equals(ThreeDModes.TIMESERIES) && !ipl.isHyperStack() && ipl.getNSlices() > 1) {
            ConvertStackToTimeseries.process(ipl);
        }

        // Adding image to workspace
        writeMessage("Adding image ("+outputImageName+") to workspace");
        workspace.addImage(new Image(outputImageName,ipl));

        // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
        if (showImage && ipl != null) {
            ipl = new Duplicator().run(ipl);
            ipl.show();
        }
    }

    @Override
    public void initialiseParameters() {
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
        parameters.add(new Parameter(FILE_PATH, Parameter.FILE_PATH,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(USE_ALL_C, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(STARTING_C, Parameter.INTEGER,1));
        parameters.add(new Parameter(ENDING_C, Parameter.INTEGER,1));
        parameters.add(new Parameter(INTERVAL_C, Parameter.INTEGER,1));
        parameters.add(new Parameter(USE_ALL_Z, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(STARTING_Z, Parameter.INTEGER,1));
        parameters.add(new Parameter(ENDING_Z, Parameter.INTEGER,1));
        parameters.add(new Parameter(INTERVAL_Z, Parameter.INTEGER,1));
        parameters.add(new Parameter(USE_ALL_T, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(STARTING_T, Parameter.INTEGER,1));
        parameters.add(new Parameter(ENDING_T, Parameter.INTEGER,1));
        parameters.add(new Parameter(INTERVAL_T, Parameter.INTEGER,1));
        parameters.add(new Parameter(SET_CAL, Parameter.BOOLEAN, false));
        parameters.add(new Parameter(XY_CAL, Parameter.DOUBLE, 1.0));
        parameters.add(new Parameter(Z_CAL, Parameter.DOUBLE, 1.0));
        parameters.add(new Parameter(UNITS, Parameter.STRING, "um"));
        parameters.add(new Parameter(USE_IMAGEJ_READER, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(THREE_D_MODE,Parameter.CHOICE_ARRAY,ThreeDModes.TIMESERIES,ThreeDModes.ALL));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
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
                    case NameFormats.INPUT_FILE_PREFIX:
                        returnedParameters.add(parameters.getParameter(PREFIX));
                        break;
                }
                break;

            case ImportModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(FILE_PATH));
                break;
        }

        if (!parameters.getValue(IMPORT_MODE).equals(ImportModes.IMAGE_SEQUENCE)) {
            returnedParameters.add(parameters.getParameter(STARTING_C));
            returnedParameters.add(parameters.getParameter(INTERVAL_C));
            returnedParameters.add(parameters.getParameter(USE_ALL_C));
            if (!(boolean) parameters.getValue(USE_ALL_C)) {
                returnedParameters.add(parameters.getParameter(ENDING_C));
            }

            returnedParameters.add(parameters.getParameter(STARTING_Z));
            returnedParameters.add(parameters.getParameter(INTERVAL_Z));
            returnedParameters.add(parameters.getParameter(USE_ALL_Z));
            if (!(boolean) parameters.getValue(USE_ALL_Z)) {
                returnedParameters.add(parameters.getParameter(ENDING_Z));
            }

            returnedParameters.add(parameters.getParameter(STARTING_T));
            returnedParameters.add(parameters.getParameter(INTERVAL_T));
            returnedParameters.add(parameters.getParameter(USE_ALL_T));
            if (!(boolean) parameters.getValue(USE_ALL_T)) {
                returnedParameters.add(parameters.getParameter(ENDING_T));
            }
        }

        returnedParameters.add(parameters.getParameter(SET_CAL));
        if (parameters.getValue(SET_CAL)) {
            returnedParameters.add(parameters.getParameter(XY_CAL));
            returnedParameters.add(parameters.getParameter(Z_CAL));
            returnedParameters.add(parameters.getParameter(UNITS));
        }

        returnedParameters.add(parameters.getParameter(USE_IMAGEJ_READER));
        returnedParameters.add(parameters.getParameter(THREE_D_MODE));
        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

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
    public void addRelationships(RelationshipCollection relationships) {

    }
}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]