package wbif.sjx.MIA.Process.AnalysisHandling.LegacyReaders;

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

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous.ChildObjectCount;
import wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous.ParentObjectID;
import wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous.PartnerObjectCount;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ParentChildRef;
import wbif.sjx.MIA.Object.References.PartnerRef;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Process.ClassHunter;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader_0p10p0_0p15p0 {
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

        Analysis analysis = new Analysis();
        ModuleCollection modules = loadModules(doc);
        analysis.setModules(modules);

        return analysis;

    }

    public static ModuleCollection loadModules(Document doc)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ModuleCollection modules = new ModuleCollection();

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

        return modules;

    }

    public static Module initialiseModule(Node moduleNode, ModuleCollection modules, List<String> availableModuleNames)
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

    public static Module initialiseModule(Node moduleNode, ModuleCollection modules, String availableModuleName)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<Module> clazz = null;
        try {
            clazz = (Class<Module>) Class.forName(availableModuleName);
        } catch (ClassNotFoundException e) {
            MIA.log.writeError(e);
        }
        Module module = (Module) clazz.getDeclaredConstructor(ModuleCollection.class).newInstance(modules);

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

                case "RELATIONSHIPS":
                case "PARENT_CHILD":
                    populateModuleParentChildRefs(moduleChildNodes.item(i), modules);
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
                parameterName = MIA.lostAndFound.findParameter(moduleName, parameterName);
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

    public static void populateModuleParentChildRefs(Node moduleNode, ModuleCollection modules) {
        NodeList referenceNodes = moduleNode.getChildNodes();
        boolean firstAdded = true; // As soon as one module is added, this is set to false.

        // Iterating over all references of this type
        for (int i = 0; i < referenceNodes.getLength(); i++) {
            Node node = referenceNodes.item(i);

            // ParentChildRef and PartnerRef no longer supports exporting, so have to use new,
            // LegacySummaryRef to get export options
            LegacySummaryRef lRef = new LegacySummaryRef(node);

            // If this reference exports anything, new ObjectCount and ParentID measurement
            // modules must be added at the end of the analysis
            if (!lRef.isExportGlobal() && !lRef.isExportIndividual() && !lRef.isExportMax() && !lRef.isExportMean()
                    && !lRef.isExportMin() && !lRef.isExportStd() && !lRef.isExportSum())
                continue;

            // Adding a GUI separator if necessary
            if (firstAdded) {
                addRefSeparatorModule(modules);
                firstAdded = false;
                MIA.log.writeMessage(
                        "Pre-v0.15.0 analysis loaded.  Analysis has been automatically updated to store exported child/partner counts and parent IDs as measurements.");
            }

            switch (node.getNodeName()) {
                case "RELATIONSHIP":
                case "PARENT_CHILD":
                    // Getting relationship properties and modules
                    ParentChildRef pcRef = new ParentChildRef(node);
                    addChildCountModule(modules, lRef, pcRef);
                    addParentIDModule(modules, lRef, pcRef);

                    break;

                case "PARTNER":
                    // Getting relationship properties and module
                    PartnerRef pRef = new PartnerRef(node);
                    addPartnerCountModule(modules, lRef, pRef);

                    break;
            }
        }
    }

    static void addRefSeparatorModule(ModuleCollection modules) {
        GUISeparator guiSeparator = new GUISeparator(modules);
        modules.add(guiSeparator);

        guiSeparator.updateParameterValue(GUISeparator.SHOW_BASIC, false);
        guiSeparator.setNickname("[AUTOGEN] Object relationship counts");
        guiSeparator.setNotes(
                "The following modules were automatically added to aid compatibility with MIA v0.15.0 and above.<br><br>Child object counts, parent IDs and partner object counts are now stored as measurements.  The following modules add the same data exporting as present in the original analysis.<br><br>Note: Spreadsheet column headers may have changed.");

    }

    static void addChildCountModule(ModuleCollection modules, LegacySummaryRef lRef, ParentChildRef pcRef) {
        // Creating the object count module
        ChildObjectCount countModule = new ChildObjectCount(modules);
        modules.add(countModule);
        countModule.updateParameterValue(ChildObjectCount.INPUT_OBJECTS, pcRef.getParentName());
        countModule.updateParameterValue(ChildObjectCount.CHILD_OBJECTS, pcRef.getChildName());

        // Getting relevant measurement
        String measurementName = ChildObjectCount.getFullName(pcRef.getChildName());
        ObjMeasurementRefCollection measRefs = countModule.updateAndGetObjectMeasurementRefs();
        ObjMeasurementRef measRef = measRefs.get(measurementName);

        // Setting measurement export states
        measRef.setExportGlobal(lRef.isExportGlobal());
        measRef.setExportIndividual(lRef.isExportIndividual());
        measRef.setExportMax(lRef.isExportMax());
        measRef.setExportMean(lRef.isExportMean());
        measRef.setExportMin(lRef.isExportMin());
        measRef.setExportStd(lRef.isExportStd());
        measRef.setExportSum(lRef.isExportSum());

    }

    static void addParentIDModule(ModuleCollection modules, LegacySummaryRef lRef, ParentChildRef pcRef) {
        // Creating the object count module
        ParentObjectID idModule = new ParentObjectID(modules);
        modules.add(idModule);
        idModule.updateParameterValue(ParentObjectID.INPUT_OBJECTS, pcRef.getChildName());
        idModule.updateParameterValue(ParentObjectID.PARENT_OBJECT, pcRef.getParentName());

        // There's no need to set export states here, as this measurement should already
        // be set to only appear as an individual measurement with no statistics.

    }

    static void addPartnerCountModule(ModuleCollection modules, LegacySummaryRef lRef, PartnerRef pRef) {
        // Creating the object count module
        PartnerObjectCount countModule = new PartnerObjectCount(modules);
        modules.add(countModule);
        countModule.updateParameterValue(PartnerObjectCount.INPUT_OBJECTS, pRef.getObject1Name());
        countModule.updateParameterValue(PartnerObjectCount.PARTNER_OBJECTS, pRef.getObject2Name());

        // Getting relevant measurement
        String measurementName = PartnerObjectCount.getFullName(pRef.getObject2Name());
        ObjMeasurementRefCollection measRefs = countModule.updateAndGetObjectMeasurementRefs();
        ObjMeasurementRef measRef = measRefs.get(measurementName);

        // Setting measurement export states
        measRef.setExportGlobal(lRef.isExportGlobal());
        measRef.setExportIndividual(lRef.isExportIndividual());
        measRef.setExportMax(lRef.isExportMax());
        measRef.setExportMean(lRef.isExportMean());
        measRef.setExportMin(lRef.isExportMin());
        measRef.setExportStd(lRef.isExportStd());
        measRef.setExportSum(lRef.isExportSum());

    }
}

class LegacySummaryRef extends SummaryRef {
    public LegacySummaryRef(Node node) {
        super(node);
    }

    @Override
    public String getDescription() {
        // This doesn't need implementing
        return null;
    }
}