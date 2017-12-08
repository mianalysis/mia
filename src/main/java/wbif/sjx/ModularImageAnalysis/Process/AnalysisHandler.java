package wbif.sjx.ModularImageAnalysis.Process;

import ij.IJ;
import ij.Prefs;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;
import wbif.sjx.common.FileConditions.FileCondition;
import wbif.sjx.common.FileConditions.NameContainsString;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisHandler {
    private BatchProcessor batchProcessor;

    public void saveAnalysis(Analysis analysis) throws IOException, ParserConfigurationException, TransformerException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);

        String outputFileName = fileDialog.getFiles()[0].getAbsolutePath();
        if (!FilenameUtils.getExtension(outputFileName).equals("mia")) {
            outputFileName = FilenameUtils.removeExtension(outputFileName)+".mia";
        }

        // Creating a module collection holding the input and output
        ModuleCollection inOutModules = new ModuleCollection();
        inOutModules.add(analysis.getInputControl());
        inOutModules.add(analysis.getOutputControl());

        // Adding an XML formatted summary of the modules and their values
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("ROOT");
        root.appendChild(Exporter.prepareModulesXML(doc,inOutModules));
        root.appendChild(Exporter.prepareModulesXML(doc,analysis.getModules()));
        doc.appendChild(root);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Preparing the target file for
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new FileOutputStream(outputFileName));
        transformer.transform(source, result);

        System.out.println("File saved ("+FilenameUtils.getName(outputFileName)+")");

    }

    public Analysis loadAnalysis() throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, ClassNotFoundException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        if (fileDialog.getFiles().length==0) return null;

        return loadAnalysis(new FileInputStream(fileDialog.getFiles()[0]));

    }

    public Analysis loadAnalysis(InputStream analysisFileStream) throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(analysisFileStream);
        doc.getDocumentElement().normalize();

        Analysis analysis = new GUIAnalysis();
        ModuleCollection modules = analysis.getModules();

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i=0;i<moduleNodes.getLength();i++) {
            Node moduleNode = moduleNodes.item(i);

            try {
                NamedNodeMap moduleAttributes = moduleNode.getAttributes();
                String moduleName = moduleAttributes.getNamedItem("NAME").getNodeValue();
                Class<?> clazz = Class.forName(moduleName);
                HCModule module = (HCModule) clazz.newInstance();

                // If the module is an input or output control, treat it differently
                if (module.getClass().isInstance(new InputControl())) {
                    NodeList moduleChildNodes = moduleNode.getChildNodes();
                    boolean foundParameters = false;
                    for (int j=0;j<moduleChildNodes.getLength();j++) {
                        switch (moduleChildNodes.item(j).getNodeName()) {
                            case "PARAMETERS":
                                populateModuleParameters(moduleChildNodes.item(j), module);
                                foundParameters = true;
                                break;

                            case "REFERENCES":
                                populateModuleReferences(moduleChildNodes.item(j), module);
                                break;
                        }
                    }
                    analysis.setInputControl((InputControl) module);

                    continue;

                } else if (module.getClass().isInstance(new OutputControl())) {
                    NodeList moduleChildNodes = moduleNode.getChildNodes();
                    boolean foundParameters = false;
                    for (int j=0;j<moduleChildNodes.getLength();j++) {
                        switch (moduleChildNodes.item(j).getNodeName()) {
                            case "PARAMETERS":
                                populateModuleParameters(moduleChildNodes.item(j), module);
                                foundParameters = true;
                                break;

                            case "REFERENCES":
                                populateModuleReferences(moduleChildNodes.item(j), module);
                                break;
                        }
                    }
                    analysis.setOutputControl((OutputControl) module);

                    continue;

                }

                // Loading all standard HCModules
                if (moduleAttributes.getNamedItem("NICKNAME") != null) {
                    String moduleNickname = moduleAttributes.getNamedItem("NICKNAME").getNodeValue();
                    module.setNickname(moduleNickname);
                } else {
                    module.setNickname(module.getTitle());
                }

                // Populating parameters
                NodeList moduleChildNodes = moduleNode.getChildNodes();
                boolean foundParameters = false;
                for (int j=0;j<moduleChildNodes.getLength();j++) {
                    switch (moduleChildNodes.item(j).getNodeName()) {
                        case "PARAMETERS":
                            populateModuleParameters(moduleChildNodes.item(j), module);
                            foundParameters = true;
                            break;

                        case "REFERENCES":
                            populateModuleReferences(moduleChildNodes.item(j), module);
                            break;
                    }
                }

                // Old file formats had parameters loose within MODULE
                if (!foundParameters) populateModuleParameters(moduleNode, module);

                modules.add(module);

            } catch (ClassNotFoundException e) {
                NamedNodeMap moduleAttributes = moduleNode.getAttributes();
                String moduleName = moduleAttributes.getNamedItem("NAME").getNodeValue();
                IJ.showMessage("Class \""+moduleName+"\" not found (skipping)");

            }
        }

        System.out.println("File loaded");

        return analysis;

    }

    private void populateModuleParameters(Node moduleNode, HCModule module) {
        NodeList parameterNodes = moduleNode.getChildNodes();
        for (int j = 0; j < parameterNodes.getLength(); j++) {
            Node parameterNode = parameterNodes.item(j);
            NamedNodeMap parameterAttributes = parameterNode.getAttributes();
            String parameterName = parameterAttributes.getNamedItem("NAME").getNodeValue();
            String parameterValue = parameterAttributes.getNamedItem("VALUE").getNodeValue();

            boolean parameterVisible = false;
            if (parameterAttributes.getNamedItem("VISIBLE") != null) {
                parameterVisible = Boolean.parseBoolean(parameterAttributes.getNamedItem("VISIBLE").getNodeValue());
            }

            try {
                int parameterType = module.getParameterType(parameterName);
                switch (parameterType) {
                    case Parameter.INPUT_IMAGE:
                    case Parameter.OUTPUT_IMAGE:
                    case Parameter.INPUT_OBJECTS:
                    case Parameter.OUTPUT_OBJECTS:
                    case Parameter.REMOVED_IMAGE:
                    case Parameter.STRING:
                    case Parameter.CHOICE_ARRAY:
                    case Parameter.FILE_PATH:
                    case Parameter.FOLDER_PATH:
                    case Parameter.IMAGE_MEASUREMENT:
                    case Parameter.OBJECT_MEASUREMENT:
                    case Parameter.CHILD_OBJECTS:
                    case Parameter.PARENT_OBJECTS:
                        module.updateParameterValue(parameterName, parameterValue);
                        break;

                    case Parameter.INTEGER:
                        module.updateParameterValue(parameterName, Integer.parseInt(parameterValue));
                        break;

                    case Parameter.DOUBLE:
                        module.updateParameterValue(parameterName, Double.parseDouble(parameterValue));
                        break;

                    case Parameter.BOOLEAN:
                        module.updateParameterValue(parameterName, Boolean.parseBoolean(parameterValue));
                        break;

                }

                module.setParameterVisibility(parameterName,parameterVisible);

            } catch (NullPointerException e) {
                IJ.showMessage("Module "+module.getTitle()
                        +", parameter \""+parameterName + "\" not set");

            }
        }
    }

    private void populateModuleReferences(Node moduleNode, HCModule module) {
        NodeList referenceNodes = moduleNode.getChildNodes();
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            NodeList currentReferenceNodes = referenceNodes.item(i).getChildNodes();

            // Iterating over all references of this type
            for (int j=0;j<currentReferenceNodes.getLength();j++) {
                Node currentReferenceNode = currentReferenceNodes.item(j);
                // Creating the reference object and adding to the relevant collection
                Reference reference = null;

                switch (currentReferenceNode.getNodeName()) {
                    case "IMAGE_REF":
                        String imageName = currentReferenceNode.getAttributes().getNamedItem("NAME").getNodeValue();
                        reference = module.getImageReference(imageName);

                        if (reference == null) continue;

                        module.addImageReference(reference);
                        break;

                    case "OBJECT_REF":
                        String objectName = currentReferenceNode.getAttributes().getNamedItem("NAME").getNodeValue();
                        reference = module.getImageReference(objectName);

                        if (reference == null) continue;

                        module.addObjectReference(reference);
                        break;
                }

                if (reference == null) continue;

                // Adding measurements to the reference object
                NodeList measurementNodes = currentReferenceNode.getChildNodes().item(0).getChildNodes();

                if (measurementNodes == null) continue;

                for (int k = 0; k < measurementNodes.getLength(); k++) {
                    Node measurementNode = measurementNodes.item(k);

                    // Getting measurement properties
                    NamedNodeMap attributes = measurementNode.getAttributes();
                    String measurementName = attributes.getNamedItem("NAME").getNodeValue();
                    boolean isCalulated = Boolean.parseBoolean(attributes.getNamedItem("IS_CALCULATED").getNodeValue());
                    boolean isExportable = Boolean.parseBoolean(attributes.getNamedItem("IS_EXPORTABLE").getNodeValue());
                    MeasurementReference measurementReference = new MeasurementReference(measurementName);
                    measurementReference.setCalculated(isCalulated);
                    measurementReference.setExportable(isExportable);

                    reference.addMeasurementReference(measurementReference);

                }
            }
        }
    }

    public void startAnalysis(Analysis analysis) throws IOException, GenericMIAException, InterruptedException {
        // Getting input options
        InputControl inputControl = analysis.getInputControl();
        String inputMode = inputControl.getParameterValue(InputControl.INPUT_MODE);
        String singleFile = inputControl.getParameterValue(InputControl.SINGLE_FILE_PATH);
        String batchFolder = inputControl.getParameterValue(InputControl.BATCH_FOLDER_PATH);
        String extension = inputControl.getParameterValue(InputControl.FILE_EXTENSION);
        boolean useFilenameFilter1 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_1);
        String filenameFilter1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_1);
        String filenameFilterType1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_1);
        boolean useFilenameFilter2 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_2);
        String filenameFilter2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_2);
        String filenameFilterType2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_2);
        boolean useFilenameFilter3 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_3);
        String filenameFilter3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_3);
        String filenameFilterType3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_3);

        // Getting output options
        OutputControl outputControl = analysis.getOutputControl();
        boolean exportXLSX = outputControl.getParameterValue(OutputControl.EXPORT_XLSX);
        boolean exportSummary = outputControl.getParameterValue(OutputControl.EXPORT_SUMMARY);
        String summaryType = outputControl.getParameterValue(OutputControl.SUMMARY_TYPE);
        boolean exportIndividualObjects = outputControl.getParameterValue(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);

        // THE OLD METHOD THAT WILL BE REMOVED ONCE THE NEW CONTROLS ARE ALSO IMPLEMENTED IN THE BASIC GUI
//        String inputFilePath = Prefs.get("MIA.inputFilePath","");
//
//        JFileChooser fileChooser = new JFileChooser(inputFilePath);
//        fileChooser.setDialogTitle("Select file to run");
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//        fileChooser.setMultiSelectionEnabled(false);
//        fileChooser.showDialog(null,"Open");
//
//        File inputFile = fileChooser.getSelectedFile();
//        Prefs.set("MIA.inputFilePath",inputFile.getParentFile().getAbsolutePath());
//        Prefs.savePreferences();
//
//        String exportName;
//        if (inputFile.isFile()) exportName = FilenameUtils.removeExtension(inputFile.getAbsolutePath());
//        else exportName = inputFile.getAbsolutePath() + "\\output";
        // END OLD SECTION

        File inputFile = null;
        String exportName = null;
        int nThreads = 1;

        switch (inputMode) {
            case InputControl.InputModes.SINGLE_FILE:
                if (singleFile == null) {
                    IJ.runMacro("waitForUser","Select an image first");
                    return;
                }

                inputFile = new File(singleFile);
                exportName = FilenameUtils.removeExtension(inputFile.getAbsolutePath());
                break;

            case InputControl.InputModes.BATCH:
                if (batchFolder == null) {
                    IJ.runMacro("waitForUser","Select a folder first");
                    return;
                }

                inputFile = new File(batchFolder);
                exportName = inputFile.getAbsolutePath() + "\\output";
                nThreads = inputControl.getParameterValue(InputControl.NUMBER_OF_THREADS);

                // Set the number of Fiji threads to 1, so it doesn't clash with MIA multi-threading
                Prefs.setThreads(1);
                Prefs.savePreferences();

                break;
        }

        // Initialising the exporter (if one was requested)
        Exporter exporter = exportXLSX ? new Exporter(exportName, Exporter.XLSX_EXPORT) : null;
        if (exporter != null) {
            exporter.setExportSummary(exportSummary);
            exporter.setExportIndividualObjects(exportIndividualObjects);

            switch (summaryType) {
                case OutputControl.SummaryTypes.ONE_AVERAGE_PER_FILE:
                    exporter.setSummaryType(Exporter.SummaryType.PER_FILE);
                    break;

                case OutputControl.SummaryTypes.AVERAGE_PER_TIMEPOINT:
                    exporter.setSummaryType(Exporter.SummaryType.PER_TIMEPOINT_PER_FILE);
                    break;
            }
        }

        // Initialising BatchProcessor
        batchProcessor = new BatchProcessor(inputFile);
        batchProcessor.setnThreads(nThreads);

        // Adding extension filter
        batchProcessor.addFileCondition(new ExtensionMatchesString(new String[]{extension}));

        // Adding filename filters
        if (useFilenameFilter1) addFilenameFilter(filenameFilterType1,filenameFilter1);
        if (useFilenameFilter2) addFilenameFilter(filenameFilterType2,filenameFilter2);
        if (useFilenameFilter3) addFilenameFilter(filenameFilterType3,filenameFilter3);

        // Running the analysis
        batchProcessor.runAnalysisOnStructure(analysis,exporter);

        // Cleaning up
        Runtime.getRuntime().gc();

    }

    private void addFilenameFilter(String filenameFilterType, String filenameFilter) {
        switch (filenameFilterType) {
            case InputControl.FilterTypes.INCLUDE_MATCHES_PARTIALLY:
                batchProcessor.addFileCondition(new NameContainsString(filenameFilter, FileCondition.INC_PARTIAL));
                break;

            case InputControl.FilterTypes.INCLUDE_MATCHES_COMPLETELY:
                batchProcessor.addFileCondition(new NameContainsString(filenameFilter, FileCondition.INC_PARTIAL));
                break;

            case InputControl.FilterTypes.EXCLUDE_MATCHES_PARTIALLY:
                batchProcessor.addFileCondition(new NameContainsString(filenameFilter, FileCondition.EXC_PARTIAL));
                break;

            case InputControl.FilterTypes.EXCLUDE_MATCHES_COMPLETELY:
                batchProcessor.addFileCondition(new NameContainsString(filenameFilter, FileCondition.EXC_PARTIAL));
                break;
        }
    }

    public void stopAnalysis() {
        batchProcessor.stopAnalysis();

    }
}
