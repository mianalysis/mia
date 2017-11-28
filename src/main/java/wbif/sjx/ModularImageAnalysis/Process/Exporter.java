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
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class Exporter {
    public static final int XML_EXPORT = 0;
    public static final int XLSX_EXPORT = 1;
    public static final int JSON_EXPORT = 2;

    private int exportMode = XLSX_EXPORT;

    private String exportFilePath;
    private boolean verbose = false;

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
            Element parametersElement = prepareParametersXML(doc,modules);
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

                        for (MIAMeasurement measurement : image.getMeasurements().values()) {
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

                        for (MIAMeasurement measurement:object.getMeasurements().values()) {
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

    public static Element prepareParametersXML(Document doc, ModuleCollection modules) {
        Element parametersElement =  doc.createElement("PARAMETERS");

        // Running through each parameter set (one for each module
        for (HCModule module:modules) {
            LinkedHashMap<String,Parameter> parameters = module.getActiveParameters();

            Element moduleElement =  doc.createElement("MODULE");
            Attr nameAttr = doc.createAttribute("NAME");
            nameAttr.appendChild(doc.createTextNode(module.getClass().getName()));
            moduleElement.setAttributeNode(nameAttr);

            Attr nicknameAttr = doc.createAttribute("NICKNAME");
            nicknameAttr.appendChild(doc.createTextNode(module.getNickname()));
            moduleElement.setAttributeNode(nicknameAttr);

            for (Parameter currParam:parameters.values()) {
                // Adding the name and value of the current parameter
                Element parameterElement =  doc.createElement("PARAMETER");

                nameAttr = doc.createAttribute("NAME");
                nameAttr.appendChild(doc.createTextNode(currParam.getName()));
                parameterElement.setAttributeNode(nameAttr);

                Attr valueAttr = doc.createAttribute("VALUE");
                valueAttr.appendChild(doc.createTextNode(currParam.getValue().toString()));
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

                moduleElement.appendChild(parameterElement);

            }

            // Adding current module to parameters
            parametersElement.appendChild(moduleElement);

        }

        return parametersElement;

    }

    private void exportXLSX(WorkspaceCollection workspaces, Analysis analysis) throws IOException {
        // Getting modules
        ModuleCollection modules = analysis.getModules();

        // Initialising the workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // Adding relevant sheets
        prepareParametersXLSX(workbook,modules);
        prepareSummaryXLSX(workbook,workspaces,modules);
        prepareObjectsXLSX(workbook,workspaces,modules);

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
            LinkedHashMap<String,Parameter> parameters = module.getActiveParameters();

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

    private void prepareSummaryXLSX(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules) {
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

        // Adding image headers
        HashMap<String,Image> exampleImages = exampleWorkspace.getImages();
        if (exampleImages.size() != 0) {

            for (Image exampleImage : exampleImages.values()) {
                String exampleImageName = exampleImage.getName();

                // Running through all the image measurement values, adding them as new columns
                HashMap<String, MIAMeasurement> exampleMeasurements = exampleImage.getMeasurements();
                for (String measurementName : exampleMeasurements.keySet()) {
                    Cell summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    String summaryDataName = getImageString(exampleImageName,measurementName);
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                }
            }
        }

        // Adding object headers
        HashMap<String, ObjSet> exampleObjSets = exampleWorkspace.getObjects();
        if (exampleObjSets.size() != 0) {

            for (ObjSet exampleObjSet : exampleObjSets.values()) {
                String exampleObjSetName = exampleObjSet.getName();

                // Adding the number of objects
                Cell summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                String summaryDataName = getObjectString(exampleObjSetName,"","NUMBER");
                summaryHeaderCell.setCellValue(summaryDataName);
                colNumbers.put(summaryDataName, headerCol++);

                // Running through all the image measurement values, adding them as new columns
                String[] exampleMeasurementNames = modules.getMeasurements().getMeasurementNames(exampleObjSetName);
                for (String measurementName : exampleMeasurementNames) {
                    if (measurementName.equals("")) continue;

                    summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    summaryDataName = getObjectString(exampleObjSetName,"MEAN",measurementName);
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                    summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    summaryDataName = getObjectString(exampleObjSetName,"STD",measurementName);
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                    summaryHeaderCell = summaryHeaderRow.createCell(headerCol);
                    summaryDataName = getObjectString(exampleObjSetName,"SUM",measurementName);
                    summaryHeaderCell.setCellValue(summaryDataName);
                    colNumbers.put(summaryDataName, headerCol++);

                }
            }
        }

        // Running through each Workspace
        int summaryRow = 1;
        for (Workspace workspace:workspaces) {
            Row summaryValueRow = summarySheet.createRow(summaryRow++);

            // Adding metadata values
            HCMetadata metadata = workspace.getMetadata();
            for (String name : metadata.keySet()) {
                String headerName = getMetadataString(name);
                int colNumber = colNumbers.get(headerName);
                Cell metaValueCell = summaryValueRow.createCell(colNumber);
                metaValueCell.setCellValue(metadata.getAsString(name));

            }

            // Adding image measurements
            HashMap<String,Image> images = workspace.getImages();
            for (Image image:images.values()) {
                String imageName = image.getName();
                HashMap<String,MIAMeasurement> measurements = image.getMeasurements();

                for (String measurementName : measurements.keySet()) {
                    String headerName = getImageString(imageName,measurementName);
                    int colNum = colNumbers.get(headerName);

                    Cell summaryCell = summaryValueRow.createCell(colNum);
                    summaryCell.setCellValue(measurements.get(measurementName).getValue());

                }
            }

            // Adding object measurements
            HashMap<String, ObjSet> objSets = workspace.getObjects();
            for (ObjSet objSet:objSets.values()) {
                String objSetName = objSet.getName();
                String[] measurementNames = modules.getMeasurements().getMeasurementNames(objSetName);

                String headerName = getObjectString(objSetName,"","NUMBER");
                int colNum = colNumbers.get(headerName);
                Cell summaryCell = summaryValueRow.createCell(colNum);
                summaryCell.setCellValue(objSet.size());

                for (String measurementName : measurementNames) {
                    if (measurementName.equals("")) continue;

                    // Running through all objects in this set, adding measurements to a CumStat object
                    CumStat cs = new CumStat();
                    for (Obj obj:objSet.values()) {
                        MIAMeasurement measurement = obj.getMeasurement(measurementName);
                        cs.addMeasure(measurement.getValue());
                    }

                    headerName = getObjectString(objSetName,"MEAN",measurementName);
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    summaryCell.setCellValue(cs.getMean());

                    headerName = getObjectString(objSetName,"STD",measurementName);
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    summaryCell.setCellValue(cs.getStd());

                    headerName = getObjectString(objSetName,"SUM",measurementName);
                    colNum = colNumbers.get(headerName);
                    summaryCell = summaryValueRow.createCell(colNum);
                    summaryCell.setCellValue(cs.getSum());

                }
            }
        }
    }

    private void prepareImagesXLSX(SXSSFWorkbook workbook, WorkspaceCollection workspaces, ModuleCollection modules) {
        // Basing column names on the first workspace in the WorkspaceCollection
        Workspace exampleWorkspace = workspaces.iterator().next();

        if (exampleWorkspace.getImages() != null) {
            // Creating a new sheet for each image.  Each analysed file will have its own row.
            HashMap<String, Sheet> imageSheets = new HashMap<>();
            HashMap<String, Integer> imageRows = new HashMap<>();

            // Using the first workspace in the WorkspaceCollection to initialise column headers
            for (String imageName : exampleWorkspace.getImages().keySet()) {
                Image image = exampleWorkspace.getImages().get(imageName);

                if (image.getMeasurements().size() != 0) {
                    // Creating relevant sheet prefixed with "IM"
                    imageSheets.put(imageName, workbook.createSheet("IM_" + imageName));

                    // Adding headers to each column
                    int col = 0;

                    imageRows.put(imageName, 1);
                    Row imageHeaderRow = imageSheets.get(imageName).createRow(0);

                    String[] measurementNames = modules.getMeasurements().getMeasurementNames(imageName);
                    // Adding measurement headers
                    for (String measurementName : measurementNames) {
                        Cell measHeaderCell = imageHeaderRow.createCell(col++);
                        measHeaderCell.setCellValue(measurementName);

                    }
                }
            }

            // Running through each Workspace, adding rows
            for (Workspace workspace : workspaces) {
                for (String imageName : workspace.getImages().keySet()) {
                    Image image = workspace.getImages().get(imageName);

                    if (image.getMeasurements().size() != 0) {
                        // Adding the measurements from this image
                        int col = 0;

                        Row imageValueRow = imageSheets.get(imageName).createRow(imageRows.get(imageName));
                        imageRows.compute(imageName, (k, v) -> v = v + 1);

                        for (MIAMeasurement measurement : image.getMeasurements().values()) {
                            Cell measValueCell = imageValueRow.createCell(col++);
                            if (Double.isNaN(measurement.getValue())) {
                                measValueCell.setCellValue("");
                            } else {
                                measValueCell.setCellValue(measurement.getValue());
                            }
                        }
                    }
                }
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

                // Adding measurement headers
                String[] measurements = modules.getMeasurements().getMeasurementNames(objectName);
                if (measurements != null) {
                    for (String measurement : measurements) {
                        if (measurement.equals("")) continue;

                        measurementNames.putIfAbsent(objectName, new LinkedHashMap<>());
                        measurementNames.get(objectName).put(col, measurement);
                        Cell measHeaderCell = objectHeaderRow.createCell(col++);
                        measHeaderCell.setCellValue(measurement);

                    }
                }
            }

            // Running through each Workspace, adding rows
            for (Workspace workspace : workspaces) {
                for (String objectName : workspace.getObjects().keySet()) {
                    ObjSet objects = workspace.getObjects().get(objectName);

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
                                    ObjSet children = object.getChildren(childName);
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
                                MIAMeasurement measurement = object.getMeasurement(measurementName);

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

}
