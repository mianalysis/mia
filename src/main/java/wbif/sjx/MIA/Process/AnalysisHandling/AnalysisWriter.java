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
import wbif.sjx.MIA.Object.References.Abstract.Ref;
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

/**
 * Created by Stephen on 22/06/2018.
 */
public class AnalysisWriter {
    public static void saveAnalysisAs(Analysis analysis, String outputFileName) throws IOException, ParserConfigurationException, TransformerException {
        if (outputFileName == null || outputFileName.equals("")) {
            saveAnalysis(analysis);
            return;
        }

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
        FileOutputStream outputStream = new FileOutputStream(outputFileName);
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);
        outputStream.close();

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
        saveAnalysisAs(analysis,outputFileName);

    }

    public static Element prepareModulesXML(Document doc, ModuleCollection modules) {
        Element modulesElement =  doc.createElement("MODULES");

        // Running through each parameter set (one for each module
        for (Module module:modules) {
            Element moduleElement =  doc.createElement("MODULE");

            // Adding module details
            module.appendXMLAttributes(moduleElement);

            // Adding parameters from this module
            Element paramElement = doc.createElement("PARAMETERS");
            ParameterCollection paraRefs = module.getAllParameters();
            paramElement = prepareRefsXML(doc, paramElement,paraRefs,"PARAMETER");
            moduleElement.appendChild(paramElement);

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

    public static Element prepareRefsXML(Document doc, Element refsElement, RefCollection<? extends Ref> refs, String groupName) {
        if (refs == null) return refsElement;

        for (Ref ref:refs.values()) {
            if (ref instanceof Parameter) {
                if (!((Parameter) ref).isExported()) continue;
            }

            Element element = doc.createElement(groupName);
            ref.appendXMLAttributes(element);
            refsElement.appendChild(element);

        }

        return refsElement;

    }
}
