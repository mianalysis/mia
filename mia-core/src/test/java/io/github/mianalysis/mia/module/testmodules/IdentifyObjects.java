package io.github.mianalysis.mia.module.testmodules;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.ConnectivityInterface;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

public class IdentifyObjects extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String IDENTIFICATION_SEPARATOR = "Object identification";
    public static final String BINARY_LOGIC = "Binary logic";
    public static final String DETECTION_MODE = "Detection mode";
    public static final String SINGLE_OBJECT = "Identify as single object";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String VOLUME_TYPE = "Volume type";
    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String MIN_STRIP_WIDTH = "Minimum strip width (px)";

    public IdentifyObjects(Modules modules) {
        super("Identify objects", modules);
    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface DetectionModes {
        String SLICE_BY_SLICE = "2D (slice-by-slice)";
        String THREE_D = "3D";

        String[] ALL = new String[] { SLICE_BY_SLICE, THREE_D };

    }

    public interface Connectivity extends ConnectivityInterface {
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
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
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(IDENTIFICATION_SEPARATOR, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new ChoiceP(DETECTION_MODE, this, DetectionModes.THREE_D, DetectionModes.ALL));
        parameters.add(new BooleanP(SINGLE_OBJECT, this, false));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, CoordinateSetFactories.getDefaultFactoryName(), CoordinateSetFactories.listFactoryNames()));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));
        parameters.add(new IntegerP(MIN_STRIP_WIDTH, this, 60));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_IMAGE));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.get(IDENTIFICATION_SEPARATOR));
        returnedParameters.add(parameters.get(BINARY_LOGIC));
        returnedParameters.add(parameters.get(DETECTION_MODE));
        returnedParameters.add(parameters.get(SINGLE_OBJECT));
        returnedParameters.add(parameters.get(CONNECTIVITY));
        returnedParameters.add(parameters.get(VOLUME_TYPE));

        returnedParameters.add(parameters.get(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.get(ENABLE_MULTITHREADING));
        if ((boolean) parameters.getValue(ENABLE_MULTITHREADING, workspace)) {
            returnedParameters.add(parameters.get(MIN_STRIP_WIDTH));
        }

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