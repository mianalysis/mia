package wbif.sjx.MIA.Module.InputOutput;


import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class ObjectLoader extends Module {
    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String COORDINATE_SEPARATOR = "Coordinate input";
    public static final String COORDINATE_SOURCE = "Coordinate source";
    public static final String INPUT_FILE = "Input file";
    public static final String ID_COLUMN_INDEX = "ID-column index";
    public static final String X_COLUMN_INDEX = "X-column index";
    public static final String Y_COLUMN_INDEX = "Y-column index";
    public static final String Z_COLUMN_INDEX = "Z-column index";
    public static final String T_COLUMN_INDEX = "Timepoint-column index";
    public static final String APPLY_COORDINATES_FROM_ID_IMAGE = "Apply coordinates from ID image";
    public static final String COORDINATES_IMAGE = "Coordinates image";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship controls";
    public static final String CREATE_PARENTS = "Create parent objects";
    public static final String PARENT_OBJECTS_NAME = "Output parent objects name";
    public static final String PARENTS_COLUMN_INDEX = "Parent object ID index";

    public ObjectLoader(ModuleCollection modules) {
        super("Load objects",modules);
    }

    public interface CoordinateSources {
        String CURRENT_FILE = "Current file";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[]{CURRENT_FILE,SPECIFIC_FILE};

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
        MIA.log.writeWarning("Need to implement Object loader");

        return false;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(COORDINATE_SEPARATOR,this));
        parameters.add(new ChoiceP(COORDINATE_SOURCE,this,CoordinateSources.CURRENT_FILE,CoordinateSources.ALL));
        parameters.add(new FilePathP(INPUT_FILE,this));
        parameters.add(new IntegerP(ID_COLUMN_INDEX,this,0));
        parameters.add(new IntegerP(X_COLUMN_INDEX,this,1));
        parameters.add(new IntegerP(Y_COLUMN_INDEX,this,2));
        parameters.add(new IntegerP(Z_COLUMN_INDEX,this,3));
        parameters.add(new IntegerP(T_COLUMN_INDEX,this,4));
        parameters.add(new BooleanP(APPLY_COORDINATES_FROM_ID_IMAGE,this,false));
        parameters.add(new InputImageP(COORDINATES_IMAGE,this));

        parameters.add(new ParamSeparatorP(RELATIONSHIP_SEPARATOR,this));
        parameters.add(new BooleanP(CREATE_PARENTS,this,false));
        parameters.add(new OutputObjectsP(PARENT_OBJECTS_NAME,this));
        parameters.add(new IntegerP(PARENTS_COLUMN_INDEX,this,5));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
      ParameterCollection returnedParameters = new ParameterCollection();

      returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
      returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

      returnedParameters.add(parameters.get(COORDINATE_SEPARATOR));
      returnedParameters.add(parameters.get(COORDINATE_SOURCE));
      switch((String) parameters.getValue(COORDINATE_SOURCE)) {
        case CoordinateSources.SPECIFIC_FILE:
          returnedParameters.add(parameters.get(INPUT_FILE));
          break;
      }
      returnedParameters.add(parameters.get(ID_COLUMN_INDEX));
      returnedParameters.add(parameters.get(X_COLUMN_INDEX));
      returnedParameters.add(parameters.get(Y_COLUMN_INDEX));
      returnedParameters.add(parameters.get(Z_COLUMN_INDEX));
      returnedParameters.add(parameters.get(T_COLUMN_INDEX));
      returnedParameters.add(parameters.get(APPLY_COORDINATES_FROM_ID_IMAGE));
      if (parameters.getValue(APPLY_COORDINATES_FROM_ID_IMAGE)) {
        returnedParameters.add(parameters.get(COORDINATES_IMAGE));
      }

      returnedParameters.add(parameters.get(RELATIONSHIP_SEPARATOR));
      returnedParameters.add(parameters.get(CREATE_PARENTS));
      if (parameters.getValue(CREATE_PARENTS)) {
        returnedParameters.add(parameters.get(PARENT_OBJECTS_NAME));
        returnedParameters.add(parameters.get(PARENTS_COLUMN_INDEX));
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
      RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

      if (parameters.getValue(CREATE_PARENTS)) {
        String childObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME);
          returnedRelationships.add(relationshipRefs.getOrPut(parentObjectsName,childObjectsName));
      }

      return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
