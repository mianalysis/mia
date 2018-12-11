// TODO: Get measurements to export from analysis.getModules().getMeasurements().get(String) for each object
// TODO: Export calibration and units to each object

package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.HCMetadata;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class Exporter {
    public static final int XML_EXPORT = 0;
    public static final int XLS_EXPORT = 1;

    public enum ExportMode {
        ALL_TOGETHER, GROUP_BY_METADATA,INDIVIDUAL_FILES;
    }

    public enum SummaryMode {
        PER_FILE, PER_TIMEPOINT_PER_FILE;
    }

    public enum AppendDateTimeMode {
        ALWAYS, IF_FILE_EXISTS, NEVER;
    }

    private int exportFormat = XLS_EXPORT;

    private String exportFilePath;
    private boolean verbose = false;
    private ExportMode exportMode = ExportMode.ALL_TOGETHER;
    private String metadataItemForGrouping = null;
    private boolean exportSummary = true;
    private boolean showObjectCounts = true;
    private boolean showChildCounts = true;
    private boolean calculateCountMean = true;
    private boolean calculateCountMin = true;
    private boolean calculateCountMax = true;
    private boolean calculateCountStd = true;
    private boolean calculateCountSum = true;
    private SummaryMode summaryMode = SummaryMode.PER_FILE;
    private boolean exportIndividualObjects = true;
    private boolean addMetadataToObjects = true;
    private AppendDateTimeMode appendDateTimeMode = AppendDateTimeMode.NEVER;


    // CONSTRUCTOR

    public Exporter(String exportFilePath, int exportFormat) {
        this.exportFilePath = exportFilePath;
        this.exportFormat = exportFormat;

    }


    // PUBLIC METHODS

    public void exportResults(WorkspaceCollection workspaces, Analysis analysis) throws IOException {
        if (exportFormat == XML_EXPORT) {
            exportXML(workspaces,analysis);

        } else if (exportFormat == XLS_EXPORT) {
            exportXLS(workspaces,analysis);

        }
    }

    public void exportResults(Workspace workspace, Analysis analysis) throws IOException {
        exportXLS(workspace,analysis);

    }

    private void exportXML(WorkspaceCollection workspaces, Analysis analysis) {
        // Initialising DecimalFormat
        DecimalFormat df = new DecimalFormat("0.000E0");

        // Getting modules
        ModuleCollection modules = analysis.getModules();

        try {
            // Initialising the document
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("MIA");
            doc.appendChild(root);

            // Getting parameters as Element and adding to the main file
            Element parametersElement = prepareModulesXML(doc,modules);
            root.appendChild(parametersElement);

            // Running through each workspace (each corresponds to a file) adding file information
            for (Workspace workspace:workspaces) {
                Element setElement =  doc.createElement("SET");

                // Adding metadata from the workspace
                HCMetadata metadata = workspace.getMetadata();
                for (String key:metadata.keySet()) {
                    Attr attr = doc.createAttribute(key);
                    attr.appendChild(doc.createTextNode(metadata.getAsString(key)));
                    setElement.setAttributeNode(attr);

                }

                // Creating new elements for each image in the current workspace with at least one measurement
                for (String imageName:workspace.getImages().keySet()) {
                    Image<?> image = workspace.getImages().get(imageName);

                    if (image.getMeasurements() != null) {
                        Element imageElement = doc.createElement("IMAGE");

                        Attr nameAttr = doc.createAttribute("NAME");
                        nameAttr.appendChild(doc.createTextNode(String.valueOf(imageName)));
                        imageElement.setAttributeNode(nameAttr);

                        for (Measurement measurement : image.getMeasurements().values()) {
                            String attrName = measurement.getName();//.replaceAll(" ", "_");
                            Attr measAttr = doc.createAttribute(attrName);
                            String attrValue = df.format(measurement.getValue());
                            measAttr.appendChild(doc.createTextNode(attrValue));
                            imageElement.setAttributeNode(measAttr);
                        }

                        setElement.appendChild(imageElement);

                    }
                }

                // Creating new elements for each object in the current workspace
                for (String objectNames:workspace.getObjects().keySet()) {
                    for (Obj object:workspace.getObjects().get(objectNames).values()) {
                        Element objectElement =  doc.createElement("OBJECT");

                        // Setting the ID number
                        Attr idAttr = doc.createAttribute("ID");
                        idAttr.appendChild(doc.createTextNode(String.valueOf(object.getID())));
                        objectElement.setAttributeNode(idAttr);

                        Attr nameAttr = doc.createAttribute("NAME");
                        nameAttr.appendChild(doc.createTextNode(String.valueOf(objectNames)));
                        objectElement.setAttributeNode(nameAttr);

                        Attr positionAttr = doc.createAttribute("TIMEPOINT");
                        positionAttr.appendChild(doc.createTextNode(String.valueOf(object.getT())));
                        objectElement.setAttributeNode(positionAttr);

                        for (Measurement measurement:object.getMeasurements().values()) {
                            Element measElement = doc.createElement("MEAS");

                            String name = measurement.getName();//.replaceAll(" ", "_");
                            measElement.setAttribute("NAME",name);

                            String value = df.format(measurement.getValue());
                            measElement.setAttribute("VALUE",value);

                            // Adding the measurement as a child of that object
                            objectElement.appendChild(measElement);

                        }

                        setElement.appendChild(objectElement);

                    }
                }

                root.appendChild(setElement);

            }

            // Preparing the filepath and filename
            String outPath = exportFilePath  +".xml";

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outPath);

            transformer.transform(source, result);

            if (verbose) System.out.println("Saved "+ outPath);


        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public static Element prepareModulesXML(Document doc, ModuleCollection modules) {
        Element modulesElement =  doc.createElement("MODULES");

        // Running through each parameter set (one for each module
        for (Module module:modules) {
            Element moduleElement =  doc.createElement("MODULE");
            Attr nameAttr = doc.createAttribute("NAME");
            nameAttr.appendChild(doc.createTextNode(module.getClass().getName()));
            moduleElement.setAttributeNode(nameAttr);

            Attr nicknameAttr = doc.createAttribute("NICKNAME");
            nicknameAttr.appendChild(doc.createTextNode(module.getNickname()));
            moduleElement.setAttributeNode(nicknameAttr);

            Attr enabledAttr = doc.createAttribute("ENABLED");
            enabledAttr.appendChild(doc.createTextNode(String.valueOf(module.isEnabled())));
            moduleElement.setAttributeNode(enabledAttr);

            Attr disableableAttr = doc.createAttribute("DISABLEABLE");
            disableableAttr.appendChild(doc.createTextNode(String.valueOf(module.canBeDisabled())));
            moduleElement.setAttributeNode(disableableAttr);

            Attr outputAttr = doc.createAttribute("SHOW_OUTPUT");
            outputAttr.appendChild(doc.createTextNode(String.valueOf(module.canShowOutput())));
            moduleElement.setAttributeNode(outputAttr);

            Attr notesAttr = doc.createAttribute("NOTES");
            notesAttr.appendChild(doc.createTextNode(module.getNotes()));
            moduleElement.setAttributeNode(notesAttr);

            Element parametersElement = prepareParametersXML(doc,module);
            moduleElement.appendChild(parametersElement);

            // Adding references from this module
            Element measurementsElement = doc.createElement("MEASUREMENTS");

            MeasurementReferenceCollection imageReferences = module.updateAndGetImageMeasurementReferences();
            prepareMeasurementReferencesXML(doc, measurementsElement,imageReferences,"IMAGE");

            MeasurementReferenceCollection objectReferences = module.updateAndGetObjectMeasurementReferences();
            prepareMeasurementReferencesXML(doc, measurementsElement,objectReferences,"OBJECTS");

            moduleElement.appendChild(measurementsElement);

            // Adding current module to modules
            modulesElement.appendChild(moduleElement);

        }

        return modulesElement;

    }

    public static Element prepareParametersXML(Document doc, Module module) {
        // Adding parameters from this module
        Element parametersElement = doc.createElement("PARAMETERS");

        LinkedHashMap<String,Parameter> parameters = module.getAllParameters();
        for (Parameter currParam:parameters.values()) {
            // Adding the name and value of the current parameter
            Element parameterElement =  doc.createElement("PARAMETER");

            Attr nameAttr = doc.createAttribute("NAME");
            nameAttr.appendChild(doc.createTextNode(currParam.getName()));
            parameterElement.setAttributeNode(nameAttr);

            Attr valueAttr = doc.createAttribute("VALUE");
            if (currParam.getValue() == null) {
                valueAttr.appendChild(doc.createTextNode(""));
            } else {
                valueAttr.appendChild(doc.createTextNode(currParam.getValue().toString()));
            }
            parameterElement.setAttributeNode(valueAttr);

            Attr visibleAttr = doc.createAttribute("VISIBLE");
            visibleAttr.appendChild(doc.createTextNode(Boolean.toString(currParam.isVisible())));
            parameterElement.setAttributeNode(visibleAttr);

            if (currParam.getType() == Parameter.CHILD_OBJECTS | currParam.getType() == Parameter.PARENT_OBJECTS) {
                if (currParam.getValueSource() != null) {
                    Attr valueSourceAttr = doc.createAttribute("VALUESOURCE");
                    valueSourceAttr.appendChild(doc.createTextNode(currParam.getValueSource().toString()));
                    parameterElement.setAttributeNode(valueSourceAttr);
                }
            }

            parametersElement.appendChild(parameterElement);

        }

        return parametersElement;

    }

    public static Element prepareMeasurementReferencesXML(Document doc, Element measurementReferencesElement, MeasurementReferenceCollection measurementReferences, String type) {
        if (measurementReferences == null) return measurementReferencesElement;

        for (MeasurementReference measurementReference:measurementReferences.values()) {
            // Don't export any measurements that aren't calculated
            if (!measurementReference.isCalculated()) continue;

            Element measurementReferenceElement = doc.createElement("MEASUREMENT");

            measurementReferenceElement.setAttribute("NAME",measurementReference.getName());
            measurementReferenceElement.setAttribute("NICKNAME",measurementReference.getNickname());
            measurementReferenceElement.setAttribute("EXPORT_GLOBAL",String.valueOf(measurementReference.isExportGlobal()));
            measurementReferenceElement.setAttribute("EXPORT_INDIVIDUAL",String.valueOf(measurementReference.isExportIndividual()));
            measurementReferenceElement.setAttribute("EXPORT_MEAN",String.valueOf(measurementReference.isExportMean()));
            measurementReferenceElement.setAttribute("EXPORT_MIN",String.valueOf(measurementReference.isExportMin()));
            measurementReferenceElement.setAttribute("EXPORT_MAX",String.valueOf(measurementReference.isExportMax()));
            measurementReferenceElement.setAttribute("EXPORT_SUM",String.valueOf(measurementReference.isExportSum()));
            measurementReferenceElement.setAttribute("EXPORT_STD",String.valueOf(measurementReference.isExportStd()));
            measurementReferenceElement.setAttribute("TYPE",type);
            measurementReferenceElement.setAttribute("IMAGE_OBJECT_NAME",measurementReference.getImageObjName());

            measurementReferencesElement.appendChild(measurementReferenceElement);

        }

        return measurementReferencesElement;

    }

    private void exportXLS(WorkspaceCollection workspaces, Analysis analysis) throws IOException {
        switch (exportMode) {
            case ALL_TOGETHER:
                exportXLS(workspaces,analysis,exportFilePath);
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

                    exportXLS(currentWorkspaces,analysis,name);

                }

                break;

        }
    }

    private void exportXLS(Workspace workspace, Analysis analysis) throws IOException {
        WorkspaceCollection currentWorkspaces = new WorkspaceCollection();
        currentWorkspaces.add(workspace);

        HCMetadata metadata = workspace.getMetadata();
        String name = FilenameUtils.removeExtension(metadata.getFile().getAbsolutePath());
        name = name +"_S"+metadata.getSeriesNumber();

        exportXLS(currentWorkspaces,analysis,name);
    }


    private void exportXLS(WorkspaceCollection workspaces, Analysis analysis, String name) throws IOException {
        // Getting modules
        ModuleCollection modules = analysis.getModules();

        // Initialising the workbook
//        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Workbook workbook = new HSSFWorkbook();

        // Adding relevant sheets
        prepareParametersXLS(workbook,modules);
        prepareErrorLogXLS(workbook);
        if (exportSummary) prepareSummaryXLS(workbook,workspaces,modules, summaryMode);
        if (exportIndividualObjects) prepareObjectsXLS(workbook,workspaces,modules);

        // Writing the workbook to file
        String outPath = name + ".xls";
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

            System.err.println("Target file ("+new File(outPath).getName()+") inaccessible");
            System.err.println("Saved to alternative file ("+new File(newOutPath).getName()+")");

        }

        workbook.close();

        if (verbose) System.out.println("Saved results");

    }

    private static String appendDateTime(String inputName) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        return inputName + "_("+ dateTime + ").xls";
    }

    private void prepareParametersXLS(Workbook workbook, ModuleCollection modules) {
        // Creating a sheet for parameters
        Sheet paramSheet = workbook.createSheet("Parameters");

        // Adding a header row for the parameter titles
        int paramRow = 0;
        int paramCol = 0;
        Row parameterHeader = paramSheet.createRow(paramRow++);

        Cell nameHeaderCell = parameterHeader.createCell(paramCol++);
        nameHeaderCell.setCellValue("PARAMETER");

        Cell valueHeaderCell = parameterHeader.createCell(paramCol++);
        valueHeaderCell.setCellValue("VALUE");

        Cell moduleHeaderCell = parameterHeader.createCell(paramCol);
        moduleHeaderCell.setCellValue("MODULE");

        // Adding information about the system used
        paramRow++;
        paramCol = 0;
        Row row = paramSheet.createRow(paramRow++);

        Cell nameValueCell = row.createCell(paramCol++);
        nameValueCell.setCellValue("MIA_VERSION");

        Cell valueValueCell = row.createCell(paramCol++);
        valueValueCell.setCellValue(MIA.getVersion());

        Cell moduleValueCell = row.createCell(paramCol);
        moduleValueCell.setCellValue("");

        // Adding a new parameter to each row
        for (Module module:modules) {
            LinkedHashMap<String,Parameter> parameters = module.updateAndGetParameters();

            paramRow++;

            for (Parameter currParam : parameters.values()) {
                paramCol = 0;
                row = paramSheet.createRow(paramRow++);

                nameValueCell = row.createCell(paramCol++);
                nameValueCell.setCellValue(currParam.getName());

                valueValueCell = row.createCell(paramCol++);
                valueValueCell.setCellValue(currParam.getValue().toString());

                moduleValueCell = row.createCell(paramCol);
                moduleValueCell.setCellValue(module.getClass().getSimpleName());

            }
        }
    }

    private void prepareErrorLogXLS(Workbook workbook) {
        // Creating a sheet for parameters
        Sheet errorSheet = workbook.createSheet("Log");

        // Getting error log text and split by line returns
        String logText = MIA.getErrorLog().getStreamContents();
        StringTokenizer tokenizer = new StringTokenizer(logText,"\n");

        // Adding a header row for the parameter titles
        int rowCount = 0;
        while (tokenizer.hasMoreTokens()) {
            Row row = errorSheet.createRow(rowCount++);
            Cell cell = row.createCell(0);
            cell.setCellValue(tokenizer.nextToken());
        }
    }

    private void prepareSummaryXLS(Workbook workbook, WorkspaceCollection workspaces, ModuleCollection modules,
                                   SummaryMode summaryType) {
        int headerCol = 0;

        // Adding header rows for the metadata sheet.
        Sheet summarySheet = workbook.createSheet("Summary");
        Row summaryHeaderRow = summarySheet.createRow(0);

        // Creating a HashMap to store column numbers
        HashMap<String,Integer> colNumbers = new HashMap<>();

        // Adding metadata headers
        String[] metadataNames = modules.getMetadataReferences(null).getMetadataNames();
        // Running through all the metadata values, adding them as new columns
        for (String name : metadataNames) {
            Cell summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
            String summaryDataName = getMetadataString(name);
            summaryHeaderCell.setCellValue(summaryDataName);
            colNumbers.put(summaryDataName,headerCol++);

        }

        // Add a column to record the timepoint
        if (summaryType == SummaryMode.PER_TIMEPOINT_PER_FILE) {
            Cell timepointHeaderCell = summaryHeaderRow.createCell(headerCol);
            String timepointDataName = getMetadataString("TIMEPOINT");
            timepointHeaderCell.setCellValue(timepointDataName);
            colNumbers.put(timepointDataName,headerCol++);
        }

        // Adding image headers
        LinkedHashSet<Parameter> availableImages = modules.getAvailableImages(null,true);
        if (availableImages != null) {
            for (Parameter availableImage : availableImages) {
                String availableImageName = availableImage.getValue();

                MeasurementReferenceCollection availableMeasurements = modules.getImageMeasurementReferences(availableImageName);

                // Running through all the image measurement values, adding them as new columns
                for (MeasurementReference imageMeasurement:availableMeasurements.values()) {
                    if (!imageMeasurement.isCalculated()) continue;
                    if (!imageMeasurement.isExportIndividual()) continue;
                    if (!imageMeasurement.isExportGlobal()) continue;

                    String measurementName = imageMeasurement.getNickname();
                    Cell summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    String summaryDataName = getImageString(availableImageName, measurementName);
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                }
            }
        }

        // Adding object headers
        LinkedHashSet<Parameter> availableObjects = modules.getAvailableObjects(null,true);
        if (availableObjects != null) {
            for (Parameter availableObject:availableObjects) {
                String availableObjectName = availableObject.getValue();

                Cell summaryHeaderCell; String summaryDataName;

                // Adding the number of objects
                if (showObjectCounts) {
                    summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    summaryDataName = getObjectString(availableObjectName, "", "NUMBER");
                    summaryHeaderCell.setCellValue(summaryDataName);
                    addComment(summaryHeaderCell,"Number of \""+availableObjectName+"\" objects.");
                    colNumbers.put(summaryDataName, headerCol++);
                }

                // Running through all the object's children
                if (showChildCounts && modules.getRelationships().getChildNames(availableObjectName,false) != null) {
                    for (String child : modules.getRelationships().getChildNames(availableObjectName,false)) {
                        if (calculateCountMean) {
                            summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                            summaryDataName = getObjectString(availableObjectName, "MEAN", "NUM_CHILDREN_" + child);
                            summaryHeaderCell.setCellValue(summaryDataName);
                            addNumberOfChildrenComment(summaryHeaderCell,availableObjectName,child,"Mean");
                            colNumbers.put(summaryDataName, headerCol++);
                        }

                        if (calculateCountMin) {
                            summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                            summaryDataName = getObjectString(availableObjectName, "MIN", "NUM_CHILDREN_" + child);
                            summaryHeaderCell.setCellValue(summaryDataName);
                            addNumberOfChildrenComment(summaryHeaderCell,availableObjectName,child,"Minimum");
                            colNumbers.put(summaryDataName, headerCol++);
                        }

                        if (calculateCountMax) {
                            summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                            summaryDataName = getObjectString(availableObjectName, "MAX", "NUM_CHILDREN_" + child);
                            summaryHeaderCell.setCellValue(summaryDataName);
                            addNumberOfChildrenComment(summaryHeaderCell,availableObjectName,child,"Maximum");
                            colNumbers.put(summaryDataName, headerCol++);
                        }

                        if (calculateCountStd) {
                            summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                            summaryDataName = getObjectString(availableObjectName, "STD", "NUM_CHILDREN_" + child);
                            summaryHeaderCell.setCellValue(summaryDataName);
                            addNumberOfChildrenComment(summaryHeaderCell,availableObjectName,child,"Standard deviation");
                            colNumbers.put(summaryDataName, headerCol++);
                        }

                        if (calculateCountSum) {
                            summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                            summaryDataName = getObjectString(availableObjectName, "SUM", "NUM_CHILDREN_" + child);
                            summaryHeaderCell.setCellValue(summaryDataName);
                            addNumberOfChildrenComment(summaryHeaderCell,availableObjectName,child,"Sum");
                            colNumbers.put(summaryDataName, headerCol++);
                        }
                    }
                }

                MeasurementReferenceCollection objectMeasurementReferences = modules.getObjectMeasurementReferences(availableObjectName);

                // If the current object hasn't got any assigned measurements, skip it
                if (objectMeasurementReferences == null) continue;

                // Running through all the object measurement values, adding them as new columns
                for (MeasurementReference objectMeasurement : objectMeasurementReferences.values()) {
                    if (!objectMeasurement.isCalculated()) continue;
                    if (!objectMeasurement.isExportIndividual()) continue;
                    if (!objectMeasurement.isExportGlobal()) continue;

                    if (objectMeasurement.isExportMean()) {
                        summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                        summaryDataName = getObjectString(availableObjectName, "MEAN", objectMeasurement.getNickname());
                        summaryHeaderCell.setCellValue(summaryDataName);
                        addSummaryComment(summaryHeaderCell,objectMeasurement,"Mean");
                        colNumbers.put(summaryDataName, headerCol++);
                    }

                    if (objectMeasurement.isExportMin()) {
                        summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                        summaryDataName = getObjectString(availableObjectName, "MIN", objectMeasurement.getNickname());
                        summaryHeaderCell.setCellValue(summaryDataName);
                        addSummaryComment(summaryHeaderCell,objectMeasurement,"Minimum");
                        colNumbers.put(summaryDataName, headerCol++);
                    }

                    if (objectMeasurement.isExportMax()) {
                        summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                        summaryDataName = getObjectString(availableObjectName, "MAX", objectMeasurement.getNickname());
                        summaryHeaderCell.setCellValue(summaryDataName);
                        addSummaryComment(summaryHeaderCell,objectMeasurement,"Maximum");
                        colNumbers.put(summaryDataName, headerCol++);
                    }

                    if (objectMeasurement.isExportStd()) {
                        summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                        summaryDataName = getObjectString(availableObjectName, "STD", objectMeasurement.getNickname());
                        summaryHeaderCell.setCellValue(summaryDataName);
                        addSummaryComment(summaryHeaderCell,objectMeasurement,"Standard deviation");
                        colNumbers.put(summaryDataName, headerCol++);
                    }

                    if (objectMeasurement.isExportSum()) {
                        summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                        summaryDataName = getObjectString(availableObjectName, "SUM", objectMeasurement.getNickname());
                        summaryHeaderCell.setCellValue(summaryDataName);
                        addSummaryComment(summaryHeaderCell,objectMeasurement,"Sum");
                        colNumbers.put(summaryDataName, headerCol++);
                    }
                }
            }
        }

        // Running through each Workspace, adding a row
        int summaryRow = 1;
        for (Workspace workspace:workspaces) {
            switch (summaryType) {
                case PER_FILE:
                    Row summaryValueRow = summarySheet.createRow(summaryRow++);
                    populateSummaryRow(summaryValueRow, workspace, modules, colNumbers, -Integer.MAX_VALUE);

                    break;

                case PER_TIMEPOINT_PER_FILE:
                    // For the current workspace, iterating over all available time points and creating a new workspace
                    HashMap<Integer,Workspace> currentWorkspaces = workspace.getSingleTimepointWorkspaces();
                    for (Integer timepoint:currentWorkspaces.keySet()) {
                        Workspace currentWorkspace = currentWorkspaces.get(timepoint);
                        summaryValueRow = summarySheet.createRow(summaryRow++);
                        populateSummaryRow(summaryValueRow, currentWorkspace, modules, colNumbers, timepoint);

                    }
                    break;

            }
        }
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

    private void addSummaryComment(Cell cell, MeasurementReference measurement, String calculation) {
        String text = "";
        switch (summaryMode) {
            case PER_FILE:
                text = calculation+" value of the measurement (described below) for all \""
                        +measurement.getImageObjName()+"\" objects in the input file." +
                        "\n\nMeasurement: "+measurement.getDescription();
                break;
            case PER_TIMEPOINT_PER_FILE:
                text = calculation+" value of the measurement (described below) for all \""
                        +measurement.getImageObjName()+"\" objects at the stated timepoint of the input file." +
                        "\n\nMeasurement: "+measurement.getDescription();
                break;
        }

        addComment(cell,text);

    }

    private void populateSummaryRow(Row summaryValueRow, Workspace workspace, ModuleCollection modules,
                                    HashMap<String,Integer> colNumbers, int timepoint) {
        // Adding metadata values
        HCMetadata metadata = workspace.getMetadata();
        for (String name : metadata.keySet()) {
            String headerName = getMetadataString(name);
            if (!colNumbers.containsKey(headerName)) continue;
            int colNumber = colNumbers.get(headerName);
            Cell metaValueCell = summaryValueRow.createCell(colNumber);
            metaValueCell.setCellValue(metadata.getAsString(name));
        }

        if (timepoint != -Integer.MAX_VALUE) {
            String timepointName = getMetadataString("TIMEPOINT");
            int colNumber = colNumbers.get(timepointName);
            Cell timepointValueCell = summaryValueRow.createCell(colNumber);
            timepointValueCell.setCellValue(String.valueOf(timepoint));
        }

        // Adding image measurements
        HashMap<String,Image> images = workspace.getImages();
        for (Image image:images.values()) {
            String imageName = image.getName();

            MeasurementReferenceCollection imageMeasurementReferences = modules.getImageMeasurementReferences(imageName);

            // If the current object hasn't got any assigned measurements, skip it
            if (imageMeasurementReferences == null) continue;

            // Running through all the object measurement values, adding them as new columns
            for (MeasurementReference imageMeasurement : imageMeasurementReferences.values()) {
                if (!imageMeasurement.isCalculated()) continue;
                if (!imageMeasurement.isExportIndividual()) continue;
                if (!imageMeasurement.isExportGlobal()) continue;

                Measurement measurement = image.getMeasurement(imageMeasurement.getName());

                String headerName = getImageString(imageName,imageMeasurement.getNickname());
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
                colNum = colNumbers.get(headerName);
                summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(objCollection.size());
            }

            // Running through all the object's children
            if (showChildCounts && modules.getRelationships().getChildNames(objSetName,false) != null) {
                for (String child : modules.getRelationships().getChildNames(objSetName,false)) {
                    // Running through all objects in this set, adding children to a CumStat object
                    CumStat cs = new CumStat();
                    for (Obj obj : objCollection.values()) {
                        ObjCollection children = obj.getChildren(child);
                        if (children != null) cs.addMeasure(children.size());
                    }

                    if (calculateCountMean) {
                        headerName = getObjectString(objSetName, "MEAN", "NUM_CHILDREN_" + child);
                        colNum = colNumbers.get(headerName);
                        summaryCell = summaryValueRow.createCell(colNum);
                        val = cs.getMean();
                        if (Double.isNaN(val)) summaryCell.setCellValue("");
                        else summaryCell.setCellValue(val);
                    }

                    if (calculateCountMin) {
                        headerName = getObjectString(objSetName, "MIN", "NUM_CHILDREN_" + child);
                        colNum = colNumbers.get(headerName);
                        summaryCell = summaryValueRow.createCell(colNum);
                        val = cs.getMin();
                        if (Double.isNaN(val)) summaryCell.setCellValue("");
                        else summaryCell.setCellValue(val);
                    }

                    if (calculateCountMax) {
                        headerName = getObjectString(objSetName, "MAX", "NUM_CHILDREN_" + child);
                        colNum = colNumbers.get(headerName);
                        summaryCell = summaryValueRow.createCell(colNum);
                        val = cs.getMax();
                        if (Double.isNaN(val)) summaryCell.setCellValue("");
                        else summaryCell.setCellValue(val);
                    }

                    if (calculateCountStd) {
                        headerName = getObjectString(objSetName, "STD", "NUM_CHILDREN_" + child);
                        colNum = colNumbers.get(headerName);
                        summaryCell = summaryValueRow.createCell(colNum);
                        val = cs.getStd();
                        if (Double.isNaN(val)) summaryCell.setCellValue("");
                        else summaryCell.setCellValue(val);
                    }

                    if (calculateCountSum) {
                        headerName = getObjectString(objSetName, "SUM", "NUM_CHILDREN_" + child);
                        colNum = colNumbers.get(headerName);
                        summaryCell = summaryValueRow.createCell(colNum);
                        val = cs.getSum();
                        if (Double.isNaN(val)) summaryCell.setCellValue("");
                        else summaryCell.setCellValue(val);
                    }
                }
            }

            MeasurementReferenceCollection objectMeasurementReferences = modules.getObjectMeasurementReferences(objSetName);

            // If the current object hasn't got any assigned measurements, skip it
            if (objectMeasurementReferences == null) continue;

            // Running through all the object measurement values, adding them as new columns
            for (MeasurementReference objectMeasurement : objectMeasurementReferences.values()) {
                if (!objectMeasurement.isCalculated()) continue;
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
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMean();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportMin()) {
                    headerName = getObjectString(objSetName, "MIN", objectMeasurement.getNickname());
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMin();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportMax()) {
                    headerName = getObjectString(objSetName, "MAX", objectMeasurement.getNickname());
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getMax();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportStd()) {
                    headerName = getObjectString(objSetName, "STD", objectMeasurement.getNickname());
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getStd();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }

                if (objectMeasurement.isExportSum()) {
                    headerName = getObjectString(objSetName, "SUM", objectMeasurement.getNickname());
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    val = cs.getSum();
                    if (Double.isNaN(val)) summaryCell.setCellValue("");
                    else summaryCell.setCellValue(val);
                }
            }
        }
    }

    private void prepareObjectsXLS(Workbook workbook, WorkspaceCollection workspaces, ModuleCollection modules) {

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

        // Metadata names should be the same for all objects
        String[] metadataNames = null;

        // Using the first workspace in the WorkspaceCollection to initialise column headers
        LinkedHashSet<Parameter> availableObjects = modules.getAvailableObjects(null,true);
        if (availableObjects == null) return;

        for (Parameter availableObject:availableObjects) {
            String objectName = availableObject.getValue();
            // Creating relevant sheet prefixed with "OBJ"
            objectSheets.put(objectName, workbook.createSheet("OBJ_" + objectName));

            objectRows.put(objectName, 1);
            Row objectHeaderRow = objectSheets.get(objectName).createRow(0);

            // Adding headers to each column
            int col = 0;

            Cell objectIDHeaderCell = objectHeaderRow.createCell(col++);
            objectIDHeaderCell.setCellValue("OBJECT_ID");
            String text = "ID number for this object.  Unique in the present image, but can be duplicated in other " +
                    "images";
            addComment(objectIDHeaderCell,text);

            // Adding metadata headers (if enabled)
            if (addMetadataToObjects) {
                // Running through all the metadata values, adding them as new columns
                metadataNames = modules.getMetadataReferences(null).getMetadataNames();
                for (String name : metadataNames) {
                    Cell metaHeaderCell = objectHeaderRow.createCell(col++);
                    metaHeaderCell.setCellValue(getMetadataString(name));
                }
            }

            // Adding parent IDs
            RelationshipCollection relationships = modules.getRelationships();
            String[] parents = relationships.getParentNames(objectName,false);
            if (parents != null) {
                for (String parent : parents) {
                    parentNames.putIfAbsent(objectName, new LinkedHashMap<>());
                    parentNames.get(objectName).put(col, parent);
                    Cell parentHeaderCell = objectHeaderRow.createCell(col++);
                    parentHeaderCell.setCellValue("PARENT_" + parent + "_ID");

                }
            }

            // Adding number of children for each child type
            String[] children = relationships.getChildNames(objectName,false);
            if (children != null) {
                for (String child : children) {
                    childNames.putIfAbsent(objectName, new LinkedHashMap<>());
                    childNames.get(objectName).put(col, child);
                    Cell childHeaderCell = objectHeaderRow.createCell(col++);
                    childHeaderCell.setCellValue("NUMBER_OF_" + child + "_CHILDREN");

                }
            }

            // Adding timepoint header
            Cell timepointHeaderCell = objectHeaderRow.createCell(col++);
            timepointHeaderCell.setCellValue("TIMEPOINT");

            MeasurementReferenceCollection objectMeasurementReferences = modules.getObjectMeasurementReferences(objectName);

            // If the current object hasn't got any assigned measurements, skip it
            if (objectMeasurementReferences == null) continue;

            // Running through all the object measurement values, adding them as new columns
            for (MeasurementReference objectMeasurement : objectMeasurementReferences.values()) {
                if (!objectMeasurement.isCalculated()) continue;
                if (!objectMeasurement.isExportIndividual()) continue;
                if (!objectMeasurement.isExportGlobal()) continue;

                measurementNames.putIfAbsent(objectName, new LinkedHashMap<>());
                measurementNames.get(objectName).put(col, objectMeasurement.getName());
                Cell measHeaderCell = objectHeaderRow.createCell(col++);
                addComment(measHeaderCell,objectMeasurement.getDescription());
                measHeaderCell.setCellValue(objectMeasurement.getNickname());

            }
        }

        // Running through each Workspace, adding rows
        for (Workspace workspace : workspaces) {
            for (String objectName : workspace.getObjects().keySet()) {
                ObjCollection objects = workspace.getObjects().get(objectName);

                if (objects.values().iterator().hasNext()) {
                    for (Obj object : objects.values()) {
                        // Adding the measurements from this image
                        int col = 0;

                        Row objectValueRow = objectSheets.get(objectName).createRow(objectRows.get(objectName));
                        objectRows.compute(objectName, (k, v) -> v = v + 1);

                        Cell objectIDValueCell = objectValueRow.createCell(col++);
                        objectIDValueCell.setCellValue(object.getID());

                        // Adding metadata (if enabled)
                        if (addMetadataToObjects && metadataNames != null) {
                            HCMetadata metadata = workspace.getMetadata();
                            for (String name : metadataNames) {
                                Cell metaValueCell = objectValueRow.createCell(col++);
                                metaValueCell.setCellValue(metadata.getAsString(name));
                            }
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

                        Cell timepointValueCell = objectValueRow.createCell(col++);
                        timepointValueCell.setCellValue(object.getT());

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

    private void exportJSON(WorkspaceCollection workspaces, Analysis analysis) {
        System.out.println("[WARN] No JSON export currently implemented.  File not saved.");

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

    public boolean isShowChildCounts() {
        return showChildCounts;
    }

    public void setShowChildCounts(boolean showChildCounts) {
        this.showChildCounts = showChildCounts;
    }

    public boolean isCalculateCountMean() {
        return calculateCountMean;
    }

    public void setCalculateCountMean(boolean calculateCountMean) {
        this.calculateCountMean = calculateCountMean;
    }

    public boolean isCalculateCountMin() {
        return calculateCountMin;
    }

    public void setCalculateCountMin(boolean calculateCountMin) {
        this.calculateCountMin = calculateCountMin;
    }

    public boolean isCalculateCountMax() {
        return calculateCountMax;
    }

    public void setCalculateCountMax(boolean calculateCountMax) {
        this.calculateCountMax = calculateCountMax;
    }

    public boolean isCalculateCountStd() {
        return calculateCountStd;
    }

    public void setCalculateCountStd(boolean calculateCountStd) {
        this.calculateCountStd = calculateCountStd;
    }

    public boolean isCalculateCountSum() {
        return calculateCountSum;
    }

    public void setCalculateCountSum(boolean calculateCountSum) {
        this.calculateCountSum = calculateCountSum;
    }

}
