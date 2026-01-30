// package io.github.mianalysis.mia.module.inputoutput;

// import java.awt.image.BufferedImage;
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.util.Arrays;
// import java.util.TreeSet;
// import java.util.stream.Collectors;

// import org.apache.commons.io.FilenameUtils;
// import org.bytedeco.ffmpeg.global.avcodec;
// import org.bytedeco.javacv.FFmpegFrameGrabber;
// import org.bytedeco.javacv.FrameGrabber;
// import org.bytedeco.javacv.Java2DFrameConverter;
// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import com.drew.lang.annotations.Nullable;

// import fiji.stacks.Hyperstack_rearranger;
// import ij.IJ;
// import ij.ImagePlus;
// import ij.measure.Calibration;
// import ij.plugin.ChannelSplitter;
// import ij.plugin.HyperStackConverter;
// import ij.process.ImageProcessor;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Objs;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.WorkspaceI;
// import io.github.mianalysis.mia.object.image.ImageI;
// import io.github.mianalysis.mia.object.image.ImageFactories;
// import io.github.mianalysis.mia.object.parameters.BooleanP;
// import io.github.mianalysis.mia.object.parameters.ChoiceP;
// import io.github.mianalysis.mia.object.parameters.FilePathP;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.InputObjectsP;
// import io.github.mianalysis.mia.object.parameters.OutputImageP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.parameters.text.DoubleP;
// import io.github.mianalysis.mia.object.parameters.text.IntegerP;
// import io.github.mianalysis.mia.object.parameters.text.StringP;
// import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import io.github.mianalysis.mia.object.units.SpatialUnit;
// import io.github.mianalysis.mia.object.units.TemporalUnit;
// import io.github.mianalysis.mia.object.metadata.Metadata;
// import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;
// import ome.units.UNITS;
// import ome.units.quantity.Time;
// import ome.units.unit.Unit;

// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class VideoLoader extends Module {
//     public static final String LOADER_SEPARATOR = "Core video loading controls";
//     public static final String OUTPUT_IMAGE = "Output image";
//     public static final String IMPORT_MODE = "Import mode";
//     public static final String NAME_FORMAT = "Name format";
//     public static final String GENERIC_FORMAT = "Generic format";
//     public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
//     public static final String PREFIX = "Prefix";
//     public static final String SUFFIX = "Suffix";
//     public static final String EXTENSION = "Extension";
//     public static final String INCLUDE_SERIES_NUMBER = "Include series number";
//     public static final String FILE_PATH = "File path";

//     public static final String RANGE_SEPARATOR = "Dimension ranges and cropping";
//     public static final String CHANNELS = "Channels";
//     public static final String FRAMES = "Frames";
//     public static final String CROP_MODE = "Crop mode";
//     public static final String REFERENCE_IMAGE = "Reference image";
//     public static final String LEFT = "Left coordinate";
//     public static final String TOP = "Top coordinate";
//     public static final String WIDTH = "Width";
//     public static final String HEIGHT = "Height";
//     public static final String OBJECTS_FOR_LIMITS = "Objects for limits";
//     public static final String SCALE_MODE = "Scale mode";
//     public static final String SCALE_FACTOR_X = "X scale factor";
//     public static final String SCALE_FACTOR_Y = "Y scale factor";

//     public static final String CALIBRATION_SEPARATOR = "Spatial calibration";
//     public static final String SET_CAL = "Set manual spatial calibration";
//     public static final String XY_CAL = "XY calibration (dist/px)";
//     public static final String Z_CAL = "Z calibration (dist/px)";

//     public VideoLoader(Modules modules) {
//         super("Load video", modules);
//     }

//     public interface ImportModes {
//         String CURRENT_FILE = "Current file";
//         String MATCHING_FORMAT = "Matching format";
//         String SPECIFIC_FILE = "Specific file";

//         String[] ALL = new String[] { CURRENT_FILE, MATCHING_FORMAT, SPECIFIC_FILE };

//     }

//     public interface NameFormats {
//         String GENERIC = "Generic (from metadata)";
//         String INPUT_FILE_PREFIX = "Input filename with prefix";
//         String INPUT_FILE_SUFFIX = "Input filename with suffix";

//         String[] ALL = new String[] { GENERIC, INPUT_FILE_PREFIX, INPUT_FILE_SUFFIX };

//     }

//     public interface CropModes {
//         String NONE = "None";
//         String FIXED = "Fixed";
//         String FROM_REFERENCE = "From reference";
//         String OBJECT_COLLECTION_LIMITS = "Object collection limits";

//         String[] ALL = new String[] { NONE, FIXED, FROM_REFERENCE, OBJECT_COLLECTION_LIMITS };

//     }

//     public interface ScaleModes {
//         String NONE = "No scaling";
//         String NO_INTERPOLATION = "Scaling (no interpolation)";
//         String BILINEAR = "Scaling (bilinear)";
//         String BICUBIC = "Scaling (bicubic)";

//         String[] ALL = new String[] { NONE, NO_INTERPOLATION, BILINEAR, BICUBIC };

//     }

//     public interface Measurements {
//         String ROI_LEFT = "VIDEO_LOADING // ROI_LEFT (PX)";
//         String ROI_TOP = "VIDEO_LOADING // ROI_TOP (PX)";
//         String ROI_WIDTH = "VIDEO_LOADING // ROI_WIDTH (PX)";
//         String ROI_HEIGHT = "VIDEO_LOADING // ROI_HEIGHT (PX)";

//     }

//     public String getGenericName(Metadata metadata, String genericFormat) {
//         String absolutePath = metadata.getFile().getAbsolutePath();
//         String path = FilenameUtils.getFullPath(absolutePath);
//         String filename;
//         try {
//             filename = ImageLoader.getGenericName(metadata, genericFormat);
//             return path + filename;
//         } catch (Exception e) {
//             MIA.log.writeWarning("Can't determine filename format");
//             return null;
//         }
//     }

//     public String getPrefixName(Metadata metadata, boolean includeSeries, String ext) {
//         String absolutePath = metadata.getFile().getAbsolutePath();
//         String path = FilenameUtils.getFullPath(absolutePath);
//         String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
//         String comment = metadata.getComment();
//         String series = includeSeries ? "_S" + metadata.getSeriesNumber() : "";

//         return path + comment + name + series + "." + ext;

//     }

//     public String getSuffixName(Metadata metadata, boolean includeSeries, String ext) {
//         String absolutePath = metadata.getFile().getAbsolutePath();
//         String path = FilenameUtils.getFullPath(absolutePath);
//         String name = FilenameUtils.removeExtension(FilenameUtils.getName(absolutePath));
//         String comment = metadata.getComment();
//         String series = includeSeries ? "_S" + metadata.getSeriesNumber() : "";

//         return path + name + series + comment + "." + ext;

//     }

//     @Override
//     public Category getCategory() {
//         return Categories.INPUT_OUTPUT;
//     }

//     @Override
//     public String getDescription() {
//         return "Uses JavaCV to import videos.";
//     }

//     public static ImagePlus getVideo(String path, String frameRange, String channelRange, @Nullable int[] crop,
//             double[] scaleFactors, String scaleMode)
//             throws FrameGrabber.Exception, FileNotFoundException, FrameOutOfRangeException {
//         String outputName = new File(path).getName();

//         // Initialising the video loader and converter
        
//         Java2DFrameConverter frameConverter = new Java2DFrameConverter();
//         FFmpegFrameGrabber loader = new FFmpegFrameGrabber(path);   
//         loader.start();  
        
//         // Getting an ordered list of frames to be imported
//         int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frameRange, true,
//                 loader.getLengthInFrames());
//         int maxFrames = loader.getLengthInFrames();
//         if (framesList[framesList.length - 1] > maxFrames) {
//             loader.close();
//             throw new FrameOutOfRangeException("Specified frame range (" + framesList[0] + "-"
//                     + framesList[framesList.length - 1] + ") exceeds video length (" + maxFrames + " frames).");
//         }
//         TreeSet<Integer> frames = Arrays.stream(framesList).boxed().collect(Collectors.toCollection(TreeSet::new));

//         int[] channelsList = CommaSeparatedStringInterpreter.interpretIntegers(channelRange, true,
//                 loader.getPixelFormat());
//         TreeSet<Integer> channels = Arrays.stream(channelsList).boxed().collect(Collectors.toCollection(TreeSet::new));

//         int left = 0;
//         int top = 0;
//         int width = loader.getImageWidth();
//         int height = loader.getImageHeight();

//         if (crop != null) {
//             left = crop[0];
//             top = crop[1];
//             width = crop[2];
//             height = crop[3];
//         }

//         int widthOut = width;
//         int heightOut = height;

//         // Applying scaling
//         switch (scaleMode) {
//             case ScaleModes.NONE:
//                 scaleFactors[0] = 1;
//                 scaleFactors[1] = 1;
//                 break;
//             case ScaleModes.NO_INTERPOLATION:
//             case ScaleModes.BILINEAR:
//             case ScaleModes.BICUBIC:
//                 widthOut = (int) Math.round(width * scaleFactors[0]);
//                 heightOut = (int) Math.round(height * scaleFactors[1]);
//                 break;
//         }

//         ImagePlus ipl = IJ.createHyperStack(outputName, widthOut, heightOut, channelsList.length, 1, framesList.length,
//                 8);
//         int count = 1;
//         int total = frames.size();
        
//         for (int frame : frames) {
//             loader.setVideoFrameNumber(frame - 1);
            
//             BufferedImage im = frameConverter.convert(loader.grabImage());
//             MIA.log.writeDebug("B "+im);
//             ImagePlus frameIpl = new ImagePlus("Temporary", im);
//             MIA.log.writeDebug(frame + "_" + frameIpl.getNChannels() + "_" + frameIpl.getNSlices() + "_"
//                     + frameIpl.getNFrames() + "_" + frameIpl.getBitDepth());

//             for (int channel : channels) {
//                 ipl.setPosition(channel, 1, count);

//                 ImageProcessor ipr = ChannelSplitter.getChannel(frameIpl, channel).getProcessor(1);

//                 if (crop != null) {
//                     ipr.setRoi(left, top, width, height);
//                     ipr = ipr.crop();
//                 }

//                 // Applying scaling
//                 switch (scaleMode) {
//                     case ScaleModes.NO_INTERPOLATION:
//                         ipr.setInterpolationMethod(ImageProcessor.NONE);
//                         ipr = ipr.resize(widthOut, heightOut);
//                         break;
//                     case ScaleModes.BILINEAR:
//                         ipr.setInterpolationMethod(ImageProcessor.BILINEAR);
//                         ipr = ipr.resize(widthOut, heightOut);
//                         break;
//                     case ScaleModes.BICUBIC:
//                         ipr.setInterpolationMethod(ImageProcessor.BICUBIC);
//                         ipr = ipr.resize(widthOut, heightOut);
//                         break;
//                 }

//                 ipl.setProcessor(ipr);

//             }

//             // for (int channel:channels) {
//             // int frameIdx = frameIpl.getStackIndex(channel, 1, 1);
//             // int iplIdx = ipl.getStackIndex(channel, 1, count);
//             // ImageProcessor frameIpr = frameIpl.getStack().getProcessor(frameIdx);

//             // if (crop != null) {
//             // frameIpr.setRoi(left,top,width,height);
//             // frameIpr = frameIpr.crop();
//             // }

//             // ipl.getStack().setProcessor(frameIpr,iplIdx);

//             // }

//             count++;

//             writeProgressStatus(count, total, "Frames", "Video loader");

//         }

//         // This will probably load as a Z-stack rather than timeseries, so convert it to
//         // a stack
//         if (((ipl.getNFrames() == 1 && ipl.getNSlices() > 1) || (ipl.getNSlices() == 1 && ipl.getNFrames() > 1))) {
//             convertToTimeseries(ipl);
//             ipl.getCalibration().pixelDepth = 1;
//         }

//         double fps = loader.getFrameRate();
//         setTemporalCalibration(ipl, fps);

//         ipl.setPosition(1);
//         ipl.updateChannelAndDraw();

//         // Closing the loader
//         loader.close();

//         return ipl;

//     }

//     public static void convertToTimeseries(ImagePlus inputImagePlus) {
//         int nChannels = inputImagePlus.getNChannels();
//         int nFrames = inputImagePlus.getNFrames();
//         int nSlices = inputImagePlus.getNSlices();
//         if (inputImagePlus.getNSlices() != 1 || inputImagePlus.getNFrames() <= 1) {
//             ImagePlus processedImagePlus = HyperStackConverter.toHyperStack(inputImagePlus, nChannels, nFrames,
//                     nSlices);
//             processedImagePlus = Hyperstack_rearranger.reorderHyperstack(processedImagePlus, "CTZ", true, false);
//             inputImagePlus.setStack(processedImagePlus.getStack());
//         }
//     }

//     public static int[] extendRangeToEnd(int[] inputRange, int end) {
//         TreeSet<Integer> values = new TreeSet<>();

//         int start;
//         for (start = 0; start < inputRange.length - 3; ++start) {
//             values.add(inputRange[start]);
//         }

//         start = inputRange[inputRange.length - 3];
//         int interval = inputRange[inputRange.length - 2] - start;

//         for (int i = start; i <= end; i += interval) {
//             values.add(i);
//         }

//         return values.stream().mapToInt(Integer::intValue).toArray();
//     }

//     public static void setTemporalCalibration(ImagePlus ipl, double fps) {
//         Unit<Time> temporalUnits = TemporalUnit.getOMEUnit();

//         Calibration cal = ipl.getCalibration();
//         cal.setTimeUnit(temporalUnits.getSymbol());
//         cal.fps = fps;
//         cal.frameInterval = UNITS.SECOND.convertValue(1 / fps, temporalUnits);

//     }

//     @Override
//     public Status process(WorkspaceI workspace) {
//         // Getting parameters
//         String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
//         String importMode = parameters.getValue(IMPORT_MODE, workspace);
//         String filePath = parameters.getValue(FILE_PATH, workspace);
//         String nameFormat = parameters.getValue(NAME_FORMAT, workspace);

//         String genericFormat = parameters.getValue(GENERIC_FORMAT, workspace);
//         String prefix = parameters.getValue(PREFIX, workspace);
//         String suffix = parameters.getValue(SUFFIX, workspace);
//         String ext = parameters.getValue(EXTENSION, workspace);
//         boolean includeSeriesNumber = parameters.getValue(INCLUDE_SERIES_NUMBER, workspace);
//         String channelRange = parameters.getValue(CHANNELS, workspace);
//         String frameRange = parameters.getValue(FRAMES, workspace);
//         String cropMode = parameters.getValue(CROP_MODE, workspace);
//         String referenceImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
//         int left = parameters.getValue(LEFT, workspace);
//         int top = parameters.getValue(TOP, workspace);
//         int width = parameters.getValue(WIDTH, workspace);
//         int height = parameters.getValue(HEIGHT, workspace);
//         String objectsForLimitsName = parameters.getValue(OBJECTS_FOR_LIMITS, workspace);
//         String scaleMode = parameters.getValue(SCALE_MODE, workspace);
//         double scaleFactorX = parameters.getValue(SCALE_FACTOR_X, workspace);
//         double scaleFactorY = parameters.getValue(SCALE_FACTOR_Y, workspace);
//         boolean setCalibration = parameters.getValue(SET_CAL, workspace);
//         double xyCal = parameters.getValue(XY_CAL, workspace);
//         double zCal = parameters.getValue(Z_CAL, workspace);

//         int[] crop = null;
//         switch (cropMode) {
//             case CropModes.FIXED:
//                 crop = new int[] { left, top, width, height };
//                 break;
//             case CropModes.FROM_REFERENCE:
//                 // Displaying the image
//                 ImageI referenceImage = workspace.getImage(referenceImageName);
//                 crop = ImageLoader.getCropROI(referenceImage);
//                 break;
//             case CropModes.OBJECT_COLLECTION_LIMITS:
//                 Objs objectsForLimits = workspace.getObjects(objectsForLimitsName);
//                 int[][] limits = objectsForLimits.getSpatialExtents();
//                 crop = new int[] { limits[0][0], limits[1][0], limits[0][1] - limits[0][0],
//                         limits[1][1] - limits[1][0] };
//                 break;
//         }

//         if (scaleMode.equals(ScaleModes.NONE)) {
//             scaleFactorX = 1;
//             scaleFactorY = 1;
//         }
//         double[] scaleFactors = new double[] { scaleFactorX, scaleFactorY };

//         String pathName = null;
//         switch (importMode) {
//             case ImportModes.CURRENT_FILE:
//                 pathName = workspace.getMetadata().getFile().getAbsolutePath();
//                 break;

//             case ImportModes.MATCHING_FORMAT:
//                 switch (nameFormat) {
//                     case NameFormats.GENERIC:
//                         Metadata metadata = (Metadata) workspace.getMetadata().clone();
//                         metadata.setComment(prefix);
//                         pathName = getGenericName(metadata, genericFormat);
//                         break;
//                     case NameFormats.INPUT_FILE_PREFIX:
//                         metadata = (Metadata) workspace.getMetadata().clone();
//                         metadata.setComment(prefix);
//                         pathName = getPrefixName(metadata, includeSeriesNumber, ext);
//                         break;

//                     case NameFormats.INPUT_FILE_SUFFIX:
//                         metadata = (Metadata) workspace.getMetadata().clone();
//                         metadata.setComment(suffix);
//                         pathName = getSuffixName(metadata, includeSeriesNumber, ext);
//                         break;
//                 }
//                 break;

//             case ImportModes.SPECIFIC_FILE:
//                 pathName = filePath;
//                 break;
//         }

//         if (pathName == null)
//             return Status.FAIL;

//         ImageI outputImage = null;
//         try {
//             // First first, testing new loader
//             ImagePlus outputIpl = getVideo(pathName, frameRange, channelRange, crop, scaleFactors, scaleMode);
//             outputImage = ImageFactories.getDefaultFactory().create(outputImageName, outputIpl);

//         } catch (FrameOutOfRangeException e1) {
//             MIA.log.writeWarning(e1.getMessage());
//             return Status.FAIL;
//         } catch (Exception e) {
//             e.printStackTrace(System.err);
//             MIA.log.writeError("Unable to read video.  Skipping this file.");
//             return Status.FAIL;
//         }

//         // If necessary, setting the spatial calibration
//         if (setCalibration) {            
//             Calibration calibration = new Calibration();

//             calibration.pixelHeight = xyCal / scaleFactorX;
//             calibration.pixelWidth = xyCal / scaleFactorY;
//             calibration.pixelDepth = zCal;
//             calibration.setUnit(SpatialUnit.getOMEUnit().getSymbol());

//             outputImage.getImagePlus().setCalibration(calibration);
//             outputImage.getImagePlus().updateChannelAndDraw();

//         }

//         // Adding image to workspace
//         workspace.addImage(outputImage);

//         if (showOutput)
//             outputImage.show(outputImageName, null, false, true);

//         return Status.PASS;

//     }

//     @Override
//     public void initialiseParameters() {
//         parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
//         parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
//         parameters.add(new ChoiceP(IMPORT_MODE, this, ImportModes.CURRENT_FILE, ImportModes.ALL));
//         parameters.add(new ChoiceP(NAME_FORMAT, this, NameFormats.GENERIC, NameFormats.ALL));

//         parameters.add(new StringP(GENERIC_FORMAT, this));
//         parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS, this, false));
//         parameters.add(new StringP(PREFIX, this));
//         parameters.add(new StringP(SUFFIX, this));
//         parameters.add(new StringP(EXTENSION, this));
//         parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER, this, true));
//         parameters.add(new FilePathP(FILE_PATH, this));

//         parameters.add(new SeparatorP(RANGE_SEPARATOR, this));
//         parameters.add(new StringP(CHANNELS, this, "1-end"));
//         parameters.add(new StringP(FRAMES, this, "1-end"));
//         parameters.add(new ChoiceP(CROP_MODE, this, CropModes.NONE, CropModes.ALL,
//                 "Choice of loading the entire frame, or cropping in XY.<br>" + "<br>- \"" + CropModes.NONE
//                         + "\" (default) will load the entire frame in XY.<br>" + "<br>- \"" + CropModes.FIXED
//                         + "\" will apply a pre-defined crop to the input frame based on the parameters \"Left\", \"Top\",\"Width\" and \"Height\".<br>"
//                         + "<br>- \"" + CropModes.FROM_REFERENCE
//                         + "\" will display a specified image and ask the user to select a region to crop the input frame to."));
//         parameters.add(new InputImageP(REFERENCE_IMAGE, this, "",
//                 "The frame to be displayed for selection of the cropping region if the cropping mode is set to \""
//                         + CropModes.FROM_REFERENCE + "\"."));
//         parameters.add(new IntegerP(LEFT, this, 0));
//         parameters.add(new IntegerP(TOP, this, 0));
//         parameters.add(new IntegerP(WIDTH, this, 512));
//         parameters.add(new IntegerP(HEIGHT, this, 512));
//         parameters.add(new InputObjectsP(OBJECTS_FOR_LIMITS, this));
//         parameters.add(new ChoiceP(SCALE_MODE, this, ScaleModes.NONE, ScaleModes.ALL));
//         parameters.add(new DoubleP(SCALE_FACTOR_X, this, 1));
//         parameters.add(new DoubleP(SCALE_FACTOR_Y, this, 1));

//         parameters.add(new SeparatorP(CALIBRATION_SEPARATOR, this));
//         parameters.add(new BooleanP(SET_CAL, this, false));
//         parameters.add(new DoubleP(XY_CAL, this, 1.0));
//         parameters.add(new DoubleP(Z_CAL, this, 1.0));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         Parameters returnedParameters = new Parameters();

//         returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
//         returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

//         returnedParameters.add(parameters.getParameter(IMPORT_MODE));
//         switch ((String) parameters.getValue(IMPORT_MODE, null)) {
//             case ImageLoader.ImportModes.CURRENT_FILE:
//             case ImageLoader.ImportModes.IMAGEJ:
//                 break;

//             case ImageLoader.ImportModes.MATCHING_FORMAT:
//                 returnedParameters.add(parameters.getParameter(NAME_FORMAT));
//                 switch ((String) parameters.getValue(NAME_FORMAT, null)) {
//                     case NameFormats.GENERIC:
//                         returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
//                         returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
//                         MetadataRefs metadataRefs = modules.getMetadataRefs(this);
//                         parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
//                         break;
//                     case NameFormats.INPUT_FILE_PREFIX:
//                         returnedParameters.add(parameters.getParameter(PREFIX));
//                         returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
//                         returnedParameters.add(parameters.getParameter(EXTENSION));
//                         break;
//                     case NameFormats.INPUT_FILE_SUFFIX:
//                         returnedParameters.add(parameters.getParameter(SUFFIX));
//                         returnedParameters.add(parameters.getParameter(INCLUDE_SERIES_NUMBER));
//                         returnedParameters.add(parameters.getParameter(EXTENSION));
//                         break;
//                 }
//                 break;

//             case ImageLoader.ImportModes.SPECIFIC_FILE:
//                 returnedParameters.add(parameters.getParameter(FILE_PATH));
//                 break;
//         }

//         returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
//         returnedParameters.add(parameters.getParameter(CHANNELS));
//         returnedParameters.add(parameters.getParameter(FRAMES));

//         returnedParameters.add(parameters.getParameter(CROP_MODE));
//         switch ((String) parameters.getValue(CROP_MODE, null)) {
//             case CropModes.FIXED:
//                 returnedParameters.add(parameters.getParameter(LEFT));
//                 returnedParameters.add(parameters.getParameter(TOP));
//                 returnedParameters.add(parameters.getParameter(WIDTH));
//                 returnedParameters.add(parameters.getParameter(HEIGHT));
//                 break;
//             case CropModes.FROM_REFERENCE:
//                 returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
//                 break;
//             case CropModes.OBJECT_COLLECTION_LIMITS:
//                 returnedParameters.add(parameters.getParameter(OBJECTS_FOR_LIMITS));
//                 break;
//         }

//         returnedParameters.add(parameters.getParameter(SCALE_MODE));
//         switch ((String) parameters.getValue(SCALE_MODE, null)) {
//             case ScaleModes.NO_INTERPOLATION:
//             case ScaleModes.BILINEAR:
//             case ScaleModes.BICUBIC:
//                 returnedParameters.add(parameters.getParameter(SCALE_FACTOR_X));
//                 returnedParameters.add(parameters.getParameter(SCALE_FACTOR_Y));
//                 break;
//         }

//         returnedParameters.add(parameters.getParameter(CALIBRATION_SEPARATOR));
//         returnedParameters.add(parameters.getParameter(SET_CAL));
//         if ((boolean) parameters.getValue(SET_CAL, null)) {
//             returnedParameters.add(parameters.getParameter(XY_CAL));
//             returnedParameters.add(parameters.getParameter(Z_CAL));
//         }

//         return returnedParameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();
//         String outputImageName = parameters.getValue(OUTPUT_IMAGE, null);

//         switch ((String) parameters.getValue(CROP_MODE, null)) {
//             case CropModes.FROM_REFERENCE:
//                 returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_LEFT).setImageName(outputImageName));
//                 returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_TOP).setImageName(outputImageName));
//                 returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_WIDTH).setImageName(outputImageName));
//                 returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.ROI_HEIGHT).setImageName(outputImageName));

//                 break;
//         }

//         return returnedRefs;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }
// }
