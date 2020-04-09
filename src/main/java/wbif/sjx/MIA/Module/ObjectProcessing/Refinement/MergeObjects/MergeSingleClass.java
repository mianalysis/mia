package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.MergeObjects;

import java.util.HashMap;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

public class MergeSingleClass extends Module {
    public final static String INPUT_SEPARATOR = "Object input";
    public final static String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output merged objects";

    public MergeSingleClass(ModuleCollection modules) {
        super("Merge single class", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_MERGE_OBJECTS;
    }

    public static ObjCollection mergeSingleClass(ObjCollection inputObjects, String outputObjectsName) {
        // Creating a HashMap to store each timepoint object instance
        HashMap<Integer,Obj> objects = new HashMap<>();

        ObjCollection outputObjects = new ObjCollection(outputObjectsName,inputObjects);

        // Iterating over all input objects, adding their coordinates to the relevant object
        for (Obj inputObject:inputObjects.values()) {
            // Getting the current timepoint instance
            int t = inputObject.getT();
            MIA.log.writeDebug("Obj "+inputObject.getID()+"_"+t);
            objects.putIfAbsent(t, outputObjects.createAndAddNewObject(inputObject.getVolumeType()).setT(t));

            // Adding coordinates to this object
            Obj outputObject = objects.get(t);
            MIA.log.writeDebug("Output "+outputObject.getID()+"_"+t);
            outputObject.getCoordinateSet().addAll(inputObject.getCoordinateSet());

        }

        return outputObjects;

    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Merging objects
        ObjCollection outputObjects = mergeSingleClass(inputObjects,outputObjectsName);

        // Adding objects to workspace
        writeMessage("Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));

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
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return "";
    }
}
