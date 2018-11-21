//package wbif.sjx.ModularImageAnalysis.Module.InputOutput;
//
//import ij.IJ;
//import ij.ImagePlus;
//import ij.process.ByteProcessor;
//import org.janelia.it.jacs.shared.ffmpeg.FFMpegLoader;
//import org.janelia.it.jacs.shared.ffmpeg.Frame;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//
//import javax.annotation.Nullable;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.ExtractSubstack.interpretRange;
//
//public class VideoLoader extends Module {
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String IMPORT_MODE = "Import mode";
//    public static final String NAME_FORMAT = "Name format";
//    public static final String PREFIX = "Prefix";
//    public static final String SUFFIX = "Suffix";
//    public static final String EXTENSION = "Extension";
//    public static final String INCLUDE_SERIES_NUMBER = "Include series number";
//    public static final String FILE_PATH = "File path";
//    public static final String FRAMES = "Frames";
//    public static final String CROP_IMAGE = "Crop image";
//    public static final String LEFT = "Left coordinate";
//    public static final String TOP = "Top coordinate";
//    public static final String WIDTH = "Width";
//    public static final String HEIGHT = "Height";
//    public static final String SET_CAL = "Set manual spatial calibration";
//    public static final String XY_CAL = "XY calibration (dist/px)";
//    public static final String Z_CAL = "Z calibration (dist/px)";
//
//
//    public ImagePlus getFFMPEGVideo(String path, @Nullable String[] dimRanges, @Nullable int[] crop) throws Exception {
//        // The following works, but loads the entire video to RAM without an apparent way to prevent this
//        FFMpegLoader loader = new FFMpegLoader(path);
//        loader.start();
//
//        Frame frame = loader.grabFrame();
//        int width = loader.getImageWidth();
//        int height = loader.getImageHeight();
//        int[] framesList = interpretRange(dimRanges[2]);
//        List<Integer> frames = Arrays.stream(framesList).boxed().collect(Collectors.toList());
//
//        ImagePlus ipl = IJ.createImage("Image",width, height,framesList.length,8);
//        for (int i=0;i<framesList[framesList.length-1];i++) {
//            if (!frames.contains(i)) continue;
//
//            ipl.setPosition(i+1);
//            ipl.setProcessor(new ByteProcessor(width,height,frame.imageBytes.get(0)));
//            frame = loader.grabFrame();
//
//        }
//
//        return ipl;
//
//    }
//
//    public interface ImportModes {
//        String CURRENT_FILE = "Current file";
//        String MATCHING_FORMAT = "Matching format";
//        String SPECIFIC_FILE = "Specific file";
//
//        String[] ALL = new String[]{CURRENT_FILE, MATCHING_FORMAT, SPECIFIC_FILE};
//
//    }
//
//    public interface NameFormats {
//        String INPUT_FILE_PREFIX = "Input filename with prefix";
//        String INPUT_FILE_SUFFIX = "Input filename with suffix";
//
//        String[] ALL = new String[]{INPUT_FILE_PREFIX,INPUT_FILE_SUFFIX};
//
//    }
//
//    @Override
//    public String getTitle() {
//        return null;
//    }
//
//    @Override
//    public String getPackageName() {
//        return null;
//    }
//
//    @Override
//    public String getHelp() {
//        return null;
//    }
//
//    @Override
//    protected void run(Workspace workspace) {
//
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
//        parameters.add(new Parameter(IMPORT_MODE, Parameter.CHOICE_ARRAY,ImportModes.CURRENT_FILE,ImportModes.ALL));
//        parameters.add(new Parameter(NAME_FORMAT,Parameter.CHOICE_ARRAY,NameFormats.INPUT_FILE_PREFIX,NameFormats.ALL));
//        parameters.add(new Parameter(PREFIX,Parameter.STRING,""));
//        parameters.add(new Parameter(SUFFIX,Parameter.STRING,""));
//        parameters.add(new Parameter(EXTENSION,Parameter.STRING,""));
//        parameters.add(new Parameter(INCLUDE_SERIES_NUMBER,Parameter.BOOLEAN,true));
//        parameters.add(new Parameter(FILE_PATH, Parameter.FILE_PATH,null));
//        parameters.add(new Parameter(FRAMES,Parameter.STRING,"1-end"));
//        parameters.add(new Parameter(CROP_IMAGE, Parameter.BOOLEAN, false));
//        parameters.add(new Parameter(LEFT, Parameter.INTEGER,0));
//        parameters.add(new Parameter(TOP, Parameter.INTEGER,0));
//        parameters.add(new Parameter(WIDTH, Parameter.INTEGER,512));
//        parameters.add(new Parameter(HEIGHT, Parameter.INTEGER,512));
//        parameters.add(new Parameter(SET_CAL, Parameter.BOOLEAN, false));
//        parameters.add(new Parameter(XY_CAL, Parameter.DOUBLE, 1.0));
//        parameters.add(new Parameter(Z_CAL, Parameter.DOUBLE, 1.0));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//
//                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
//
//        returnedParameters.add(parameters.getParameter(IMPORT_MODE));
//        switch((String) parameters.getValue(IMPORT_MODE)) {
//            case ImageLoader.ImportModes.CURRENT_FILE:
//            case ImageLoader.ImportModes.IMAGEJ:
//                break;
//
//            case ImageLoader.ImportModes.MATCHING_FORMAT:
//                returnedParameters.add(parameters.getParameter(NAME_FORMAT));
//                switch ((String) parameters.getValue(NAME_FORMAT)) {
//                    case ImageLoader.NameFormats.INPUT_FILE_PREFIX:
//                        returnedParameters.add(parameters.getParameter(PREFIX));
//                        returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
//                        returnedParameters.add(parameters.getParameter(EXTENSION));
//                        break;
//                    case ImageLoader.NameFormats.INPUT_FILE_SUFFIX:
//                        returnedParameters.add(parameters.getParameter(SUFFIX));
//                        returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
//                        returnedParameters.add(parameters.getParameter(EXTENSION));
//                        break;
//                }
//                break;
//
//            case ImageLoader.ImportModes.SPECIFIC_FILE:
//                returnedParameters.add(parameters.getParameter(FILE_PATH));
//                break;
//        }
//
//            returnedParameters.add(parameters.getParameter(FRAMES));
//
//        returnedParameters.add(parameters.getParameter(CROP_IMAGE));
//        if (parameters.getValue(CROP_IMAGE)){
//            returnedParameters.add(parameters.getParameter(LEFT));
//            returnedParameters.add(parameters.getParameter(TOP));
//            returnedParameters.add(parameters.getParameter(WIDTH));
//            returnedParameters.add(parameters.getParameter(HEIGHT));
//        }
//
//        returnedParameters.add(parameters.getParameter(SET_CAL));
//        if (parameters.getValue(SET_CAL)) {
//            returnedParameters.add(parameters.getParameter(XY_CAL));
//            returnedParameters.add(parameters.getParameter(Z_CAL));
//        }
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public MetadataReferenceCollection updateAndGetMetadataReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
