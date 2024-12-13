package io.github.mianalysis.mia.module.testmodules;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

public class FilterImage extends Module {

    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String FILTER_SEPARATOR = "Filter controls";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius";
    public static final String FILTER_RADIUS_2 = "Filter radius 2";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String ROLLING_METHOD = "Rolling filter method";
    public static final String WINDOW_INDICES = "Window indices";
    public static final String CONTOUR_CONTRAST = "Contour contrast";

    public FilterImage(Modules modules) {
        super("Filter image", modules);
    }

    public interface FilterModes {
        String DOG2D = "DoG 2D";
        String GAUSSIAN2D = "Gaussian 2D"; // Tested
        String GAUSSIAN3D = "Gaussian 3D"; // Tested
        String GRADIENT2D = "Gradient 2D";
        String LOG2DAPPROX = "LoG 2D (approximation)";
        String MAXIMUM2D = "Maximum 2D";
        String MAXIMUM3D = "Maximum 3D";
        String MEAN2D = "Mean 2D";
        String MEAN3D = "Mean 3D";
        String MEDIAN2D = "Median 2D";
        String MEDIAN3D = "Median 3D";
        String MINIMUM2D = "Minimum 2D";
        String MINIMUM3D = "Minimum 3D";
        String RIDGE_ENHANCEMENT = "Ridge enhancement 2D";
        String ROLLING_FRAME = "Rolling frame";
        String VARIANCE2D = "Variance 2D";
        String VARIANCE3D = "Variance 3D";

        String[] ALL = new String[] { DOG2D, GAUSSIAN2D, GAUSSIAN3D, GRADIENT2D, LOG2DAPPROX, MAXIMUM2D, MAXIMUM3D,
                MEAN2D, MEAN3D,
                MEDIAN2D, MEDIAN3D, MINIMUM2D, MINIMUM3D, RIDGE_ENHANCEMENT, ROLLING_FRAME, VARIANCE2D, VARIANCE3D };

    }

    public interface RollingMethods {
        String AVERAGE = "Average";
        String MEDIAN = "Median";
        String MINIMUM = "Minimum";
        String MAXIMUM = "Maximum";
        String STDEV = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[] { AVERAGE, MEDIAN, MINIMUM, MAXIMUM, STDEV, SUM };

    }

    public interface WindowModes {
        String BOTH_SIDES = "Both sides";
        String PREVIOUS = "Previous only";
        String FUTURE = "Future only";

        String[] ALL = new String[] { BOTH_SIDES, PREVIOUS, FUTURE };

    }

    public interface ContourContrast {
        String DARK_LINE = "Dark line";
        String LIGHT_LINE = "Light line";

        String[] ALL = new String[] { DARK_LINE, LIGHT_LINE };

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
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
    public Status process(WorkspaceI workspace) {
        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.LOG2DAPPROX, FilterModes.ALL));
        parameters.add(new DoubleP(FILTER_RADIUS, this, 2d));
        parameters.add(new DoubleP(FILTER_RADIUS_2, this, 3d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new ChoiceP(ROLLING_METHOD, this, RollingMethods.AVERAGE, RollingMethods.ALL));
        parameters.add(new StringP(WINDOW_INDICES, this, "-1-1"));
        parameters.add(new ChoiceP(CONTOUR_CONTRAST, this, ContourContrast.DARK_LINE, ContourContrast.ALL));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (!parameters.getValue(FILTER_MODE, workspace).equals(FilterModes.ROLLING_FRAME)) {
            returnedParameters.add(parameters.getParameter(FILTER_RADIUS));
            if (((String) parameters.getValue(FILTER_MODE, workspace)).equals(FilterModes.DOG2D))
                returnedParameters.add(parameters.getParameter(FILTER_RADIUS_2));
            returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        } else {
            returnedParameters.add(parameters.getParameter(ROLLING_METHOD));
            returnedParameters.add(parameters.getParameter(WINDOW_INDICES));

        }

        if (parameters.getValue(FILTER_MODE, workspace).equals(FilterModes.RIDGE_ENHANCEMENT))
            returnedParameters.add(parameters.getParameter(CONTOUR_CONTRAST));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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
        return true;
    }
}
