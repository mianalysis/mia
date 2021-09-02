package io.github.mianalysis.MIA.Process.AnalysisHandling;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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

import fiji.plugin.trackmate.util.Version;
import io.github.mianalysis.MIA.MIA;
import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.Core.InputControl;
import io.github.mianalysis.MIA.Module.Core.OutputControl;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;
import io.github.mianalysis.MIA.Object.Refs.ImageMeasurementRef;
import io.github.mianalysis.MIA.Object.Refs.MetadataRef;
import io.github.mianalysis.MIA.Object.Refs.ObjMeasurementRef;
import io.github.mianalysis.MIA.Process.ClassHunter;
import io.github.mianalysis.MIA.Process.AnalysisHandling.LegacyReaders.AnalysisReader_0p10p0_0p15p0;
import io.github.mianalysis.MIA.Process.AnalysisHandling.LegacyReaders.AnalysisReader_Pre_0p10p0;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader {
    public static Analysis loadAnalysis()
            throws SAXException, IllegalAccessException, IOException, InstantiationException,
            ParserConfigurationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        if (fileDialog.getFiles().length == 0)
            return null;

        Analysis analysis = loadAnalysis(fileDialog.getFiles()[0]);
        analysis.setAnalysisFilename(fileDialog.getFiles()[0].getAbsolutePath());

        MIA.log.writeStatus("File loaded (" + FilenameUtils.getName(fileDialog.getFiles()[0].getName()) + ")");

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
        Version loadedVersion = versionNode == null ? null : new Version(versionNode.getNodeValue());
        if (loadedVersion == null || new Version("0.10.0").compareTo(loadedVersion) > 0)
            return AnalysisReader_Pre_0p10p0.loadAnalysis(xml);
        else if (new Version("0.15.0").compareTo(loadedVersion) > 0)
            return AnalysisReader_0p10p0_0p15p0.loadAnalysis(xml);

        Analysis analysis = new Analysis();
        Modules modules = loadModules(doc, loadedVersion);
        analysis.setModules(modules);

        return analysis;

    }

    public static Modules loadModules(Document doc, Version loadedVersion)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Modules modules = new Modules();

        // Creating a list of all available modules (rather than reading their full
        // path, in case they move) using
        // Reflections tool
        List<String> availableModuleNames = ClassHunter.getModules(false);

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
        if (new Version("0.18.0").compareTo(loadedVersion) > 0)
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
                    + ".  Value: " + parameterValue+".");
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