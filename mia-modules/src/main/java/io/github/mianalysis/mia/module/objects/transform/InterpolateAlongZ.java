package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSet;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeCoordinates;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.coordinates.ZInterpolator;

/**
 * 
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class InterpolateAlongZ extends Module {

    /**
	* 
	*/
    public static final String MESSAGE_SEPARATOR = "Message";

    /**
	* 
	*/
    public static final String QUALITY_MESSAGE = "Quality message";

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
    * 
    */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String INTERPOLATION_SEPARATOR = "Interpolation controls";

    /**
    * 
    */
    public static final String OUTPUT_Z_SPATIAL_CALIBRATION = "Output spatial calibration";

    public InterpolateAlongZ(Modules modules) {
        super("Interpolate along Z", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static Objs process(Objs inputObjects, double outputZCalibration, String outputObjectsName) {
        Objs outputObjects = inputObjects.duplicate(outputObjectsName, false, false, false, false);

        double inputZCalibration = outputObjects.getDppZ();
        int inputNSlices = outputObjects.getNSlices();

        double scale = inputZCalibration/outputZCalibration;
        int outputNSlices = (int) Math.round(inputNSlices*scale);
        outputObjects.getSpatialCalibration().nSlices = outputNSlices;
        outputObjects.getSpatialCalibration().dppZ = outputZCalibration;
        
        // Iterating over each coordinate, moving it to a new slice
        for (Obj outputObject:outputObjects.values()) {
            outputObject.getSpatialCalibration().nSlices = outputNSlices;
            outputObject.getSpatialCalibration().dppZ = outputZCalibration;

            CoordinateSet inputCoords = outputObject.getCoordinateSet();
            outputObject.setCoordinateSet(new QuadtreeCoordinates());
            outputObject.clearROIs();

            for (Point<Integer> pt:inputCoords) {
                try {
                    outputObject.add(pt.x,pt.y,(int) Math.round(pt.getZ()*scale));
                } catch (PointOutOfRangeException e) {
                }
            }
        }
        
        ZInterpolator.applySpatialInterpolation(outputObjects,"Quadtree");

        return outputObjects;

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        double outputZCalibration = parameters.getValue(OUTPUT_Z_SPATIAL_CALIBRATION, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectName);

        Objs outputObjects = process(inputObjects, outputZCalibration, outputObjectsName);
        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(MESSAGE_SEPARATOR, this));
        parameters.add(new MessageP(QUALITY_MESSAGE, this,
                "Interpolation works best for large objects which don't vary too much between slices.  Small objects, especially those which appear in only one slice, may end up disproportionately small.",
                ParameterState.WARNING));

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(INTERPOLATION_SEPARATOR, this));
        parameters.add(new DoubleP(OUTPUT_Z_SPATIAL_CALIBRATION, this, 1d));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

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

    void addParameterDescriptions() {

    }
}
