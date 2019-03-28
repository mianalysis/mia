package wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Process.Exporter;

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
 * Created by sc13967 on 22/06/2018.
 */
public class AnalysisWriter {
    public static void saveAnalysis(Analysis analysis, String outputFileName) throws IOException, ParserConfigurationException, TransformerException {
        // Updating the analysis filename
        analysis.setAnalysisFilename(new File(outputFileName).getAbsolutePath());

        // Creating a module collection holding the input and output
        ModuleCollection inOutModules = new ModuleCollection();
        inOutModules.add(analysis.getInputControl());
        inOutModules.add(analysis.getOutputControl());

        // Adding an XML formatted summary of the modules and their values
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("ROOT");

        // Adding MIA version number as an attribute
        Attr version = doc.createAttribute("MIA_VERSION");
        version.appendChild(doc.createTextNode(MIA.getVersion()));
        root.setAttributeNode(version);

        root.appendChild(Exporter.prepareModulesXML(doc,inOutModules));
        root.appendChild(Exporter.prepareModulesXML(doc,analysis.getModules()));
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
}
