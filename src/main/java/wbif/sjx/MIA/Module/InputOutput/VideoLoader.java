package wbif.sjx.MIA.Module.InputOutput;

import javax.annotation.Nullable;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.AVI_Reader;
import ij.plugin.CompositeConverter;
import ij.process.ByteProcessor;
import ij.process.ColorSpaceConverter;
import ij.process.ImageProcessor;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConvertStackToTimeseries;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;
import wbif.sjx.common.Object.HCMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack.extendRangeToEnd;

public class VideoLoader extends Module {
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String IMPORT_MODE = "Import mode";
    public static final String NAME_FORMAT = "Name format";
    public static final String GENERIC_FORMAT = "Generic format";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
    public static final String PREFIX = "Prefix";
    public static final String SUFFIX = "Suffix";
    public static final String EXTENSION = "Extension";
    public static final String INCLUDE_SERIES_NUMBER = "Include series number";
    public static final String FILE_PATH = "File path";
    public static final String FRAMES = "Frames";
    public static final String CROP_IMAGE = "Crop image";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String SET_CAL = "Set manual spatial calibration";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";

    public VideoLoader(ModuleCollection modules) {
        super(modules);
    }

    public interface ImportModes {
        String CURRENT_FILE = "Current file";
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[]{CURRENT_FILE, MATCHING_FORMAT, SPECIFIC_FILE};

    }

    public interface NameFormats {
        String GENERIC = "Generic (from metadata)";
        String INPUT_FILE_PREFIX = "Input filename with prefix";
        String INPUT_FILE_SUFFIX = "Input filename with suffix";

        String[] ALL = new String[]{GENERIC,INPUT_FILE_PREFIX,INPUT_FILE_SUFFIX};

    }


    public Image getFFMPEGVideo(String path, String outputImageName, String frameRange, @Nullable int[] crop) throws Exception {
        // Getting the first frame as a virtual stack to act as a size reference
        AVI_Reader aviReader = new AVI_Reader();
        ImageStack ist = aviReader.makeStack(path,1,-1,true,false,false);

        // Initialising the output ImagePlus using the first frame
        int left = 0;
        int top = 0;
        int origWidth = ist.getWidth();
        int origHeight = ist.getHeight();
        int origFrames = ist.size();
        int width = origWidth;
        int height = origHeight;

        // Getting an ordered list of frames to be imported
        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frameRange,true);
        if (framesList[framesList.length-1] == Integer.MAX_VALUE) framesList = extendRangeToEnd(framesList,origFrames);
        TreeSet<Integer> frames = Arrays.stream(framesList).boxed().collect(Collectors.toCollection(TreeSet::new));

        if (crop != null) {
            left = crop[0];
            top = crop[1];
            width = crop[2];
            height = crop[3];
        }

        ImagePlus ipl = IJ.createHyperStack("Image",width, height,3,1,framesList.length,8);

        int count = 0;
        for (int frame:frames) {
            writeMessage("Loading frame "+(count++)+" of "+origFrames);

            // Loading frame
            ImageStack currIst = aviReader.makeStack(path,frame,frame,false,false,false);

            // Convert to composite
            ImagePlus currIpl = CompositeConverter.makeComposite(new ImagePlus("Temp",currIst));

            for (int c=1;c<=3;c++) {
                currIpl.setPosition(c);
                ipl.setPosition(c,1,count++);

                ImageProcessor ipr = currIpl.getProcessor();
                if (crop != null) {
                    ipr.setRoi(left,top,width,height);
                    ipr = ipr.crop();
                }

                ipl.setProcessor(ipr);
            }
        }

        // This will probably load as a Z-stack rather than timeseries, so convert it to a stack
        if (((ipl.getNFrames() == 1 && ipl.getNSlices() > 1) || (ipl.getNSlices() == 1 && ipl.getNFrames() > 1) )) {
            ConvertStackToTimeseries.process(ipl);
            ipl.getCalibration().pixelDepth = 1;
        }

        return new Image(outputImageName,ipl);

    }

    public String getGenericName(HCMetadata metadata, String genericFormat) {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String filename = ImageLoader.compileGenericFilename(genericFormat, metadata);
        return path + filename;

    }

    public String getPrefixName(HCMetadata metadata, int seriesNumber, boolean includeSeries, String ext) {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String series = includeSeries ? "_S"+metadata.getSeriesNumber() : "";

        return path+comment+name+series+"."+ext;

    }

    public String getSuffixName(HCMetadata metadata, int seriesNumber, boolean includeSeries, String ext ) {
        String absolutePath = metadata.getFile().getAbsolutePath();
        String path = FilenameUtils.getFullPath(absolutePath);
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
        String comment = metadata.getComment();
        String series = includeSeries ? "_S"+metadata.getSeriesNumber() : "";

        return path+name+series+comment+"."+ext;

    }

    @Override
    public String getTitle() {
        return "Load video";
    }

    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String importMode = parameters.getValue(IMPORT_MODE);
        String filePath = parameters.getValue(FILE_PATH);
        String nameFormat = parameters.getValue(NAME_FORMAT);

        String genericFormat = parameters.getValue(GENERIC_FORMAT);
        String prefix = parameters.getValue(PREFIX);
        String suffix = parameters.getValue(SUFFIX);
        String ext = parameters.getValue(EXTENSION);
        boolean includeSeriesNumber = parameters.getValue(INCLUDE_SERIES_NUMBER);
        String frameRange = parameters.getValue(FRAMES);
        boolean cropImage = parameters.getValue(CROP_IMAGE);
        int left = parameters.getValue(LEFT);
        int top = parameters.getValue(TOP);
        int width = parameters.getValue(WIDTH);
        int height = parameters.getValue(HEIGHT);
        boolean setCalibration = parameters.getValue(SET_CAL);
        double xyCal = parameters.getValue(XY_CAL);
        double zCal = parameters.getValue(Z_CAL);

        // Series number comes from the Workspace
        int seriesNumber = workspace.getMetadata().getSeriesNumber();


        int[] crop = null;
        if (cropImage) crop = new int[]{left,top,width,height};

        String pathName = null;
        switch (importMode) {
            case ImportModes.CURRENT_FILE:
                pathName = workspace.getMetadata().getFile().getAbsolutePath();
                break;

            case ImportModes.MATCHING_FORMAT:
                switch (nameFormat) {
                    case NameFormats.GENERIC:
                        HCMetadata metadata = (HCMetadata) workspace.getMetadata().clone();
                        metadata.setComment(prefix);
                        pathName = getGenericName(metadata, genericFormat);
                        break;
                    case NameFormats.INPUT_FILE_PREFIX:
                        metadata = (HCMetadata) workspace.getMetadata().clone();
                        metadata.setComment(prefix);
                        pathName = getPrefixName(metadata, seriesNumber,  includeSeriesNumber,ext);
                        break;

                    case NameFormats.INPUT_FILE_SUFFIX:
                        metadata = (HCMetadata) workspace.getMetadata().clone();
                        metadata.setComment(suffix);
                        pathName = getSuffixName(metadata, seriesNumber, includeSeriesNumber,ext);
                        break;
                }
                break;

            case ImportModes.SPECIFIC_FILE:
                pathName = filePath;
                break;
        }

        if (pathName == null) return false;

        Image outputImage = null;
        try {
            outputImage = getFFMPEGVideo(pathName,outputImageName,frameRange,crop);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // If necessary, setting the spatial calibration
        if (setCalibration) {
            writeMessage("Setting spatial calibration (XY = "+xyCal+", Z = "+zCal+")");
            Calibration calibration = new Calibration();

            calibration.pixelHeight = xyCal;
            calibration.pixelWidth = xyCal;
            calibration.pixelDepth = zCal;
            calibration.setUnit(Units.getOMEUnits().getSymbol());

            outputImage.getImagePlus().setCalibration(calibration);
            outputImage.getImagePlus().updateChannelAndDraw();

        }

        // Converting RGB to 3-channel
        ImagePlus outputImagePlus = outputImage.getImagePlus();
        boolean toConvert = outputImagePlus.getBitDepth() == 24;
        if (toConvert) outputImagePlus = CompositeConverter.makeComposite(outputImagePlus);

        // Adding image to workspace
        writeMessage("Adding image (" + outputImageName + ") to workspace");
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(IMPORT_MODE, this,ImportModes.CURRENT_FILE,ImportModes.ALL));
        parameters.add(new ChoiceP(NAME_FORMAT,this,NameFormats.GENERIC,NameFormats.ALL));
        parameters.add(new StringP(GENERIC_FORMAT,this));
        parameters.add(new TextDisplayP(AVAILABLE_METADATA_FIELDS,this));
        parameters.add(new StringP(PREFIX,this));
        parameters.add(new StringP(SUFFIX,this));
        parameters.add(new StringP(EXTENSION,this));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER,this,true));
        parameters.add(new FilePathP(FILE_PATH, this));
        parameters.add(new StringP(FRAMES,this,"1-end"));
        parameters.add(new BooleanP(CROP_IMAGE, this, false));
        parameters.add(new IntegerP(LEFT, this,0));
        parameters.add(new IntegerP(TOP, this,0));
        parameters.add(new IntegerP(WIDTH, this,512));
        parameters.add(new IntegerP(HEIGHT, this,512));
        parameters.add(new BooleanP(SET_CAL, this, false));
        parameters.add(new DoubleP(XY_CAL, this, 1.0));
        parameters.add(new DoubleP(Z_CAL, this, 1.0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(IMPORT_MODE));
        switch((String) parameters.getValue(IMPORT_MODE)) {
            case ImageLoader.ImportModes.CURRENT_FILE:
            case ImageLoader.ImportModes.IMAGEJ:
                break;

            case ImageLoader.ImportModes.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(NAME_FORMAT));
                switch ((String) parameters.getValue(NAME_FORMAT)) {
                    case NameFormats.GENERIC:
                        returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                        returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                        MetadataRefCollection metadataRefs = modules.getMetadataRefs(this);
                        parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(ImageLoader.getMetadataValues(metadataRefs));
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
                }
                break;

            case ImageLoader.ImportModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(FILE_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(FRAMES));

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

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return objectMeasurementRefs;
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
