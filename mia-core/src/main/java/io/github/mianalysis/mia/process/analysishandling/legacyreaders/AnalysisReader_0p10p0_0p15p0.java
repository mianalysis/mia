package io.github.mianalysis.mia.process.analysishandling.legacyreaders;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
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
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import io.github.mianalysis.mia.process.logging.ProgressBar;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader_0p10p0_0p15p0 {
    public static Modules loadModules()
            throws SAXException, IllegalAccessException, IOException, InstantiationException,
            ParserConfigurationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        String previousPath = Prefs.get("MIA.PreviousPath", "");
        JFileChooser fileChooser = new JFileChooser(previousPath);
        fileChooser.setPreferredSize(new Dimension(800,640));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("MIA workflow (.mia)", "mia"));
        fileChooser.showDialog(null, "Load workflow");

        File file = fileChooser.getSelectedFile();
        if (file == null)
            return null;

        Prefs.set("MIA.PreviousPath", file.getAbsolutePath());
        Prefs.savePreferences();

        Modules analysis = loadModules(file);
        analysis.setAnalysisFilename(file.getAbsolutePath());

        MIA.log.writeStatus("File loaded (" + FilenameUtils.getName(file.getName()) + ")");
        MIA.log.writeWarning("Pre MIA v0.15.0 workflow loaded.  Child object counts and parent ID numbers will need to be manually added as new modules");

        return analysis;

    }

    public static Modules loadModules(File file)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String xml = FileUtils.readFileToString(file, "UTF-8");

        return loadModules(xml);

    }

    public static Modules loadModules(String xml)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MIA.log.writeStatus("Loading analysis");

        if (MIA.isHeadless())
            LogRenderer.setProgress(0);
        else
            ProgressBar.update(0);

        if (xml.startsWith("\uFEFF"))
            xml = xml.substring(1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        doc.getDocumentElement().normalize();

        return loadModules(doc);

    }

    public static Modules loadModules(Document doc)
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

        return modules;

    }

    public static Module initialiseModule(Node moduleNode, Modules modules, List<String> availableModuleNames)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String className = moduleAttributes.getNamedItem("CLASSNAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(className);

        // Checking if this module has been reassigned
        moduleName = MIA.getLostAndFound().findModule(moduleName);

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
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            Node referenceNode = referenceNodes.item(i);

            // Getting measurement properties
            NamedNodeMap attributes = referenceNode.getAttributes();
            String parameterName = attributes.getNamedItem("NAME").getNodeValue();
            Parameter parameter = module.getParameter(parameterName);

            // If parameter isn't found, try the lost and found
            if (parameter == null) {
                String moduleName = module.getClass().getSimpleName();
                parameterName = MIA.getLostAndFound().findParameter(moduleName, parameterName);
                parameter = module.getParameter(parameterName);
            }

            // If the parameter still isn't found, display a warning
            if (parameter == null) {
                String parameterValue = attributes.getNamedItem("VALUE").getNodeValue();
                MIA.log.writeWarning("Parameter \"" + parameterName + "\" (value = \"" + parameterValue
                        + "\") not found for module \"" + module.getName() + "\", skipping.");
                continue;
            }

            parameter.setAttributesFromXML(referenceNode);

        }
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

    static class RefPairCollection {
        private HashSet<RefPair> refPairs = new HashSet<>();

        void addPair(String object1, String object2) {
            refPairs.add(new RefPair(object1, object2));
        }

        boolean contains(String object1, String object2) {
            for (RefPair refPair : refPairs) {
                if (object1.equals(refPair.getObject1()) && object2.equals(refPair.getObject2()))
                    return true;

                if (object1.equals(refPair.getObject2()) && object2.equals(refPair.getObject1()))
                    return true;
            }

            return false;

        }

        static class RefPair {
            String object1;
            String object2;

            RefPair(String object1, String object2) {
                this.object1 = object1;
                this.object2 = object2;
            }

            String getObject1() {
                return object1;
            }

            String getObject2() {
                return object2;
            }
        }
    }
}