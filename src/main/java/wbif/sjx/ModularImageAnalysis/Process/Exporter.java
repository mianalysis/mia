// TODO: Get measurements to export from analysis.getModules().getMeasurements().get(String) for each object
// TODO: Export calibration and units to each object

package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class Exporter {
    public static final int XML_EXPORT = 0;
    public static final int XLSX_EXPORT = 1;
    public static final int JSON_EXPORT = 2;

    public enum SummaryType {
        PER_FILE, PER_TIMEPOINT_PER_FILE;
    }

    private int exportMode = XLSX_EXPORT;

    private String exportFilePath;
    private boolean verbose = false;
    private boolean exportSummary = true;
    private SummaryType summaryType = SummaryType.PER_FILE;
    private boolean exportIndividualObjects = true;
    private boolean addMetadataToObjects = true;


    // CONSTRUCTOR

    public Exporter(String exportFilePath, int exportMode) {
        this.exportFilePath = exportFilePath;
        this.exportMode = exportMode;

    }


    // PUBLIC METHODS

    public void exportResults(WorkspaceCollection workspaces, Analysis analysis) throws IOException {
        if (exportMode == XML_EXPORT) {
            exportXML(workspaces,analysis);

        } else if (exportMode == XLSX_EXPORT) {
            exportXLSX(workspaces,analysis);

        } else if (exportMode == JSON_EXPORT) {
            exportJSON(workspaces,analysis);

        }
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
                    String attrName = key.toUpperCase();
                    Attr attr = doc.createAttribute(attrName);
                    attr.appendChild(doc.createTextNode(metadata.getAsString(key)));
                    setElement.setAttributeNode(attr);

                }

                // Creating new elements for each image in the current workspace with at least one measurement
                for (String imageName:workspace.getImages().keySet()) {
                    Image image = workspace.getImages().get(imageName);

                    if (image.getMeasurements() != null) {
                        Element imageElement = doc.createElement("IMAGE");

                        Attr nameAttr = doc.createAttribute("NAME");
                        nameAttr.appendChild(doc.createTextNode(String.valueOf(imageName)));
                        imageElement.setAttributeNode(nameAttr);

                        for (Measurement measurement : image.getMeasurements().values()) {
                            String attrName = measurement.getName().toUpperCase().replaceAll(" ", "_");
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

                            String name = measurement.getName().toUpperCase().replaceAll(" ", "_");
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
            String outPath = FilenameUtils.removeExtension(exportFilePath) +".xml";

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
        for (HCModule module:modules) {
            Element moduleElement =  doc.createElement("MODULE");
            Attr nameAttr = doc.createAttribute("NAME");
            nameAttr.appendChild(doc.createTextNode(module.getClass().getName()));
            moduleElement.setAttributeNode(nameAttr);

            Attr nicknameAttr = doc.createAttribute("NICKNAME");
            nicknameAttr.appendChild(doc.createTextNode(module.getNickname()));
            moduleElement.setAttributeNode(nicknameAttr);

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

    public static Element prepareParametersXML(Document doc, HCModule module) {
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

//    public static Element prepareMeasurementsXML(Document doc, MeasurementReferenceCollection references, String type) {
//        Element measurementsElement = doc.createElement(type+"S");
//
//        // If there are no references, return the empty element
//        if (references==null) return null;
//        for (MeasurementReference reference :references) {
//            Element referenceElement = doc.createElement(type);
//
//            referenceElement.setAttribute("NAME", reference.getName());
//
//            // Adding measurements to this imageObjReference
//            LinkedHashSet<MeasurementReference> measurementReferences = reference.getMeasurementReferences();
//            Element measurementReferencesElement = prepareMeasurementReferencesXML(doc, measurementReferences);
//            referenceElement.appendChild(measurementReferencesElement);
//
//            measurementsElement.appendChild(referenceElement);
//
//        }
//
//        return measurementsElement;
//
//    }

    public static Element prepareMeasurementReferencesXML(Document doc, Element measurementReferencesElement, MeasurementReferenceCollection measurementReferences, String type) {
        if (measurementReferences == null) return measurementReferencesElement;

        for (MeasurementReference measurementReference:measurementReferences.values()) {
            Element measurementReferenceElement = doc.createElement("MEASUREMENT");

            measurementReferenceElement.setAttribute("NAME",measurementReference.getName());
            measurementReferenceElement.setAttribute("IS_CALCULATED",String.valueOf(measurementReference.isCalculated()));
            measurementReferenceElement.setAttribute("IS_EXPORTABLE",String.valueOf(measurementReference.isExportable()));
            measurementReferenceElement.setAttribute("TYPE",type);
            measurementReferenceElement.setAttribute("IMAGE_OBJECT_NAME",measurementReference.getImageObjName());

            measurementReferencesElement.appendChild(measurementReferenceElement);

        }

        return measurementReferencesElement;

    }

    private void exportXLSX(WorkspaceCollection workspaces, Analysis analysis) throws IOException {
        // Getting modules
        ModuleCollection modules = analysis.getModules();

        // Initialising the workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // Adding relevant sheets
        prepareParametersXLSX(workbook,modules);
        if (exportSummary) prepareSummaryXLSX(workbook,workspaces,modules,summaryType);
        if (exportIndividualObjects) prepareObjectsXLSX(workbook,workspaces,modules);

        // Writing the workbook to file
        String outPath = FilenameUtils.removeExtension(exportFilePath) +".xlsx";
        FileOutputStream outputStream = new FileOutputStream(outPath);
        workbook.write(outputStream);
        workbook.close();

        if (verbose) System.out.println("Saved "+ outPath);

    }

    private void prepareParametersXLSX(SXSSFWorkbook workbook, ModuleCollection modules) {
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

        // Adding a new parameter to each row
        for (HCModule module:modules) {
            LinkedHashMap<String,Parameter> parameters = module.updateAndGetParameters();

            paramRow++;

            for (Parameter currParam : parameters.values()) {
                paramCol = 0;
                Row row = paramSheet.createRow(paramRow++);

                Cell nameValueCell = row.createCell(paramCol++);
                nameValueCell.setCellValue(currParam.getName());

                Cell valueValueCell = row.createCell(paramCol++);
                valueValueCell.setCellValue(currParam.getValue().toString());

                Cell moduleValueCell = row.createCell(paramCol);
                moduleValueCell.setCellValue(module.getClass().getSimpleName());

            }
        }
    }

    private void prepareSummaryXLSX(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules,
                                    SummaryType summaryType) {
        // Basing column names on the first workspace in the WorkspaceCollection
        Workspace exampleWorkspace = workspaces.iterator().next();

        if (exampleWorkspace == null) return;

        int headerCol = 0;

        // Adding header rows for the metadata sheet.
        Sheet summarySheet = workbook.createSheet("Summary");
        Row summaryHeaderRow = summarySheet.createRow(0);

        // Creating a HashMap to store column numbers
        HashMap<String,Integer> colNumbers = new HashMap<>();

        // Adding metadata headers
        HCMetadata exampleMetadata = exampleWorkspace.getMetadata();
        if (exampleMetadata.size() != 0) {
            // Running through all the metadata values, adding them as new columns
            for (String name : exampleMetadata.keySet()) {
                Cell summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                String summaryDataName = getMetadataString(name);
                summaryHeaderCell.setCellValue(summaryDataName);
                colNumbers.put(summaryDataName,headerCol++);

            }
        }

        // Add a column to record the timepoint
        if (summaryType == SummaryType.PER_TIMEPOINT_PER_FILE) {
            Cell timepointHeaderCell = summaryHeaderRow.createCell(headerCol);
            String timepointDataName = getMetadataString("TIMEPOINT");
            timepointHeaderCell.setCellValue(timepointDataName);
            colNumbers.put(timepointDataName,headerCol++);
        }

        // Adding image headers
        HashMap<String,Image> exampleImages = exampleWorkspace.getImages();
        if (exampleImages.size() != 0) {

            for (Image exampleImage : exampleImages.values()) {
                String exampleImageName = exampleImage.getName();

                // Running through all the image measurement values, adding them as new columns
                HashMap<String, Measurement> exampleMeasurements = exampleImage.getMeasurements();
                for (String measurementName : exampleMeasurements.keySet()) {
                    Cell summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    String summaryDataName = getImageString(exampleImageName,measurementName);
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                }
            }
        }

        // Adding object headers
        HashMap<String, ObjCollection> exampleObjSets = exampleWorkspace.getObjects();
        if (exampleObjSets.size() != 0) {

            for (ObjCollection exampleObjCollection : exampleObjSets.values()) {
                String exampleObjSetName = exampleObjCollection.getName();

                // Adding the number of objects
                Cell summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                String summaryDataName = getObjectString(exampleObjSetName,"","NUMBER");
                summaryHeaderCell.setCellValue(summaryDataName);
                colNumbers.put(summaryDataName, headerCol++);

                MeasurementReferenceCollection objectMeasurementReferences = modules.getObjectReferences(exampleObjSetName);

                // If the current object hasn't got any assigned measurements, skip it
                if (objectMeasurementReferences == null) continue;

                // Running through all the object measurement values, adding them as new columns
                for (MeasurementReference objectMeasurement : objectMeasurementReferences.values()) {
                    if (!objectMeasurement.isCalculated()) continue;
                    if (!objectMeasurement.isExportable()) continue;

                    summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    summaryDataName = getObjectString(exampleObjSetName,"MEAN",objectMeasurement.getName());
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                    summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    summaryDataName = getObjectString(exampleObjSetName,"STD",objectMeasurement.getName());
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                    summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    summaryDataName = getObjectString(exampleObjSetName,"SUM",objectMeasurement.getName());
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                }
            }
        }

        // Running through each Workspace, adding a row
        int summaryRow = 1;
        for (Workspace workspace:workspaces) {
            switch (summaryType) {
                case PER_FILE:
                    Row summaryValueRow = summarySheet.createRow(summaryRow++);
                    populateSummaryRow(summaryValueRow, workspace, modules, colNumbers, Integer.MIN_VALUE);

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

    private void populateSummaryRow(Row summaryValueRow, Workspace workspace, ModuleCollection modules,
                                    HashMap<String,Integer> colNumbers, int timepoint) {
        // Adding metadata values
        HCMetadata metadata = workspace.getMetadata();
        for (String name : metadata.keySet()) {
            String headerName = getMetadataString(name);
            int colNumber = colNumbers.get(headerName);
            Cell metaValueCell = summaryValueRow.createCell(colNumber);
            metaValueCell.setCellValue(metadata.getAsString(name));

        }

        if (timepoint != Integer.MIN_VALUE) {
            String timepointName = getMetadataString("TIMEPOINT");
            int colNumber = colNumbers.get(timepointName);
            Cell timepointValueCell = summaryValueRow.createCell(colNumber);
            timepointValueCell.setCellValue(String.valueOf(timepoint));
        }

        // Adding image measurements
        HashMap<String,Image> images = workspace.getImages();
        for (Image image:images.values()) {
            String imageName = image.getName();
            HashMap<String,Measurement> measurements = image.getMeasurements();

            for (String measurementName : measurements.keySet()) {
                String headerName = getImageString(imageName,measurementName);
                int colNum = colNumbers.get(headerName);

                Cell summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(measurements.get(measurementName).getValue());

            }
        }

        // Adding object measurements
        HashMap<String, ObjCollection> objSets = workspace.getObjects();
        for (ObjCollection objCollection :objSets.values()) {
            String objSetName = objCollection.getName();

            String headerName = getObjectString(objSetName,"","NUMBER");
            int colNum = colNumbers.get(headerName);
            Cell summaryCell = summaryValueRow.createCell(colNum);
            summaryCell.setCellValue(objCollection.size());

            MeasurementReferenceCollection objectMeasurementReferences = modules.getObjectReferences(objSetName);

            // If the current object hasn't got any assigned measurements, skip it
            if (objectMeasurementReferences == null) continue;

            // Running through all the object measurement values, adding them as new columns
            for (MeasurementReference objectMeasurement : objectMeasurementReferences.values()) {
                if (!objectMeasurement.isCalculated()) continue;
                if (!objectMeasurement.isExportable()) continue;

                // Running through all objects in this set, adding measurements to a CumStat object
                CumStat cs = new CumStat();
                for (Obj obj: objCollection.values()) {
                    Measurement measurement = obj.getMeasurement(objectMeasurement.getName());
                    if (measurement != null) cs.addMeasure(measurement.getValue());
                }

                headerName = getObjectString(objSetName,"MEAN",objectMeasurement.getName());
                colNum = colNumbers.get(headerName);
                summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(cs.getMean());

                headerName = getObjectString(objSetName,"STD",objectMeasurement.getName());
                colNum = colNumbers.get(headerName);
                summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(cs.getStd());

                headerName = getObjectString(objSetName,"SUM",objectMeasurement.getName());
                colNum = colNumbers.get(headerName);
                summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(cs.getSum());

            }
        }
    }

    private void prepareObjectsXLSX(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules) {
        // Basing column names on the first workspace in the WorkspaceCollection
        Workspace exampleWorkspace = workspaces.iterator().next();

        if (exampleWorkspace != null) {
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

            // Using the first workspace in the WorkspaceCollection to initialise column headers
            for (String objectName : exampleWorkspace.getObjects().keySet()) {
                // Creating relevant sheet prefixed with "IM"
                objectSheets.put(objectName, workbook.createSheet("OBJ_" + objectName));

                objectRows.put(objectName, 1);
                Row objectHeaderRow = objectSheets.get(objectName).createRow(0);

                // Adding headers to each column
                int col = 0;

                Cell objectIDHeaderCell = objectHeaderRow.createCell(col++);
                objectIDHeaderCell.setCellValue("OBJECT_ID");

                // Adding metadata headers (if enabled)
                if (addMetadataToObjects) {
                    // Running through all the metadata values, adding them as new columns
                    HCMetadata exampleMetadata = exampleWorkspace.getMetadata();
                    for (String name : exampleMetadata.keySet()) {
                        Cell metaHeaderCell = objectHeaderRow.createCell(col++);
                        String metadataName = name.toUpperCase().replaceAll(" ", "_");
                        metaHeaderCell.setCellValue(metadataName);

                    }
                }

                // Adding parent IDs
                RelationshipCollection relationships = modules.getRelationships();
                String[] parents = relationships.getParentNames(objectName);
                if (parents.length != 1 && !parents[0].equals("")) {
                    for (String parent : parents) {
                        parentNames.putIfAbsent(objectName, new LinkedHashMap<>());
                        parentNames.get(objectName).put(col, parent);
                        Cell parentHeaderCell = objectHeaderRow.createCell(col++);
                        parentHeaderCell.setCellValue("PARENT_" + parent + "_ID");

                    }
                }

                // Adding number of children for each child type
                String[] children = relationships.getChildNames(objectName);
                if (children.length != 1 && !children[0].equals("")) {
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


                MeasurementReferenceCollection objectMeasurementReferences = modules.getObjectReferences(objectName);

                // If the current object hasn't got any assigned measurements, skip it
                if (objectMeasurementReferences == null) continue;

                // Running through all the object measurement values, adding them as new columns
                for (MeasurementReference objectMeasurement : objectMeasurementReferences.values()) {
                    if (!objectMeasurement.isCalculated()) continue;
                    if (!objectMeasurement.isExportable()) continue;

                    measurementNames.putIfAbsent(objectName, new LinkedHashMap<>());
                    measurementNames.get(objectName).put(col, objectMeasurement.getName());
                    Cell measHeaderCell = objectHeaderRow.createCell(col++);
                    measHeaderCell.setCellValue(objectMeasurement.getName());

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
                            if (addMetadataToObjects) {
                                HCMetadata metadata = workspace.getMetadata();
                                for (String name : metadata.keySet()) {
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
    }

    private void exportJSON(WorkspaceCollection workspaces, Analysis analysis) {
        System.out.println("[WARN] No JSON export currently implemented.  File not saved.");

    }

    private String getMetadataString(String metadataName) {
        return "META//"+metadataName.toUpperCase().replaceAll(" ", "_");

    }

    private String getImageString(String imageName, String measurementName) {
        imageName = imageName.toUpperCase().replaceAll(" ", "_");

        return imageName+"_(IM)//"+measurementName.toUpperCase().replaceAll(" ", "_");

    }

    private String getObjectString(String objectName, String mode, String measurementName) {
        objectName = objectName.toUpperCase().replaceAll(" ", "_");

        if (mode.equals("")) {
            return objectName+"_(OBJ)//"+measurementName.toUpperCase().replaceAll(" ", "_");
        } else {
            return objectName+"_(OBJ_"+mode+")//"+measurementName.toUpperCase().replaceAll(" ", "_");
        }
    }


    // GETTERS AND SETTERS

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

    public SummaryType getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(SummaryType summaryType) {
        this.summaryType = summaryType;
    }

    public boolean isExportIndividualObjects() {
        return exportIndividualObjects;
    }

    public void setExportIndividualObjects(boolean exportIndividualObjects) {
        this.exportIndividualObjects = exportIndividualObjects;
    }
}
