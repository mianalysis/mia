package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import com.drew.lang.annotations.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.InputOutput.ImageSaver;
import wbif.sjx.MIA.Module.ObjectProcessing.Relationships.RelateManyToOne;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParentObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 22/06/2017.
 */
public class CalculateNearestNeighbour extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RELATIONSHIP_MODE = "Relationship mode";
    public static final String NEIGHBOUR_OBJECTS = "Neighbour objects";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship settings";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String CALCULATE_WITHIN_PARENT = "Only calculate for objects in same parent";
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String LIMIT_LINKING_DISTANCE = "Limit linking distance";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance";
    public static final String CALIBRATED_DISTANCE = "Calibrated distance";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public static final String OUTPUT_SEPARATOR = "Data output";
    public static final String EXPORT_ALL_DISTANCES = "Export all distances";
    public static final String INSIDE_OUTSIDE_MODE = "Inside/outside mode";
    public static final String INCLUDE_TIMEPOINTS = "Include timepoints";
    public static final String INCLUDE_INPUT_PARENT = "Include input object parent";
    public static final String INPUT_PARENT = "Input object parent";
    public static final String INCLUDE_NEIGHBOUR_PARENT = "Include neighbour object parent";
    public static final String NEIGHBOUR_PARENT = "Neighbour object parent";

    public static final String FILE_SAVING_SEPARATOR = "File saving controls";
    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String APPEND_SERIES_MODE = "Append series mode";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    public static final String SAVE_SUFFIX = "Add filename suffix";

    public interface ReferenceModes {
        String CENTROID = "Centroid";
        String SURFACE = "Surface";

        String[] ALL = new String[] { CENTROID, SURFACE };

    }

    public interface InsideOutsideModes extends RelateManyToOne.InsideOutsideModes {};

    public interface SaveNameModes extends ImageSaver.SaveNameModes {}

    public interface AppendSeriesModes extends ImageSaver.AppendSeriesModes {}

    public interface AppendDateTimeModes extends ImageSaver.AppendDateTimeModes {}

    public CalculateNearestNeighbour(ModuleCollection modules) {
        super("Calculate nearest neighbour", modules);
    }

    public interface RelationshipModes {
        String WITHIN_SAME_SET = "Within same object set";
        String DIFFERENT_SET = "Different object set";

        String[] ALL = new String[] { WITHIN_SAME_SET, DIFFERENT_SET };

    }

    public interface Measurements {
        String NN_DISTANCE_PX = "NN_DISTANCE_TO_${NEIGHBOUR}_(PX)";
        String NN_DISTANCE_CAL = "NN_DISTANCE_TO_${NEIGHBOUR}_(${CAL})";
        String NN_ID = "NN_${NEIGHBOUR}_ID";
    }

    public static String getFullName(String measurement, String neighbourObjectsName) {
        return "NEAREST_NEIGHBOUR // " + measurement.replace("${NEIGHBOUR}", neighbourObjectsName);
    }

    public Obj getNearestNeighbour(Obj inputObject, ObjCollection testObjects, String referenceMode,
            double maximumLinkingDistance, boolean linkInSameFrame,
            @Nullable LinkedHashMap<Obj, Double> currDistances) {
        double minDist = Double.MAX_VALUE;
        Obj nearestNeighbour = null;

        if (testObjects == null)
            return null;

        for (Obj testObject : testObjects.values()) {
            // Don't compare an object to itself
            if (testObject == inputObject)
                continue;

            // Check if we should only be comparing objects in same timepoint
            if (linkInSameFrame && (inputObject.getT() != testObject.getT()))
                continue;

            double dist;
            switch (referenceMode) {
                default:
                case ReferenceModes.CENTROID:
                    dist = inputObject.getCentroidSeparation(testObject, true);
                    break;

                case ReferenceModes.SURFACE:
                    dist = inputObject.getSurfaceSeparation(testObject, true);
                    break;
            }

            if (Math.abs(dist) < minDist) {
                minDist = Math.abs(dist);
                nearestNeighbour = testObject;
            }

            // If storing all distances, adding current distance
            if (currDistances != null)
                currDistances.put(testObject, dist);

        }

        if (minDist > maximumLinkingDistance)
            return null;

        return nearestNeighbour;

    }

    public void addMeasurements(Obj inputObject, Obj nearestNeighbour, String nearestNeighbourName) {
        // Adding details of the nearest neighbour to the input object's measurements
        if (nearestNeighbour != null) {
            double dppXY = inputObject.getDppXY();
            double minDist = inputObject.getCentroidSeparation(nearestNeighbour, true);

            String name = getFullName(Measurements.NN_ID, nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, nearestNeighbour.getID()));

            name = getFullName(Measurements.NN_DISTANCE_PX, nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, minDist));

            name = getFullName(Measurements.NN_DISTANCE_CAL, nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, minDist * dppXY));

        } else {
            String name = getFullName(Measurements.NN_ID, nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.NN_DISTANCE_PX, nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.NN_DISTANCE_CAL, nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

        }
    }

    public static SXSSFWorkbook exportDistances(LinkedHashMap<Obj, LinkedHashMap<Obj, Double>> distances,
            String inputObjectsName, String neighbourObjectsName, boolean includeTimepoints, boolean linkInSameFrame,
            @Nullable String inputParentsName, @Nullable String neighbourParentsName) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("Distances");

        // Creating header row
        int rowCount = 0;
        int colCount = 0;
        Row row = sheet.createRow(rowCount++);

        // Header cells will be bold
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        Cell cell = row.createCell(colCount++);
        cell.setCellValue("\"" + inputObjectsName + "\" ID");
        cell.setCellStyle(cellStyle);

        if (includeTimepoints) {
            cell = row.createCell(colCount++);
            if (linkInSameFrame) {
                cell.setCellValue("TIMEPOINT");
            } else {
                cell.setCellValue(inputObjectsName + "\" TIMEPOINT");
            }
            cell.setCellStyle(cellStyle);
        }

        if (inputParentsName != null) {
            cell = row.createCell(colCount++);
            cell.setCellValue("\"" + inputParentsName + "\" ID");
            cell.setCellStyle(cellStyle);
        }

        cell = row.createCell(colCount++);
        cell.setCellValue("\"" + neighbourObjectsName + "\" ID");
        cell.setCellStyle(cellStyle);

        if (includeTimepoints & !linkInSameFrame) {
            cell = row.createCell(colCount++);
            cell.setCellValue("\"" + neighbourObjectsName + "\" TIMEPOINT");
            cell.setCellStyle(cellStyle);
        }

        if (neighbourParentsName != null) {
            cell = row.createCell(colCount++);
            cell.setCellValue("\"" + neighbourParentsName + "\" ID");
            cell.setCellStyle(cellStyle);
        }

        cell = row.createCell(colCount++);
        cell.setCellValue("DISTANCE (PX)");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colCount++);
        cell.setCellValue(Units.replace("DISTANCE (${CAL})"));
        cell.setCellStyle(cellStyle);

        if (distances.size() == 0)
            return workbook;

        // Adding header rows for input objects
        for (Obj inputObject : distances.keySet()) {
            LinkedHashMap<Obj, Double> currDistances = distances.get(inputObject);
            for (Obj neighbourObject : currDistances.keySet()) {
                colCount = 0;

                // Creating row for this object
                row = sheet.createRow(rowCount++);

                cell = row.createCell(colCount++);
                cell.setCellValue(inputObject.getID());

                if (includeTimepoints) {
                    cell = row.createCell(colCount++);
                    cell.setCellValue(inputObject.getT());
                }

                if (inputParentsName != null) {
                    cell = row.createCell(colCount++);
                    Obj inputParentObject = inputObject.getParent(inputParentsName);
                    double inputParentID = inputParentObject == null ? Double.NaN : inputParentObject.getID();
                    cell.setCellValue(inputParentID);
                }

                cell = row.createCell(colCount++);
                cell.setCellValue(neighbourObject.getID());

                if (includeTimepoints) {
                    if (!linkInSameFrame) {
                        cell = row.createCell(colCount++);
                        cell.setCellValue(neighbourObject.getT());
                    }
                }

                if (neighbourParentsName != null) {
                    cell = row.createCell(colCount++);
                    Obj neighbourParentObject = neighbourObject.getParent(neighbourParentsName);
                    double neighbourParentID = neighbourParentObject == null ? Double.NaN
                            : neighbourParentObject.getID();
                    cell.setCellValue(neighbourParentID);
                }

                cell = row.createCell(colCount++);
                double distPx = currDistances.get(neighbourObject);
                cell.setCellValue(distPx);

                cell = row.createCell(colCount++);
                double distCal = distPx * inputObject.getDppXY();
                cell.setCellValue(distCal);

            }
        }

        return workbook;

    }

    public static void writeDistancesFile(SXSSFWorkbook workbook, String path) {
        // Writing the workbook to file
        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            try {
                String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                String rootPath = FilenameUtils.removeExtension(path);
                String newOutPath = rootPath + "_(" + dateTime + ").xlsx";
                FileOutputStream outputStream = null;

                outputStream = new FileOutputStream(newOutPath);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();

                MIA.log.writeWarning("Target file (" + new File(path).getName() + ") inaccessible");
                MIA.log.writeWarning("Saved to alternative file (" + new File(newOutPath).getName() + ")");

            } catch (IOException e1) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE);
        String neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        boolean calculateWithinParent = parameters.getValue(CALCULATE_WITHIN_PARENT);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        boolean limitLinkingDistance = parameters.getValue(LIMIT_LINKING_DISTANCE);
        double maxLinkingDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE);
        boolean calibratedDistance = parameters.getValue(CALIBRATED_DISTANCE);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);
        boolean exportAllDistances = parameters.getValue(EXPORT_ALL_DISTANCES);
        String insideOutsideMode = parameters.getValue(INSIDE_OUTSIDE_MODE);
        boolean includeTimepoints = parameters.getValue(INCLUDE_TIMEPOINTS);
        boolean includeInputParent = parameters.getValue(INCLUDE_INPUT_PARENT);
        String inputParentsName = parameters.getValue(INPUT_PARENT);
        boolean includeNeighbourParent = parameters.getValue(INCLUDE_NEIGHBOUR_PARENT);
        String neighbourParentsName = parameters.getValue(NEIGHBOUR_PARENT);
        String saveNameMode = parameters.getValue(SAVE_NAME_MODE);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE);
        String suffix = parameters.getValue(SAVE_SUFFIX);

        // If there are no input objects skip the module
        if (inputObjects == null)
            return Status.PASS;
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null)
            return Status.PASS;

        // If the maximum linking distance was specified in calibrated units convert it
        // to pixels
        if (limitLinkingDistance && calibratedDistance)
            maxLinkingDist = maxLinkingDist / firstObj.getDppXY();

        // If the linking distance limit isn't to be used, use Double.MAX_VALUE instead
        if (!limitLinkingDistance)
            maxLinkingDist = Double.MAX_VALUE;

        ObjCollection neighbourObjects = null;
        String nearestNeighbourName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjects = workspace.getObjectSet(neighbourObjectsName);
                nearestNeighbourName = neighbourObjectsName;
                break;

            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjects = inputObjects;
                nearestNeighbourName = inputObjectsName;
                break;
        }

        // Creating a store for distance-distance measurements
        LinkedHashMap<Obj, LinkedHashMap<Obj, Double>> distances = null;
        if (exportAllDistances)
            distances = new LinkedHashMap<>();

        // Running through each object, calculating the nearest neighbour distance
        for (Obj inputObject : inputObjects.values()) {
            // Creating store for this input object
            LinkedHashMap<Obj, Double> currDistances = null;
            if (exportAllDistances) {
                distances.putIfAbsent(inputObject, new LinkedHashMap<Obj, Double>());
                currDistances = distances.get(inputObject);
            }

            if (calculateWithinParent) {
                Obj parentObject = inputObject.getParent(parentObjectsName);
                if (parentObject == null) {
                    addMeasurements(inputObject, null, nearestNeighbourName);
                    continue;
                }

                ObjCollection childObjects = parentObject.getChildren(nearestNeighbourName);
                Obj nearestNeighbour = getNearestNeighbour(inputObject, childObjects, referenceMode, maxLinkingDist,
                        linkInSameFrame, currDistances);
                addMeasurements(inputObject, nearestNeighbour, nearestNeighbourName);

            } else {
                Obj nearestNeighbour = getNearestNeighbour(inputObject, neighbourObjects, referenceMode, maxLinkingDist,
                        linkInSameFrame, currDistances);
                addMeasurements(inputObject, nearestNeighbour, nearestNeighbourName);
            }
        }

        if (exportAllDistances) {
            if (!includeInputParent)
                inputParentsName = null;

            if (!includeNeighbourParent)
                neighbourParentsName = null;

            File rootFile = workspace.getMetadata().getFile();
            String path = rootFile.getParent() + MIA.getSlashes();

            String name;
            switch (saveNameMode) {
                case SaveNameModes.MATCH_INPUT:
                default:
                    name = FilenameUtils.removeExtension(rootFile.getName());
                    break;

                case SaveNameModes.SPECIFIC_NAME:
                    name = FilenameUtils.removeExtension(saveFileName);
                    break;
            }

            // Adding last bits to name
            path = path + name;
            path = ImageSaver.appendSeries(path, workspace, appendSeriesMode);
            path = ImageSaver.appendDateTime(path, appendDateTimeMode);
            path = path + suffix + ".xlsx";

            // Applying inside/outside policy
            for (LinkedHashMap<Obj,Double> collection:distances.values()) {
                for (Obj obj : collection.keySet()) {
                    // Applying the inside outside mode
                    if (!RelateManyToOne.applyInsideOutsidePolicy(collection.get(obj), insideOutsideMode))
                        collection.put(obj, 0d);
                }
            }            

            // Writing distances to file
            SXSSFWorkbook workbook = exportDistances(distances, inputObjectsName, nearestNeighbourName,
                    includeTimepoints, linkInSameFrame, inputParentsName, neighbourParentsName);
            writeDistancesFile(workbook, path);

        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(RELATIONSHIP_MODE, this, RelationshipModes.WITHIN_SAME_SET, RelationshipModes.ALL));
        parameters.add(new InputObjectsP(NEIGHBOUR_OBJECTS, this));

        parameters.add(new ParamSeparatorP(RELATIONSHIP_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.CENTROID, ReferenceModes.ALL));
        parameters.add(new BooleanP(CALCULATE_WITHIN_PARENT, this, false));
        parameters.add(new ParentObjectsP(PARENT_OBJECTS, this));
        parameters.add(new BooleanP(LIMIT_LINKING_DISTANCE, this, false));
        parameters.add(new DoubleP(MAXIMUM_LINKING_DISTANCE, this, 100d));
        parameters.add(new BooleanP(CALIBRATED_DISTANCE, this, false));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, false));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(EXPORT_ALL_DISTANCES, this, false));
        parameters.add(new ChoiceP(INSIDE_OUTSIDE_MODE, this, InsideOutsideModes.INSIDE_AND_OUTSIDE,
                InsideOutsideModes.ALL));
        parameters.add(new BooleanP(INCLUDE_TIMEPOINTS, this, false));
        parameters.add(new BooleanP(INCLUDE_INPUT_PARENT, this, false));
        parameters.add(new ParentObjectsP(INPUT_PARENT, this));
        parameters.add(new BooleanP(INCLUDE_NEIGHBOUR_PARENT, this, false));
        parameters.add(new ParentObjectsP(NEIGHBOUR_PARENT, this));

        parameters.add(new ParamSeparatorP(FILE_SAVING_SEPARATOR, this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this, SaveNameModes.MATCH_INPUT, SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME, this));
        parameters.add(new ChoiceP(APPEND_SERIES_MODE, this, AppendSeriesModes.SERIES_NUMBER, AppendSeriesModes.ALL));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String neighbourObjectsName;

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(RELATIONSHIP_MODE));

        switch ((String) parameters.getValue(RELATIONSHIP_MODE)) {
            case RelationshipModes.DIFFERENT_SET:
            default:
                returnedParameters.add(parameters.getParameter(NEIGHBOUR_OBJECTS));
                neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS);
                break;
            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjectsName = inputObjectsName;
                break;
        }

        returnedParameters.add(parameters.getParameter(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        returnedParameters.add(parameters.getParameter(CALCULATE_WITHIN_PARENT));
        if ((boolean) parameters.getValue(CALCULATE_WITHIN_PARENT)) {
            returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
            ((ParentObjectsP) parameters.getParameter(PARENT_OBJECTS)).setChildObjectsName(inputObjectsName);
        }

        returnedParameters.add(parameters.getParameter(LIMIT_LINKING_DISTANCE));
        if ((boolean) parameters.getValue(LIMIT_LINKING_DISTANCE)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCE));
        }
        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(EXPORT_ALL_DISTANCES));
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        if (referenceMode.equals(ReferenceModes.SURFACE)) 
            returnedParameters.add(parameters.getParameter(INSIDE_OUTSIDE_MODE));
                        
        if ((boolean) parameters.getValue(EXPORT_ALL_DISTANCES)) {
            returnedParameters.add(parameters.getParameter(INCLUDE_TIMEPOINTS));
            returnedParameters.add(parameters.getParameter(INCLUDE_INPUT_PARENT));
            if ((boolean) parameters.getValue(INCLUDE_INPUT_PARENT)) {
                returnedParameters.add(parameters.getParameter(INPUT_PARENT));
                ((ParentObjectsP) parameters.get(INPUT_PARENT)).setChildObjectsName(inputObjectsName);
            }
            returnedParameters.add(parameters.getParameter(INCLUDE_NEIGHBOUR_PARENT));
            if ((boolean) parameters.getValue(INCLUDE_NEIGHBOUR_PARENT)) {
                returnedParameters.add(parameters.getParameter(NEIGHBOUR_PARENT));
                ((ParentObjectsP) parameters.get(NEIGHBOUR_PARENT)).setChildObjectsName(neighbourObjectsName);
            }

            returnedParameters.add(parameters.getParameter(FILE_SAVING_SEPARATOR));
            returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
            switch ((String) parameters.getValue(SAVE_NAME_MODE)) {
                case SaveNameModes.SPECIFIC_NAME:
                    returnedParameters.add(parameters.getParameter(SAVE_FILE_NAME));
                    break;
            }

            returnedParameters.add(parameters.getParameter(APPEND_SERIES_MODE));
            returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));
            returnedParameters.add(parameters.getParameter(SAVE_SUFFIX));

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE);

        String neighbourObjectsName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS);
                break;
            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjectsName = inputObjectsName;
                break;
        }

        String name = getFullName(Measurements.NN_DISTANCE_CAL, neighbourObjectsName);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.NN_DISTANCE_PX, neighbourObjectsName);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.NN_ID, neighbourObjectsName);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        return returnedRefs;

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
}