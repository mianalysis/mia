package io.github.mianalysis.mia.module.testmodules;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
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
import io.github.mianalysis.mia.object.system.Status;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

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

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
         return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(IMPORT_MODE, this, ImportModes.CURRENT_FILE, ImportModes.ALL));
        parameters.add(new ChoiceP(READER, this, Readers.BIOFORMATS, Readers.ALL));
        parameters.add(new StringP(SEQUENCE_ROOT_NAME, this));
        parameters.add(new ChoiceP(NAME_FORMAT, this, NameFormats.GENERIC, NameFormats.ALL));
        parameters.add(new StringP(COMMENT, this));
        parameters.add(new StringP(EXTENSION, this));
        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, ParameterState.MESSAGE, 130));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER, this, true));
        parameters.add(new FilePathP(FILE_PATH, this));
        parameters.add(new ChoiceP(SERIES_MODE, this, SeriesModes.CURRENT_SERIES, SeriesModes.ALL));
        parameters.add(new StringP(SERIES_NUMBER, this, "1"));

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
}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]
