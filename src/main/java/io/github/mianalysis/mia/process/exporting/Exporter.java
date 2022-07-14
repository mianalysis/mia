// TODO: Get measurements to export from analysis.getMacros().getMeasurements().get(String) for each object
// TODO: Export calibration and units to each object

package io.github.mianalysis.mia.process.exporting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.SystemUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.w3c.dom.Document;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.process.analysishandling.Analysis;
import io.github.mianalysis.mia.process.analysishandling.AnalysisWriter;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import io.github.mianalysis.mia.process.logging.LogRenderer.Level;
import io.github.sjcross.sjcommon.mathfunc.CumStat;
import io.github.sjcross.sjcommon.metadataextractors.Metadata;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class Exporter {
    public enum ExportMode {
        ALL_TOGETHER, GROUP_BY_METADATA, INDIVIDUAL_FILES;
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

    public void exportResults(Workspaces workspaces, Analysis analysis, String exportFilePath) throws IOException {
        switch (exportMode) {
            case ALL_TOGETHER:
                export(workspaces, analysis, exportFilePath);
                break;

            case GROUP_BY_METADATA:
                // Getting list of unique metadata values
                HashSet<String> metadataValues = new HashSet<>();
                for (Workspace workspace : workspaces) {
                    if (!workspace.exportWorkspace())
                        continue;
                    if (workspace.getMetadata().containsKey(metadataItemForGrouping)) {
                        metadataValues.add(workspace.getMetadata().get(metadataItemForGrouping).toString());
                    }
                }

                for (String metadataValue : metadataValues) {
                    Workspaces currentWorkspaces = new Workspaces();

                    // Adding Workspaces matching this metadata value
                    for (Workspace workspace : workspaces) {
                        if (!workspace.exportWorkspace())
                            continue;
                        if (!workspace.getMetadata().containsKey(metadataItemForGrouping))
                            continue;
                        if (workspace.getMetadata().get(metadataItemForGrouping).toString().equals(metadataValue)) {
                            currentWorkspaces.add(workspace);
                        }
                    }

                    String name = exportFilePath + "_" + metadataItemForGrouping + "-" + metadataValue;

                    export(currentWorkspaces, analysis, name);

                }

                break;

        }
    }

    public void exportResults(Workspace workspace, Analysis analysis, String name) throws IOException {
        Workspaces currentWorkspaces = new Workspaces();
        currentWorkspaces.add(workspace);

        export(currentWorkspaces, analysis, name);

    }

    public void export(Workspaces workspaces, Analysis analysis, String name) throws IOException {
        // Getting modules
        Modules modules = analysis.getModules();

        // Initialising the workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // Adding relevant sheets
        if (exportSummary)
            prepareSummary(workbook, workspaces, modules, summaryMode);
        if (exportIndividualObjects)
            prepareObjectsXLS(workbook, workspaces, modules);

        prepareConfiguration(workbook, analysis);
        prepareLog(workbook);

        // Writing the workbook to file
        String outPath = name + ".xlsx";
        switch (appendDateTimeMode) {
            case ALWAYS:
                outPath = appendDateTime(outPath);
                break;
            case IF_FILE_EXISTS:
                if (new File(outPath).exists())
                    outPath = appendDateTime(outPath);
                break;
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(outPath);
            workbook.write(outputStream);

        } catch (FileNotFoundException e) {
            String newOutPath = appendDateTime(outPath);
            FileOutputStream outputStream = new FileOutputStream(newOutPath);
            workbook.write(outputStream);

            MIA.log.write("Target file (" + new File(outPath).getName() + ") inaccessible", LogRenderer.Level.WARNING);
            MIA.log.write("Saved to alternative file (" + new File(newOutPath).getName() + ")",
                    LogRenderer.Level.WARNING);

        }

        workbook.close();

        if (verbose)
            MIA.log.writeStatus("Saved results");

    }

    private static String appendDateTime(String inputName) {
        String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        return inputName + "_(" + dateTime + ").xlsx";
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

        row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue("OS VERSION");
        row.createCell(1).setCellValue(SystemUtils.OS_VERSION);

        row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue("DATE AND TIME COMPLETED");
        row.createCell(1).setCellValue(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));

        return rowIdx;

    }

    private void prepareConfiguration(SXSSFWorkbook workbook, Analysis analysis) {
        // Creating a sheet for parameters
        Sheet paramSheet = workbook.createSheet("Configuration");

        int rowIdx = 0;

        // Adding configuration information about the system
        rowIdx = appendConfigurationReport(paramSheet, analysis, rowIdx);

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
        cell.setCellValue("WORKFLOW CONFIGURATION (XML)");
        cell.setCellStyle(cellStyle);

        try {
            Document doc = AnalysisWriter.prepareAnalysisDocument(analysis);
            StringWriter stringWriter = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
            stringWriter.close();

            String xml = stringWriter.getBuffer().toString();
            while (xml.length() > 30000) {
                String part = xml.substring(0, 30000);
                xml = xml.substring(30000);

                row = paramSheet.createRow(rowIdx++);
                cell = row.createCell(0);
                cell.setCellValue(part);
            }
            row = paramSheet.createRow(rowIdx++);
            cell = row.createCell(0);
            cell.setCellValue(xml);

        } catch (ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException
                | IOException e) {
            MIA.log.writeError(e);
        }
    }

    private void prepareLog(SXSSFWorkbook workbook) {
        // Creating a sheet for parameters
        Sheet errorSheet = workbook.createSheet("Log");

        // Getting error write text and split by line returns
        String logText = MIA.getLogHistory().getLogHistory(Level.ERROR);
        StringTokenizer tokenizer = new StringTokenizer(logText, "\n");

        // Adding a header row for the parameter titles
        int rowIdx = 0;

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Adding headings
        Row row = errorSheet.createRow(rowIdx++);
        Cell cell = row.createCell(0);
        cell.setCellValue("LOG");
        cell.setCellStyle(cellStyle);

        rowIdx++;

        while (tokenizer.hasMoreTokens()) {
            row = errorSheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(tokenizer.nextToken());
        }
    }

    private void prepareSummary(SXSSFWorkbook workbook, Workspaces workspaces, Modules modules,
            SummaryMode summaryType) {
        AtomicInteger headerCol = new AtomicInteger(0);

        // Adding header rows for the metadata sheet.
        Sheet summarySheet = workbook.createSheet("Summary");
        Row summaryHeaderRow = summarySheet.createRow(0);

        // Creating a HashMap to store column numbers
        HashMap<String, Integer> colNumbers = new HashMap<>();
        addSummaryMetadataHeaders(summaryHeaderRow, modules, colNumbers, headerCol, summaryType);
        addSummaryGroupHeader(summaryHeaderRow, colNumbers, headerCol, summaryType);
        addSummaryImageHeaders(summaryHeaderRow, modules, colNumbers, headerCol);
        addSummaryObjectHeaders(summaryHeaderRow, modules, colNumbers, headerCol);

        // Running through each Workspace, adding a row
        int summaryRow = 1;
        switch (summaryType) {
            case PER_FILE:
                for (Workspace workspace : workspaces) {
                    if (!workspace.exportWorkspace())
                        continue;
                    Row summaryValueRow = summarySheet.createRow(summaryRow++);
                    populateSummaryRow(summaryValueRow, workspace, modules, colNumbers, null, null);
                }
                break;

            case PER_TIMEPOINT_PER_FILE:
                for (Workspace workspace : workspaces) {
                    if (!workspace.exportWorkspace())
                        continue;
                    // For the current workspace, iterating over all available time points and
                    // creating a new workspace
                    HashMap<Integer, Workspace> currentWorkspaces = workspace.getSingleTimepointWorkspaces();
                    for (Integer timepoint : currentWorkspaces.keySet()) {
                        Workspace currentWorkspace = currentWorkspaces.get(timepoint);
                        Row summaryValueRow = summarySheet.createRow(summaryRow++);
                        populateSummaryRow(summaryValueRow, currentWorkspace, modules, colNumbers, "TIMEPOINT",
                                String.valueOf(timepoint));

                    }
                }
                break;

            case GROUP_BY_METADATA:
                HashMap<String, Workspace> metadataWorkspaces = workspaces
                        .getMetadataWorkspaces(metadataItemForSummary);
                for (String metadataValue : metadataWorkspaces.keySet()) {
                    Workspace currentWorkspace = metadataWorkspaces.get(metadataValue);
                    if (!currentWorkspace.exportWorkspace())
                        continue;
                    Row summaryValueRow = summarySheet.createRow(summaryRow++);
                    populateSummaryRow(summaryValueRow, currentWorkspace, modules, colNumbers, metadataItemForSummary,
                            metadataValue);

                }
                break;
        }
    }

    private void addSummaryMetadataHeaders(Row summaryHeaderRow, Modules modules, HashMap<String, Integer> colNumbers,
            AtomicInteger headerCol, SummaryMode summaryType) {
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
                MetadataRefs metadataRefs = modules.getMetadataRefs(null);

                // Sorting by nickname
                TreeMap<String, MetadataRef> sortedRefs = new TreeMap<>();
                for (MetadataRef ref : metadataRefs.values())
                    sortedRefs.put(ref.getNickname(), ref);

                // Running through all the metadata values, adding them as new columns
                for (MetadataRef ref : sortedRefs.values()) {
                    if (!ref.isExportGlobal())
                        continue;

                    summaryDataName = getMetadataString(ref.getName());
                    cell = summaryHeaderRow.createCell(headerCol.get());
                    cell.setCellValue(summaryDataName);
                    cell.setCellStyle(cellStyle);
                    colNumbers.put(summaryDataName, headerCol.getAndIncrement());
                }
                break;
        }
    }

    private void addSummaryGroupHeader(Row summaryHeaderRow, HashMap<String, Integer> colNumbers,
            AtomicInteger headerCol, SummaryMode summaryType) {
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
                colNumbers.put(timepointDataName, headerCol.getAndIncrement());
                break;
            case GROUP_BY_METADATA:
                String metadataDataName = getMetadataString(metadataItemForSummary);
                cell = summaryHeaderRow.createCell(headerCol.get());
                cell.setCellValue(metadataDataName);
                cell.setCellStyle(cellStyle);
                colNumbers.put(metadataDataName, headerCol.getAndIncrement());
                break;
        }
    }

    private void addSummaryImageHeaders(Row summaryHeaderRow, Modules modules, HashMap<String, Integer> colNumbers,
            AtomicInteger headerCol) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        LinkedHashSet<OutputImageP> availableImages = modules.getAvailableImages(null, true);
        if (availableImages != null) {
            for (OutputImageP availableImage : availableImages) {
                String availableImageName = availableImage.getImageName();

                ImageMeasurementRefs availableMeasurements = modules.getImageMeasurementRefs(availableImageName);

                // Sorting by nickname
                TreeMap<String, ImageMeasurementRef> sortedRefs = new TreeMap<>();
                for (ImageMeasurementRef ref : availableMeasurements.values())
                    sortedRefs.put(ref.getNickname(), ref);

                // Running through all the image measurement values, adding them as new columns
                for (ImageMeasurementRef imageMeasurement : sortedRefs.values()) {
                    if (!imageMeasurement.isExportGlobal())
                        continue;

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

    private void addSummaryObjectHeaders(Row summaryHeaderRow, Modules modules, HashMap<String, Integer> colNumbers,
            AtomicInteger headerCol) {
        Workbook workbook = summaryHeaderRow.getSheet().getWorkbook();

        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Adding object headers
        LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(null, true);
        if (availableObjects != null) {
            for (OutputObjectsP availableObject : availableObjects) {
                String availableObjectName = availableObject.getObjectsName();

                Cell cell;
                String summaryDataName;

                // Adding the number of objects
                if (showObjectCounts) {
                    summaryDataName = getObjectString(availableObjectName, "", "NUMBER");
                    cell = summaryHeaderRow.createCell(headerCol.get());
                    cell.setCellValue(summaryDataName);
                    cell.setCellStyle(cellStyle);
                    addComment(cell, "Num of \"" + availableObjectName + "\" objects.");
                    colNumbers.put(summaryDataName, headerCol.getAndIncrement());
                }

                ObjMeasurementRefs objectMeasurementRefs = modules.getObjectMeasurementRefs(availableObjectName);

                // If the current object hasn't got any assigned measurements, skip it
                if (objectMeasurementRefs == null)
                    continue;

                // Sorting by nickname
                TreeMap<String, ObjMeasurementRef> sortedRefs = new TreeMap<>();
                for (ObjMeasurementRef ref : objectMeasurementRefs.values())
                    sortedRefs.put(ref.getNickname(), ref);

                // Running through all the object measurement values, adding them as new columns
                for (ObjMeasurementRef objectMeasurement : sortedRefs.values()) {
                    if (!objectMeasurement.isExportGlobal())
                        continue;

                    if (objectMeasurement.isExportMean()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow, colNumbers, headerCol, objectMeasurement,
                                "MEAN", "Mean");
                    }

                    if (objectMeasurement.isExportMin()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow, colNumbers, headerCol, objectMeasurement,
                                "MIN", "Minimum");
                    }

                    if (objectMeasurement.isExportMax()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow, colNumbers, headerCol, objectMeasurement,
                                "MAX", "Maximum");
                    }

                    if (objectMeasurement.isExportStd()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow, colNumbers, headerCol, objectMeasurement,
                                "STD", "Standard deviation");
                    }

                    if (objectMeasurement.isExportSum()) {
                        addSummaryObjectStatisticHeader(summaryHeaderRow, colNumbers, headerCol, objectMeasurement,
                                "SUM", "Sum");
                    }
                }
            }
        }
    }

    private void addSummaryObjectStatisticHeader(Row summaryHeaderRow, HashMap<String, Integer> colNumbers,
            AtomicInteger headerCol, ObjMeasurementRef objectMeasurement, String shortName, String longName) {
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
        addSummaryComment(cell, objectMeasurement, longName);
        colNumbers.put(summaryDataName, headerCol.getAndIncrement());
    }

    private void addSummaryComment(Cell cell, ObjMeasurementRef measurement, String calculation) {
        String text = "";
        switch (summaryMode) {
            case PER_FILE:
                text = calculation + " value of the measurement (described below) for all \""
                        + measurement.getObjectsName()
                        + "\" objects in the input file." + "\n\nMeasurement: " + measurement.getDescription();
                break;
            case PER_TIMEPOINT_PER_FILE:
                text = calculation + " value of the measurement (described below) for all \""
                        + measurement.getObjectsName()
                        + "\" objects at the stated timepoint of the input file." + "\n\nMeasurement: "
                        + measurement.getDescription();
                break;
        }

        addComment(cell, text);

    }

    private void populateSummaryRow(Row summaryValueRow, Workspace workspace, Modules modules,
            HashMap<String, Integer> colNumbers, @Nullable String groupTitle, @Nullable String groupValue) {

        // Adding metadata values
        Metadata metadata = workspace.getMetadata();
        for (String name : metadata.keySet()) {
            String headerName = getMetadataString(name);
            if (!colNumbers.containsKey(headerName))
                continue;
            int colNumber = colNumbers.get(headerName);
            summaryValueRow.createCell(colNumber).setCellValue(metadata.getAsString(name));
        }

        if (groupTitle != null) {
            String timepointName = getMetadataString(groupTitle);
            int colNumber = colNumbers.get(timepointName);
            summaryValueRow.createCell(colNumber).setCellValue(groupValue);
        }

        // Adding image measurements
        HashMap<String, Image> images = workspace.getImages();
        for (Image image : images.values()) {
            String imageName = image.getName();

            ImageMeasurementRefs imageMeasurementRefs = modules.getImageMeasurementRefs(imageName);

            // If the current object hasn't got any assigned measurements, skip it
            if (imageMeasurementRefs == null)
                continue;

            // Running through all the object measurement values, adding them as new columns
            for (ImageMeasurementRef imageMeasurement : imageMeasurementRefs.values()) {
                if (!imageMeasurement.isExportGlobal())
                    continue;

                Measurement measurement = image.getMeasurement(imageMeasurement.getName());

                String headerName = getImageString(imageName, imageMeasurement.getNickname());
                if (!colNumbers.containsKey(headerName))
                    continue;
                int colNum = colNumbers.get(headerName);

                Cell summaryCell = summaryValueRow.createCell(colNum);
                if (measurement == null)
                    continue;

                double val = measurement.getValue();
                if (Double.isNaN(val))
                    summaryCell.setCellValue("");
                else
                    summaryCell.setCellValue(val);
            }
        }

        // Adding object measurements
        HashMap<String, Objs> objSets = workspace.getObjects();
        for (Objs objCollection : objSets.values()) {
            String objSetName = objCollection.getName();
            double val;
            String headerName;
            int colNum;
            Cell summaryCell;

            if (showObjectCounts) {
                headerName = getObjectString(objSetName, "", "NUMBER");
                if (!colNumbers.containsKey(headerName))
                    break;
                colNum = colNumbers.get(headerName);
                summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(objCollection.size());
            }

            ObjMeasurementRefs objectMeasurementRefs = modules.getObjectMeasurementRefs(objSetName);

            // If the current object hasn't got any assigned measurements, skip it
            if (objectMeasurementRefs == null)
                continue;

            // Running through all the object measurement values, adding them as new columns
            for (ObjMeasurementRef objectMeasurement : objectMeasurementRefs.values()) {
                if (!objectMeasurement.isExportGlobal())
                    continue;

                // Running through all objects in this set, adding measurements to a CumStat
                // object
                CumStat cs = new CumStat();
                for (Obj obj : objCollection.values()) {
                    Measurement measurement = obj.getMeasurement(objectMeasurement.getName());
                    if (measurement != null)
                        cs.addMeasure(measurement.getValue());
                }

                if (objectMeasurement.isExportMean()) {
                    headerName = getObjectString(objSetName, "MEAN", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName))
                        break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMean();
                    if (Double.isNaN(val))
                        summaryCell.setCellValue("");
                    else
                        summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportMin()) {
                    headerName = getObjectString(objSetName, "MIN", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName))
                        break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMin();
                    if (Double.isNaN(val))
                        summaryCell.setCellValue("");
                    else
                        summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportMax()) {
                    headerName = getObjectString(objSetName, "MAX", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName))
                        break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMax();
                    if (Double.isNaN(val))
                        summaryCell.setCellValue("");
                    else
                        summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportStd()) {
                    headerName = getObjectString(objSetName, "STD", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName))
                        break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getStd();
                    if (Double.isNaN(val))
                        summaryCell.setCellValue("");
                    else
                        summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportSum()) {
                    headerName = getObjectString(objSetName, "SUM", objectMeasurement.getNickname());
                    if (!colNumbers.containsKey(headerName))
                        break;
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getSum();
                    if (Double.isNaN(val))
                        summaryCell.setCellValue("");
                    else
                        summaryCell.setCellValue(val);
                }
            }
        }
    }

    private void prepareObjectsXLS(SXSSFWorkbook workbook, Workspaces workspaces, Modules modules) {
        // Creating bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        // Creating a new sheet for each object. Each analysed file has its own set of
        // rows (one for each object)
        HashMap<String, Sheet> objectSheets = new HashMap<>();
        HashMap<String, Integer> objectRows = new HashMap<>();

        // Creating a LinkedHashMap that links measurement names to column numbers. This
        // keeps the correct
        // measurements in the correct columns
        LinkedHashMap<String, LinkedHashMap<Integer, String>> measurementNames = new LinkedHashMap<>();

        LinkedHashMap<Integer, String> metadataNames = new LinkedHashMap<>();

        // Using the first workspace in the Workspaces to initialise column headers
        LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(null, true);
        if (availableObjects == null)
            return;

        for (OutputObjectsP availableObject : availableObjects) {
            if (!availableObject.isExported())
                return;

            String objectName = availableObject.getObjectsName();

            // Check if this object has any associated measurements; if not, skip it
            if (!modules.objectsExportMeasurements(objectName))
                continue;

            // Creating relevant sheet prefixed with "OBJ"
            objectSheets.put(objectName, workbook.createSheet(objectName));

            objectRows.put(objectName, 1);
            Row objectHeaderRow = objectSheets.get(objectName).createRow(0);

            // Adding headers to each column
            int col = 0;

            Cell cell = objectHeaderRow.createCell(col++);
            cell.setCellValue("OBJECT_ID");
            cell.setCellStyle(cellStyle);
            String text = "ID number for this object.  Unique in the present image, but can be duplicated in other "
                    + "images";
            addComment(cell, text);

            // Running through all the metadata values, adding them as new columns
            MetadataRefs metadataRefs = modules.getMetadataRefs(null);

            // Sorting by nickname
            TreeMap<String, MetadataRef> sortedRefs = new TreeMap<>();
            for (MetadataRef ref : metadataRefs.values())
                sortedRefs.put(ref.getNickname(), ref);

            for (MetadataRef ref : sortedRefs.values()) {
                if (!ref.isExportGlobal())
                    continue;
                if (!ref.isExportIndividual())
                    continue;

                metadataNames.put(col, ref.getName());
                cell = objectHeaderRow.createCell(col++);
                cell.setCellValue(getMetadataString(ref.getName()));
                cell.setCellStyle(cellStyle);
                
            }

            // Running through all the object measurement values, adding them as new columns
            ObjMeasurementRefs objectMeasurementRefs = modules.getObjectMeasurementRefs(objectName);

            // Sorting by nickname
            TreeMap<String, ObjMeasurementRef> sortedObjMeasurementRefs = new TreeMap<>();
            for (ObjMeasurementRef ref : objectMeasurementRefs.values())
                sortedObjMeasurementRefs.put(ref.getNickname(), ref);

            for (ObjMeasurementRef objectMeasurement : sortedObjMeasurementRefs.values()) {
                if (!objectMeasurement.isExportIndividual())
                    continue;
                if (!objectMeasurement.isExportGlobal())
                    continue;

                measurementNames.putIfAbsent(objectName, new LinkedHashMap<>());
                measurementNames.get(objectName).put(col, objectMeasurement.getName());
                cell = objectHeaderRow.createCell(col++);
                addComment(cell, objectMeasurement.getDescription());
                cell.setCellValue(objectMeasurement.getNickname());
                cell.setCellStyle(cellStyle);

            }
        }

        // Running through each Workspace, adding rows
        for (Workspace workspace : workspaces) {
            for (String objectName : workspace.getObjects().keySet()) {
                Objs objects = workspace.getObjects().get(objectName);

                if (!modules.objectsExportMeasurements(objectName))
                    continue;

                if (objects.values().iterator().hasNext()) {
                    for (Obj object : objects.values()) {
                        if (objectSheets.get(objectName) == null || objectRows.get(objectName) == null)
                            continue;

                        Row objectValueRow = objectSheets.get(objectName).createRow(objectRows.get(objectName));
                        objectRows.compute(objectName, (k, v) -> v = v + 1);

                        Cell objectIDValueCell = objectValueRow.createCell(0);
                        objectIDValueCell.setCellValue(object.getID());

                        // Adding metadata (if enabled)
                        Metadata metadata = workspace.getMetadata();
                        for (int column : metadataNames.keySet()) {
                            Cell metaValueCell = objectValueRow.createCell(column);
                            metaValueCell.setCellValue(metadata.getAsString(metadataNames.get(column)));
                        }

                        if (measurementNames.get(objectName) == null)
                            continue;

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
        return metadataName;// .replaceAll(" ", "_");

    }

    private String getImageString(String imageName, String measurementName) {
        // imageName = imageName.replaceAll(" ", "_");

        return imageName + "_(IM) // " + measurementName;// .replaceAll(" ", "_");

    }

    private String getObjectString(String objectName, String mode, String measurementName) {
        // objectName = objectName.replaceAll(" ", "_");

        if (mode.equals("")) {
            return objectName + "_(OBJ) // " + measurementName;// .replaceAll(" ", "_");
        } else {
            return objectName + "_(OBJ_" + mode + ") // " + measurementName;// .replaceAll(" ", "_");
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
        anchor.setCol2(cell.getColumnIndex() + 6);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 10);

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
