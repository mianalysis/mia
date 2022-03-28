package io.github.mianalysis.mia.process.analysishandling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.Refs;

/**
 * Created by Stephen on 22/06/2018.
 */
public class AnalysisWriter {
    public static void saveAnalysisAs(Analysis analysis, String outputFileName)
            throws IOException, ParserConfigurationException, TransformerException {
        if (outputFileName == null || outputFileName.equals("")) {
            saveAnalysis(analysis);
            return;
        }

        // Updating the analysis filename
        analysis.setAnalysisFilename(new File(outputFileName).getAbsolutePath());

        // Creating the document to save
        Document doc = prepareAnalysisDocument(analysis);

        // Preparing the target file for
        FileOutputStream outputStream = new FileOutputStream(outputFileName);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
        outputStream.close();

        MIA.log.writeStatus("File saved (" + FilenameUtils.getName(outputFileName) + ")");

    }

    public static void saveAnalysis(Analysis analysis)
            throws IOException, ParserConfigurationException, TransformerException {
        String previousPath = analysis.getAnalysisFilename();
        if (previousPath.equals(""))
            previousPath = Prefs.get("MIA.PreviousPath", "");

        JFileChooser fileChooser = new JFileChooser(previousPath);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.showDialog(null, "Save workflow");

        File file = fileChooser.getSelectedFile();
        // If no file was selected quit the method
        if (file == null)
            return;

        Prefs.set("MIA.PreviousPath", file.getAbsolutePath());

        // Updating the analysis filename
        String outputFileName = file.getAbsolutePath();
        analysis.setAnalysisFilename(outputFileName);

        if (!FilenameUtils.getExtension(outputFileName).equals("mia")) {
            outputFileName = FilenameUtils.removeExtension(outputFileName) + ".mia";
        }
        saveAnalysisAs(analysis, outputFileName);

    }

    public static Document prepareAnalysisDocument(Analysis analysis) throws ParserConfigurationException {
        // Adding an XML formatted summary of the modules and their values
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("ROOT");

        // Adding MIA version number as an attribute
        Attr version = doc.createAttribute("MIA_VERSION");
        version.appendChild(doc.createTextNode(MIA.getVersion()));
        root.setAttributeNode(version);

        // Creating a module collection holding the single-instance modules (input,
        // output and global variables)
        Modules singleModules = new Modules();
        singleModules.add(analysis.getModules().getInputControl());
        singleModules.add(analysis.getModules().getOutputControl());

        // Adding module elements
        root.appendChild(prepareModulesXML(doc, singleModules));
        root.appendChild(prepareModulesXML(doc, analysis.getModules()));
        doc.appendChild(root);

        return doc;

    }

    public static Element prepareModulesXML(Document doc, Modules modules) {
        Element modulesElement = doc.createElement("MODULES");

        // Running through each parameter set (one for each module)
        for (Module module : modules) {
            Element moduleElement = doc.createElement("MODULE");

            // Adding module details
            module.appendXMLAttributes(moduleElement);

            // Adding parameters from this module
            Element paramElement = doc.createElement("PARAMETERS");
            Parameters paraRefs = module.getAllParameters();
            paramElement = prepareRefsXML(doc, paramElement, paraRefs, "PARAMETER");
            moduleElement.appendChild(paramElement);

            // Adding measurement references from this module
            Element imageMeasurementsElement = doc.createElement("IMAGE_MEASUREMENTS");
            ImageMeasurementRefs imageReferences = module.updateAndGetImageMeasurementRefs();
            imageMeasurementsElement = prepareRefsXML(doc, imageMeasurementsElement, imageReferences, "MEASUREMENT");
            moduleElement.appendChild(imageMeasurementsElement);

            Element objectMeasurementsElement = doc.createElement("OBJECT_MEASUREMENTS");
            ObjMeasurementRefs objectReferences = module.updateAndGetObjectMeasurementRefs();
            objectMeasurementsElement = prepareRefsXML(doc, objectMeasurementsElement, objectReferences, "MEASUREMENT");
            moduleElement.appendChild(objectMeasurementsElement);

            // Adding metadata references from this module
            Element metadataElement = doc.createElement("METADATA");
            MetadataRefs metadataRefs = module.updateAndGetMetadataReferences();
            metadataElement = prepareRefsXML(doc, metadataElement, metadataRefs, "METADATUM");
            moduleElement.appendChild(metadataElement);

            // Adding current module to modules
            modulesElement.appendChild(moduleElement);

        }

        return modulesElement;

    }

    public static Element prepareRefsXML(Document doc, Element refsElement, Refs<? extends Ref> refs,
            String groupName) {
        if (refs == null)
            return refsElement;

        for (Ref ref : refs.values()) {
            if (ref instanceof Parameter) {
                if (!((Parameter) ref).isExported())
                    continue;
            }

            Element element = doc.createElement(groupName);
            ref.appendXMLAttributes(element);
            refsElement.appendChild(element);

        }

        return refsElement;

    }
}
