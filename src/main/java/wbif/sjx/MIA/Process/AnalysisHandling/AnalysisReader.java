package wbif.sjx.MIA.Process.AnalysisHandling;

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
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.RelationshipRef;
import wbif.sjx.MIA.Process.ClassHunter;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader {
    public static Analysis loadAnalysis()
            throws SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        if (fileDialog.getFiles().length==0) return null;

        Analysis analysis = loadAnalysis(fileDialog.getFiles()[0]);
        analysis.setAnalysisFilename(fileDialog.getFiles()[0].getAbsolutePath());

        MIA.log.writeStatus("File loaded ("+ FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

        return analysis;

    }

    public static Analysis loadAnalysis(File file)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String xml = FileUtils.readFileToString(file,"UTF-8");

        return loadAnalysis(xml);

    }

    public static Analysis loadAnalysis(String xml)
            throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        MIA.log.writeStatus("Loading analysis");
        GUI.updateProgressBar(0);

        if (xml.startsWith("\uFEFF")) xml = xml.substring(1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        doc.getDocumentElement().normalize();

        // If loading a version older than v0.10.0 use the legacy loader.  Also, has handling for older versions still,
        // which didn't include a version number
        Version thisVersion = new Version("0.10.0");
        Node versionNode = doc.getChildNodes().item(0).getAttributes().getNamedItem("MIA_VERSION");
        Version loadedVersion = versionNode == null ? null : new Version(versionNode.getNodeValue());
        if(loadedVersion == null || thisVersion.compareTo(loadedVersion) > 0) return LegacyAnalysisReader.loadAnalysis(xml);

        Analysis analysis = new Analysis();
        ModuleCollection modules = loadModules(doc);
        analysis.setModules(modules);

        return analysis;

    }

    public static ModuleCollection loadModules(Document doc) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ModuleCollection modules = new ModuleCollection();

        // Creating a list of all available modules (rather than reading their full path, in case they move) using
        // Reflections tool
        List<String> availableModuleNames = ClassHunter.getModules(false);

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i=0;i<moduleNodes.getLength();i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type.  If none was found the loop skips to the next Module
            Module module = initialiseModule(moduleNode,modules,availableModuleNames);
            if (module == null) continue;

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

    public static Module initialiseModule(Node moduleNode, ModuleCollection modules, List<String> availableModuleNames)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String className = moduleAttributes.getNamedItem("CLASSNAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(className);

        for (String availableModuleName:availableModuleNames) {
            if (moduleName.equals(FilenameUtils.getExtension(availableModuleName))) {
                Class<Module> clazz = null;
                try {
                    clazz = (Class<Module>) Class.forName(availableModuleName);
                } catch (ClassNotFoundException e) {
                    MIA.log.writeError(e);
                }
                Module module = (Module) clazz.getDeclaredConstructor(ModuleCollection.class).newInstance(modules);

                // Populating parameters
                NodeList moduleChildNodes = moduleNode.getChildNodes();
                for (int j=0;j<moduleChildNodes.getLength();j++) {
                    switch (moduleChildNodes.item(j).getNodeName()) {
                        case "PARAMETERS":
                            populateParameters(moduleChildNodes.item(j), module);
                            break;

                        case "MEASUREMENTS":
                            populateLegacyMeasurementRefs(moduleChildNodes.item(j), module);
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

                return module;

            }
        }

        // If no module was found matching that name an error message is displayed
        MIA.log.writeWarning("Module \""+moduleName+"\" not found (skipping)");

        return null;

    }

    public static void populateParameters(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            Node referenceNode = referenceNodes.item(j);

            // Getting measurement properties
            NamedNodeMap attributes = referenceNode.getAttributes();
            String parameterName = attributes.getNamedItem("NAME").getNodeValue();
            String parameterValue = attributes.getNamedItem("VALUE").getNodeValue();
            Parameter parameter = module.getParameter(parameterName);

            if (parameter == null) {
                MIA.log.writeWarning("Parameter \""+parameterName+"\" (value = \""+parameterValue+"\") not found for module \""+module.getName()+"\", skipping.");
                continue;
            }

            parameter.setAttributesFromXML(referenceNode);

        }
    }

    public static void populateLegacyMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            Node referenceNode = referenceNodes.item(j);

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
        for (int j=0;j<referenceNodes.getLength();j++) {
            // Getting measurement properties
            MetadataRef ref = new MetadataRef(referenceNodes.item(j));
            module.addMetadataRef(ref);
        }
    }

    public static void populateImageMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            // Getting measurement properties
            ImageMeasurementRef ref = new ImageMeasurementRef(referenceNodes.item(j));
            module.addImageMeasurementRef(ref);
        }
    }

    public static void populateObjMeasurementRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            // Getting measurement properties
            ObjMeasurementRef ref = new ObjMeasurementRef(referenceNodes.item(j));
            module.addObjectMeasurementRef(ref);
        }
    }

    public static void populateModuleRelationshipRefs(Node moduleNode, Module module) {
        NodeList referenceNodes = moduleNode.getChildNodes();

        // Iterating over all references of this type
        for (int j=0;j<referenceNodes.getLength();j++) {
            // Getting measurement properties
            RelationshipRef ref = new RelationshipRef(referenceNodes.item(j));
            module.addRelationshipRef(ref);
        }
    }

}
