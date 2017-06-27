// TODO: Get measurements to export from analysis.getModules().getMeasurements().get(HCName) for each object

package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
public class HCExporter {
    public static final int XML_EXPORT = 0;
    public static final int XLSX_EXPORT = 1;
    public static final int JSON_EXPORT = 2;

    private int exportMode = XLSX_EXPORT;

    private String exportFilePath;
    private boolean verbose = false;


    // CONSTRUCTOR

    public HCExporter(String exportFilePath,int exportMode) {
        this.exportFilePath = exportFilePath;
        this.exportMode = exportMode;

    }


    // PUBLIC METHODS

    public void exportResults(HCWorkspaceCollection workspaces, HCAnalysis analysis) throws IOException {
        if (exportMode == XML_EXPORT) {
            exportXML(workspaces,analysis);

        } else if (exportMode == XLSX_EXPORT) {
            exportXLSX(workspaces,analysis);

        } else if (exportMode == JSON_EXPORT) {
            exportJSON(workspaces,analysis);

        }
    }

    private void exportXML(HCWorkspaceCollection workspaces, HCAnalysis analysis) {
        // Initialising DecimalFormat
        DecimalFormat df = new DecimalFormat("0.000E0");

        // Getting modules
        HCModuleCollection modules = analysis.getModules();

        try {
            // Initialising the document
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("ROOT");
            doc.appendChild(root);

            // Getting parameters as Element and adding to the main file
            Element parametersElement = prepareParametersXML(doc,modules);
            root.appendChild(parametersElement);

            // Running through each workspace (each corresponds to a file) adding file information
            for (HCWorkspace workspace:workspaces) {
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
                for (HCName imageName:workspace.getImages().keySet()) {
                    HCImage image = workspace.getImages().get(imageName);

                    if (image.getSingleMeasurements() != null) {
                        Element imageElement = doc.createElement("IMAGE");

                        Attr nameAttr = doc.createAttribute("NAME");
                        nameAttr.appendChild(doc.createTextNode(String.valueOf(imageName.getName())));
                        imageElement.setAttributeNode(nameAttr);

                        for (HCMeasurement measurement : image.getSingleMeasurements().values()) {
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
                for (HCName objectNames:workspace.getObjects().keySet()) {
                    for (HCObject object:workspace.getObjects().get(objectNames).values()) {
                        Element objectElement =  doc.createElement("OBJECT");

                        // Setting the ID number
                        Attr idAttr = doc.createAttribute("ID");
                        idAttr.appendChild(doc.createTextNode(String.valueOf(object.getID())));
                        objectElement.setAttributeNode(idAttr);

                        // Setting the group ID number
                        Attr groupidAttr = doc.createAttribute("GROUP_ID");
                        groupidAttr.appendChild(doc.createTextNode(String.valueOf(object.getGroupID())));
                        objectElement.setAttributeNode(groupidAttr);

                        Attr nameAttr = doc.createAttribute("NAME");
                        nameAttr.appendChild(doc.createTextNode(String.valueOf(objectNames.getName())));
                        objectElement.setAttributeNode(nameAttr);

                        for (int dim:object.getPositions().keySet()) {
                            String dimName = dim==3 ? "CHANNEL" : dim == 4 ? "TIME" : "DIM_"+dim;
                            Attr positionAttr = doc.createAttribute(dimName);
                            positionAttr.appendChild(doc.createTextNode(String.valueOf(dim)));
                            objectElement.setAttributeNode(positionAttr);

                        }

                        for (HCMeasurement measurement:object.getMeasurements().values()) {
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

    public static Element prepareParametersXML(Document doc, HCModuleCollection modules) {
        Element parametersElement =  doc.createElement("PARAMETERS");

        // Running through each parameter set (one for each module
        for (HCModule module:modules) {
            LinkedHashMap<String,HCParameter> parameters = module.getActiveParameters();

            boolean first = true;
            Element moduleElement =  doc.createElement("MODULE");
            for (HCParameter currParam:parameters.values()) {
                // For the first parameter in a module, adding the name
                if (first) {
                    Attr nameAttr = doc.createAttribute("NAME");
                    nameAttr.appendChild(doc.createTextNode(module.getClass().getSimpleName()));
                    moduleElement.setAttributeNode(nameAttr);

                    first = false;
                }

                // Adding the name and value of the current parameter
                Element parameterElement =  doc.createElement("PARAMETER");

                Attr nameAttr = doc.createAttribute("NAME");
                nameAttr.appendChild(doc.createTextNode(currParam.getName()));
                parameterElement.setAttributeNode(nameAttr);

                Attr valueAttr = doc.createAttribute("VALUE");
                valueAttr.appendChild(doc.createTextNode(currParam.getValue().toString()));
                parameterElement.setAttributeNode(valueAttr);

                moduleElement.appendChild(parameterElement);

            }

            // Adding current module to parameters
            parametersElement.appendChild(moduleElement);

        }

        return parametersElement;

    }

    private void exportXLSX(HCWorkspaceCollection workspaces, HCAnalysis analysis) throws IOException {
        // Getting modules
        HCModuleCollection modules = analysis.getModules();

        // Initialising the workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Adding relevant sheets
        prepareParametersXLSX(workbook,modules);
        prepareMetadataXLSX(workbook,workspaces);
        prepareImagesXLSX(workbook,workspaces,modules);
        prepareObjectsXLSX(workbook,workspaces,modules);

        // Writing the workbook to file
        String outPath = FilenameUtils.removeExtension(exportFilePath) +".xlsx";
        FileOutputStream outputStream = new FileOutputStream(outPath);
        workbook.write(outputStream);
        workbook.close();

        if (verbose) System.out.println("Saved "+ outPath);

    }

    private void prepareParametersXLSX(XSSFWorkbook workbook, HCModuleCollection modules) {
        // Creating a sheet for parameters
        XSSFSheet paramSheet = workbook.createSheet("Parameters");

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
            LinkedHashMap<String,HCParameter> parameters = module.getActiveParameters();

            paramRow++;

            for (HCParameter currParam : parameters.values()) {
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

    private void prepareMetadataXLSX(XSSFWorkbook workbook, HCWorkspaceCollection workspaces) {
        // Basing column names on the first workspace in the WorkspaceCollection
        HCWorkspace exampleWorkspace = workspaces.get(0);

        if (exampleWorkspace != null) {
            HCMetadata exampleMetadata = exampleWorkspace.getMetadata();

            if (exampleMetadata.size() != 0) {
                // Adding header rows for the metadata sheet.
                XSSFSheet metaSheet = workbook.createSheet("Metadata");

                // Creating the header row
                int metaRow = 0;
                int metaCol = 0;
                Row metaHeaderRow = metaSheet.createRow(metaRow++);

                // Setting the analysis ID.  This is the same value on each sheet
                Cell IDHeaderCell = metaHeaderRow.createCell(metaCol++);
                IDHeaderCell.setCellValue("ANALYSIS_ID");

                // Running through all the metadata values, adding them as new columns
                for (String name : exampleMetadata.keySet()) {
                    Cell metaHeaderCell = metaHeaderRow.createCell(metaCol++);
                    String metadataName = name.toUpperCase().replaceAll(" ", "_");
                    metaHeaderCell.setCellValue(metadataName);

                }

                // Running through each workspace, adding the relevant values.  Metadata is stored as a LinkedHashMap, so values
                // should always come off in the same order for the same analysis
                for (HCWorkspace workspace : workspaces) {
                    HCMetadata metadata = workspace.getMetadata();

                    metaCol = 0;
                    Row metaValueRow = metaSheet.createRow(metaRow++);

                    // Setting the analysis ID.  This is the same value on each sheet
                    Cell metaValueCell = metaValueRow.createCell(metaCol++);
                    metaValueCell.setCellValue(workspace.getID());

                    // Running through all the metadata values, adding them as new columns
                    for (String name : metadata.keySet()) {
                        metaValueCell = metaValueRow.createCell(metaCol++);
                        metaValueCell.setCellValue(metadata.getAsString(name));

                    }
                }
            }
        }
    }

    private void prepareImagesXLSX(XSSFWorkbook workbook, HCWorkspaceCollection workspaces, HCModuleCollection modules) {
        // Basing column names on the first workspace in the WorkspaceCollection
        HCWorkspace exampleWorkspace = workspaces.get(0);

        if (exampleWorkspace.getImages() != null) {
            // Creating a new sheet for each image.  Each analysed file will have its own row.
            HashMap<HCName, XSSFSheet> imageSheets = new HashMap<>();
            HashMap<HCName, Integer> imageRows = new HashMap<>();

            // Using the first workspace in the WorkspaceCollection to initialise column headers
            for (HCName imageName : exampleWorkspace.getImages().keySet()) {
                HCImage image = exampleWorkspace.getImages().get(imageName);

                if (image.getSingleMeasurements().size() != 0) {
                    // Creating relevant sheet prefixed with "IM"
                    imageSheets.put(imageName, workbook.createSheet("IM_" + imageName.getName()));

                    // Adding headers to each column
                    int col = 0;

                    imageRows.put(imageName, 1);
                    Row imageHeaderRow = imageSheets.get(imageName).createRow(0);

                    // Creating a cell holding the path to the analysed file
                    Cell IDHeaderCell = imageHeaderRow.createCell(col++);
                    IDHeaderCell.setCellValue("ANALYSIS_ID");

                    String[] measurementNames = modules.getMeasurements().getMeasurementNames(imageName);
                    // Adding measurement headers
                    for (String measurementName : measurementNames) {
                        Cell measHeaderCell = imageHeaderRow.createCell(col++);
                        measHeaderCell.setCellValue(measurementName);

                    }
                }
            }

            // Running through each Workspace, adding rows
            for (HCWorkspace workspace : workspaces) {
                for (HCName imageName : workspace.getImages().keySet()) {
                    HCImage image = exampleWorkspace.getImages().get(imageName);

                    if (image.getSingleMeasurements().size() != 0) {
                        // Adding the measurements from this image
                        int col = 0;

                        Row imageValueRow = imageSheets.get(imageName).createRow(imageRows.get(imageName));
                        imageRows.compute(imageName, (k, v) -> v = v + 1);

                        // Creating a cell holding the path to the analysed file
                        Cell IDValueCell = imageValueRow.createCell(col++);
                        IDValueCell.setCellValue(workspace.getID());

                        for (HCMeasurement measurement : image.getSingleMeasurements().values()) {
                            Cell measValueCell = imageValueRow.createCell(col++);
                            measValueCell.setCellValue(measurement.getValue());
                        }
                    }
                }
            }
        }
    }

    private void prepareObjectsXLSX(XSSFWorkbook workbook,HCWorkspaceCollection workspaces, HCModuleCollection modules) {
        // Basing column names on the first workspace in the WorkspaceCollection
        HCWorkspace exampleWorkspace = workspaces.get(0);

        if (exampleWorkspace != null) {
            // Creating a new sheet for each object.  Each analysed file has its own set of rows (one for each object)
            HashMap<HCName, XSSFSheet> objectSheets = new HashMap<>();
            HashMap<HCName, Integer> objectRows = new HashMap<>();

            // Creating a LinkedHashMap that links relationship ID names to column numbers.  This keeps the correct
            // relationships in the correct columns
            LinkedHashMap<HCName, LinkedHashMap<Integer,HCName>> relationshipNames = new LinkedHashMap<>();

            // Creating a LinkedHashMap that links measurement names to column numbers.  This keeps the correct
            // measurements in the correct columns
            LinkedHashMap<HCName, LinkedHashMap<Integer,String>> measurementNames = new LinkedHashMap<>();

            // Using the first workspace in the WorkspaceCollection to initialise column headers
            for (HCName objectName : exampleWorkspace.getObjects().keySet()) {
                HCObjectSet objects = exampleWorkspace.getObjects().get(objectName);

                if (objects.values().iterator().next().getMeasurements().size() != 0) {
                    // Creating relevant sheet prefixed with "IM"
                    objectSheets.put(objectName, workbook.createSheet("OBJ_" + objectName.getName()));

                    // Adding headers to each column
                    int col = 0;

                    objectRows.put(objectName, 1);
                    Row objectHeaderRow = objectSheets.get(objectName).createRow(0);

                    // Creating a cell holding the path to the analysed file
                    Cell IDHeaderCell = objectHeaderRow.createCell(col++);
                    IDHeaderCell.setCellValue("ANALYSIS_ID");

                    Cell filenameHeaderCell = objectHeaderRow.createCell(col++);
                    filenameHeaderCell.setCellValue("FILENAME");

                    Cell objectIDHeaderCell = objectHeaderRow.createCell(col++);
                    objectIDHeaderCell.setCellValue("OBJECT_ID");

//                    Cell groupIDHeaderCell = objectHeaderRow.createCell(col++);
//                    groupIDHeaderCell.setCellValue("GROUP_ID");

                    // Adding parent IDs
                    HCRelationshipCollection relationships = modules.getRelationships();
                    HCName[] parentNames = relationships.getParentNames(objectName);
                    for (HCName parentName:parentNames) {
                        relationshipNames.putIfAbsent(objectName,new LinkedHashMap<>());
                        relationshipNames.get(objectName).put(col,parentName);
                        Cell parentHeaderCell = objectHeaderRow.createCell(col++);
                        parentHeaderCell.setCellValue("PARENT_"+parentName.getName()+"_ID");

                    }

                    // Getting an example object
                    HCObject object = objects.values().iterator().next();

                    // Adding single-valued position headers
                    for (int dim:object.getPositions().keySet()) {
                        Cell positionsHeaderCell = objectHeaderRow.createCell(col++);
                        String dimName = dim==3 ? "CHANNEL" : dim == 4 ? "FRAME" : "DIM_"+dim;
                        positionsHeaderCell.setCellValue(dimName);

                    }

                    // Adding measurement headers
                    String[] measurements = modules.getMeasurements().getMeasurementNames(objectName);
                    for (String measurement : measurements) {
                        measurementNames.putIfAbsent(objectName,new LinkedHashMap<>());
                        measurementNames.get(objectName).put(col,measurement);
                        Cell measHeaderCell = objectHeaderRow.createCell(col++);
                        measHeaderCell.setCellValue(measurement);

                    }
                }
            }

            // Running through each Workspace, adding rows
            for (HCWorkspace workspace : workspaces) {
                for (HCName objectName : exampleWorkspace.getObjects().keySet()) {
                    HCObjectSet objects = exampleWorkspace.getObjects().get(objectName);

                    if (objects.values().iterator().next().getMeasurements().size() != 0) {
                        for (HCObject object : objects.values()) {
                            // Adding the measurements from this image
                            int col = 0;

                            Row objectValueRow = objectSheets.get(objectName).createRow(objectRows.get(objectName));
                            objectRows.compute(objectName, (k, v) -> v = v + 1);

                            // Creating a cell holding the path to the analysed file
                            Cell IDValueCell = objectValueRow.createCell(col++);
                            IDValueCell.setCellValue(workspace.getID());

                            Cell filenameValueCell = objectValueRow.createCell(col++);
                            filenameValueCell.setCellValue(workspace.getMetadata().getFile().getName());

                            Cell objectIDValueCell = objectValueRow.createCell(col++);
                            objectIDValueCell.setCellValue(object.getID());

//                            Cell groupIDValueCell = objectValueRow.createCell(col++);
//                            groupIDValueCell.setCellValue(object.getGroupID());

                            // Adding relationships to the columns specified in relationshipNames
                            for (int column:relationshipNames.get(objectName).keySet()) {
                                Cell parentValueCell = objectValueRow.createCell(column);
                                HCName parentName = relationshipNames.get(objectName).get(column);
                                HCObject parent = object.getParent(parentName);
                                if (parent != null) {
                                    parentValueCell.setCellValue(parent.getID());
                                } else {
                                    parentValueCell.setCellValue(Double.NaN);
                                }
                                col++;
                            }

                            for (int dim:object.getPositions().keySet()) {
                                Cell positionsValueCell = objectValueRow.createCell(col++);
                                positionsValueCell.setCellValue(object.getPosition(dim));

                            }

                            // Adding measurements to the columns specified in measurementNames
                            for (int column:measurementNames.get(objectName).keySet()) {
                                Cell measValueCell = objectValueRow.createCell(column);
                                String measurementName = measurementNames.get(objectName).get(column);
                                HCMeasurement measurement = object.getMeasurement(measurementName);
                                if (measurement != null) {
                                    measValueCell.setCellValue(measurement.getValue());
                                } else {
                                    measValueCell.setCellValue(Double.NaN);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void exportJSON(HCWorkspaceCollection workspaces, HCAnalysis analysis) {
        System.out.println("[WARN] No JSON export currently implemented.  File not saved.");

    }


    // GETTERS AND SETTERS

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
