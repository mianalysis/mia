package wbif.sjx.MIA.Module.InputOutput;

import javax.annotation.Nullable;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.AVI_Reader;
import ij.plugin.ChannelSplitter;
import ij.plugin.CompositeConverter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import org.apache.commons.io.FilenameUtils;
import org.janelia.it.jacs.shared.ffmpeg.FFMpegLoader;
import org.janelia.it.jacs.shared.ffmpeg.Frame;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConvertStackToTimeseries;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;
import wbif.sjx.MIA.Process.Logging.Log;
import wbif.sjx.common.Object.HCMetadata;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack.extendRangeToEnd;

public class VideoLoader extends Module {
    public static final String LOADER_SEPARATOR = "Core video loading controls";
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

    public static final String RANGE_SEPARATOR = "Dimension ranges and cropping";
    public static final String CHANNELS = "Channels";
    public static final String FRAMES = "Frames";
    public static final String CROP_IMAGE = "Crop image";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";

    public static final String CALIBRATION_SEPARATOR = "Spatial calibration";
    public static final String SET_CAL = "Set manual spatial calibration";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";


    public VideoLoader(ModuleCollection modules) {
        super("Load video",modules);
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

    public Image getAviVideo(String path, String outputImageName, String frameRange, String channelRange, @Nullable int[] crop) throws Exception {
        // Getting the first frame as a virtual stack to act as a size reference
        AVI_Reader aviReader = new AVI_Reader();
        ImageStack ist = aviReader.makeStack(path,1,0,true,false,false);

        // Testing if AVi_Reader was able to load the video.  If not, return null
        if (ist == null) return null;

        // Initialising the output ImagePlus using the first frame
        int left = 0;
        int top = 0;
        int origWidth = ist.getWidth();
        int origHeight = ist.getHeight();
        int origFrames = ist.getSize();
        int width = origWidth;
        int height = origHeight;

        // Getting an ordered list of frames to be imported
        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frameRange,true);
        if (framesList[framesList.length-1] == Integer.MAX_VALUE) framesList = extendRangeToEnd(framesList,origFrames);
        TreeSet<Integer> frames = Arrays.stream(framesList).boxed().collect(Collectors.toCollection(TreeSet::new));

        int[] channelsList = CommaSeparatedStringInterpreter.interpretIntegers(channelRange,true);
        if (channelsList[channelsList.length-1] == Integer.MAX_VALUE) channelsList = extendRangeToEnd(channelsList,3);
        TreeSet<Integer> channels = Arrays.stream(channelsList).boxed().collect(Collectors.toCollection(TreeSet::new));

        if (crop != null) {
            left = crop[0];
            top = crop[1];
            width = crop[2];
            height = crop[3];
        }

        ImagePlus ipl = IJ.createHyperStack("Image",width, height,channelsList.length,1,framesList.length,8);

        int count = 1;
        for (int frame:frames) {
            writeMessage("Loading frame "+(count)+" of "+frames.size());

            // Loading frame and converting to composite format
            ImageStack currIst = new AVI_Reader().makeStack(path,frame,frame,false,false,false);
            ImageStack[] splitIsts = ChannelSplitter.splitRGB(currIst,false);

            for (int channel:channels) {
                ipl.setPosition(channel,1,count);

                ImageProcessor ipr = splitIsts[channel-1].getProcessor(1);
                if (crop != null) {
                    ipr.setRoi(left,top,width,height);
                    ipr = ipr.crop();
                }

                ipl.setProcessor(ipr);

            }

            count++;

        }

        // This will probably load as a Z-stack rather than timeseries, so convert it to a stack
        if (((ipl.getNFrames() == 1 && ipl.getNSlices() > 1) || (ipl.getNSlices() == 1 && ipl.getNFrames() > 1) )) {
            ConvertStackToTimeseries.process(ipl);
            ipl.getCalibration().pixelDepth = 1;
        }

        return new Image(outputImageName,ipl);

    }

    public Image getFFMPEGVideo(String path, String outputImageName, String frameRange, @Nullable int[] crop) throws Exception {
        // Initialising the FFMPEG loader
        FFMpegLoader loader = new FFMpegLoader(path);
        loader.start();

        // Getting an ordered list of frames to be imported
        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frameRange,true);
        if (framesList[framesList.length-1] == Integer.MAX_VALUE) framesList = extendRangeToEnd(framesList,loader.getLengthInFrames());
        TreeSet<Integer> frames = Arrays.stream(framesList).boxed().collect(Collectors.toCollection(TreeSet::new));

        // Initialising the output ImagePlus using the first frame
        Frame frame = loader.grabFrame();
        int left = 0;
        int top = 0;
        int origWidth = loader.getImageWidth();
        int origHeight = loader.getImageHeight();
        int width = origWidth;
        int height = origHeight;

        if (crop != null) {
            left = crop[0];
            top = crop[1];
            width = crop[2];
            height = crop[3];
        }

        ImagePlus ipl = IJ.createImage("Image",width, height,framesList.length,8);

        int progressCount = 0;
        int totalFrames = loader.getLengthInFrames();
        int count = 0;
        int total = frames.size();
        for (int i=0;i<loader.getLengthInFrames();i++) {
            writeMessage("Scanning frame "+(progressCount++)+" of "+totalFrames);

            // If all the frames have been loaded we can skip anything else
            if (count == total) break;

            // Checking if the current frame is in the list to be imported
            if (!frames.contains(i+1)) {
                // We still need to progress to the next frame
                frame.release();
                frame = loader.grabFrame();
                continue;
            }

            count++;
            ipl.setPosition(count);

            writeMessage("Loading frame "+(count)+" of "+total);
            ImageProcessor ipr = new ByteProcessor(origWidth,origHeight,frame.imageBytes.get(0));
            if (crop != null) {
                ipr.setRoi(left,top,width,height);
                ipr = ipr.crop();
            }
            ipl.setProcessor(ipr);

            frame.release();
            frame = loader.grabFrame();

        }

        // This will probably load as a Z-stack rather than timeseries, so convert it to a stack
        if (((ipl.getNFrames() == 1 && ipl.getNSlices() > 1) || (ipl.getNSlices() == 1 && ipl.getNFrames() > 1) )) {
            ConvertStackToTimeseries.process(ipl);
            ipl.getCalibration().pixelDepth = 1;
        }

        return new Image(outputImageName,ipl);

    }

//    public Image getFFMPEGVideo(String path, String outputImageName, String frameRange, @Nullable int[] crop) {
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(path);
//        try {
//            grabber.start();
//            Frame frame = grabber.grab();
//            Buffer[] buffers = frame.image;
//            Indexer indexer = frame.createIndexer();
//
//            OpenCVFrameConverter converter = new OpenCVFrameConverter.ToIplImage();
//            IplImage iplImage = converter.convertToIplImage(frame);
//
//
////            IplImage iplImage = new IplImage(grabber.);
////            System.out.println(iplImage);
//
////            Arrays.stream(indexer.sizes()).forEach(System.out::println);
////
////            ImagePlus ipl = IJ.createHyperStack("Demo",frame.imageWidth,frame.imageHeight,3,1,1,8);
////
////            for (int c=0;c<indexer.channels();c++) {
////                for (int x = 0; x < indexer.width(); x++) {
////                    System.out.println(c+"_"+x);
////                    for (int y = 0; y < indexer.height(); y++) {
////                        double val = indexer.getDouble(y, x, c);
////                        ipl.setPosition(c+1,1,1);
////                        ipl.getProcessor().setf(x, y, (int) Math.round(val));
////                    }
////                }
////            }
////
////            ipl.show();
////            IJ.runMacro("waitForUser");
//
//        } catch (FrameGrabber.Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }

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
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
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
        String channelRange = parameters.getValue(CHANNELS);
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

        String extension = FilenameUtils.getExtension(pathName);
        Image outputImage = null;
        try {
            // First, trying to load via AVI Reader (since this can split channels)
            outputImage = getAviVideo(pathName,outputImageName,frameRange,channelRange,crop);

            // If this has failed (outputImage is null) try FFMPEG loading
            if (outputImage == null) {
                MIA.log.write("Video reading with AVI_Reader failed.  Trying FFMPEG (this can only load first channel).",Log.Level.MESSAGE);
                outputImage = getFFMPEGVideo(pathName,outputImageName,frameRange,crop);
            }

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
        parameters.add(new ParamSeparatorP(LOADER_SEPARATOR,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(IMPORT_MODE, this,ImportModes.CURRENT_FILE,ImportModes.ALL));
        parameters.add(new ChoiceP(NAME_FORMAT,this,NameFormats.GENERIC,NameFormats.ALL));

        parameters.add(new StringP(GENERIC_FORMAT,this));
        parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS,this,false));
        parameters.add(new StringP(PREFIX,this));
        parameters.add(new StringP(SUFFIX,this));
        parameters.add(new StringP(EXTENSION,this));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER,this,true));
        parameters.add(new FilePathP(FILE_PATH, this));

        parameters.add(new ParamSeparatorP(RANGE_SEPARATOR,this));
        parameters.add(new StringP(CHANNELS,this,"1-end"));
        parameters.add(new StringP(FRAMES,this,"1-end"));
        parameters.add(new BooleanP(CROP_IMAGE, this, false));
        parameters.add(new IntegerP(LEFT, this,0));
        parameters.add(new IntegerP(TOP, this,0));
        parameters.add(new IntegerP(WIDTH, this,512));
        parameters.add(new IntegerP(HEIGHT, this,512));

        parameters.add(new ParamSeparatorP(CALIBRATION_SEPARATOR,this));
        parameters.add(new BooleanP(SET_CAL, this, false));
        parameters.add(new DoubleP(XY_CAL, this, 1.0));
        parameters.add(new DoubleP(Z_CAL, this, 1.0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
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

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CHANNELS));
        returnedParameters.add(parameters.getParameter(FRAMES));

        returnedParameters.add(parameters.getParameter(CROP_IMAGE));
        if (parameters.getValue(CROP_IMAGE)){
            returnedParameters.add(parameters.getParameter(LEFT));
            returnedParameters.add(parameters.getParameter(TOP));
            returnedParameters.add(parameters.getParameter(WIDTH));
            returnedParameters.add(parameters.getParameter(HEIGHT));
        }

        returnedParameters.add(parameters.getParameter(CALIBRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SET_CAL));
        if (parameters.getValue(SET_CAL)) {
            returnedParameters.add(parameters.getParameter(XY_CAL));
            returnedParameters.add(parameters.getParameter(Z_CAL));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
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

    @Override
    public boolean verify() {
        return true;
    }
}
