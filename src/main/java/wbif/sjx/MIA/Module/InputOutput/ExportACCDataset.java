package wbif.sjx.MIA.Module.InputOutput;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;

public class ExportACCDataset extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_RAW_IMAGE = "Input raw image";
    public static final String INPUT_OVERLAY_IMAGE = "Input overlay image";
    public static final String ROOT_DATASET_FOLDER = "Root dataset folder";
    public static final String PLATE_NAME = "Plate name";
    public static final String ROW_LETTER = "Row letter";
    public static final String COLUMN_NUMBER = "Column number";

    public ExportACCDataset(ModuleCollection modules) {
        super("Export ACC dataset", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        String inputRawImageName = parameters.getValue(INPUT_RAW_IMAGE);
        Image inputRawImage = workspace.getImage(inputRawImageName);
        String inputOverlayImageName = parameters.getValue(INPUT_OVERLAY_IMAGE);
        Image inputOverlayImage = workspace.getImage(inputOverlayImageName);
        String rootFolder = parameters.getValue(ROOT_DATASET_FOLDER);
        String plateMetadataName = parameters.getValue(PLATE_NAME);
        String plateName = workspace.getMetadata().getAsString(plateMetadataName);
        String rowMetadataName = parameters.getValue(ROW_LETTER);
        String rowLetter = workspace.getMetadata().getAsString(rowMetadataName);
        String columnMetadataName = parameters.getValue(COLUMN_NUMBER);
        String columnNumber = workspace.getMetadata().getAsString(columnMetadataName);


        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(INPUT_RAW_IMAGE,this));
        parameters.add(new InputImageP(INPUT_OVERLAY_IMAGE,this));
        parameters.add(new FolderPathP(ROOT_DATASET_FOLDER,this));
        parameters.add(new MetadataItemP(PLATE_NAME,this));
        parameters.add(new MetadataItemP(ROW_LETTER,this));
        parameters.add(new MetadataItemP(COLUMN_NUMBER,this));

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
