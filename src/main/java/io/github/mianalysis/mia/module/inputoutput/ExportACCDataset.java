package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.TreeMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.MetadataItemP;
import io.github.mianalysis.mia.object.parameters.ObjMeasurementSelectorP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


/**
* Exports objects and associated measurements in the Advanced Cell Classifier (ACC) format.  The output dataset can be loaded into ACC, where machine learning can be applied to classify objects in a GUI-based environment.  This module allows specific measurements to be exported from all those associated with the input objects.  For more information on the format, please visit the <a href="https://www.cellclassifier.org/">Advanced Cell Classifier documentation</a>.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ExportACCDataset extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image/objects input";

	/**
	* Objects for which an ACC dataset will be generated.  A summary of each object is output along with the specified measurements in a format that can be loaded into ACC.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* Raw image from which objects were ultimately detected.  This image doesn't need to show the object selections, but will be displayed in ACC with an optional overlay variant (specified by "Input overlay image").  As such, the objects themselves should be visible.  Typically this would be a fluorescence or brightfield image.
	*/
    public static final String INPUT_RAW_IMAGE = "Input raw image";

	/**
	* Equivalent image to that specified by "Input raw image", but with an overlay showing the input objects.  The overlay will be automatically flattened onto this image prior to saving.
	*/
    public static final String INPUT_OVERLAY_IMAGE = "Input overlay image";


	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Dataset output";

	/**
	* Root folder where the output ACC dataset will be stored.
	*/
    public static final String ROOT_DATASET_FOLDER = "Root dataset folder";

	/**
	* Metadata item associated with the input image that corresponds to the plate name from which the image was taken.  Note: The ACC format requires objects and images to be organised in a plate-based format; however, for single image samples, this text value could be set to the filename.
	*/
    public static final String PLATE_NAME = "Plate name";

	/**
	* Metadata item associated with the input image that corresponds to the row letter for the plate well from which the image was taken.  Note: The ACC format requires objects and images to be organised in a plate-based format; however, for single image samples, this text value could be set to the series number.
	*/
    public static final String ROW_LETTER = "Row letter";

	/**
	* Metadata item associated with the input image that corresponds to the column number for the plate well from which the image was taken.  Note: The ACC format requires objects and images to be organised in a plate-based format; however, for single image samples, this numeric value could be set to the series number.
	*/
    public static final String COLUMN_NUMBER = "Column number";


	/**
	* 
	*/
    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";

	/**
	* When selected, the available measurements are displayed in the MIA interface.  This allows only the relevant measurements associated with the objects to be used in ACC.
	*/
    public static final String SHOW_MEASUREMENTS = "Show measurement selection";

	/**
	* If "Show measurement selection" is selected, all measurements associated with the input objects will be shown in the MIA interface, along with tickboxes that can be used to mark those for inclusion in the ACC dataset.
	*/
    public static final String MEASUREMENTS = "Measurements";

    public ExportACCDataset(Modules modules) {
        super("Export ACC dataset", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Exports objects and associated measurements in the Advanced Cell Classifier (ACC) format.  The output dataset can be loaded into ACC, where machine learning can be applied to classify objects in a GUI-based environment.  This module allows specific measurements to be exported from all those associated with the input objects.  For more information on the format, please visit the <a href=\"https://www.cellclassifier.org/\">Advanced Cell Classifier documentation</a>.";

    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    static String getFolderName(String rootFolder, String analysisFolderName, String plateName) {
        return rootFolder + File.separator + plateName + File.separator + analysisFolderName + File.separator;

    }

    static String getFileName(String plateName, String rowLetter, int columnNumber, int seriesNumber, String fileName,
            String seriesName, String extension) {
        DecimalFormat df = new DecimalFormat("00");
        StringBuilder builder = new StringBuilder();

        builder.append(plateName).append("_w").append(rowLetter).append(df.format(columnNumber)).append("_s")
                .append(seriesNumber).append("_n").append(fileName);

        // If from a series file (e.g. LIF file), add series name as well
        if (!seriesName.equals(fileName)) {
            builder.append("_").append(seriesName);
        }

        // Adding file extension
        builder.append(".").append(extension);

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
        IJ.saveAs(rgbImage, "PNG", folderName + fileName);

        return true;

    }

    static boolean saveFeatureNames(ObjMeasurementRefs refs, TreeMap<String, Boolean> states, String folderName) {
        // Check if featureName file exists
        File featureNamesFile = new File(folderName + "featureNames.acc");
        if (featureNamesFile.exists())
            return true;

        // Check analysis folder exists
        File featureFolder = new File(folderName);
        if (!featureFolder.exists()) {
            if (!featureFolder.mkdirs()) {
                return false;
            }
        }

        // Compiling list of measurement names
        StringBuilder builder = new StringBuilder();
        builder.append("Location_Center_X\n").append("Location_Center_Y\n");
        for (ObjMeasurementRef ref : refs.values()) {
            if (!states.containsKey(ref.getName()))
                continue;
            if (!states.get(ref.getName()))
                continue;

            builder.append(ref.getName().replace(" ", "_")).append("\n");
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

    static boolean saveFeatures(Objs objects, ObjMeasurementRefs refs, TreeMap<String, Boolean> states,
            String folderName, String fileName) {
        DecimalFormat df = new DecimalFormat("0.0000000E0");

        // Check analysis folder exists
        File featureFolder = new File(folderName);
        if (!featureFolder.exists()) {
            if (!featureFolder.mkdirs()) {
                return false;
            }
        }

        StringBuilder builder = new StringBuilder();

        for (Obj obj : objects.values()) {
            // Adding centroid
            builder.append(df.format(obj.getXMean(true))).append(" ").append(df.format(obj.getYMean(true)));

            // Adding extra features
            for (ObjMeasurementRef ref : refs.values()) {
                if (!states.containsKey(ref.getName()))
                    continue;
                if (!states.get(ref.getName()))
                    continue;

                builder.append(" ").append(df.format(obj.getMeasurement(ref.getName()).getValue()));
            }

            builder.append("\n");

        }

        // Writing the features to file
        try {
            File featuresFile = new File(folderName + fileName);
            FileWriter writer = new FileWriter(featuresFile);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            return false;
        }

        return true;

    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        String inputRawImageName = parameters.getValue(INPUT_RAW_IMAGE,workspace);
        Image inputRawImage = workspace.getImage(inputRawImageName);
        String inputOverlayImageName = parameters.getValue(INPUT_OVERLAY_IMAGE,workspace);
        Image inputOverlayImage = workspace.getImage(inputOverlayImageName);
        String rootFolder = parameters.getValue(ROOT_DATASET_FOLDER,workspace);
        String plateMetadataName = parameters.getValue(PLATE_NAME,workspace);
        String plateName = workspace.getMetadata().getAsString(plateMetadataName);
        String rowMetadataName = parameters.getValue(ROW_LETTER,workspace);
        String rowLetter = workspace.getMetadata().getAsString(rowMetadataName);
        String columnMetadataName = parameters.getValue(COLUMN_NUMBER,workspace);
        int columnNumber = Integer.parseInt(workspace.getMetadata().getAsString(columnMetadataName));
        TreeMap<String, Boolean> states = parameters.getValue(MEASUREMENTS,workspace);

        // Constructing filename (also removing any slash characters from seriesName)
        String fileName = workspace.getMetadata().getFilename();
        int seriesNumber = workspace.getMetadata().getSeriesNumber();
        String seriesName = workspace.getMetadata().getSeriesName();
        seriesName = seriesName.replace("\\", "_");
        seriesName = seriesName.replace("/", "_");
        String imageFilename = getFileName(plateName, rowLetter, columnNumber, seriesNumber, fileName, seriesName,
                "png");
        String textFilename = getFileName(plateName, rowLetter, columnNumber, seriesNumber, fileName, seriesName,
                "txt");

        // Storing raw image
        String folderName1 = getFolderName(rootFolder, "anal1", plateName);
        if (!saveImage(inputOverlayImage, folderName1, imageFilename)) {
            MIA.log.writeWarning("Could not write overlay image to file");
            return Status.FAIL;
        }
        String folderName3 = getFolderName(rootFolder, "anal3", plateName);
        if (!saveImage(inputRawImage, folderName3, imageFilename)) {
            MIA.log.writeWarning("Could not write raw image to file");
            return Status.FAIL;
        }

        // Getting measurements for input objects
        String folderName2 = getFolderName(rootFolder, "anal2", plateName);
        ObjMeasurementRefs measurementRefs = modules.getObjectMeasurementRefs(inputObjectsName);
        if (!saveFeatureNames(measurementRefs, states, folderName2)) {
            MIA.log.writeWarning("Could not write feature names to file");
            return Status.FAIL;
        }

        // Writing object features to file
        if (!saveFeatures(inputObjects, measurementRefs, states, folderName2, textFilename)) {
            MIA.log.writeWarning("Could not write features to file");
            return Status.FAIL;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_RAW_IMAGE, this));
        parameters.add(new InputImageP(INPUT_OVERLAY_IMAGE, this));
        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new FolderPathP(ROOT_DATASET_FOLDER, this));
        parameters.add(new MetadataItemP(PLATE_NAME, this));
        parameters.add(new MetadataItemP(ROW_LETTER, this));
        parameters.add(new MetadataItemP(COLUMN_NUMBER, this));
        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_MEASUREMENTS, this, true));
        parameters.add(new ObjMeasurementSelectorP(MEASUREMENTS, this));

        addParameterDescriptions();
        
    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

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
        if ((boolean) parameters.getValue(SHOW_MEASUREMENTS,workspace)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENTS));
        }

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        ObjMeasurementSelectorP parameter = parameters.getParameter(MEASUREMENTS);
        parameter.setObjectName(inputObjectsName);

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

    public void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects for which an ACC dataset will be generated.  A summary of each object is output along with the specified measurements in a format that can be loaded into ACC.");

        parameters.get(INPUT_RAW_IMAGE).setDescription(
                "Raw image from which objects were ultimately detected.  This image doesn't need to show the object selections, but will be displayed in ACC with an optional overlay variant (specified by \""
                        + INPUT_OVERLAY_IMAGE
                        + "\").  As such, the objects themselves should be visible.  Typically this would be a fluorescence or brightfield image.");

        parameters.get(INPUT_OVERLAY_IMAGE).setDescription("Equivalent image to that specified by \"" + INPUT_RAW_IMAGE
                + "\", but with an overlay showing the input objects.  The overlay will be automatically flattened onto this image prior to saving.");

        parameters.get(ROOT_DATASET_FOLDER).setDescription("Root folder where the output ACC dataset will be stored.");

        parameters.get(PLATE_NAME).setDescription("Metadata item associated with the input image that corresponds to the plate name from which the image was taken.  Note: The ACC format requires objects and images to be organised in a plate-based format; however, for single image samples, this text value could be set to the filename.");

        parameters.get(ROW_LETTER).setDescription("Metadata item associated with the input image that corresponds to the row letter for the plate well from which the image was taken.  Note: The ACC format requires objects and images to be organised in a plate-based format; however, for single image samples, this text value could be set to the series number.");

        parameters.get(COLUMN_NUMBER).setDescription("Metadata item associated with the input image that corresponds to the column number for the plate well from which the image was taken.  Note: The ACC format requires objects and images to be organised in a plate-based format; however, for single image samples, this numeric value could be set to the series number.");

        parameters.get(SHOW_MEASUREMENTS).setDescription("When selected, the available measurements are displayed in the MIA interface.  This allows only the relevant measurements associated with the objects to be used in ACC.");

        parameters.get(MEASUREMENTS).setDescription("If \""+SHOW_MEASUREMENTS+"\" is selected, all measurements associated with the input objects will be shown in the MIA interface, along with tickboxes that can be used to mark those for inclusion in the ACC dataset.");

    }
}
