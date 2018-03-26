package wbif.sjx.ModularImageAnalysis.Process;

import ij.IJ;
import ij.Prefs;
import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.Module.Module;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisHandler {
    private static BatchProcessor batchProcessor;

    public void saveAnalysis(Analysis analysis, String outputFileName) throws IOException, ParserConfigurationException, TransformerException {
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

    public void saveAnalysis(Analysis analysis) throws IOException, ParserConfigurationException, TransformerException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);

        // If no file was selected quit the method
        if (fileDialog.getFiles().length==0) return;

        String outputFileName = fileDialog.getFiles()[0].getAbsolutePath();
        if (!FilenameUtils.getExtension(outputFileName).equals("mia")) {
            outputFileName = FilenameUtils.removeExtension(outputFileName)+".mia";
        }

        saveAnalysis(analysis,outputFileName);

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

        // Creating a list of all available modules (rather than reading their full path, in case they move) using
        // Reflections tool
        Reflections.log = null;
        Reflections reflections = new Reflections("wbif.sjx.ModularImageAnalysis");
        Set<Class<? extends Module>> availableModules = reflections.getSubTypesOf(Module.class);

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i=0;i<moduleNodes.getLength();i++) {
            Node moduleNode = moduleNodes.item(i);

            NamedNodeMap moduleAttributes = moduleNode.getAttributes();
            String fullModuleName = moduleAttributes.getNamedItem("NAME").getNodeValue();
            String moduleName = FilenameUtils.getExtension(fullModuleName);
            Module module = null;
            for (Class<?> clazz:availableModules) {
                if (moduleName.equals(clazz.getSimpleName())) {
                    module = (Module) clazz.newInstance();
                    break;
                }
            }

            // If no module was found, display an error, then continue
            if (module == null) {
                System.err.println("Class \""+moduleName+"\" not found (skipping)");
                continue;
            }

            // If the module is an input or output control, treat it differently
            if (module.getClass().isInstance(new InputControl())) {
                if (moduleAttributes.getNamedItem("NICKNAME") != null) {
                    String moduleNickname = moduleAttributes.getNamedItem("NICKNAME").getNodeValue();
                    module.setNickname(moduleNickname);
                } else {
                    module.setNickname(module.getTitle());
                }

                NodeList moduleChildNodes = moduleNode.getChildNodes();
                for (int j=0;j<moduleChildNodes.getLength();j++) {
                    switch (moduleChildNodes.item(j).getNodeName()) {
                        case "PARAMETERS":
                            populateModuleParameters(moduleChildNodes.item(j), module);
                            break;

                        case "MEASUREMENTS":
                            populateModuleMeasurementReferences(moduleChildNodes.item(j), module);
                            break;
                    }
                }
                analysis.setInputControl((InputControl) module);

                continue;

            } else if (module.getClass().isInstance(new OutputControl())) {
                if (moduleAttributes.getNamedItem("NICKNAME") != null) {
                    String moduleNickname = moduleAttributes.getNamedItem("NICKNAME").getNodeValue();
                    module.setNickname(moduleNickname);
                } else {
                    module.setNickname(module.getTitle());
                }

                NodeList moduleChildNodes = moduleNode.getChildNodes();
                for (int j=0;j<moduleChildNodes.getLength();j++) {
                    switch (moduleChildNodes.item(j).getNodeName()) {
                        case "PARAMETERS":
                            populateModuleParameters(moduleChildNodes.item(j), module);
                            break;

                        case "MEASUREMENTS":
                            populateModuleMeasurementReferences(moduleChildNodes.item(j), module);
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

            if (moduleAttributes.getNamedItem("ENABLED") != null) {
                String isEnabled = moduleAttributes.getNamedItem("ENABLED").getNodeValue();
                module.setEnabled(Boolean.parseBoolean(isEnabled));
            } else {
                module.setEnabled(true);
            }

            if (moduleAttributes.getNamedItem("DISABLEABLE") != null) {
                String isDisableable = moduleAttributes.getNamedItem("DISABLEABLE").getNodeValue();
                module.setCanBeDisabled(Boolean.parseBoolean(isDisableable));
            } else {
                module.setCanBeDisabled(false);
            }

            if (moduleAttributes.getNamedItem("NOTES") != null) {
                String notes = moduleAttributes.getNamedItem("NOTES").getNodeValue();
                module.setNotes(notes);
            } else {
                module.setNotes("");
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

                    case "MEASUREMENTS":
                        populateModuleMeasurementReferences(moduleChildNodes.item(j), module);
                        break;
                }
            }

            // Old file formats had parameters loose within MODULE
            if (!foundParameters) populateModuleParameters(moduleNode, module);

            modules.add(module);

        }

        System.out.println("File loaded");

        return analysis;

    }

    private void populateModuleParameters(Node moduleNode, Module module) {
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
                    case Parameter.REMOVED_OBJECTS:
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
                System.err.println("Module \""+module.getTitle()
                        +"\" parameter \""+parameterName + "\" not set");

            }
        }
    }

    private void populateModuleMeasurementReferences(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            Node referenceNode = referenceNodes.item(j);

            // Getting measurement properties
            NamedNodeMap attributes = referenceNode.getAttributes();
            String measurementName = attributes.getNamedItem("NAME").getNodeValue();
            boolean isCalulated = Boolean.parseBoolean(attributes.getNamedItem("IS_CALCULATED").getNodeValue());
            boolean isExportable = Boolean.parseBoolean(attributes.getNamedItem("IS_EXPORTABLE").getNodeValue());
            String type = attributes.getNamedItem("TYPE").getNodeValue();
            String imageObjectName = attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue();

            // Acquiring the relevant reference
            MeasurementReference measurementReference = null;
            switch (type) {
                case "IMAGE":
                    measurementReference = module.getImageMeasurementReference(measurementName);
                    break;

                case "OBJECTS":
                    measurementReference = module.getObjectMeasurementReference(measurementName);
                    break;

            }

            if (measurementReference == null) continue;

            // Updating the reference's parameters
            measurementReference.setCalculated(isCalulated);
            measurementReference.setExportable(isExportable);
            measurementReference.setImageObjName(imageObjectName);

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
        boolean exportXLSX = outputControl.isEnabled();
        boolean exportSummary = outputControl.getParameterValue(OutputControl.EXPORT_SUMMARY);
        String summaryType = outputControl.getParameterValue(OutputControl.SUMMARY_TYPE);
        boolean calculateMean = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_MEAN);
        boolean calculateStd = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_STD);
        boolean calculateSum = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_SUM);
        boolean exportIndividualObjects = outputControl.getParameterValue(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);

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
            exporter.setCalculateMean(calculateMean);
            exporter.setCalculateStd(calculateStd);
            exporter.setCalculateSum(calculateSum);
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
        if (useFilenameFilter1) batchProcessor.addFilenameFilter(filenameFilterType1,filenameFilter1);
        if (useFilenameFilter2) batchProcessor.addFilenameFilter(filenameFilterType2,filenameFilter2);
        if (useFilenameFilter3) batchProcessor.addFilenameFilter(filenameFilterType3,filenameFilter3);

        // Running the analysis
        batchProcessor.runAnalysisOnStructure(analysis,exporter);

        // Cleaning up
        Runtime.getRuntime().gc();

        System.out.println("Complete!");

    }

    public void stopAnalysis() {
        batchProcessor.stopAnalysis();

    }

    public static BatchProcessor getBatchProcessor() {
        return batchProcessor;
    }
}
