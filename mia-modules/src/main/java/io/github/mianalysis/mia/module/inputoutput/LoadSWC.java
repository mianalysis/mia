package io.github.mianalysis.mia.module.inputoutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.measure.Calibration;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import util.opencsv.CSVReader;

/**
 * Created by Stephen Cross on 13/11/2025.
 */

/**
 * Load centroid coordinates of pre-detected objects from file. Loaded objects
 * are stored in a single object collection and are represented by a single
 * coordinate point. For example, this module could be used to import detections
 * from another piece of software or from a previous analysis run.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class LoadSWC extends Module {
    private static int classIdx = 1;
    private static int coordIdx = 2;
    private static int xIdx = 0;
    private static int yIdx = 1;
    private static int zIdx = 2;
    private static int rIdx = 3;

    public static final String INPUT_SEPARATOR = "Input controls";
    public static final String SEQUENCE_ROOT_NAME = "Sequence root name";
    public static final String SEQUENCE_MESSAGE = "Sequence message";

    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String LIMIT_SEPARATOR = "Coordinate limits and calibration";
    public static final String LIMITS_SOURCE = "Limits source";
    public static final String LIMITS_REFERENCE_IMAGE = "Limits reference image";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String N_SLICES = "Number of slices";
    public static final String SPATIAL_CALIBRATION_SOURCE = "Spatial calibration source";
    public static final String SPATIAL_CALIBRATION_REFERENCE_IMAGE = "Spatial calibration ref. image";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";

    public interface LimitsSources {
        String FROM_IMAGE = "From image";
        String MANUAL = "Manual";
        String MAXIMUM_COORDINATE = "Maximum coordinate";

        String[] ALL = new String[] { FROM_IMAGE, MANUAL, MAXIMUM_COORDINATE };

    }

    public interface CalibrationSources {
        String FROM_FILE = "From file";
        String FROM_IMAGE = "From image";
        String MANUAL = "Manual";

        String[] ALL = new String[] { FROM_FILE, FROM_IMAGE, MANUAL };

    }

    public LoadSWC(Modules modules) {
        super("Load objects from SWC file", modules);
    }

    public ArrayList<File> getFileList(String absolutePath) {
        // Number format
        Pattern pattern = Pattern.compile("Z\\{0+}");
        Matcher matcher = pattern.matcher(absolutePath);
        int numberOfZeroes = 0;
        String nameBefore = "";
        String nameAfter = "";
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            numberOfZeroes = end - start - 3; // Removing 3 for the "Z{}"
            nameBefore = absolutePath.substring(0, start);
            nameAfter = absolutePath.substring(end);
        } else {
            MIA.log.writeWarning("Zero location in sequence filename uncertain.");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numberOfZeroes; i++)
            stringBuilder.append("0");
        DecimalFormat df = new DecimalFormat(stringBuilder.toString());

        // Determining the number of files to load
        int slice = 0;
        ArrayList<File> files = new ArrayList<>();
        boolean fileExists = true;
        while (fileExists) {
            File file = new File(nameBefore + df.format(slice) + nameAfter);
            fileExists = file.exists();

            if (fileExists)
                files.add(file);

            slice++;

        }

        return files;

    }

    int[] getLimitsFromImage(Workspace workspace) {
        String referenceImageName = parameters.getValue(LIMITS_REFERENCE_IMAGE, workspace);
        Image image = workspace.getImage(referenceImageName);
        ImagePlus ipl = image.getImagePlus();

        return new int[] { ipl.getWidth(), ipl.getHeight(), ipl.getNSlices(), ipl.getNFrames() };

    }

    int[] getLimitsFromManualValues(Workspace workspace) {
        int[] limits = new int[4];

        limits[0] = parameters.getValue(WIDTH, workspace);
        limits[1] = parameters.getValue(HEIGHT, workspace);
        limits[2] = parameters.getValue(N_SLICES, workspace);
        limits[4] = 1;

        return limits;

    }

    int[] getLimitsFromMaximumCoordinates(Workspace workspace, File inputFile) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            MIA.log.writeWarning("File not found: \"" + inputFile.getName() + "\"");
            return null;
        }

        // Initialising the limits array
        int[] limits = new int[] { 0, 0, 0, 0 };

        // Iterating over each row, adding as a new Obj
        CSVReader csvReader = new CSVReader(reader, '\t');
        try {
            String[] row = csvReader.readNext();
            while (row != null) {
                // Skipping comments
                if (row[0].substring(0, 1).equals("#"))
                    continue;

                try {
                    // Comparing the coordinates
                    int x = (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    int y = (int) Math.round((double) Double.parseDouble(row[yIdx]));
                    int z = zIdx == -1 ? 0 : (int) Math.round((double) Double.parseDouble(row[xIdx]));
                    limits[0] = Math.max(limits[0], x);
                    limits[1] = Math.max(limits[1], y);
                    limits[2] = Math.max(limits[2], z);

                } catch (NumberFormatException e) {
                }

                row = csvReader.readNext();

            }

        } catch (IOException e) {
            MIA.log.writeError(e);
            return null;
        }

        limits[0] = limits[0] + 1;
        limits[1] = limits[1] + 1;
        limits[2] = limits[2] + 1;
        limits[3] = 1;

        return limits;

    }

    double[] getSpatialCalibrationFromFile(File inputFile) {
        double[] cal = new double[2];

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            CSVReader csvReader = new CSVReader(reader, '\t');
            String[] row = csvReader.readNext();
            while (row != null) {
                String text = row[0];

                // Skipping comments
                if (!text.substring(0, 1).equals("#")) {
                    row = csvReader.readNext();
                    continue;
                }

                if (!text.contains("Voxel separation (x,y,z)")) {
                    row = csvReader.readNext();
                    continue;
                }

                Pattern pattern = Pattern.compile("[^:]+: ([0-9.]+), ([0-9.]+), ([0-9.]+)");
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    cal[0] = Double.parseDouble(matcher.group(1)); // XY should be the same, so taking X
                    cal[1] = Double.parseDouble(matcher.group(3)); // Z
                }

                row = csvReader.readNext();

            }

        } catch (IOException e) {
            MIA.log.writeWarning("Problem loading calibration from file: \"" + inputFile.getName()
                    + "\".  No spatial calibration applied.");
            return cal;
        }

        return cal;

    }

    double[] getSpatialCalibrationFromImage(Workspace workspace) {
        double[] cal = new double[2];

        String referenceImageName = parameters.getValue(SPATIAL_CALIBRATION_REFERENCE_IMAGE, workspace);
        Image image = workspace.getImage(referenceImageName);
        Calibration calibration = image.getImagePlus().getCalibration();

        cal[0] = calibration.pixelWidth;
        cal[1] = calibration.pixelDepth;

        return cal;

    }

    double[] getSpatialCalibrationFromManualValues(Workspace workspace) {
        double[] cal = new double[2];

        cal[0] = parameters.getValue(XY_CAL, workspace);
        cal[1] = parameters.getValue(Z_CAL, workspace);

        return cal;

    }

    void loadObjects(Objs outputObjects, File inputFile, Workspace workspace) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            MIA.log.writeWarning("File not found: \"" + inputFile.getName() + "\"");
            return;
        }

        Obj obj = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);
        int outOfRangeCount = 0;
        try {
            CSVReader csvReader = new CSVReader(reader, '\t');

            String[] row = csvReader.readNext();
            while (row != null) {
                // Skipping comments
                if (row[0].substring(0, 1).equals("#")) {
                    // Getting the next row
                    row = csvReader.readNext();

                    continue;

                }

                // If any of these fields can't be read (e.g. for the title row) we get a
                // NumberFormatException and the row is skipped
                try {
                    String[] coords = row[coordIdx].split(" ");

                    // Loading coordinates
                    double x = Double.parseDouble(coords[xIdx]) / outputObjects.getDppXY();
                    double y = Double.parseDouble(coords[yIdx]) / outputObjects.getDppXY();
                    double z = Double.parseDouble(coords[zIdx]) / outputObjects.getDppZ();

                    try {
                        obj.add((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
                    } catch (PointOutOfRangeException e) {
                        outOfRangeCount++;
                    }
                    obj.setT(0);
                    outputObjects.add(obj);

                } catch (NumberFormatException e) {
                }

                // Getting the next row
                row = csvReader.readNext();

            }

            // Closing the readers
            csvReader.close();
            reader.close();

        } catch (IOException e) {
            MIA.log.writeError(e);
        }

        if (outOfRangeCount > 0)
            MIA.log.writeWarning(outOfRangeCount + " points ignored due to exceeding spatial limits");
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Load centroid coordinates of pre-detected objects from file.  Loaded objects are stored in a single object collection and are represented by a single coordinate point.  For example, this module could be used to import detections from another piece of software or from a previous analysis run.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String sequenceRootName = parameters.getValue(SEQUENCE_ROOT_NAME, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String limitsSource = parameters.getValue(LIMITS_SOURCE, workspace);
        String spatialCalSource = parameters.getValue(SPATIAL_CALIBRATION_SOURCE, workspace);

        // Getting file to load
        sequenceRootName = workspace.getMetadata().insertMetadataValues(sequenceRootName);
        ArrayList<File> inputFiles = getFileList(sequenceRootName);

        Objs outputObjects = null;

        int count = 0;
        int total = inputFiles.size();
        for (File inputFile : inputFiles) {
            // Creating output objects
            if (outputObjects == null) {
                // Getting limits for output objects
                int[] limits = null;
                switch (limitsSource) {
                    case LimitsSources.FROM_IMAGE:
                        limits = getLimitsFromImage(workspace);
                        break;
                    case LimitsSources.MANUAL:
                        limits = getLimitsFromManualValues(workspace);
                        break;
                    case LimitsSources.MAXIMUM_COORDINATE:
                        limits = getLimitsFromMaximumCoordinates(workspace, inputFile);
                        break;
                }
                if (limits == null)
                    return Status.FAIL;

                // Getting calibration for output objects
                double[] spatialCal = null;
                switch (spatialCalSource) {
                    case CalibrationSources.FROM_FILE:
                        spatialCal = getSpatialCalibrationFromFile(inputFile);
                        break;
                    case CalibrationSources.FROM_IMAGE:
                        spatialCal = getSpatialCalibrationFromImage(workspace);
                        break;
                    case CalibrationSources.MANUAL:
                        spatialCal = getSpatialCalibrationFromManualValues(workspace);
                        break;
                }
                if (spatialCal == null)
                    return Status.FAIL;

                String units = SpatialUnit.getOMEUnit().getSymbol();
                SpatCal calibration = new SpatCal(spatialCal[0], spatialCal[1], units, limits[0], limits[1], limits[2]);

                outputObjects = new Objs(outputObjectsName, calibration, limits[3], 1, TemporalUnit.getOMEUnit());

            }

            // Loading objects to the specified collections
            loadObjects(outputObjects, inputFile, workspace);

            writeProgressStatus(++count, total, "objects");

        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new StringP(SEQUENCE_ROOT_NAME, this));
        parameters.add(new MessageP(SEQUENCE_MESSAGE, this, "Z{000}", ParameterState.MESSAGE));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new SeparatorP(LIMIT_SEPARATOR, this));
        parameters.add(new ChoiceP(LIMITS_SOURCE, this, LimitsSources.FROM_IMAGE, LimitsSources.ALL));
        parameters.add(new InputImageP(LIMITS_REFERENCE_IMAGE, this));
        parameters.add(new IntegerP(WIDTH, this, 512));
        parameters.add(new IntegerP(HEIGHT, this, 512));
        parameters.add(new IntegerP(N_SLICES, this, 1));
        parameters.add(
                new ChoiceP(SPATIAL_CALIBRATION_SOURCE, this, CalibrationSources.FROM_FILE, CalibrationSources.ALL));
        parameters.add(new InputImageP(SPATIAL_CALIBRATION_REFERENCE_IMAGE, this));
        parameters.add(new DoubleP(XY_CAL, this, 1d));
        parameters.add(new DoubleP(Z_CAL, this, 1d));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(SEQUENCE_ROOT_NAME));
        returnedParameters.add(parameters.get(SEQUENCE_MESSAGE));
        MetadataRefs metadataRefs = modules.getMetadataRefs(this);
        parameters.getParameter(SEQUENCE_MESSAGE).setValue(metadataRefs.getMetadataValues()
                + "<br><br>The position of the index (e.g. 000, 001, etc.) can be added in the form Z{000}, where the number of zeroes should match the number of digits in the index."
                + "<br><br>An example of a typical root name is \"M{Filepath}M{Filename}-Z{000}.swc\"");

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.get(LIMIT_SEPARATOR));
        returnedParameters.add(parameters.get(LIMITS_SOURCE));
        switch ((String) parameters.getValue(LIMITS_SOURCE, workspace)) {
            case LimitsSources.FROM_IMAGE:
                returnedParameters.add(parameters.get(LIMITS_REFERENCE_IMAGE));
                break;
            case LimitsSources.MANUAL:
                returnedParameters.add(parameters.get(WIDTH));
                returnedParameters.add(parameters.get(HEIGHT));
                returnedParameters.add(parameters.get(N_SLICES));
                break;
        }

        returnedParameters.add(parameters.get(SPATIAL_CALIBRATION_SOURCE));
        switch ((String) parameters.getValue(SPATIAL_CALIBRATION_SOURCE, workspace)) {
            case CalibrationSources.FROM_IMAGE:
                returnedParameters.add(parameters.get(SPATIAL_CALIBRATION_REFERENCE_IMAGE));
                break;
            case CalibrationSources.MANUAL:
                returnedParameters.add(parameters.get(XY_CAL));
                returnedParameters.add(parameters.get(Z_CAL));
                break;
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

    void addParameterDescriptions() {
        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Objects loaded into the workspace will be stored with this name.  They will be accessible by subsequent modules using this name.");

        parameters.get(LIMITS_SOURCE).setDescription(
                "Controls how the spatial limits (width, height, number of slices and number of timepoints) for the output object collection are defined:<br><ul>"

                        + "<li>\"" + LimitsSources.FROM_IMAGE
                        + "\" limits match the spatial and temporal dimensions of an image in the workspace (specified with the \""
                        + LIMITS_REFERENCE_IMAGE
                        + "\" parameter).  This is equivalent to how spatial limits are determined when identifying objects directly from an image.</li>"

                        + "<li>\"" + LimitsSources.MANUAL + "\" limits are specified using the \"" + WIDTH + "\", \""
                        + HEIGHT + "\" and \"" + N_SLICES + "\" parameters.</li>"

                        + "<li>\"" + LimitsSources.MAXIMUM_COORDINATE
                        + "\" limits are determined from the maximum x,y,z coordinates and timepoint present in the loaded coordinate set.</li></ul>");

        parameters.get(LIMITS_REFERENCE_IMAGE).setDescription(
                "Image used to determine spatial and temporal limits of the output object collection if \""
                        + LIMITS_SOURCE + "\" is set to \"" + LimitsSources.FROM_IMAGE + "\".");

        parameters.get(WIDTH).setDescription("Output object collection spatial width to be used if \"" + LIMITS_SOURCE
                + "\" is set to \"" + LimitsSources.MANUAL + "\".  Specified in pixel units.");

        parameters.get(HEIGHT).setDescription("Output object collection spatial height to be used if \"" + LIMITS_SOURCE
                + "\" is set to \"" + LimitsSources.MANUAL + "\".  Specified in pixel units.");

        parameters.get(N_SLICES).setDescription("Output object collection number of slices (depth) to be used if \""
                + LIMITS_SOURCE + "\" is set to \"" + LimitsSources.MANUAL + "\".  Specified in slice units.");

        parameters.get(SPATIAL_CALIBRATION_SOURCE).setDescription(
                "Controls how the spatial calibration for the output object collection are defined:<br><ul>"

                        + "<li>\"" + CalibrationSources.FROM_IMAGE
                        + "\" spatial calibrations match those of an image in the workspace (specified with the \""
                        + SPATIAL_CALIBRATION_REFERENCE_IMAGE
                        + "\" parameter).  This is equivalent to how calibrations are determined when identifying objects directly from an image.</li>"

                        + "<li>\"" + CalibrationSources.MANUAL + "\" spatial calibrations are specified using the \""
                        + XY_CAL + "\" and \"" + Z_CAL + "\" parameters.</li></ul>");

        parameters.get(SPATIAL_CALIBRATION_REFERENCE_IMAGE)
                .setDescription("Image used to determine spatial calibrations of the output object collection if \""
                        + SPATIAL_CALIBRATION_SOURCE + "\" is set to \"" + CalibrationSources.FROM_IMAGE + "\".");

        parameters.get(XY_CAL).setDescription(
                "Distance per pixel in the XY plane.  Units for this are specified in the main \"Input control\" module.");

        parameters.get(Z_CAL).setDescription(
                "Distance per slice (Z-axis).  Units for this are specified in the main \"Input control\" module.");

    }
}
