package wbif.sjx.MIA.Module.InputOutput;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

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

    static String getFileName(String plateName, String rowLetter, int columnNumber, int seriesNumber, String seriesName, String extension) {
        DecimalFormat df = new DecimalFormat("00");
        StringBuilder builder = new StringBuilder();

        builder.append(plateName)
                .append("_w")
                .append(rowLetter)
                .append(df.format(columnNumber))
                .append("_s")
                .append(seriesNumber)
                .append("_n")
                .append(seriesName)
                .append(".")
                .append(extension);

        return builder.toString();

    }

    static boolean saveImage(Image image, String rootFolder, String analysisFolderName, String plateName, String filename) {
        // Check analysis folder exists
        String folderName = rootFolder+"\\"+plateName+"\\"+analysisFolderName+"\\";

        File analysisFolder = new File(folderName);
        if (!analysisFolder.exists()) {
            if (!analysisFolder.mkdirs()) {
                return false;
            }
        }

        // Converting image to RGB
        ImagePlus ipl = image.getImagePlus();
        ipl.setDisplayMode(CompositeImage.COMPOSITE);
        ImagePlus rgbImage = ipl.flatten();

        // Writing image to file (using png to keep data size down)
        IJ.saveAs(rgbImage,"PNG",folderName+filename);

        return true;

    }

    static boolean saveFeatureNames(ObjMeasurementRefCollection refs, String rootFolder, String analysisFolderName, String plateName) {
        // Check if featureName file exists
        String folderName = rootFolder+"\\"+plateName+"\\"+analysisFolderName+"\\";
        File featureNamesFile = new File(folderName+"featureNames.acc");
        if (featureNamesFile.exists()) return true;

        // Check analysis folder exists
        File featureFolder = new File(folderName);
        if (!featureFolder.exists()) {
            if (!featureFolder.mkdirs()) {
                return false;
            }
        }

        // Compiling list of measurement names
        StringBuilder builder = new StringBuilder();
        builder.append("Location_Center_X\n")
                .append("Location_Center_Y\n");
        for (ObjMeasurementRef ref:refs.values()) {
            MIA.log.writeWarning(ref.getFinalName());
            builder.append(ref.getFinalName())
                    .append("\n");
        }

        // Writing the feature names to file
        try {
            FileWriter writer = new FileWriter(featureNamesFile);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            return false;
        }

        return true;

    }

    static boolean saveFeatures() {
        return true;
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
        int columnNumber = Integer.parseInt(workspace.getMetadata().getAsString(columnMetadataName));

        // Constructing filename
        int seriesNumber = workspace.getMetadata().getSeriesNumber();
        String seriesName = workspace.getMetadata().getSeriesName();
        String imageFilename = getFileName(plateName,rowLetter,columnNumber,seriesNumber,seriesName,"png");
        String textFilename = getFileName(plateName,rowLetter,columnNumber,seriesNumber,seriesName,"txt");

        // Storing raw image
        if (!saveImage(inputOverlayImage,rootFolder,"anal1",plateName,imageFilename)) {
            MIA.log.writeWarning("Could not write overlay image to file");
            return false;
        };
        if (!saveImage(inputRawImage,rootFolder,"anal3",plateName,imageFilename)) {
            MIA.log.writeWarning("Could not write raw image to file");
            return false;
        };

        // Getting measurements for input objects
        ObjMeasurementRefCollection measurementRefs = modules.getObjectMeasurementRefs(inputObjectsName);
        if (!saveFeatureNames(measurementRefs,rootFolder,"anal2",plateName)) {
            MIA.log.writeWarning("Could not write feature names to file");
            return false;
        };

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
