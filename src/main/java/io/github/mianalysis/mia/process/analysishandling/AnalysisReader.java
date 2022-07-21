package io.github.mianalysis.mia.process.analysishandling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.scijava.util.VersionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.process.analysishandling.legacyreaders.AnalysisReader_0p10p0_0p15p0;
import io.github.mianalysis.mia.process.analysishandling.legacyreaders.AnalysisReader_Pre_0p10p0;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader {
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
        GUI.updateProgressBar(0);

        if (xml.startsWith("\uFEFF"))
            xml = xml.substring(1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        doc.getDocumentElement().normalize();

        // If loading a suitably old version, use the relevant legacy loader. Also, has
        // handling for older versions still, which didn't include a version number.
        Node versionNode = doc.getChildNodes().item(0).getAttributes().getNamedItem("MIA_VERSION");
        String loadedVersion = versionNode.getNodeValue();

        if (VersionUtils.compare(MIA.getVersion(), loadedVersion) != 0) {
            MIA.log.writeMessage("Loaded workflow created in different version of MIA.");
            MIA.log.writeMessage("    Workflow will likely still be compatible, but some issues may be encountered.");
            MIA.log.writeMessage("    Workflow version: " + loadedVersion);
            MIA.log.writeMessage("    Installed version: " + MIA.getVersion());
        }

        if (versionNode == null || VersionUtils.compare("0.10.0", loadedVersion) > 0)
            return AnalysisReader_Pre_0p10p0.loadAnalysis(xml);
        else if (VersionUtils.compare("0.15.0", loadedVersion) > 0)
            return AnalysisReader_0p10p0_0p15p0.loadAnalysis(xml);

        Analysis analysis = new Analysis();
        Modules modules = loadModules(doc, loadedVersion);
        analysis.setModules(modules);

        return analysis;

    }

    public static Modules loadModules(Document doc, String loadedVersion)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Modules modules = new Modules();

        // Creating a list of all available modules (rather than reading their full
        // path, in case they move) using
        // Reflections tool
        List<String> availableModuleNames = AvailableModules.getModuleNames(false);

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i = 0; i < moduleNodes.getLength(); i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type. If none was found the loop
            // skips to the next Module
            Module module = initialiseModule(moduleNode, modules, availableModuleNames);
            if (module == null)
                continue;

            module.setAttributesFromXML(moduleNode);

            // If the module is an input, treat it differently
            if (module.getClass().isInstance(new InputControl(modules))) {
                modules.setInputControl((InputControl) module);
            } else if (module.getClass().isInstance(new OutputControl(modules))) {
                modules.setOutputControl((OutputControl) module);
            } else {
                modules.add(module);
            }
        }

        // Adding timepoint measurements for all objects
        if (VersionUtils.compare("0.18.0", loadedVersion) > 0)
            AnalysisReader_0p10p0_0p15p0.addTimepointMeasurements(modules);

        return modules;

    }

    public static Module initialiseModule(Node moduleNode, Modules modules, List<String> availableModuleNames)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String className = moduleAttributes.getNamedItem("CLASSNAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(className);

        // Checking if this module has been reassigned
        moduleName = MIA.lostAndFound.findModule(moduleName);

        // Trying to load from available modules
        for (String availableModuleName : availableModuleNames) {
            if (moduleName.equals(FilenameUtils.getExtension(availableModuleName))) {
                return initialiseModule(moduleNode, modules, availableModuleName);
            }
        }

        // If no module was found matching that name an error message is displayed
        MIA.log.writeWarning("Module \"" + moduleName + "\" not found (skipping)");

        return null;

    }

    public static Module initialiseModule(Node moduleNode, Modules modules, String availableModuleName)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<Module> clazz = null;
        try {
            String shortName = availableModuleName.substring(availableModuleName.lastIndexOf(".") + 1);
            if (!MIA.dependencies.compatible(shortName, false)) {
                MIA.log.writeWarning("Module \"" + shortName + "\" not available");
                return null;
            }

            clazz = (Class<Module>) Class.forName(availableModuleName);
        } catch (ClassNotFoundException e) {
            MIA.log.writeError(e);
        }
        Module module = (Module) clazz.getDeclaredConstructor(Modules.class).newInstance(modules);

        // Populating parameters
        NodeList moduleChildNodes = moduleNode.getChildNodes();
        for (int i = 0; i < moduleChildNodes.getLength(); i++) {
            switch (moduleChildNodes.item(i).getNodeName()) {
                case "PARAMETERS":
                    populateParameters(moduleChildNodes.item(i), module);
                    break;

                case "MEASUREMENTS":
                    populateLegacyMeasurementRefs(moduleChildNodes.item(i), module);
                    break;

                case "IMAGE_MEASUREMENTS":
                    populateImageMeasurementRefs(moduleChildNodes.item(i), module);
                    break;

                case "OBJECT_MEASUREMENTS":
                    populateObjMeasurementRefs(moduleChildNodes.item(i), module);
                    break;

                case "METADATA":
                    populateModuleMetadataRefs(moduleChildNodes.item(i), module);
                    break;
            }
        }

        return module;

    }

    public static void populateParameters(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int i = 0; i < referenceNodes.getLength(); i++)
            initialiseParameter(referenceNodes.item(i), module, module.getAllParameters());

    }

    public static void initialiseParameter(Node referenceNode, Module module, Parameters parameters) {
        // Getting measurement properties
        NamedNodeMap attributes = referenceNode.getAttributes();
        if (attributes == null)
            return;
        Node name = attributes.getNamedItem("NAME");
        if (name == null)
            return;
        String parameterName = name.getNodeValue();
        Parameter parameter = parameters.getParameter(parameterName);

        // If parameter isn't found, try the lost and found
        if (parameter == null) {
            String moduleName = module.getClass().getSimpleName();
            parameterName = MIA.lostAndFound.findParameter(moduleName, parameterName);
            if (parameterName.equals("")) // blank parameter names mean that parameter has been removed, but shouldn't
                                          // show a warning
                return;
            parameter = parameters.getParameter(parameterName);
        }

        // If the parameter still isn't found, display a warning
        if (parameter == null) {
            String parameterValue = attributes.getNamedItem("VALUE").getNodeValue();
            MIA.log.writeWarning("Parameter not found.  Module: " + module.getName() + ".  Parameter: " + parameterName
                    + ".  Value: " + parameterValue + ".");
            return;
        }

        parameter.setAttributesFromXML(referenceNode);

    }

    public static void populateLegacyMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            Node referenceNode = referenceNodes.item(i);

            // Getting measurement properties
            String type = referenceNode.getAttributes().getNamedItem("TYPE").getNodeValue();

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
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            // Getting measurement properties
            MetadataRef ref = new MetadataRef(referenceNodes.item(i));
            module.addMetadataRef(ref);
        }
    }

    public static void populateImageMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            // Getting measurement properties
            ImageMeasurementRef ref = new ImageMeasurementRef(referenceNodes.item(i));
            module.addImageMeasurementRef(ref);
        }
    }

    public static void populateObjMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            // Getting measurement properties
            ObjMeasurementRef ref = new ObjMeasurementRef(referenceNodes.item(i));
            module.addObjectMeasurementRef(ref);
        }
    }
}
