package wbif.sjx.MIA.Process.AnalysisHandling;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.RelationshipRef;
import wbif.sjx.MIA.Process.ClassHunter;
import wbif.sjx.MIA.Process.Logging.LogRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Created by Stephen on 23/06/2017.
 */
public class LegacyAnalysisReader {
    public static Analysis loadAnalysis()
            throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        if (fileDialog.getFiles().length==0) return null;

        Analysis analysis = loadAnalysis(fileDialog.getFiles()[0]);
        analysis.setAnalysisFilename(fileDialog.getFiles()[0].getAbsolutePath());

        System.out.println("File loaded ("+ FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

        return analysis;

    }

    public static Analysis loadAnalysis(File file)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String xml = FileUtils.readFileToString(file,"UTF-8");

        return loadAnalysis(xml);

    }

    public static Analysis loadAnalysis(String xml)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        System.out.println("Loading analysis");
        GUI.setProgress(0);

        if (xml.startsWith("\uFEFF")) {
            xml = xml.substring(1);
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        doc.getDocumentElement().normalize();

        Analysis analysis = new Analysis();
        ModuleCollection modules = analysis.getModules();

        // Creating a list of all available modules (rather than reading their full path, in case they move) using
        // Reflections tool
        Set<Class<? extends Module>> availableModules = new ClassHunter<Module>().getClasses(Module.class,MIA.isDebug());

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i=0;i<moduleNodes.getLength();i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type.  If none was found the loop skips to the next Module
            Module module = initialiseModule(moduleNode,modules,availableModules);
            if (module == null) continue;

            // If the module is an input, treat it differently
            if (module.getClass().isInstance(new WorkflowParameters(modules))) {
                addSingleInstanceSpecificComponents(module,moduleNode);
            } else if (module.getClass().isInstance(new InputControl(modules))) {
                addSingleInstanceSpecificComponents(module,moduleNode);
                analysis.getModules().setInputControl((InputControl) module);
            } else if (module.getClass().isInstance(new OutputControl(modules))) {
                addSingleInstanceSpecificComponents(module,moduleNode);
                analysis.getModules().setOutputControl((OutputControl) module);
            } else {
                addStandardModuleSpecificComponents(module, moduleNode);
                modules.add(module);
            }

            System.out.println("Loaded "+i+" of "+moduleNodes.getLength()+" modules");
            GUI.setProgress(100*Math.floorDiv(i,moduleNodes.getLength()));

        }

        return analysis;

    }

    public static Module initialiseModule(Node moduleNode, ModuleCollection modules, Set<Class<? extends Module>> availableModules)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String fullModuleName = moduleAttributes.getNamedItem("NAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(fullModuleName);

        for (Class<?> clazz:availableModules) {
            if (moduleName.equals(clazz.getSimpleName())) {
                Module module = (Module) clazz.getDeclaredConstructor(ModuleCollection.class).newInstance(modules);

                if (moduleAttributes.getNamedItem("NICKNAME") != null) {
                    String moduleNickname = moduleAttributes.getNamedItem("NICKNAME").getNodeValue();
                    module.setNickname(moduleNickname);
                } else {
                    module.setNickname(module.getName());
                }

                // Populating parameters
                NodeList moduleChildNodes = moduleNode.getChildNodes();
                boolean foundParameters = false;
                for (int j=0;j<moduleChildNodes.getLength();j++) {
                    switch (moduleChildNodes.item(j).getNodeName()) {
                        case "PARAMETERS":
                            populateModuleParameters(moduleChildNodes.item(j), module.getAllParameters(), moduleName);
                            foundParameters = true;
                            break;

                        case "MEASUREMENTS":
                            populateModuleMeasurementRefs(moduleChildNodes.item(j), module);
                            break;

                        case "IMAGE_MEASUREMENTS":
                            populateImageMeasurementRefs(moduleChildNodes.item(j), module);
                            break;

                        case "OBJECT_MEASUREMENTS":
                            populateObjMeasurementRefs(moduleChildNodes.item(j), module);
                            break;

                        case "METADATA":
                            populateModuleMetadataRefs(moduleChildNodes.item(j), module);
                            break;

                        case "RELATIONSHIPS":
                            populateModuleRelationshipRefs(moduleChildNodes.item(j), module);
                            break;
                    }
                }

                // Old file formats had parameters loose within MODULE
                if (!foundParameters) populateModuleParameters(moduleNode, module.getAllParameters(),moduleName);

                return module;

            }
        }

        // If no module was found matching that name an error message is displayed
        MIA.log.write("Module \""+moduleName+"\" not found (skipping)", LogRenderer.Level.WARNING);

        return null;

    }

    public static void addSingleInstanceSpecificComponents(Module module, Node moduleNode) {
        NamedNodeMap moduleAttributes = moduleNode.getAttributes();

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

    public static void populateModuleParameters(Node moduleNode, ParameterCollection parameters, String moduleName) {
        NodeList parameterNodes = moduleNode.getChildNodes();
        for (int j = 0; j < parameterNodes.getLength(); j++) {
            Node parameterNode = parameterNodes.item(j);

            if (parameterNode.getNodeName().equals("COLLECTIONS")) {
                populateModuleParameterGroups(parameterNode,parameters,moduleName);
                continue;
            }

            NamedNodeMap parameterAttributes = parameterNode.getAttributes();

            String parameterName = parameterAttributes.getNamedItem("NAME").getNodeValue();
            String parameterValue = parameterAttributes.getNamedItem("VALUE").getNodeValue();
            String parameterValueSource = "";
            if (parameterAttributes.getNamedItem("VALUESOURCE") != null) {
                parameterValueSource = parameterAttributes.getNamedItem("VALUESOURCE").getNodeValue();
            }

            try {
                Parameter parameter = parameters.getParameter(parameterName);
                if (parameter instanceof InputImageP) {
                    ((InputImageP) parameters.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof OutputImageP) {
                    ((OutputImageP) parameters.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof InputObjectsP) {
                    ((InputObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof OutputObjectsP) {
                    ((OutputObjectsP) parameters.getParameter(parameterName)).setObjectsName(parameterValue);
                } else if (parameter instanceof RemovedImageP) {
                    ((RemovedImageP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof RemovedObjectsP) {
                    ((RemovedObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof StringP) {
                    ((StringP) parameters.getParameter(parameterName)).setValue(parameterValue);
                } else if (parameter instanceof IntegerP) {
                    ((IntegerP) parameters.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof DoubleP) {
                    ((DoubleP) parameters.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof BooleanP) {
                    ((BooleanP) parameters.getParameter(parameterName)).setValueFromString(parameterValue);
                } else if (parameter instanceof ChoiceP) {
                    ((ChoiceP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof ChildObjectsP) {
                    ((ChildObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ChildObjectsP) parameters.getParameter(parameterName)).setParentObjectsName(parameterValueSource);
                } else if (parameter instanceof ParentObjectsP) {
                    ((ParentObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ParentObjectsP) parameters.getParameter(parameterName)).setChildObjectsName(parameterValueSource);
                } else if (parameter instanceof ImageMeasurementP) {
                    ((ImageMeasurementP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ImageMeasurementP) parameters.getParameter(parameterName)).setImageName(parameterValueSource);
                } else if (parameter instanceof ObjectMeasurementP) {
                    ((ObjectMeasurementP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ObjectMeasurementP) parameters.getParameter(parameterName)).setObjectName(parameterValueSource);
                } else if (parameter instanceof FilePathP) {
                    ((FilePathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FolderPathP) {
                    ((FolderPathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FileFolderPathP) {
                    ((FileFolderPathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof MetadataItemP) {
                    ((MetadataItemP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof TextAreaP) {
                    ((TextAreaP) parameters.getParameter(parameterName)).setValue(parameterValue);
                }

                if (parameterAttributes.getNamedItem("VISIBLE") != null) {
                    boolean visible = Boolean.parseBoolean(parameterAttributes.getNamedItem("VISIBLE").getNodeValue());
                    parameter.setVisible(visible);
                }

                if (parameterAttributes.getNamedItem("NICKNAME") != null) {
                    String nickname = parameterAttributes.getNamedItem("NICKNAME").getNodeValue();
                    parameter.setNickname(nickname);
                }

            } catch (NullPointerException e) {
                MIA.log.write("Module \""+moduleName+"\" parameter \""+parameterName + "\" ("+parameterValue+") not set", LogRenderer.Level.WARNING);

            }
        }
    }

    @Deprecated
    public static void populateModuleMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            Node referenceNode = referenceNodes.item(j);

            // Getting measurement properties
            NamedNodeMap attributes = referenceNode.getAttributes();
            String type = attributes.getNamedItem("TYPE").getNodeValue();

            // Acquiring the relevant reference
            switch (type) {
                case "IMAGE":
                    ImageMeasurementRef imageMeasurementRef = new ImageMeasurementRef(referenceNode);
                    module.addImageMeasurementRef(imageMeasurementRef);
                    break;

                case "OBJECTS":
                    ObjMeasurementRef objMeasurementRef = new ObjMeasurementRef(referenceNode);
                    module.addObjectMeasurementRef(objMeasurementRef);
                    break;
            }
        }
    }

    public static void populateModuleMetadataRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            MetadataRef ref = new MetadataRef(referenceNodes.item(j));
            module.addMetadataRef(ref);

        }
    }

    public static void populateImageMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            ImageMeasurementRef ref = new ImageMeasurementRef(referenceNodes.item(j));
            module.addImageMeasurementRef(ref);

        }
    }

    public static void populateObjMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            ObjMeasurementRef ref = new ObjMeasurementRef(referenceNodes.item(j));
            module.addObjectMeasurementRef(ref);

        }
    }

    public static void populateModuleRelationshipRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            RelationshipRef ref = new RelationshipRef(referenceNodes.item(j));
            module.addRelationshipRef(ref);

        }
    }

    public static void populateModuleParameterGroups(Node parameterNode, ParameterCollection parameters, String moduleName) {
        NodeList collectionNodes = parameterNode.getChildNodes();
        String groupName = parameterNode.getAttributes().getNamedItem("NAME").getNodeValue();

        // Loading the ParameterGroup and clearing all previously-initialised parameters
        ParameterGroup group = parameters.getParameter(groupName);
        group.removeAllParameters();

        for (int j = 0; j < collectionNodes.getLength(); j++) {
            ParameterCollection newParameters = group.addParameters();

            Node collectionNode = collectionNodes.item(j);
            Node newParametersNode = collectionNode.getChildNodes().item(0);
            populateModuleParameters(newParametersNode,newParameters,moduleName);

        }
    }
}
