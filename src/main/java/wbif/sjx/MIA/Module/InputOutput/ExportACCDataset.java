package wbif.sjx.MIA.Module.InputOutput;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.TreeMap;

public class ExportACCDataset extends Module {
    public static final String INPUT_SEPARATOR = "Image/objects input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_RAW_IMAGE = "Input raw image";
    public static final String INPUT_OVERLAY_IMAGE = "Input overlay image";
    public static final String OUTPUT_SEPARATOR = "Dataset output";
    public static final String ROOT_DATASET_FOLDER = "Root dataset folder";
    public static final String PLATE_NAME = "Plate name";
    public static final String ROW_LETTER = "Row letter";
    public static final String COLUMN_NUMBER = "Column number";
    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";
    public static final String SHOW_MEASUREMENTS = "Show measurement selection";
    public static final String MEASUREMENTS = "Measurements";

    public ExportACCDataset(ModuleCollection modules) {
        super("Export ACC dataset", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    static String getFolderName(String rootFolder, String analysisFolderName, String plateName) {
        return rootFolder+"\\"+plateName+"\\"+analysisFolderName+"\\";

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

    static boolean saveImage(Image image, String folderName, String fileName) {
        // Check analysis folder exists
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
        IJ.saveAs(rgbImage,"PNG",folderName+fileName);

        return true;

    }

    static boolean saveFeatureNames(ObjMeasurementRefCollection refs, TreeMap<String,Boolean> states, String folderName) {
        // Check if featureName file exists
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
            if (!states.containsKey(ref.getName())) continue;
            if (!states.get(ref.getName())) continue;

            builder.append(ref.getFinalName().replace(" ","_"))
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

    static boolean saveFeatures(ObjCollection objects, ObjMeasurementRefCollection refs, TreeMap<String,Boolean> states, String folderName, String fileName) {
        DecimalFormat df = new DecimalFormat("0.0000000E0");

        // Check analysis folder exists
        File featureFolder = new File(folderName);
        if (!featureFolder.exists()) {
            if (!featureFolder.mkdirs()) {
                return false;
            }
        }

        StringBuilder builder = new StringBuilder();

        for (Obj obj:objects.values()) {
            // Adding centroid
            builder.append(df.format(obj.getXMean(true)))
                    .append(" ")
                    .append(df.format(obj.getYMean(true)));

            // Adding extra features
            for (ObjMeasurementRef ref:refs.values()) {
                if (!states.containsKey(ref.getName())) continue;
                if (!states.get(ref.getName())) continue;

                builder.append(" ")
                        .append(df.format(obj.getMeasurement(ref.getName()).getValue()));
            }

            builder.append("\n");

        }

        // Writing the features to file
        try {
            File featuresFile = new File(folderName+fileName);
            FileWriter writer = new FileWriter(featuresFile);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            return false;
        }

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
        TreeMap<String,Boolean> states = parameters.getValue(MEASUREMENTS);

        // Constructing filename
        int seriesNumber = workspace.getMetadata().getSeriesNumber();
        String seriesName = workspace.getMetadata().getSeriesName();
        String imageFilename = getFileName(plateName,rowLetter,columnNumber,seriesNumber,seriesName,"png");
        String textFilename = getFileName(plateName,rowLetter,columnNumber,seriesNumber,seriesName,"txt");

        // Storing raw image
        String folderName1 = getFolderName(rootFolder,"anal1",plateName);
        if (!saveImage(inputOverlayImage,folderName1,imageFilename)) {
            MIA.log.writeWarning("Could not write overlay image to file");
            return false;
        }
        String folderName3 = getFolderName(rootFolder,"anal3",plateName);
        if (!saveImage(inputRawImage,folderName3,imageFilename)) {
            MIA.log.writeWarning("Could not write raw image to file");
            return false;
        }

        // Getting measurements for input objects
        String folderName2 = getFolderName(rootFolder,"anal2",plateName);
        ObjMeasurementRefCollection measurementRefs = modules.getObjectMeasurementRefs(inputObjectsName);
        if (!saveFeatureNames(measurementRefs,states,folderName2)) {
            MIA.log.writeWarning("Could not write feature names to file");
            return false;
        }

        // Writing object features to file
        if (!saveFeatures(inputObjects,measurementRefs,states,folderName2,textFilename)) {
            MIA.log.writeWarning("Could not write features to file");
            return false;
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(INPUT_RAW_IMAGE,this));
        parameters.add(new InputImageP(INPUT_OVERLAY_IMAGE,this));
        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new FolderPathP(ROOT_DATASET_FOLDER,this));
        parameters.add(new MetadataItemP(PLATE_NAME,this));
        parameters.add(new MetadataItemP(ROW_LETTER,this));
        parameters.add(new MetadataItemP(COLUMN_NUMBER,this));
        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR,this));
        parameters.add(new BooleanP(SHOW_MEASUREMENTS,this,true));
        parameters.add(new ObjMeasurementSelectorP(MEASUREMENTS,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_RAW_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OVERLAY_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ROOT_DATASET_FOLDER));
        returnedParameters.add(parameters.getParameter(PLATE_NAME));
        returnedParameters.add(parameters.getParameter(ROW_LETTER));
        returnedParameters.add(parameters.getParameter(COLUMN_NUMBER));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_MEASUREMENTS));
        if ((boolean) parameters.getValue(SHOW_MEASUREMENTS)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENTS));
        }

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjMeasurementSelectorP parameter = parameters.getParameter(MEASUREMENTS);
        parameter.setObjectName(inputObjectsName);

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
