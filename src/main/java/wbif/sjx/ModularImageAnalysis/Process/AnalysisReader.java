package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.util.Set;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader {
    public static Analysis loadAnalysis() throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, ClassNotFoundException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        if (fileDialog.getFiles().length==0) return null;

        FileInputStream fileInputStream = new FileInputStream(fileDialog.getFiles()[0]);
        Analysis analysis = loadAnalysis(fileInputStream);
        fileInputStream.close();

        System.out.println("File loaded ("+ FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

        return analysis;

    }

    public static Analysis loadAnalysis(InputStream analysisFileStream)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(analysisFileStream);
        doc.getDocumentElement().normalize();

        Analysis analysis = new Analysis();
        ModuleCollection modules = analysis.getModules();

        // Creating a list of all available modules (rather than reading their full path, in case they move) using
        // Reflections tool
        Set<Class<? extends Module>> availableModules = ModuleReader.getModules(MIA.isDebug());

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i=0;i<moduleNodes.getLength();i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type.  If none was found the loop skips to the next Module
            Module module = initialiseModule(moduleNode,availableModules);
            if (module == null) continue;

            // If the module is an input, treat it differently
            if (module.getClass().isInstance(new InputControl())) {
                analysis.setInputControl((InputControl) module);

            } else if (module.getClass().isInstance(new OutputControl())) {
                addOutputSpecificComponents(module,moduleNode);
                analysis.setOutputControl((OutputControl) module);

            } else {
                addStandardModuleSpecificComponents(module, moduleNode);
                modules.add(module);
            }
        }

        return analysis;

    }

    public static Module initialiseModule(Node moduleNode, Set<Class<? extends Module>> availableModules)
            throws IllegalAccessException, InstantiationException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String fullModuleName = moduleAttributes.getNamedItem("NAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(fullModuleName);

        for (Class<?> clazz:availableModules) {
            if (moduleName.equals(clazz.getSimpleName())) {
                Module module = (Module) clazz.newInstance();

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

                        case "MEASUREMENTS":
                            populateModuleMeasurementRefs(moduleChildNodes.item(j), module);
                            break;
                    }
                }

                // Old file formats had parameters loose within MODULE
                if (!foundParameters) populateModuleParameters(moduleNode, module);

                return module;

            }
        }

        // If no module was found matching that name an error message is displayed
        System.err.println("Module \""+moduleName+"\" not found (skipping)");

        return null;

    }

    public static void addOutputSpecificComponents(Module module, Node moduleNode) {
        NamedNodeMap moduleAttributes = moduleNode.getAttributes();

        if (moduleAttributes.getNamedItem("DISABLEABLE") != null) {
            String isDisableable = moduleAttributes.getNamedItem("DISABLEABLE").getNodeValue();
            module.setCanBeDisabled(Boolean.parseBoolean(isDisableable));
        } else {
            module.setCanBeDisabled(false);
        }
    }

    public static void addStandardModuleSpecificComponents(Module module, Node moduleNode) {
        NamedNodeMap moduleAttributes = moduleNode.getAttributes();

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

        if (moduleAttributes.getNamedItem("SHOW_OUTPUT") != null) {
            String canShowOutput = moduleAttributes.getNamedItem("SHOW_OUTPUT").getNodeValue();
            module.setShowOutput(Boolean.parseBoolean(canShowOutput));
        } else {
            module.setShowOutput(false);
        }

        if (moduleAttributes.getNamedItem("NOTES") != null) {
            String notes = moduleAttributes.getNamedItem("NOTES").getNodeValue();
            module.setNotes(notes);
        } else {
            module.setNotes("");
        }
    }

    public static void populateModuleParameters(Node moduleNode, Module module) {
        NodeList parameterNodes = moduleNode.getChildNodes();
        for (int j = 0; j < parameterNodes.getLength(); j++) {
            Node parameterNode = parameterNodes.item(j);
            NamedNodeMap parameterAttributes = parameterNode.getAttributes();

            String parameterName = parameterAttributes.getNamedItem("NAME").getNodeValue();
            String parameterValue = parameterAttributes.getNamedItem("VALUE").getNodeValue();
            String parameterValueSource = "";
            if (parameterAttributes.getNamedItem("VALUESOURCE") != null) {
                parameterValueSource = parameterAttributes.getNamedItem("VALUESOURCE").getNodeValue();
            }

            try {
                Parameter parameter = module.getParameter(parameterName);
                if (parameter instanceof InputImageP) {
                    ((InputImageP) module.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof OutputImageP) {
                    ((OutputImageP) module.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof InputObjectsP) {
                    ((InputObjectsP) module.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof OutputObjectsP) {
                    ((OutputObjectsP) module.getParameter(parameterName)).setObjectsName(parameterValue);
                } else if (parameter instanceof RemovedImageP) {
                    ((RemovedImageP) module.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof RemovedObjectsP) {
                    ((RemovedObjectsP) module.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof StringP) {
                    ((StringP) module.getParameter(parameterName)).setValue(parameterValue);
                } else if (parameter instanceof IntegerP) {
                    ((IntegerP) module.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof DoubleP) {
                    ((DoubleP) module.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof BooleanP) {
                    ((BooleanP) module.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof ChoiceP) {
                    ((ChoiceP) module.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof ChildObjectsP) {
                    ((ChildObjectsP) module.getParameter(parameterName)).setChoice(parameterValue);
                    ((ChildObjectsP) module.getParameter(parameterName)).setParentObjectsName(parameterValueSource);
                } else if (parameter instanceof ParentObjectsP) {
                    ((ParentObjectsP) module.getParameter(parameterName)).setChoice(parameterValue);
                    ((ParentObjectsP) module.getParameter(parameterName)).setChildObjectsName(parameterValueSource);
                } else if (parameter instanceof ImageMeasurementP) {
                    ((ImageMeasurementP) module.getParameter(parameterName)).setChoice(parameterValue);
                    ((ImageMeasurementP) module.getParameter(parameterName)).setImageName(parameterValueSource);
                } else if (parameter instanceof ObjectMeasurementP) {
                    ((ObjectMeasurementP) module.getParameter(parameterName)).setChoice(parameterValue);
                    ((ObjectMeasurementP) module.getParameter(parameterName)).setObjectName(parameterValueSource);
                } else if (parameter instanceof FilePathP) {
                    ((FilePathP) module.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FolderPathP) {
                    ((FolderPathP) module.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FileFolderPathP) {
                    ((FileFolderPathP) module.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof MetadataItemP) {
                    ((MetadataItemP) module.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof TextDisplayP) {
                    ((TextDisplayP) module.getParameter(parameterName)).setValue(parameterValue);
                }

                if (parameterAttributes.getNamedItem("VISIBLE") != null) {
                    boolean visible = Boolean.parseBoolean(parameterAttributes.getNamedItem("VISIBLE").getNodeValue());
                    parameter.setVisible(visible);
                }

            } catch (NullPointerException e) {
                System.err.println("Module \""+module.getTitle()
                        +"\" parameter \""+parameterName + "\" ("+parameterValue+") not set");

            }
        }
    }

    public static void populateModuleMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            Node referenceNode = referenceNodes.item(j);

            // Getting measurement properties
            NamedNodeMap attributes = referenceNode.getAttributes();
            String measurementName = attributes.getNamedItem("NAME").getNodeValue();
            String type = attributes.getNamedItem("TYPE").getNodeValue();

            // Acquiring the relevant reference
            MeasurementRef measurementReference = null;
            switch (type) {
                case "IMAGE":
                    measurementReference = module.getImageMeasurementRef(measurementName);
                    break;

                case "OBJECTS":
                    measurementReference = module.getObjectMeasurementRef(measurementName);
                    break;

            }

            if (measurementReference == null) continue;

            // Updating the reference's parameters
            String measurementNickName = measurementName;
            if (attributes.getNamedItem("NICKNAME") != null) measurementNickName = attributes.getNamedItem("NICKNAME").getNodeValue();
            measurementReference.setNickname(measurementNickName);
            measurementReference.setImageObjName(attributes.getNamedItem("IMAGE_OBJECT_NAME").getNodeValue());

            boolean exportGlobal = true;
            if (attributes.getNamedItem("EXPORT_GLOBAL") != null) {
                exportGlobal= Boolean.parseBoolean(attributes.getNamedItem("EXPORT_GLOBAL").getNodeValue());
            }
            measurementReference.setExportGlobal(exportGlobal);

            boolean exportIndividual = true;
            if (attributes.getNamedItem("EXPORT_INDIVIDUAL") != null) {
                exportIndividual = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_INDIVIDUAL").getNodeValue());
            }
            measurementReference.setExportIndividual(exportIndividual);

            boolean exportMean = true;
            if (attributes.getNamedItem("EXPORT_MEAN") != null) {
                exportMean = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MEAN").getNodeValue());
            }
            measurementReference.setExportMean(exportMean);

            boolean exportMin = true;
            if (attributes.getNamedItem("EXPORT_MIN") != null) {
                exportMin = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MIN").getNodeValue());
            }
            measurementReference.setExportMin(exportMin);

            boolean exportMax = true;
            if (attributes.getNamedItem("EXPORT_MAX") != null) {
                exportMax = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_MAX").getNodeValue());
            }
            measurementReference.setExportMax(exportMax);

            boolean exportSum = true;
            if (attributes.getNamedItem("EXPORT_SUM") != null) {
                exportSum = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_SUM").getNodeValue());
            }
            measurementReference.setExportSum(exportSum);

            boolean exportStd = true;
            if (attributes.getNamedItem("EXPORT_STD") != null) {
                exportStd = Boolean.parseBoolean(attributes.getNamedItem("EXPORT_STD").getNodeValue());
            }
            measurementReference.setExportStd(exportStd);

        }
    }
}
