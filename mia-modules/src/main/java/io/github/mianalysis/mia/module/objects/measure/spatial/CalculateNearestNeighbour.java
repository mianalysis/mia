package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.module.objects.relate.RelateManyToOne;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;

/**
 * Created by sc13967 on 22/06/2017.
 */

/**
 * Measures the shortest distance between all objects in the specified input
 * collection and all other objects in the same, or a different collection. The
 * shortest distance (nearest neighbour distance) is recorded as a measurement
 * associated with the input object. Optionally, the distances between all
 * objects can be calculated and exported as a standalone spreadsheet.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CalculateNearestNeighbour extends AbstractSaver {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Objects input";

    /**
     * Objects for which the distance to a closest neighbour will be calculated. The
     * closest distance will be stored as a measurement associated with this object.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Controls whether the nearest neighbour distance from each input object will
     * be calculated relative to all other objects in that input collection ("Within
     * same object set") or to all objects in another object collection ("Different
     * object set").
     */
    public static final String RELATIONSHIP_MODE = "Relationship mode";

    /**
     * If "Relationship mode" is set to "Different object set", the distance from
     * the input objects to these objects will be calculated and the shortest
     * distance recorded.
     */
    public static final String NEIGHBOUR_OBJECTS = "Neighbour objects";

    /**
    * 
    */
    public static final String RELATIONSHIP_SEPARATOR = "Relationship settings";

    /**
     * Controls the method used for determining the nearest neighbour distances:<br>
     * <ul>
     * <li>"Centroid (2D)" Distances are between the input and neighbour object
     * centroids, but only in the XY plane. These distances are always positive;
     * increasing as the distance between centroids increases.</li>
     * <li>"Centroid (3D)" Distances are between the input and neighbour object
     * centroids. These distances are always positive; increasing as the distance
     * between centroids increases.</li>
     * <li>"Surface (2D)" Distances are between the closest points on the input and
     * neighbour surfaces, but only in the XY plane. These distances increase in
     * magnitude the greater the minimum input-neighbour object surface distance is;
     * however, they are assigned a positive value if the closest input object
     * surface point is outside the neighbour and a negative value if the closest
     * input object surface point is inside the neighbour. For example, a closest
     * input object surface point 5px outside the neighbour will be simply "5px",
     * whereas a closest input object surface point 5px from the surface, but
     * contained within the neighbour object will be recorded as "-5px". Note: Any
     * instances where the input and neighbour object surfaces overlap will be
     * recorded as "0px" distance.</li>
     * <li>"Surface (3D)" Distances are between the closest points on the input and
     * neighbour surfaces. These distances increase in magnitude the greater the
     * minimum input-neighbour object surface distance is; however, they are
     * assigned a positive value if the closest input object surface point is
     * outside the neighbour and a negative value if the closest input object
     * surface point is inside the neighbour. For example, a closest input object
     * surface point 5px outside the neighbour will be simply "5px", whereas a
     * closest input object surface point 5px from the surface, but contained within
     * the neighbour object will be recorded as "-5px". Note: Any instances where
     * the input and neighbour object surfaces overlap will be recorded as "0px"
     * distance.</li>
     * </ul>
     */
    public static final String REFERENCE_MODE = "Reference mode";

    /**
     * When selected, only distances between objects within the same parent
     * (specified by "Parent objects") will be considered.
     */
    public static final String CALCULATE_WITHIN_PARENT = "Only calculate for objects in same parent";

    /**
     * When "Only calculate for objects in same parent" is selected, objects must
     * have this same parent to have their nearest neighbour distances calculated.
     */
    public static final String PARENT_OBJECTS = "Parent objects";

    /**
     * When selected, nearest neighbour distances will only be calculated if that
     * distance (as calculated by the "Reference mode" metric) is less than or equal
     * to the distance defined by "Maximum linking distance".
     */
    public static final String LIMIT_LINKING_DISTANCE = "Limit linking distance";

    /**
     * If "Limit linking distance" is selected, this is the maximum permitted
     * distance between objects for them to have their nearest neighbour distance
     * recorded.
     */
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance";

    /**
     * When selected, linking distances are to be specified in calibrated units;
     * otherwise, units are specified in pixels.
     */
    public static final String CALIBRATED_DISTANCE = "Calibrated distance";

    /**
     * When selected, objects must be in the same time frame for them to be linked.
     */
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Data output";

    /**
     * For each analysis run, create a separate spreadsheet file, which records the
     * distance of all objects to all other objects.
     */
    public static final String EXPORT_ALL_DISTANCES = "Export all distances";

    /**
     * When relating objects by surfaces it's possible to only consider objects
     * inside, outside or on the edge of the neighbouring object. This parameter
     * controls which objects are allowed to be related to a neighbour. Choices are:
     * Inside and outside (all distances), Inside only (distances less than 0),
     * Inside and on surface (distances less than or equal to 0), On surface only
     * (distances = 0), Outside and on surface (distances greater than or equal to
     * 0), Outside only (distances greater than 0).
     */
    public static final String INSIDE_OUTSIDE_MODE = "Inside/outside mode";

    /**
     * Include a column recording the timepoint that the objects were present in. If
     * only linking objects in the same frame, there will be a single timepoint
     * column; however, if links are permitted between objects in different
     * timepoints, a timepoint colummn for each of the related objects will be
     * included.
     */
    public static final String INCLUDE_TIMEPOINTS = "Include timepoints";

    /**
     * Include a column recording the ID number of a specific parent of the input
     * object. For example, this could be a track ID number.
     */
    public static final String INCLUDE_INPUT_PARENT = "Include input object parent";

    /**
     * Parent object collection of the input object. If "Include input object
     * parent" is selected, the corresponding parent ID number will be included as a
     * column in the output distances spreadsheet.
     */
    public static final String INPUT_PARENT = "Input object parent";

    /**
     * Include a column recording the ID number of a specific parent of the
     * neighbour object. For example, this could be a track ID number.
     */
    public static final String INCLUDE_NEIGHBOUR_PARENT = "Include neighbour object parent";

    /**
     * Parent object collection of the neighbour object. If "Include neighbour
     * object parent" is selected, the corresponding parent ID number will be
     * included as a column in the output distances spreadsheet.
     */
    public static final String NEIGHBOUR_PARENT = "Neighbour object parent";

    public interface RelationshipModes {
        String WITHIN_SAME_SET = "Within same object set";
        String DIFFERENT_SET = "Different object set";

        String[] ALL = new String[] { WITHIN_SAME_SET, DIFFERENT_SET };

    }

    public interface ReferenceModes {
        String CENTROID_2D = "Centroid (2D)";
        String CENTROID_3D = "Centroid (3D)";
        String SURFACE_2D = "Surface (2D)";
        String SURFACE_3D = "Surface (3D)";

        String[] ALL = new String[] { CENTROID_2D, CENTROID_3D, SURFACE_2D, SURFACE_3D };

    }

    public interface InsideOutsideModes extends RelateManyToOne.InsideOutsideModes {
    }

    public interface Measurements {
        String NN_DISTANCE_PX = "DISTANCE_(PX)";
        String NN_DISTANCE_CAL = "DISTANCE_(${SCAL})";
        String NN_ID = "NN_NEIGHBOUR_ID";
    }

    public CalculateNearestNeighbour(Modules modules) {
        super("Calculate nearest neighbour", modules);
    }

    public static String getFullName(String measurement, String neighbourObjectsName, String referenceMode) {
        return "NEAREST_NEIGHBOUR // " + neighbourObjectsName + " // " + referenceMode + " // " + measurement;
    }

    public static Obj getNearestNeighbour(Obj inputObject, Objs testObjects, String referenceMode,
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
                case ReferenceModes.CENTROID_2D:
                    dist = inputObject.getCentroidSeparation(testObject, true, true);
                    break;
                default:
                case ReferenceModes.CENTROID_3D:
                    dist = inputObject.getCentroidSeparation(testObject, true, false);
                    break;
                case ReferenceModes.SURFACE_2D:
                    dist = inputObject.getSurfaceSeparation(testObject, true, true, false, false);
                    break;
                case ReferenceModes.SURFACE_3D:
                    dist = inputObject.getSurfaceSeparation(testObject, true, false, false, false);
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

    public void addMeasurements(Obj inputObject, Obj nearestNeighbour, String referenceMode,
            String nearestNeighbourName) {

        // Adding details of the nearest neighbour to the input object's measurements
        if (nearestNeighbour != null) {
            double dppXY = inputObject.getDppXY();
            double minDist = 0;

            switch (referenceMode) {
                default:
                case ReferenceModes.CENTROID_3D:
                    minDist = inputObject.getCentroidSeparation(nearestNeighbour, true);
                    break;

                case ReferenceModes.SURFACE_3D:
                    minDist = inputObject.getSurfaceSeparation(nearestNeighbour, true, false, false);
                    break;
            }

            String name = getFullName(Measurements.NN_ID, nearestNeighbourName, referenceMode);
            inputObject.addMeasurement(new Measurement(name, nearestNeighbour.getID()));

            name = getFullName(Measurements.NN_DISTANCE_PX, nearestNeighbourName, referenceMode);
            inputObject.addMeasurement(new Measurement(name, minDist));

            name = getFullName(Measurements.NN_DISTANCE_CAL, nearestNeighbourName, referenceMode);
            inputObject.addMeasurement(new Measurement(name, minDist * dppXY));

        } else {
            String name = getFullName(Measurements.NN_ID, nearestNeighbourName, referenceMode);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.NN_DISTANCE_PX, nearestNeighbourName, referenceMode);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.NN_DISTANCE_CAL, nearestNeighbourName, referenceMode);
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
            if (linkInSameFrame)
                cell.setCellValue("TIMEPOINT");
            else
                cell.setCellValue(inputObjectsName + "\" TIMEPOINT");

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
        cell.setCellValue(SpatialUnit.replace("DISTANCE (${SCAL})"));
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
                MIA.log.writeError(e);
            }
        } catch (IOException e) {
            MIA.log.writeError(e);
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Measures the shortest distance between all objects in the specified input collection and all other objects in the same, or a different collection.  The shortest distance (nearest neighbour distance) is recorded as a measurement associated with the input object.  Optionally, the distances between all objects can be calculated and exported as a standalone spreadsheet.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE, workspace);
        String neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        boolean calculateWithinParent = parameters.getValue(CALCULATE_WITHIN_PARENT, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS, workspace);
        boolean limitLinkingDistance = parameters.getValue(LIMIT_LINKING_DISTANCE, workspace);
        double maxLinkingDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE, workspace);
        boolean calibratedDistance = parameters.getValue(CALIBRATED_DISTANCE, workspace);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME, workspace);
        boolean exportAllDistances = parameters.getValue(EXPORT_ALL_DISTANCES, workspace);
        String insideOutsideMode = parameters.getValue(INSIDE_OUTSIDE_MODE, workspace);
        boolean includeTimepoints = parameters.getValue(INCLUDE_TIMEPOINTS, workspace);
        boolean includeInputParent = parameters.getValue(INCLUDE_INPUT_PARENT, workspace);
        String inputParentsName = parameters.getValue(INPUT_PARENT, workspace);
        boolean includeNeighbourParent = parameters.getValue(INCLUDE_NEIGHBOUR_PARENT, workspace);
        String neighbourParentsName = parameters.getValue(NEIGHBOUR_PARENT, workspace);
        String saveNameMode = parameters.getValue(SAVE_NAME_MODE, workspace);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

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

        Objs neighbourObjects = null;
        String nearestNeighbourName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjects = workspace.getObjects(neighbourObjectsName);
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
        int count = 0;
        int total = inputObjects.size();
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
                    addMeasurements(inputObject, null, referenceMode, nearestNeighbourName);
                    continue;
                }

                Objs childObjects = parentObject.getChildren(nearestNeighbourName);
                Obj nearestNeighbour = getNearestNeighbour(inputObject, childObjects, referenceMode, maxLinkingDist,
                        linkInSameFrame, currDistances);
                addMeasurements(inputObject, nearestNeighbour, referenceMode, nearestNeighbourName);

            } else {
                Obj nearestNeighbour = getNearestNeighbour(inputObject, neighbourObjects, referenceMode, maxLinkingDist,
                        linkInSameFrame, currDistances);
                addMeasurements(inputObject, nearestNeighbour, referenceMode, nearestNeighbourName);
            }

            writeProgressStatus(++count, total, "objects");
            
        }

        if (exportAllDistances) {
            if (!includeInputParent)
                inputParentsName = null;

            if (!includeNeighbourParent)
                neighbourParentsName = null;

            String outputPath = getOutputPath(modules, workspace);
            String outputName = getOutputName(modules, workspace);

            // Adding last bits to name
            outputPath = outputPath + outputName;
            outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
            outputPath = appendDateTime(outputPath, appendDateTimeMode);
            outputPath = outputPath + suffix + ".xlsx";

            // Applying inside/outside policy
            for (LinkedHashMap<Obj, Double> collection : distances.values()) {
                for (Obj obj : collection.keySet()) {
                    // Applying the inside outside mode
                    if (!RelateManyToOne.applyInsideOutsidePolicy(collection.get(obj), insideOutsideMode))
                        collection.put(obj, 0d);
                }
            }

            // Writing distances to file
            SXSSFWorkbook workbook = exportDistances(distances, inputObjectsName, nearestNeighbourName,
                    includeTimepoints, linkInSameFrame, inputParentsName, neighbourParentsName);
            writeDistancesFile(workbook, outputPath);

        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(RELATIONSHIP_MODE, this, RelationshipModes.WITHIN_SAME_SET, RelationshipModes.ALL));
        parameters.add(new InputObjectsP(NEIGHBOUR_OBJECTS, this));

        parameters.add(new SeparatorP(RELATIONSHIP_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.CENTROID_3D, ReferenceModes.ALL));
        parameters.add(new BooleanP(CALCULATE_WITHIN_PARENT, this, false));
        parameters.add(new ParentObjectsP(PARENT_OBJECTS, this));
        parameters.add(new BooleanP(LIMIT_LINKING_DISTANCE, this, false));
        parameters.add(new DoubleP(MAXIMUM_LINKING_DISTANCE, this, 100d));
        parameters.add(new BooleanP(CALIBRATED_DISTANCE, this, false));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, false));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(EXPORT_ALL_DISTANCES, this, false));
        parameters.add(
                new ChoiceP(INSIDE_OUTSIDE_MODE, this, InsideOutsideModes.INSIDE_AND_OUTSIDE, InsideOutsideModes.ALL));
        parameters.add(new BooleanP(INCLUDE_TIMEPOINTS, this, false));
        parameters.add(new BooleanP(INCLUDE_INPUT_PARENT, this, false));
        parameters.add(new ParentObjectsP(INPUT_PARENT, this));
        parameters.add(new BooleanP(INCLUDE_NEIGHBOUR_PARENT, this, false));
        parameters.add(new ParentObjectsP(NEIGHBOUR_PARENT, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String neighbourObjectsName;

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(RELATIONSHIP_MODE));

        switch ((String) parameters.getValue(RELATIONSHIP_MODE, workspace)) {
            case RelationshipModes.DIFFERENT_SET:
            default:
                returnedParameters.add(parameters.getParameter(NEIGHBOUR_OBJECTS));
                neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS, workspace);
                break;
            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjectsName = inputObjectsName;
                break;
        }

        returnedParameters.add(parameters.getParameter(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        returnedParameters.add(parameters.getParameter(CALCULATE_WITHIN_PARENT));
        if ((boolean) parameters.getValue(CALCULATE_WITHIN_PARENT, workspace)) {
            returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
            ((ParentObjectsP) parameters.getParameter(PARENT_OBJECTS)).setChildObjectsName(inputObjectsName);
        }

        returnedParameters.add(parameters.getParameter(LIMIT_LINKING_DISTANCE));
        if ((boolean) parameters.getValue(LIMIT_LINKING_DISTANCE, workspace)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCE));
        }
        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(EXPORT_ALL_DISTANCES));
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        if (referenceMode.equals(ReferenceModes.SURFACE_3D))
            returnedParameters.add(parameters.getParameter(INSIDE_OUTSIDE_MODE));

        if ((boolean) parameters.getValue(EXPORT_ALL_DISTANCES, workspace)) {
            returnedParameters.add(parameters.getParameter(INCLUDE_TIMEPOINTS));
            returnedParameters.add(parameters.getParameter(INCLUDE_INPUT_PARENT));
            if ((boolean) parameters.getValue(INCLUDE_INPUT_PARENT, workspace)) {
                returnedParameters.add(parameters.getParameter(INPUT_PARENT));
                ((ParentObjectsP) parameters.get(INPUT_PARENT)).setChildObjectsName(inputObjectsName);
            }
            returnedParameters.add(parameters.getParameter(INCLUDE_NEIGHBOUR_PARENT));
            if ((boolean) parameters.getValue(INCLUDE_NEIGHBOUR_PARENT, workspace)) {
                returnedParameters.add(parameters.getParameter(NEIGHBOUR_PARENT));
                ((ParentObjectsP) parameters.get(NEIGHBOUR_PARENT)).setChildObjectsName(neighbourObjectsName);
            }

            returnedParameters.addAll(super.updateAndGetParameters());

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);

        String neighbourObjectsName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS, workspace);
                break;
            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjectsName = inputObjectsName;
                break;
        }

        String name = getFullName(Measurements.NN_DISTANCE_CAL, neighbourObjectsName, referenceMode);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.NN_DISTANCE_PX, neighbourObjectsName, referenceMode);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.NN_ID, neighbourObjectsName, referenceMode);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        return returnedRefs;

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

    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects for which the distance to a closest neighbour will be calculated.  The closest distance will be stored as a measurement associated with this object.");

        parameters.get(RELATIONSHIP_MODE).setDescription(
                "Controls whether the nearest neighbour distance from each input object will be calculated relative to all other objects in that input collection (\""
                        + RelationshipModes.WITHIN_SAME_SET + "\") or to all objects in another object collection (\""
                        + RelationshipModes.DIFFERENT_SET + "\").");

        parameters.get(NEIGHBOUR_OBJECTS).setDescription("If \"" + RELATIONSHIP_MODE + "\" is set to \""
                + RelationshipModes.DIFFERENT_SET
                + "\", the distance from the input objects to these objects will be calculated and the shortest distance recorded.");

        parameters.get(REFERENCE_MODE)
                .setDescription("Controls the method used for determining the nearest neighbour distances:<br><ul>"

                        + "<li>\"" + ReferenceModes.CENTROID_2D
                        + "\" Distances are between the input and neighbour object centroids, but only in the XY plane.  These distances are always positive; increasing as the distance between centroids increases.</li>"

                        + "<li>\"" + ReferenceModes.CENTROID_3D
                        + "\" Distances are between the input and neighbour object centroids.  These distances are always positive; increasing as the distance between centroids increases.</li>"

                        + "<li>\"" + ReferenceModes.SURFACE_2D
                        + "\" Distances are between the closest points on the input and neighbour surfaces, but only in the XY plane.  These distances increase in magnitude the greater the minimum input-neighbour object surface distance is; however, they are assigned a positive value if the closest input object surface point is outside the neighbour and a negative value if the closest input object surface point is inside the neighbour.  For example, a closest input object surface point 5px outside the neighbour will be simply \"5px\", whereas a closest input object surface point 5px from the surface, but contained within the neighbour object will be recorded as \"-5px\".  Note: Any instances where the input and neighbour object surfaces overlap will be recorded as \"0px\" distance.</li>"

                        + "<li>\"" + ReferenceModes.SURFACE_3D
                        + "\" Distances are between the closest points on the input and neighbour surfaces.  These distances increase in magnitude the greater the minimum input-neighbour object surface distance is; however, they are assigned a positive value if the closest input object surface point is outside the neighbour and a negative value if the closest input object surface point is inside the neighbour.  For example, a closest input object surface point 5px outside the neighbour will be simply \"5px\", whereas a closest input object surface point 5px from the surface, but contained within the neighbour object will be recorded as \"-5px\".  Note: Any instances where the input and neighbour object surfaces overlap will be recorded as \"0px\" distance.</li></ul>");

        parameters.get(CALCULATE_WITHIN_PARENT)
                .setDescription("When selected, only distances between objects within the same parent (specified by \""
                        + PARENT_OBJECTS + "\") will be considered.");

        parameters.get(PARENT_OBJECTS).setDescription("When \"" + CALCULATE_WITHIN_PARENT
                + "\" is selected, objects must have this same parent to have their nearest neighbour distances calculated.");

        parameters.get(LIMIT_LINKING_DISTANCE).setDescription(
                "When selected, nearest neighbour distances will only be calculated if that distance (as calculated by the \""
                        + REFERENCE_MODE + "\" metric) is less than or equal to the distance defined by \""
                        + MAXIMUM_LINKING_DISTANCE + "\".");

        parameters.get(MAXIMUM_LINKING_DISTANCE).setDescription("If \"" + LIMIT_LINKING_DISTANCE
                + "\" is selected, this is the maximum permitted distance between objects for them to have their nearest neighbour distance recorded.");

        parameters.get(CALIBRATED_DISTANCE).setDescription(
                "When selected, linking distances are to be specified in calibrated units; otherwise, units are specified in pixels.");

        parameters.get(LINK_IN_SAME_FRAME)
                .setDescription("When selected, objects must be in the same time frame for them to be linked.");

        parameters.get(EXPORT_ALL_DISTANCES).setDescription(
                "For each analysis run, create a separate spreadsheet file, which records the distance of all objects to all other objects.");

        parameters.get(INSIDE_OUTSIDE_MODE).setDescription(
                "When relating objects by surfaces it's possible to only consider objects inside, outside or on the edge of the neighbouring object.  This parameter controls which objects are allowed to be related to a neighbour.  Choices are: "
                        + String.join(", ", InsideOutsideModes.ALL) + ".");

        parameters.get(INCLUDE_TIMEPOINTS).setDescription(
                "Include a column recording the timepoint that the objects were present in.  If only linking objects in the same frame, there will be a single timepoint column; however, if links are permitted between objects in different timepoints, a timepoint colummn for each of the related objects will be included.");

        parameters.get(INCLUDE_INPUT_PARENT).setDescription(
                "Include a column recording the ID number of a specific parent of the input object.  For example, this could be a track ID number.");

        parameters.get(INPUT_PARENT).setDescription("Parent object collection of the input object.  If \""
                + INCLUDE_INPUT_PARENT
                + "\" is selected, the corresponding parent ID number will be included as a column in the output distances spreadsheet.");

        parameters.get(INCLUDE_NEIGHBOUR_PARENT).setDescription(
                "Include a column recording the ID number of a specific parent of the neighbour object.  For example, this could be a track ID number.");

        parameters.get(NEIGHBOUR_PARENT).setDescription("Parent object collection of the neighbour object.  If \""
                + INCLUDE_NEIGHBOUR_PARENT
                + "\" is selected, the corresponding parent ID number will be included as a column in the output distances spreadsheet.");

    }
}
