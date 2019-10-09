// TODO: Get measurements to export from analysis.getMacros().getMeasurements().get(String) for each object
// TODO: Export calibration and units to each object

package wbif.sjx.MIA.Process.Exporting;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.Logging.LogRenderer;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Metadata;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class Exporter {
    public enum ExportMode {
        ALL_TOGETHER, GROUP_BY_METADATA,INDIVIDUAL_FILES;
    }

    public enum SummaryMode {
        PER_FILE, PER_TIMEPOINT_PER_FILE, GROUP_BY_METADATA;
    }

    public enum AppendDateTimeMode {
        ALWAYS, IF_FILE_EXISTS, NEVER;
    }

    private boolean verbose = false;
    private ExportMode exportMode = ExportMode.ALL_TOGETHER;
    private String metadataItemForGrouping = null;
    private boolean exportSummary = true;
    private boolean showObjectCounts = true;
    private SummaryMode summaryMode = SummaryMode.PER_FILE;
    private String metadataItemForSummary = null;
    private boolean exportIndividualObjects = true;
    private AppendDateTimeMode appendDateTimeMode = AppendDateTimeMode.NEVER;


    // PUBLIC METHODS

    public void exportResults(WorkspaceCollection workspaces, Analysis analysis, String exportFilePath) throws IOException {
        switch (exportMode) {
            case ALL_TOGETHER:
                export(workspaces,analysis,exportFilePath);
                break;

            case GROUP_BY_METADATA:
                // Getting list of unique metadata values
                HashSet<String> metadataValues = new HashSet<>();
                for (Workspace workspace:workspaces) {
                    if (workspace.getMetadata().containsKey(metadataItemForGrouping)) {
                        metadataValues.add(workspace.getMetadata().get(metadataItemForGrouping).toString());
                    }
                }

                for (String metadataValue:metadataValues) {
                    WorkspaceCollection currentWorkspaces = new WorkspaceCollection();

                    // Adding Workspaces matching this metadata value
                    for (Workspace workspace:workspaces) {
                        if (!workspace.getMetadata().containsKey(metadataItemForGrouping)) continue;
                        if (workspace.getMetadata().get(metadataItemForGrouping).toString().equals(metadataValue)) {
                            currentWorkspaces.add(workspace);
                        }
                    }

                    String name = exportFilePath+"_"+metadataItemForGrouping+"-"+metadataValue;

                    export(currentWorkspaces,analysis,name);

                }

                break;

        }
    }

    public void exportResults(Workspace workspace, Analysis analysis, String name) throws IOException {
        WorkspaceCollection currentWorkspaces = new WorkspaceCollection();
        currentWorkspaces.add(workspace);

        export(currentWorkspaces,analysis,name);

    }

    public void export(WorkspaceCollection workspaces, Analysis analysis, String name) throws IOException {
        // Getting modules
        ModuleCollection modules = analysis.getModules();

        // Initialising the workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // Adding relevant sheets
        prepareParameters(workbook,analysis);
        prepareErrorLog(workbook);
        if (exportSummary) prepareSummary(workbook,workspaces,modules, summaryMode);
        if (exportIndividualObjects) prepareObjectsXLS(workbook,workspaces,modules);

        // Writing the workbook to file
        String outPath = name + ".xlsx";
        switch (appendDateTimeMode) {
            case ALWAYS:
                outPath = appendDateTime(outPath);
                break;
            case IF_FILE_EXISTS:
                if (new File(outPath).exists()) outPath = appendDateTime(outPath);
                break;
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(outPath);
            workbook.write(outputStream);

        } catch(FileNotFoundException e) {
            String newOutPath = appendDateTime(outPath);
            FileOutputStream outputStream = new FileOutputStream(newOutPath);
            workbook.write(outputStream);

            MIA.log.write("Target file ("+new File(outPath).getName()+") inaccessible", LogRenderer.Level.WARNING);
            MIA.log.write("Saved to alternative file ("+new File(newOutPath).getName()+")", LogRenderer.Level.WARNING);

        }

        workbook.close();

        if (verbose) System.out.println("Saved results");

    }

    private static String appendDateTime(String inputName) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        return inputName + "_("+ dateTime + ").xlsx";
    }

    private int appendConfigurationReport(Sheet sheet, Analysis analysis, int rowIdx) {
        Workbook workbook = sheet.getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Adding information about the system used
        Row row = sheet.createRow(rowIdx++);
        Cell cell = row.createCell(0);
        cell.setCellValue("SYSTEM CONFIGURATION");
        cell.setCellStyle(cellStyle);

        rowIdx++;

        // Adding information about the system used
        row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("MIA_VERSION");
        row.createCell(1).setCellValue(MIA.getVersion());

        row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("WORKFLOW FILENAME");
        row.createCell(1).setCellValue(analysis.getAnalysisFilename());

        row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("OS NAME");
        row.createCell(1).setCellValue(SystemUtils.OS_NAME);

        row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("OS ARCHITECTURE");
        row.createCell(1).setCellValue(SystemUtils.OS_ARCH);

        row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue("OS VERSION");
        row.createCell(1).setCellValue(SystemUtils.OS_VERSION);

        return rowIdx;

    }

    private void prepareParameters(SXSSFWorkbook workbook, Analysis analysis) {
        ModuleCollection modules = analysis.getModules();

        // Creating a sheet for parameters
        Sheet paramSheet = workbook.createSheet("Parameters");

        int rowIdx = 0;

        // Adding configuration information about the system
        rowIdx = appendConfigurationReport(paramSheet,analysis,rowIdx);

        // Adding a header row for the parameter titles
        rowIdx += 3;
        Row row = paramSheet.createRow(rowIdx++);

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Adding headings
        Cell cell = row.createCell(0);
        cell.setCellValue("PARAMETER/MODULE");
        cell.setCellStyle(cellStyle);
        cell = row.createCell(1);
        cell.setCellValue("NICKNAME");
        cell.setCellStyle(cellStyle);
        cell = row.createCell(2);
        cell.setCellValue("VALUE");
        cell.setCellStyle(cellStyle);
        cell = row.createCell(3);
        cell.setCellValue("VISIBLE");
        cell.setCellStyle(cellStyle);


        // Adding a new parameter to each row
        rowIdx++;
        Module module = modules.getInputControl();
        rowIdx = appendModuleParameters(paramSheet,module,module.updateAndGetParameters(),rowIdx) + 1;
        module = modules.getOutputControl();
        rowIdx = appendModuleParameters(paramSheet,module,module.updateAndGetParameters(),rowIdx) + 1;
        for (Module module1:modules) rowIdx = appendModuleParameters(paramSheet,module1,module1.updateAndGetParameters(),rowIdx) + 1;

    }

    private int appendModuleParameters(Sheet sheet, Module module, ParameterCollection parameters, int rowIdx) {
        // Adding module row.  Module will be null if coming from a ParameterGroup
        Row row = null;
        if (module != null) {
            row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue("MODULE: " + module.getName());
            row.createCell(1).setCellValue(module.getNickname());
            row.createCell(2).setCellValue(module.isEnabled());
            row.createCell(3).setCellValue(module.canBeDisabled());
        }

        for (Parameter currParam : parameters.values()) {
            // Check if the parameter is to be exported
            if (!currParam.isExported()) continue;

            row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(currParam.getNameAsString());
            row.createCell(1).setCellValue(currParam.getNickname());
            row.createCell(2).setCellValue(currParam.getRawStringValue());
            row.createCell(3).setCellValue(currParam.isVisible());

            // If this parameter is a ParameterGroup, also list those parameters
            if (currParam instanceof ParameterGroup) {
                LinkedHashSet<ParameterCollection> collections = ((ParameterGroup) currParam).getCollections();
                for (ParameterCollection collection:collections) rowIdx = appendModuleParameters(sheet,null,collection,rowIdx);
            }
        }

        return rowIdx;

    }

    private void prepareErrorLog(SXSSFWorkbook workbook) {
        // Creating a sheet for parameters
        Sheet errorSheet = workbook.createSheet("Log");

        // Getting error write text and split by line returns
        String logText = MIA.log.getLogText();
        StringTokenizer tokenizer = new StringTokenizer(logText,"\n");

        // Adding a header row for the parameter titles
        int rowCount = 0;
        while (tokenizer.hasMoreTokens()) {
            Row row = errorSheet.createRow(rowCount++);
            row.createCell(0).setCellValue(tokenizer.nextToken());
        }
    }

    private void prepareSummary(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules,
                                SummaryMode summaryType) {
        AtomicInteger headerCol = new AtomicInteger(0);

        // Adding header rows for the metadata sheet.
        Sheet summarySheet = workbook.createSheet("Summary");
        Row summaryHeaderRow = summarySheet.createRow(0);

        // Creating a HashMap to store column numbers
        HashMap<String,Integer> colNumbers = new HashMap<>();
        addSummaryMetadataHeaders(summaryHeaderRow,modules,colNumbers,headerCol,summaryType);
        addSummaryGroupHeader(summaryHeaderRow,colNumbers,headerCol,summaryType);
        addSummaryImageHeaders(summaryHeaderRow,modules,colNumbers,headerCol);
        addSummaryObjectHeaders(summaryHeaderRow,modules,colNumbers,headerCol);

        // Running through each Workspace, adding a row
        int summaryRow = 1;
        switch (summaryType) {
            case PER_FILE:
                for (Workspace workspace:workspaces) {
                    Row summaryValueRow = summarySheet.createRow(summaryRow++);
                    populateSummaryRow(summaryValueRow, workspace, modules, colNumbers, null, null);
                }
                break;

            case PER_TIMEPOINT_PER_FILE:
                for (Workspace workspace:workspaces) {
                    // For the current workspace, iterating over all available time points and creating a new workspace
                    HashMap<Integer, Workspace> currentWorkspaces = workspace.getSingleTimepointWorkspaces();
                    for (Integer timepoint : currentWorkspaces.keySet()) {
                        Workspace currentWorkspace = currentWorkspaces.get(timepoint);
                        Row summaryValueRow = summarySheet.createRow(summaryRow++);
                        populateSummaryRow(summaryValueRow, currentWorkspace, modules, colNumbers, "TIMEPOINT", String.valueOf(timepoint));

                    }
                }
                break;

            case GROUP_BY_METADATA:
                HashMap<String, Workspace> metadataWorkspaces = workspaces.getMetadataWorkspaces(metadataItemForSummary);
                for (String metadataValue: metadataWorkspaces.keySet()) {
                    Workspace currentWorkspace = metadataWorkspaces.get(metadataValue);
                    Row summaryValueRow = summarySheet.createRow(summaryRow++);
                    populateSummaryRow(summaryValueRow, currentWorkspace, modules, colNumbers, metadataItemForSummary, metadataValue);

                }
                break;
        }
    }

    private void addSummaryMetadataHeaders(Row summaryHeaderRow, ModuleCollection modules, HashMap<String,Integer> colNumbers, AtomicInteger headerCol, SummaryMode summaryType) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        switch (summaryType) {
            case GROUP_BY_METADATA:
                String summaryDataName = getMetadataString("Count");
                Cell cell = summaryHeaderRow.createCell(headerCol.get());
                cell.setCellValue(summaryDataName);
                cell.setCellStyle(cellStyle);
                colNumbers.put(summaryDataName, headerCol.getAndIncrement());
                break;

            case PER_FILE:
            case PER_TIMEPOINT_PER_FILE:
                // Adding metadata headers
                MetadataRefCollection metadataRefs = modules.getMetadataRefs(null);
                // Running through all the metadata values, adding them as new columns
                for (MetadataRef ref:metadataRefs.values()) {
                    if (!ref.isExportGlobal()) continue;
                    if (!ref.isExportIndividual()) continue;

                    summaryDataName = getMetadataString(ref.getName());
                    cell = summaryHeaderRow.createCell(headerCol.get());
                    cell.setCellValue(summaryDataName);
                    cell.setCellStyle(cellStyle);
                    colNumbers.put(summaryDataName, headerCol.getAndIncrement());
                }
                break;
        }
    }

    private void addSummaryGroupHeader(Row summaryHeaderRow, HashMap<String,Integer> colNumbers, AtomicInteger headerCol, SummaryMode summaryType) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Add a column to record the timepoint
        switch (summaryType) {
            case PER_TIMEPOINT_PER_FILE:
                String timepointDataName = getMetadataString("TIMEPOINT");
                Cell cell = summaryHeaderRow.createCell(headerCol.get());
                cell.setCellValue(timepointDataName);
                cell.setCellStyle(cellStyle);
                colNumbers.put(timepointDataName,headerCol.getAndIncrement());
                break;
            case GROUP_BY_METADATA:
                String metadataDataName = getMetadataString(metadataItemForSummary);
                cell = summaryHeaderRow.createCell(headerCol.get());
                cell.setCellValue(metadataDataName);
                cell.setCellStyle(cellStyle);
                colNumbers.put(metadataDataName,headerCol.getAndIncrement());
                break;
        }
    }

    private void addSummaryImageHeaders(Row summaryHeaderRow, ModuleCollection modules, HashMap<String,Integer> colNumbers, AtomicInteger headerCol) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        LinkedHashSet<OutputImageP> availableImages = modules.getAvailableImages(null,true);
        if (availableImages != null) {
            for (OutputImageP availableImage : availableImages) {
                String availableImageName = availableImage.getImageName();

                ImageMeasurementRefCollection availableMeasurements = modules.getImageMeasurementRefs(availableImageName);

                // Running through all the image measurement values, adding them as new columns
                for (ImageMeasurementRef imageMeasurement:availableMeasurements.values()) {
                    if (!imageMeasurement.isExportIndividual()) continue;
                    if (!imageMeasurement.isExportGlobal()) continue;

                    String measurementName = imageMeasurement.getNickname();
                    String summaryDataName = getImageString(availableImageName, measurementName);
                    Cell cell = summaryHeaderRow.createCell(headerCol.get());
                    cell.setCellValue(summaryDataName);
                    cell.setCellStyle(cellStyle);
                    colNumbers.put(summaryDataName, headerCol.getAndIncrement());

                }
            }
        }
    }

    private void addSummaryObjectHeaders(Row summaryHeaderRow, ModuleCollection modules, HashMap<String,Integer> colNumbers, AtomicInteger headerCol) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Adding object headers
        LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(null,true);
        if (availableObjects != null) {
            for (OutputObjectsP availableObject:availableObjects) {
                String availableObjectName = availableObject.getObjectsName();

                Cell cell; String summaryDataName;

                // Adding the number of objects
                if (showObjectCounts) {
                    summaryDataName = getObjectString(availableObjectName, "", "NUMBER");
                    cell = summaryHeaderRow.createCell(headerCol.get());
                    cell.setCellValue(summaryDataName);
                    cell.setCellStyle(cellStyle);
                    addComment(cell,"Num of \""+availableObjectName+"\" objects.");
                    colNumbers.put(summaryDataName, headerCol.getAndIncrement());
                }

                // Running through all the object's children
                RelationshipRefCollection relationshipRefs = modules.getRelationshipRefs();
                for (RelationshipRef ref:relationshipRefs.getChildren(availableObjectName,false)) {
                    if (!ref.isExportGlobal()) continue;
                    if (!ref.isExportIndividual()) continue;

                    String child = ref.getChildName();
                    if (ref.isExportMean()) {
                        addSummaryChildHeader(summaryHeaderRow,colNumbers,headerCol,availableObjectName,child,"MEAN","Mean");
                    }

                    if (ref.isExportMin()) {
                        addSummaryChildHeader(summaryHeaderRow,colNumbers,headerCol,availableObjectName,child,"MIN","Minimum");
                    }

                    if (ref.isExportMax()) {
                        addSummaryChildHeader(summaryHeaderRow,colNumbers,headerCol,availableObjectName,child,"MAX","Maximum");
                    }

                    if (ref.isExportStd()) {
                        addSummaryChildHeader(summaryHeaderRow,colNumbers,headerCol,availableObjectName,child,"STD","Standard deviation");
                    }

                    if (ref.isExportSum()) {
                        addSummaryChildHeader(summaryHeaderRow,colNumbers,headerCol,availableObjectName,child,"SUM","Sum");
                    }
                }

                ObjMeasurementRefCollection objectMeasurementRefs = modules.getObjectMeasurementRefs(availableObjectName);

                // If the current object hasn't got any assigned measurements, skip it
                if (objectMeasurementRefs == null) continue;

                // Running through all the object measurement values, adding them as new columns
                for (ObjMeasurementRef objectMeasurement : objectMeasurementRefs.values()) {
                    if (!objectMeasurement.isExportIndividual()) continue;
                    if (!objectMeasurement.isExportGlobal()) continue;

                    if (objectMeasurement.isExportMean()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow,colNumbers,headerCol,objectMeasurement,"MEAN","Mean");
                    }

                    if (objectMeasurement.isExportMin()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow,colNumbers,headerCol,objectMeasurement,"MIN","Minimum");
                    }

                    if (objectMeasurement.isExportMax()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow,colNumbers,headerCol,objectMeasurement,"MAX","Maximum");
                    }

                    if (objectMeasurement.isExportStd()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow,colNumbers,headerCol,objectMeasurement,"STD","Standard deviation");
                    }

                    if (objectMeasurement.isExportSum()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow,colNumbers,headerCol,objectMeasurement,"SUM","Sum");
                    }
                }
            }
        }
    }

    private void addSummaryChildHeader(Row summaryHeaderRow, HashMap<String,Integer> colNumbers, AtomicInteger headerCol, String objectName, String child, String shortName, String longName) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        String summaryDataName = getObjectString(objectName, shortName, "NUM_CHILDREN_" + child);
        Cell cell = summaryHeaderRow.createCell(headerCol.get());
        cell.setCellValue(summaryDataName);
        cell.setCellStyle(cellStyle);
        addNumberOfChildrenComment(cell,objectName,child,longName);
        colNumbers.put(summaryDataName, headerCol.getAndIncrement());
    }

    private void addSummaryObjectStatisticHeader(Row summaryHeaderRow, HashMap<String,Integer> colNumbers, AtomicInteger headerCol, ObjMeasurementRef objectMeasurement, String shortName, String longName) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        String availableObjectName = objectMeasurement.getObjectsName();
        String summaryDataName = getObjectString(availableObjectName, shortName, objectMeasurement.getNickname());

        Cell cell = summaryHeaderRow.createCell(headerCol.get());
        cell.setCellValue(summaryDataName);
        cell.setCellStyle(cellStyle);
        addSummaryComment(cell,objectMeasurement,longName);
        colNumbers.put(summaryDataName, headerCol.getAndIncrement());
    }

    private void addNumberOfChildrenComment(Cell cell, String parent, String child, String calculation) {
        String text = "";
        switch (summaryMode) {
            case PER_FILE:
                text = calculation+" number of \""+child+"\" child objects for all \""+parent+"\" parent objects " +
                        "in the input file.";
                break;
            case PER_TIMEPOINT_PER_FILE:
                text = calculation+" number of \""+child+"\" child objects for all \""+parent+"\" parent objects " +
                        "at the stated timepoint of the input file.";
                break;
        }

        addComment(cell,text);

    }

    private void addSummaryComment(Cell cell, ObjMeasurementRef measurement, String calculation) {
        String text = "";
        switch (summaryMode) {
            case PER_FILE:
                text = calculation+" value of the measurement (described below) for all \""
                        +measurement.getObjectsName()+"\" objects in the input file." +
                        "\n\nMeasurement: "+measurement.getDescription();
                break;
            case PER_TIMEPOINT_PER_FILE:
                text = calculation+" value of the measurement (described below) for all \""
                        +measurement.getObjectsName()+"\" objects at the stated timepoint of the input file." +
                        "\n\nMeasurement: "+measurement.getDescription();
                break;
        }

        addComment(cell,text);

    }

    private void populateSummaryRow(Row summaryValueRow, Workspace workspace, ModuleCollection modules,
                                    HashMap<String,Integer> colNumbers, @Nullable String groupTitle, @Nullable String groupValue) {

        // Adding metadata values
        Metadata metadata = workspace.getMetadata();
        for (String name : metadata.keySet()) {
            String headerName = getMetadataString(name);
            if (!colNumbers.containsKey(headerName)) continue;
            int colNumber = colNumbers.get(headerName);
            summaryValueRow.createCell(colNumber).setCellValue(metadata.getAsString(name));
        }

        if (groupTitle != null) {
            String timepointName = getMetadataString(groupTitle);
            int colNumber = colNumbers.get(timepointName);
            summaryValueRow.createCell(colNumber).setCellValue(groupValue);
        }

        // Adding image measurements
        HashMap<String,Image<?>> images = workspace.getImages();
        for (Image image:images.values()) {
            String imageName = image.getName();

            ImageMeasurementRefCollection imageMeasurementRefs = modules.getImageMeasurementRefs(imageName);

            // If the current object hasn't got any assigned measurements, skip it
            if (imageMeasurementRefs == null) continue;

            // Running through all the object measurement values, adding them as new columns
            for (ImageMeasurementRef imageMeasurement : imageMeasurementRefs.values()) {
                if (!imageMeasurement.isExportIndividual()) continue;
                if (!imageMeasurement.isExportGlobal()) continue;

                Measurement measurement = image.getMeasurement(imageMeasurement.getName());

                String headerName = getImageString(imageName,imageMeasurement.getNickname());
                if (!colNumbers.containsKey(headerName)) continue;
                int colNum = colNumbers.get(headerName);

                Cell summaryCell = summaryValueRow.createCell(colNum);
                if (measurement == null) continue;

                double val = measurement.getValue();
                if (Double.isNaN(val)) summaryCell.setCellValue("");
                else summaryCell.setCellValue(val);
            }
        }

        // Adding object measurements
        HashMap<String, ObjCollection> objSets = workspace.getObjects();
        for (ObjCollection objCollection :objSets.values()) {
            String objSetName = objCollection.getName();
            double val; String headerName; int colNum; Cell summaryCell;

            if (showObjectCounts) {
                headerName = getObjectString(objSetName, "", "NUMBER");
                if (!colNumbers.containsKey(headerName)) break;
                colNum = colNumbers.get(headerName);
                summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(objCollection.size());
            }

            // Running through all the object's children
            RelationshipRefCollection relationshipRefs = modules.getRelationshipRefs();
            for (RelationshipRef ref:relationshipRefs.getChildren(objSetName,false)) {
                if (!ref.isExportGlobal()) continue;
                if (!ref.isExportIndividual()) continue;

                String child = ref.getChildName();

                // Running through all objects in this set, adding children to a CumStat object
                CumStat cs = new CumStat();
                for (Obj obj : objCollection.values()) {
                    ObjCollection children = obj.getChildren(child);
                    if (children != null) cs.addMeasure(children.size());
                }

                if (ref.isExportMean()) {
                    headerName = getObjectString(objSetName, "MEAN", "NUM_CHILDREN_" + child);
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMean();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (ref.isExportMin()) {
                    headerName = getObjectString(objSetName, "MIN", "NUM_CHILDREN_" + child);
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMin();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (ref.isExportMax()) {
                    headerName = getObjectString(objSetName, "MAX", "NUM_CHILDREN_" + child);
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMax();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (ref.isExportStd()) {
                    headerName = getObjectString(objSetName, "STD", "NUM_CHILDREN_" + child);
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getStd();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (ref.isExportSum()) {
                    headerName = getObjectString(objSetName, "SUM", "NUM_CHILDREN_" + child);
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getSum();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }
            }

            ObjMeasurementRefCollection objectMeasurementRefs = modules.getObjectMeasurementRefs(objSetName);

            // If the current object hasn't got any assigned measurements, skip it
            if (objectMeasurementRefs == null) continue;

            // Running through all the object measurement values, adding them as new columns
            for (ObjMeasurementRef objectMeasurement : objectMeasurementRefs.values()) {
                if (!objectMeasurement.isExportIndividual()) continue;
                if (!objectMeasurement.isExportGlobal()) continue;

                // Running through all objects in this set, adding measurements to a CumStat object
                CumStat cs = new CumStat();
                for (Obj obj: objCollection.values()) {
                    Measurement measurement = obj.getMeasurement(objectMeasurement.getName());
                    if (measurement != null) cs.addMeasure(measurement.getValue());
                }

                if (objectMeasurement.isExportMean()) {
                    headerName = getObjectString(objSetName, "MEAN", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMean();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportMin()) {
                    headerName = getObjectString(objSetName, "MIN", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMin();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportMax()) {
                    headerName = getObjectString(objSetName, "MAX", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMax();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportStd()) {
                    headerName = getObjectString(objSetName, "STD", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getStd();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportSum()) {
                    headerName = getObjectString(objSetName, "SUM", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName)) break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getSum();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }
            }
        }
    }

    private void prepareObjectsXLS(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules) {
        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Creating a new sheet for each object.  Each analysed file has its own set of rows (one for each object)
        HashMap<String, Sheet> objectSheets = new HashMap<>();
        HashMap<String, Integer> objectRows = new HashMap<>();

        // Creating a LinkedHashMap that links relationship ID names to column numbers.  This keeps the correct
        // relationships in the correct columns
        LinkedHashMap<String, LinkedHashMap<Integer,String>> parentNames = new LinkedHashMap<>();
        LinkedHashMap<String, LinkedHashMap<Integer,String>> childNames = new LinkedHashMap<>();

        // Creating a LinkedHashMap that links measurement names to column numbers.  This keeps the correct
        // measurements in the correct columns
        LinkedHashMap<String, LinkedHashMap<Integer,String>> measurementNames = new LinkedHashMap<>();

        LinkedHashMap<Integer, String> metadataNames = new LinkedHashMap<>();

        // Using the first workspace in the WorkspaceCollection to initialise column headers
        LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(null,true);
        if (availableObjects == null) return;

        for (OutputObjectsP availableObject:availableObjects) {
            if (!availableObject.isExported()) return;

            String objectName = availableObject.getObjectsName();

            // Check if this object has any associated measurements; if not, skip it
            if (!modules.objectsExportMeasurements(objectName)) continue;

            // Creating relevant sheet prefixed with "OBJ"
            objectSheets.put(objectName, workbook.createSheet("OBJ_" + objectName));

            objectRows.put(objectName, 1);
            Row objectHeaderRow = objectSheets.get(objectName).createRow(0);

            // Adding headers to each column
            int col = 0;

            Cell cell = objectHeaderRow.createCell(col++);
            cell.setCellValue("OBJECT_ID");
            cell.setCellStyle(cellStyle);
            String text = "ID number for this object.  Unique in the present image, but can be duplicated in other " +
                    "images";
            addComment(cell,text);

            // Adding timepoint header
            cell = objectHeaderRow.createCell(col++);
            cell.setCellValue("TIMEPOINT");
            cell.setCellStyle(cellStyle);

            // Running through all the metadata values, adding them as new columns
            MetadataRefCollection metadataRefs = modules.getMetadataRefs(null);
            for (MetadataRef ref : metadataRefs.values()) {
                if (!ref.isExportGlobal()) continue;
                if (!ref.isExportIndividual()) continue;

                metadataNames.put(col,ref.getName());
                cell = objectHeaderRow.createCell(col++);
                cell.setCellValue(getMetadataString(ref.getName()));
                cell.setCellStyle(cellStyle);
            }

            // Adding parent IDs
            RelationshipRefCollection relationships = modules.getRelationshipRefs();
            String[] parents = relationships.getParentNames(objectName,false);
            if (parents != null) {
                for (String parent : parents) {
                    RelationshipRef ref = relationships.getOrPut(parent,objectName);
                    if (!ref.isExportGlobal()) continue;
                    if (!ref.isExportIndividual()) continue;

                    parentNames.putIfAbsent(objectName, new LinkedHashMap<>());
                    parentNames.get(objectName).put(col, parent);
                    cell = objectHeaderRow.createCell(col++);
                    cell.setCellValue("PARENT_" + parent + "_ID");
                    cell.setCellStyle(cellStyle);
                }
            }

            // Adding number of children for each child type
            String[] children = relationships.getChildNames(objectName,false);
            if (children != null) {
                for (String child : children) {
                    RelationshipRef ref = relationships.getOrPut(objectName,child);
                    if (!ref.isExportGlobal()) continue;
                    if (!ref.isExportIndividual()) continue;

                    childNames.putIfAbsent(objectName, new LinkedHashMap<>());
                    childNames.get(objectName).put(col, child);
                    cell = objectHeaderRow.createCell(col++);
                    cell.setCellValue("NUMBER_OF_" + child + "_CHILDREN");
                    cell.setCellStyle(cellStyle);
                }
            }

            // Running through all the object measurement values, adding them as new columns
            ObjMeasurementRefCollection objectMeasurementRefs = modules.getObjectMeasurementRefs(objectName);
            for (ObjMeasurementRef objectMeasurement : objectMeasurementRefs.values()) {
                if (!objectMeasurement.isExportIndividual()) continue;
                if (!objectMeasurement.isExportGlobal()) continue;

                measurementNames.putIfAbsent(objectName, new LinkedHashMap<>());
                measurementNames.get(objectName).put(col, objectMeasurement.getName());
                cell = objectHeaderRow.createCell(col++);
                addComment(cell,objectMeasurement.getDescription());
                cell.setCellValue(objectMeasurement.getNickname());
                cell.setCellStyle(cellStyle);

            }
        }

        // Running through each Workspace, adding rows
        for (Workspace workspace : workspaces) {
            for (String objectName : workspace.getObjects().keySet()) {
                ObjCollection objects = workspace.getObjects().get(objectName);

                if (!modules.objectsExportMeasurements(objectName)) continue;

                if (objects.values().iterator().hasNext()) {
                    for (Obj object : objects.values()) {
                        // Adding the measurements from this image
                        int col = 0;

                        Row objectValueRow = objectSheets.get(objectName).createRow(objectRows.get(objectName));
                        objectRows.compute(objectName, (k, v) -> v = v + 1);

                        Cell objectIDValueCell = objectValueRow.createCell(col++);
                        objectIDValueCell.setCellValue(object.getID());

                        Cell timepointValueCell = objectValueRow.createCell(col++);
                        timepointValueCell.setCellValue(object.getT());

                        // Adding metadata (if enabled)
                        Metadata metadata = workspace.getMetadata();
                        for (int column:metadataNames.keySet()) {
                            Cell metaValueCell = objectValueRow.createCell(column);
                            metaValueCell.setCellValue(metadata.getAsString(metadataNames.get(column)));
                        }

                        // Adding parents to the columns specified in parentNames
                        if (parentNames.get(objectName) != null) {
                            for (int column : parentNames.get(objectName).keySet()) {
                                Cell parentValueCell = objectValueRow.createCell(column);
                                String parentName = parentNames.get(objectName).get(column);
                                Obj parent = object.getParent(parentName);
                                if (parent != null) {
                                    parentValueCell.setCellValue(parent.getID());
                                } else {
                                    parentValueCell.setCellValue("");
                                }
                                col++;
                            }
                        }

                        // Adding number of children to the columns specified in childNames
                        if (childNames.get(objectName) != null) {
                            for (int column : childNames.get(objectName).keySet()) {
                                Cell childValueCell = objectValueRow.createCell(column);
                                String childName = childNames.get(objectName).get(column);
                                ObjCollection children = object.getChildren(childName);
                                if (children != null) {
                                    childValueCell.setCellValue(children.size());
                                } else {
                                    childValueCell.setCellValue("0");
                                }
                                col++;
                            }
                        }

                        if (measurementNames.get(objectName) == null) continue;

                        // Adding measurements to the columns specified in measurementNames
                        for (int column : measurementNames.get(objectName).keySet()) {
                            Cell measValueCell = objectValueRow.createCell(column);
                            String measurementName = measurementNames.get(objectName).get(column);
                            Measurement measurement = object.getMeasurement(measurementName);

                            // If there isn't a corresponding value for this object, set a blank cell
                            if (measurement == null) {
                                measValueCell.setCellValue("");
                                continue;
                            }

                            // If the value is a NaN, also set a blank cell
                            if (Double.isNaN(measurement.getValue())) {
                                measValueCell.setCellValue("");
                                continue;

                            }

                            measValueCell.setCellValue(measurement.getValue());

                        }
                    }
                }
            }
        }
    }

    private String getMetadataString(String metadataName) {
        return "META // "+metadataName;//.replaceAll(" ", "_");

    }

    private String getImageString(String imageName, String measurementName) {
//        imageName = imageName.replaceAll(" ", "_");

        return imageName+"_(IM) // "+measurementName;//.replaceAll(" ", "_");

    }

    private String getObjectString(String objectName, String mode, String measurementName) {
//        objectName = objectName.replaceAll(" ", "_");

        if (mode.equals("")) {
            return objectName+"_(OBJ) // "+measurementName;//.replaceAll(" ", "_");
        } else {
            return objectName+"_(OBJ_"+mode+") // "+measurementName;//.replaceAll(" ", "_");
        }
    }

    private void addComment(Cell cell, String text) {
        Row row = cell.getRow();
        Sheet sheet = cell.getSheet();
        Workbook workbook = sheet.getWorkbook();

        // When the comment box is visible, have it show in a 1x3 space
        Drawing drawing = sheet.createDrawingPatriarch();
        CreationHelper factory = workbook.getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex()+6);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum()+10);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        comment.setAuthor("MIA");

        // Assign the comment to the cell
        cell.setCellComment(comment);

    }


    // GETTERS AND SETTERS

    public ExportMode getExportMode() {
        return exportMode;
    }

    public void setExportMode(ExportMode exportMode) {
        this.exportMode = exportMode;
    }

    public String getMetadataItemForGrouping() {
        return metadataItemForGrouping;
    }

    public void setMetadataItemForGrouping(String metadataItemForGrouping) {
        this.metadataItemForGrouping = metadataItemForGrouping;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isExportSummary() {
        return exportSummary;
    }

    public void setExportSummary(boolean exportSummary) {
        this.exportSummary = exportSummary;
    }

    public SummaryMode getSummaryMode() {
        return summaryMode;
    }

    public void setSummaryMode(SummaryMode summaryMode) {
        this.summaryMode = summaryMode;
    }

    public AppendDateTimeMode getAppendDateTimeMode() {
        return appendDateTimeMode;
    }

    public void setAppendDateTimeMode(AppendDateTimeMode appendDateTimeMode) {
        this.appendDateTimeMode = appendDateTimeMode;
    }

    public boolean isExportIndividualObjects() {
        return exportIndividualObjects;
    }

    public void setExportIndividualObjects(boolean exportIndividualObjects) {
        this.exportIndividualObjects = exportIndividualObjects;
    }

    public boolean isShowObjectCounts() {
        return showObjectCounts;
    }

    public void setShowObjectCounts(boolean showObjectCounts) {
        this.showObjectCounts = showObjectCounts;
    }

    public String getMetadataItemForSummary() {
        return metadataItemForSummary;
    }

    public void setMetadataItemForSummary(String metadataItemForSummary) {
        this.metadataItemForSummary = metadataItemForSummary;
    }
}
