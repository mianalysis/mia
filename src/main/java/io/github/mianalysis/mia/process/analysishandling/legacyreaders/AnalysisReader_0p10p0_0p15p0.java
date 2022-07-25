package io.github.mianalysis.mia.process.analysishandling.legacyreaders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.ChildObjectCount;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.ObjectTimepoint;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.ParentObjectID;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.PartnerObjectCount;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.PartnerRef;
import io.github.mianalysis.mia.object.refs.abstrakt.SummaryRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.process.analysishandling.Analysis;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisReader_0p10p0_0p15p0 {
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

        Analysis analysis = new Analysis();
        Modules modules = loadModules(doc);
        analysis.setModules(modules);

        return analysis;

    }

    public static Modules loadModules(Document doc)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Modules modules = new Modules();
        ArrayList<Node> relationshipsToCovert = new ArrayList<>();

        // Creating a list of all available modules (rather than reading their full
        // path, in case they move) using
        // Reflections tool
        List<String> availableModuleNames = AvailableModules.getModuleNames(false);

        NodeList moduleNodes = doc.getElementsByTagName("MODULE");
        for (int i = 0; i < moduleNodes.getLength(); i++) {
            Node moduleNode = moduleNodes.item(i);

            // Creating an empty Module matching the input type. If none was found the loop
            // skips to the next Module
            Module module = initialiseModule(moduleNode, modules, availableModuleNames, relationshipsToCovert);
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

        // Adding relationships
        convertRelationshipRefs(modules, relationshipsToCovert);

        // Adding timepoint measurements for all objects
        addTimepointMeasurements(modules);

        return modules;

    }

    public static Module initialiseModule(Node moduleNode, Modules modules, List<String> availableModuleNames,
            ArrayList<Node> relationshipsToCovert)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        NamedNodeMap moduleAttributes = moduleNode.getAttributes();
        String className = moduleAttributes.getNamedItem("CLASSNAME").getNodeValue();
        String moduleName = FilenameUtils.getExtension(className);

        // Checking if this module has been reassigned
        moduleName = MIA.lostAndFound.findModule(moduleName);

        // Trying to load from available modules
        for (String availableModuleName : availableModuleNames) {
            if (moduleName.equals(FilenameUtils.getExtension(availableModuleName))) {
                return initialiseModule(moduleNode, modules, availableModuleName, relationshipsToCovert);
            }
        }

        // If no module was found matching that name an error message is displayed
        MIA.log.writeWarning("Module \"" + moduleName + "\" not found (skipping)");

        return null;

    }

    public static Module initialiseModule(Node moduleNode, Modules modules, String availableModuleName,
            ArrayList<Node> relationshipsToCovert)
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

                case "RELATIONSHIPS":
                case "PARENT_CHILD":
                    relationshipsToCovert.add(moduleChildNodes.item(i));
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

    public static void convertRelationshipRefs(Modules modules, ArrayList<Node> relationshipsToCovert) {
        Module separator = null;
        int nAdded = 0;

        // Storing ParentChildRefs and PartnerRefs to prevent duplicate entries
        RefPairCollection parentChildRefs = new RefPairCollection();
        RefPairCollection partnerRefs = new RefPairCollection();

        // Getting final list of available objects
        LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(null);
        HashSet<String> availableObjectNames = new HashSet<>();
        for (OutputObjectsP availableObject : availableObjects)
            availableObjectNames.add(availableObject.getValue(null));

        for (Node moduleNode : relationshipsToCovert) {
            NodeList referenceNodes = moduleNode.getChildNodes();

            // Iterating over all references of this type
            for (int i = 0; i < referenceNodes.getLength(); i++) {
                Node node = referenceNodes.item(i);

                // ParentChildRef and PartnerRef no longer supports exporting, so have to use
                // new,
                // LegacySummaryRef to get export options
                LegacySummaryRef lRef = new LegacySummaryRef(node);

                // If this reference exports anything, new ObjectCount and ParentID measurement
                // modules must be added at the end of the analysis
                if (!lRef.isExportGlobal() && !lRef.isExportIndividual() && !lRef.isExportMax() && !lRef.isExportMean()
                        && !lRef.isExportMin() && !lRef.isExportStd() && !lRef.isExportSum())
                    continue;

                // Adding a GUI separator. If no relationships are added, this will be removed
                if (separator == null)
                    separator = addRefSeparatorModule(modules);

                switch (node.getNodeName()) {
                    case "RELATIONSHIP":
                    case "PARENT_CHILD":
                        // Getting relationship properties and modules
                        ParentChildRef pcRef = new ParentChildRef(node);
                        String parentName = pcRef.getParentName();
                        String childName = pcRef.getChildName();

                        // Checking if this pair has already been added
                        if (parentChildRefs.contains(parentName, childName))
                            continue;
                        else
                            parentChildRefs.addPair(parentName, childName);

                        // Checking objects still exist (i.e. haven't been removed)
                        if (!availableObjectNames.contains(parentName))
                            continue;

                        if (!availableObjectNames.contains(childName))
                            continue;

                        addChildCountModule(modules, lRef, parentName, childName);
                        addParentIDModule(modules, lRef, parentName, childName);

                        nAdded += 2;

                        break;

                    case "PARTNER":
                        // Getting relationship properties and module
                        PartnerRef pRef = new PartnerRef(node);
                        String object1Name = pRef.getObject1Name();
                        String object2Name = pRef.getObject2Name();

                        // Checking if this pair has already been added
                        if (partnerRefs.contains(object1Name, object2Name))
                            continue;
                        else
                            partnerRefs.addPair(object1Name, object2Name);

                        // Checking objects still exist (i.e. haven't been removed)
                        if (!availableObjectNames.contains(object1Name))
                            continue;

                        if (!availableObjectNames.contains(object2Name))
                            continue;

                        addPartnerCountModule(modules, lRef, object1Name, object2Name);
                        addPartnerCountModule(modules, lRef, object2Name, object1Name);

                        nAdded += 2;

                        break;
                }

                // Display a message if any relationship modules were added, otherwise remove
                // the redundant separator
                if (nAdded > 0)
                    MIA.log.writeMessage(
                            "Pre-v0.15.0 analysis loaded.  Analysis has been automatically updated to store exported child/partner counts and parent IDs as measurements.");
                else
                    modules.remove(separator);

            }
        }
    }

    public static void addTimepointMeasurements(Modules modules) {
        LinkedHashSet<OutputObjectsP> availableObjects = modules.getAvailableObjects(null);

        if (availableObjects.size() == 0)
            return;

        GUISeparator guiSeparator = new GUISeparator(modules);
        modules.add(guiSeparator);

        guiSeparator.updateParameterValue(GUISeparator.SHOW_PROCESSING, false);
        guiSeparator.setNickname("[AUTOGEN] Timepoint measurements");
        guiSeparator.setNotes(
                "The following modules were automatically added to aid compatibility with MIA v0.18.0 and above.  Object timepoints are now exported as measurements.");

        for (OutputObjectsP availableObject : availableObjects) {
            // Creating the object count module
            ObjectTimepoint timepointModule = new ObjectTimepoint(modules);
            modules.add(timepointModule);
            timepointModule.updateParameterValue(PartnerObjectCount.INPUT_OBJECTS, availableObject.getObjectsName());

            // Getting relevant measurement
            ObjMeasurementRefs measRefs = timepointModule.updateAndGetObjectMeasurementRefs();
            ObjMeasurementRef measRef = measRefs.get("TIMEPOINT");

            // Setting measurement export states
            measRef.setExportGlobal(true);
            measRef.setExportIndividual(true);
            measRef.setExportMax(false);
            measRef.setExportMean(false);
            measRef.setExportMin(false);
            measRef.setExportStd(false);
            measRef.setExportSum(false);

        }
    }

    static GUISeparator addRefSeparatorModule(Modules modules) {
        GUISeparator guiSeparator = new GUISeparator(modules);
        modules.add(guiSeparator);

        guiSeparator.updateParameterValue(GUISeparator.SHOW_PROCESSING, false);
        guiSeparator.setNickname("[AUTOGEN] Object relationships");
        guiSeparator.setNotes(
                "The following modules were automatically added to aid compatibility with MIA v0.15.0 and above.  Child object counts, parent IDs and partner object counts are now stored as measurements.  The following modules add the same data exporting as present in the original analysis.  Note: Spreadsheet column headers may have changed.");

        return guiSeparator;

    }

    static void addChildCountModule(Modules modules, LegacySummaryRef lRef, String parentName,
            String childName) {
        // Creating the object count module
        ChildObjectCount countModule = new ChildObjectCount(modules);
        modules.add(countModule);
        countModule.updateParameterValue(ChildObjectCount.INPUT_OBJECTS, parentName);
        countModule.updateParameterValue(ChildObjectCount.CHILD_OBJECTS, childName);

        // Getting relevant measurement
        String measurementName = ChildObjectCount.getFullName(childName);
        ObjMeasurementRefs measRefs = countModule.updateAndGetObjectMeasurementRefs();
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

    static void addParentIDModule(Modules modules, LegacySummaryRef lRef, String parentName,
            String childName) {
        // Creating the object count module
        ParentObjectID idModule = new ParentObjectID(modules);
        modules.add(idModule);
        idModule.updateParameterValue(ParentObjectID.INPUT_OBJECTS, childName);
        idModule.updateParameterValue(ParentObjectID.PARENT_OBJECT, parentName);

        // There's no need to set export states here, as this measurement should already
        // be set to only appear as an individual measurement with no statistics.

    }

    static void addPartnerCountModule(Modules modules, LegacySummaryRef lRef, String object1Name,
            String object2Name) {
        // Creating the object count module
        PartnerObjectCount countModule = new PartnerObjectCount(modules);
        modules.add(countModule);
        countModule.updateParameterValue(PartnerObjectCount.INPUT_OBJECTS, object1Name);
        countModule.updateParameterValue(PartnerObjectCount.PARTNER_OBJECTS, object2Name);

        // Getting relevant measurement
        String measurementName = PartnerObjectCount.getFullName(object2Name);
        ObjMeasurementRefs measRefs = countModule.updateAndGetObjectMeasurementRefs();
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

    static class LegacySummaryRef extends SummaryRef {
        LegacySummaryRef(Node node) {
            super(node);
            super.setAttributesFromXML(node);
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