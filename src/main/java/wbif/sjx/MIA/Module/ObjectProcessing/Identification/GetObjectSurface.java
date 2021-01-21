package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Object.Volume.Volume;
import wbif.sjx.common.Object.Volume.VolumeType;

public class GetObjectSurface extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";


    public static Obj getSurface(Obj inputObject, String outputObjectsName, int outputID) {
        Volume outputVolume = inputObject.getSurface();
        Obj outputObject = new Obj(VolumeType.POINTLIST,outputObjectsName,outputID,inputObject);
        outputObject.setCoordinateSet(outputVolume.getCoordinateSet());

        return outputObject;

    }

    public GetObjectSurface(ModuleCollection modules) {
        super("Get object surface", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Create surface objects for each input object.  Surface coordinates are those with at least one " +
                "non-object neighbouring pixel (using 6-way connectivity).  Surfaces are stored as children of the " +
                "input object.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName,inputObjects);

        for (Obj inputObject:inputObjects.values()) {
            Obj outputObject = getSurface(inputObject,outputObjectsName,outputObjects.getAndIncrementID());
            outputObjects.add(outputObject);
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this, "", "Input objects to extract surface from."));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this, "", "Output surface objects to be stored in the workspace."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
