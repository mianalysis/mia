package io.github.mianalysis.mia.process.analysishandling.legacyreaders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FileFolderPathP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.MetadataItemP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.RemovedImageP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.RemovedObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.process.ClassHunter;
import io.github.mianalysis.mia.process.analysishandling.Analysis;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import io.github.mianalysis.mia.process.logging.ProgressBar;

/**
 * Created by Stephen on 23/06/2017.
 */
public class AnalysisReader_Pre_0p10p0 {
    public static Analysis loadAnalysis()
            throws SAXException, IllegalAccessException, IOException, InstantiationException,
            ParserConfigurationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        String previousPath = Prefs.get("MIA.PreviousPath", "");
        JFileChooser fileChooser = new JFileChooser(previousPath);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("MIA workflow (.mia)", "mia"));
        fileChooser.showDialog(null, "Load workflow");

        File file = fileChooser.getSelectedFile();
        if (file == null)
            return null;

        Prefs.set("MIA.PreviousPath", file.getAbsolutePath());

        Analysis analysis = loadAnalysis(file);
        analysis.setAnalysisFilename(file.getAbsolutePath());

        MIA.log.writeStatus("File loaded (" + FilenameUtils.getName(file.getName()) + ")");
        MIA.log.writeWarning("Pre MIA v0.15.0 workflow loaded.  Child object counts and parent ID numbers will need to be manually added as new modules");

        return analysis;

    }

    public static Analysis loadAnalysis(File file)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String xml = FileUtils.readFileToString(file, "UTF-8");

        return loadAnalysis(xml);

    }

    public static Analysis loadAnalysis(String xml)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MIA.log.writeStatus("Loading analysis");

        if (MIA.isHeadless())
            LogRenderer.setProgress(0);
        else
            ProgressBar.update(0);

        if (xml.startsWith("\uFEFF")) {
            xml = xml.substring(1);
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        doc.getDocumentElement().normalize();

        Analysis analysis = new Analysis();
        Modules modules = analysis.getModules();

        // Creating a list of all available modules (rather than reading their full
        // path, in case they move) using
        // Reflections tool
        List<String> availableModules = new ClassHunter<Module>().getClassNames(Module.class);

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i = 0; i < moduleNodes.getLength(); i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type. If none was found the loop
            // skips to the next Module
            Module module = initialiseModule(moduleNode, modules, availableModules);
            if (module == null)
                continue;

            // If the module is an input, treat it differently
            if (module.getClass().isInstance(new GlobalVariables(modules))) {
                addSingleInstanceSpecificComponents(module, moduleNode);
            } else if (module.getClass().isInstance(new InputControl(modules))) {
                addSingleInstanceSpecificComponents(module, moduleNode);
                analysis.getModules().setInputControl((InputControl) module);
            } else if (module.getClass().isInstance(new OutputControl(modules))) {
                addSingleInstanceSpecificComponents(module, moduleNode);
                analysis.getModules().setOutputControl((OutputControl) module);
            } else {
                addStandardModuleSpecificComponents(module, moduleNode);
                modules.add(module);
            }

            MIA.log.writeStatus("Processed " + i + " of " + moduleNodes.getLength() + " modules ("
                    + Math.floorDiv(100 * i, moduleNodes.getLength()) + "%)");

            int progress = 100 * Math.floorDiv(i, moduleNodes.getLength());
            if (MIA.isHeadless())
                LogRenderer.setProgress(progress);
            else
                ProgressBar.update(progress);
            
        }

        return analysis;

    }

    public static Module initialiseModule(Node moduleNode, Modules modules, List<String> availableModules)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String className = moduleAttributes.getNamedItem("NAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(className);
        moduleName = MIA.getLostAndFound().findModule(moduleName);

        for (String availableModule : availableModules) {
            if (moduleName.equals(FilenameUtils.getExtension(availableModule))) {
                Module module;
                try {
                    module = (Module) Class.forName(availableModule).getDeclaredConstructor(Modules.class)
                            .newInstance(modules);
                } catch (ClassNotFoundException e) {
                    MIA.log.writeError(e);
                    continue;
                }

                if (moduleAttributes.getNamedItem("NICKNAME") != null) {
                    String moduleNickname = moduleAttributes.getNamedItem("NICKNAME").getNodeValue();
                    module.setNickname(moduleNickname);
                } else {
                    module.setNickname(module.getName());
                }

                // Populating parameters
                NodeList moduleChildNodes = moduleNode.getChildNodes();
                boolean foundParameters = false;
                for (int j = 0; j < moduleChildNodes.getLength(); j++) {
                    switch (moduleChildNodes.item(j).getNodeName()) {
                        case "PARAMETERS":
                            populateModuleParameters(moduleChildNodes.item(j), module.getAllParameters(), module);
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

                    }
                }

                // Old file formats had parameters loose within MODULE
                if (!foundParameters)
                    populateModuleParameters(moduleNode, module.getAllParameters(), module);

                return module;

            }
        }

        // If no module was found matching that name an error message is displayed
        MIA.log.writeWarning("Module \"" + moduleName + "\" not found (skipping)");

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

    public static void populateModuleParameters(Node moduleNode, Parameters parameters, Module module) {
        String moduleName = module.getName();

        NodeList parameterNodes = moduleNode.getChildNodes();
        for (int j = 0; j < parameterNodes.getLength(); j++) {
            Node parameterNode = parameterNodes.item(j);

            if (parameterNode.getNodeName().equals("COLLECTIONS")) {
                populateModuleParameterGroups(parameterNode, parameters, module);
                continue;
            }

            NamedNodeMap parameterAttributes = parameterNode.getAttributes();

            String parameterName = parameterAttributes.getNamedItem("NAME").getNodeValue();
            String parameterValue = parameterAttributes.getNamedItem("VALUE").getNodeValue();
            String parameterValueSource = "";

            // Updating parameter names
            parameterName = MIA.getLostAndFound().findParameter(module.getClass().getSimpleName(), parameterName);

            if (parameterAttributes.getNamedItem("VALUESOURCE") != null)
                parameterValueSource = parameterAttributes.getNamedItem("VALUESOURCE").getNodeValue();

            try {
                Parameter parameter = parameters.getParameter(parameterName);
                if (parameter instanceof InputImageP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
                    ((InputImageP) parameters.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof OutputImageP) {
                    ((OutputImageP) parameters.getParameter(parameterName)).setImageName(parameterValue);
                } else if (parameter instanceof InputObjectsP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
                    ((InputObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof OutputObjectsP) {
                    ((OutputObjectsP) parameters.getParameter(parameterName)).setObjectsName(parameterValue);
                } else if (parameter instanceof RemovedImageP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
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
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
                    ((ChoiceP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                } else if (parameter instanceof ChildObjectsP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
                    ((ChildObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ChildObjectsP) parameters.getParameter(parameterName)).setParentObjectsName(parameterValueSource);
                } else if (parameter instanceof ParentObjectsP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
                    ((ParentObjectsP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ParentObjectsP) parameters.getParameter(parameterName)).setChildObjectsName(parameterValueSource);
                } else if (parameter instanceof ImageMeasurementP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
                    ((ImageMeasurementP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ImageMeasurementP) parameters.getParameter(parameterName)).setImageName(parameterValueSource);
                } else if (parameter instanceof ObjectMeasurementP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
                    ((ObjectMeasurementP) parameters.getParameter(parameterName)).setChoice(parameterValue);
                    ((ObjectMeasurementP) parameters.getParameter(parameterName)).setObjectName(parameterValueSource);
                } else if (parameter instanceof FilePathP) {
                    ((FilePathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FolderPathP) {
                    ((FolderPathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof FileFolderPathP) {
                    ((FileFolderPathP) parameters.getParameter(parameterName)).setPath(parameterValue);
                } else if (parameter instanceof MetadataItemP) {
                    parameterValue = MIA.getLostAndFound().findParameterValue(module.getClass().getSimpleName(),
                            parameterName, parameterValue);
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
                MIA.log.writeWarning("Module \"" + moduleName + "\" parameter \"" + parameterName + "\" ("
                        + parameterValue + ") not set");

            }
        }
    }

    @Deprecated
    public static void populateModuleMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j = 0; j < referenceNodes.getLength(); j++) {
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
        for (int j = 0; j < referenceNodes.getLength(); j++) {
            MetadataRef ref = new MetadataRef(referenceNodes.item(j));
            module.addMetadataRef(ref);

        }
    }

    public static void populateImageMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j = 0; j < referenceNodes.getLength(); j++) {
            ImageMeasurementRef ref = new ImageMeasurementRef(referenceNodes.item(j));
            module.addImageMeasurementRef(ref);

        }
    }

    public static void populateObjMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j = 0; j < referenceNodes.getLength(); j++) {
            ObjMeasurementRef ref = new ObjMeasurementRef(referenceNodes.item(j));
            module.addObjectMeasurementRef(ref);

        }
    }

    public static void populateModuleParameterGroups(Node parameterNode, Parameters parameters,
            Module module) {
        NodeList collectionNodes = parameterNode.getChildNodes();
        String groupName = parameterNode.getAttributes().getNamedItem("NAME").getNodeValue();

        // Loading the ParameterGroup and clearing all previously-initialised parameters
        ParameterGroup group = parameters.getParameter(groupName);
        if (group != null)
            group.removeAllParameters();

        for (int j = 0; j < collectionNodes.getLength(); j++) {
            Parameters newParameters = group.addParameters();

            Node collectionNode = collectionNodes.item(j);
            Node newParametersNode = collectionNode.getChildNodes().item(0);
            populateModuleParameters(newParametersNode, newParameters, module);

        }
    }
}
