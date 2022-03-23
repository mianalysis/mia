package io.github.mianalysis.mia.module.objects.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.Volume;
import ome.units.quantity.Time;
import ome.units.unit.Unit;


@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ProjectObjects extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public ProjectObjects(Modules modules) {
        super("Project objects",modules);
    }

    public static Obj process(Obj inputObject, Objs outputObjects, boolean addRelationship) throws IntegerOverflowException {
        Volume projected = inputObject.getProjected();

        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType(), inputObject.getID());
        outputObject.setCoordinateSet(projected.getCoordinateSet());
        outputObject.setT(inputObject.getT());

        if (addRelationship) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        Objs inputObjects = workspace.getObjects().get(inputObjectsName);
        SpatCal calIn = inputObjects.getSpatialCalibration();
        SpatCal calOut = new SpatCal(calIn.getDppXY(), calIn.getDppZ(), calIn.getUnits(), calIn.getWidth(),
                calIn.getHeight(), 1);
        double frameInterval = inputObjects.getFrameInterval();
        Unit<Time> temporalUnit = inputObjects.getTemporalUnit();
        Objs outputObjects = new Objs(outputObjectsName, calOut, inputObjects.getNFrames(),
                frameInterval, temporalUnit);

        for (Obj inputObject:inputObjects.values()) {
            try {
                process(inputObject,outputObjects, true);
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

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
        parameters.get(INPUT_OBJECTS).setDescription("Objects to be projected into the xy-plane.  Tese are related as a parent of their respective projected object.");

        parameters.get(OUTPUT_OBJECTS).setDescription("Output projected objects to be stored in the workspace.  These are related as children of the respective input object.");

    }
}
