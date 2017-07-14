package wbif.sjx.ModularImageAnalysis.Process;

import ij.IJ;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;

import javax.swing.*;
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
    boolean exportXLSX = true;
    boolean exportXML = false;

    private static File inputFile = null;

    public void saveAnalysis(Analysis analysis) throws IOException, ParserConfigurationException, TransformerException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);

        String outputFileName = fileDialog.getFiles()[0].getAbsolutePath();
        if (!FilenameUtils.getExtension(outputFileName).equals("mia")) {
            outputFileName = FilenameUtils.removeExtension(outputFileName)+".mia";
        }

        // Adding an XML formatted summary of the modules and their values
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(Exporter.prepareParametersXML(doc,analysis.getModules()));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Preparing the target file for
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new FileOutputStream(outputFileName));
        transformer.transform(source, result);

        System.out.println("File saved ("+FilenameUtils.getName(outputFileName)+")");

    }

    public Analysis loadAnalysis() throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(fileDialog.getFiles()[0]);
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

                NodeList parameterNodes = moduleNode.getChildNodes();
                for (int j = 0; j < parameterNodes.getLength(); j++) {
                    Node parameterNode = parameterNodes.item(j);
                    if (parameterNode.getNodeName().equals("PARAMETER")) {
                        NamedNodeMap parameterAttributes = parameterNode.getAttributes();
                        String parameterName = parameterAttributes.getNamedItem("NAME").getNodeValue();
                        String parameterValue = parameterAttributes.getNamedItem("VALUE").getNodeValue();

                        try {
                            int parameterType = module.getParameterType(parameterName);
                            switch (parameterType) {
                                case Parameter.INPUT_IMAGE:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.OUTPUT_IMAGE:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.INPUT_OBJECTS:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.OUTPUT_OBJECTS:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.REMOVED_IMAGE:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.INTEGER:
                                    module.updateParameterValue(parameterName, Integer.parseInt(parameterValue));
                                    break;

                                case Parameter.DOUBLE:
                                    module.updateParameterValue(parameterName, Double.parseDouble(parameterValue));
                                    break;

                                case Parameter.STRING:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.CHOICE_ARRAY:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.CHOICE_MAP:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.BOOLEAN:
                                    module.updateParameterValue(parameterName, Boolean.parseBoolean(parameterValue));
                                    break;

                                case Parameter.FILE_PATH:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.MEASUREMENT:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.CHILD_OBJECTS:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                                case Parameter.PARENT_OBJECTS:
                                    module.updateParameterValue(parameterName, parameterValue);
                                    break;

                            }

                        } catch (NullPointerException e) {
                            IJ.showMessage("Module "+moduleName+", parameter \""+parameterName + "\" not set");

                        }
                    }
                }

                modules.add(module);

            } catch (ClassNotFoundException e) {
                NamedNodeMap moduleAttributes = moduleNode.getAttributes();
                String moduleName = moduleAttributes.getNamedItem("NAME").getNodeValue();
                IJ.showMessage("Class \""+moduleName+"\" not found (skipping)");

            }
        }

        System.out.println("File loaded ("+FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

        return analysis;

    }

    public Workspace startAnalysis(Analysis analysis) throws IOException, GenericMIAException {
        JFileChooser fileChooser = new JFileChooser(inputFile);
        fileChooser.setDialogTitle("Select file to run");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.showDialog(null,"Open");

        inputFile = fileChooser.getSelectedFile();
        if (inputFile.isDirectory()) { // Batch mode
            Exporter exporter = new Exporter(inputFile.getAbsolutePath()+"\\output", Exporter.XLSX_EXPORT);
            BatchProcessor batchProcessor = new BatchProcessor(inputFile);
            batchProcessor.addFileCondition(new ExtensionMatchesString(new String[]{"tif"}));
            batchProcessor.runAnalysisOnStructure(analysis,exporter);

            Runtime.getRuntime().gc();

            return null;

        } else if (inputFile.isFile()) { // Single file mode
            String inputFilePath = inputFile.getAbsolutePath();
            String outputFilePath = FilenameUtils.removeExtension(inputFilePath);

            // Initialising the testWorkspace
            WorkspaceCollection workspaces = new WorkspaceCollection();
            Workspace workspace;
            if (!inputFilePath.equals("")) {
                workspace = workspaces.getNewWorkspace(new File(inputFilePath));

            } else {
                workspace = workspaces.getNewWorkspace(null);

            }

            // Running the analysis
            if (!analysis.execute(workspace, true)) return null;

            // Exporting XLSX
            if (exportXLSX & !outputFilePath.equals("")) {
                Exporter exporter = new Exporter(outputFilePath, Exporter.XLSX_EXPORT);
                exporter.exportResults(workspaces, analysis);

            }

            // Exporting XML
            if (exportXML & !outputFilePath.equals("")) {
                Exporter exporter = new Exporter(outputFilePath, Exporter.XML_EXPORT);
                exporter.exportResults(workspaces, analysis);

            }

            return workspace;

        }

        return null;

    }
}
