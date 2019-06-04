package wbif.sjx.MIA.Process.AnalysisHandling;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;

/**
 * Created by Stephen on 22/06/2018.
 */
public class AnalysisWriter {
    public static void saveAnalysis(Analysis analysis, String outputFileName) throws IOException, ParserConfigurationException, TransformerException {
        // Updating the analysis filename
        analysis.setAnalysisFilename(new File(outputFileName).getAbsolutePath());

        // Creating a module collection holding the single-instance modules (input, output and global variables)
        ModuleCollection singleModules = new ModuleCollection();
        singleModules.add(MIA.getGlobalVariables());
        singleModules.add(analysis.getModules().getInputControl());
        singleModules.add(analysis.getModules().getOutputControl());

        // Adding an XML formatted summary of the modules and their values
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("ROOT");

        // Adding MIA version number as an attribute
        Attr version = doc.createAttribute("MIA_VERSION");
        version.appendChild(doc.createTextNode(MIA.getVersion()));
        root.setAttributeNode(version);

        root.appendChild(prepareModulesXML(doc,singleModules));
        root.appendChild(prepareModulesXML(doc,analysis.getModules()));
        doc.appendChild(root);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Preparing the target file for
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new FileOutputStream(outputFileName));
        transformer.transform(source, result);

        System.out.println("File saved ("+ FilenameUtils.getName(outputFileName)+")");

    }

    public static void saveAnalysis(Analysis analysis) throws IOException, ParserConfigurationException, TransformerException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile(analysis.getAnalysisFilename());
        fileDialog.setVisible(true);

        // If no file was selected quit the method
        if (fileDialog.getFiles().length==0) return;

        // Updating the analysis filename
        String outputFileName = fileDialog.getFiles()[0].getAbsolutePath();
        analysis.setAnalysisFilename(outputFileName);

        if (!FilenameUtils.getExtension(outputFileName).equals("mia")) {
            outputFileName = FilenameUtils.removeExtension(outputFileName)+".mia";
        }
        saveAnalysis(analysis,outputFileName);

    }

    public static Element prepareModulesXML(Document doc, ModuleCollection modules) {
        Element modulesElement =  doc.createElement("MODULES");

        // Running through each parameter set (one for each module
        for (Module module:modules) {
            Element moduleElement =  doc.createElement("MODULE");
            Attr nameAttr = doc.createAttribute("NAME");
            nameAttr.appendChild(doc.createTextNode(module.getClass().getName()));
            moduleElement.setAttributeNode(nameAttr);

            Attr nicknameAttr = doc.createAttribute("NICKNAME");
            nicknameAttr.appendChild(doc.createTextNode(module.getNickname()));
            moduleElement.setAttributeNode(nicknameAttr);

            Attr enabledAttr = doc.createAttribute("ENABLED");
            enabledAttr.appendChild(doc.createTextNode(String.valueOf(module.isEnabled())));
            moduleElement.setAttributeNode(enabledAttr);

            Attr disableableAttr = doc.createAttribute("DISABLEABLE");
            disableableAttr.appendChild(doc.createTextNode(String.valueOf(module.canBeDisabled())));
            moduleElement.setAttributeNode(disableableAttr);

            Attr outputAttr = doc.createAttribute("SHOW_OUTPUT");
            outputAttr.appendChild(doc.createTextNode(String.valueOf(module.canShowOutput())));
            moduleElement.setAttributeNode(outputAttr);

            Attr notesAttr = doc.createAttribute("NOTES");
            notesAttr.appendChild(doc.createTextNode(module.getNotes()));
            moduleElement.setAttributeNode(notesAttr);

            Element parametersElement = prepareParametersXML(doc,module.getAllParameters());
            moduleElement.appendChild(parametersElement);

            // Adding measurement references from this module
            Element imageMeasurementsElement = doc.createElement("IMAGE_MEASUREMENTS");
            ImageMeasurementRefCollection imageReferences = module.updateAndGetImageMeasurementRefs();
            imageMeasurementsElement = prepareRefsXML(doc, imageMeasurementsElement,imageReferences,"MEASUREMENT");
            moduleElement.appendChild(imageMeasurementsElement);

            Element objectMeasurementsElement = doc.createElement("OBJECT_MEASUREMENTS");
            ObjMeasurementRefCollection objectReferences = module.updateAndGetObjectMeasurementRefs();
            objectMeasurementsElement = prepareRefsXML(doc, objectMeasurementsElement,objectReferences,"MEASUREMENT");
            moduleElement.appendChild(objectMeasurementsElement);

            // Adding metadata references from this module
            Element metadataElement = doc.createElement("METADATA");
            MetadataRefCollection metadataRefs = module.updateAndGetMetadataReferences();
            metadataElement = prepareRefsXML(doc, metadataElement,metadataRefs,"METADATUM");
            moduleElement.appendChild(metadataElement);

            // Adding relationship references from this module
            Element relationshipElement = doc.createElement("RELATIONSHIPS");
            RelationshipRefCollection relationshipRefs = module.updateAndGetRelationships();
            relationshipElement = prepareRefsXML(doc, relationshipElement,relationshipRefs,"RELATIONSHIP");
            moduleElement.appendChild(relationshipElement);

            // Adding current module to modules
            modulesElement.appendChild(moduleElement);

        }

        return modulesElement;

    }

    public static Element prepareParametersXML(Document doc, ParameterCollection parameters) {
        // Adding parameters from this module
        Element parametersElement = doc.createElement("PARAMETERS");

        for (Parameter currParam:parameters) {
            // Check if the parameter is to be exported
            if (!currParam.isExported()) continue;

            // ParameterGroups are treated differently
            if (currParam.getClass() == ParameterGroup.class) {
                LinkedHashSet<ParameterCollection> collections = ((ParameterGroup) currParam).getCollections();
                Element collectionsElement = doc.createElement("COLLECTIONS");

                Attr nameAttr = doc.createAttribute("NAME");
                nameAttr.appendChild(doc.createTextNode(currParam.getNameAsString()));
                collectionsElement.setAttributeNode(nameAttr);

                for (ParameterCollection collection:collections) {
                    Element collectionElement = doc.createElement("COLLECTION");

                    collectionElement.appendChild(prepareParametersXML(doc,collection));
                    collectionsElement.appendChild(collectionElement);
                }
                parametersElement.appendChild(collectionsElement);
                continue;
            } else if (currParam.getClass() == RemoveParameters.class) continue;

            // Adding the name and value of the current parameter
            Element parameterElement =  doc.createElement("PARAMETER");

            Attr nameAttr = doc.createAttribute("NAME");
            nameAttr.appendChild(doc.createTextNode(currParam.getNameAsString()));
            parameterElement.setAttributeNode(nameAttr);

            Attr valueAttr = doc.createAttribute("VALUE");
            if (currParam.getRawStringValue() == null) {
                valueAttr.appendChild(doc.createTextNode(""));
            } else {
                valueAttr.appendChild(doc.createTextNode(currParam.getRawStringValue()));
            }
            parameterElement.setAttributeNode(valueAttr);

            Attr visibleAttr = doc.createAttribute("VISIBLE");
            visibleAttr.appendChild(doc.createTextNode(Boolean.toString(currParam.isVisible())));
            parameterElement.setAttributeNode(visibleAttr);

            if (currParam.getClass().isInstance(ChildObjectsP.class) && ((ChildObjectsP) currParam).getParentObjectsName() != null) {
                Attr valueSourceAttr = doc.createAttribute("VALUESOURCE");
                valueSourceAttr.appendChild(doc.createTextNode(((ChildObjectsP) currParam).getParentObjectsName()));
                parameterElement.setAttributeNode(valueSourceAttr);
            }

            if (currParam.getClass().isInstance(ParentObjectsP.class) && ((ParentObjectsP) currParam).getChildObjectsName() != null) {
                Attr valueSourceAttr = doc.createAttribute("VALUESOURCE");
                valueSourceAttr.appendChild(doc.createTextNode(((ParentObjectsP) currParam).getChildObjectsName()));
                parameterElement.setAttributeNode(valueSourceAttr);
            }

            parametersElement.appendChild(parameterElement);

        }

        return parametersElement;

    }

    public static Element prepareRefsXML(Document doc, Element refsElement, RefCollection<? extends ExportableRef> refs, String groupName) {
        if (refs == null) return refsElement;

        for (ExportableRef ref:refs.values()) {
            Element element = doc.createElement(groupName);
            ref.appendXMLAttributes(element);
            refsElement.appendChild(element);

        }

        return refsElement;

    }
}
