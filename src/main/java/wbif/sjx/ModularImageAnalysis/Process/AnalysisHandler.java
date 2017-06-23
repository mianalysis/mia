package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.Object.HCWorkspace;
import wbif.sjx.ModularImageAnalysis.Object.HCWorkspaceCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class AnalysisHandler {
    boolean exportXLSX = true;
    boolean exportXML = false;

    public void saveAnalysis(HCAnalysis analysis) throws IOException, ParserConfigurationException, TransformerException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);

        String outputFileName = fileDialog.getFiles()[0].getAbsolutePath();
        if (!FilenameUtils.getExtension(outputFileName).equals("mia")) {
            outputFileName = FilenameUtils.removeExtension(outputFileName)+".mia";
        }

        // Creating the outputStream
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outputFileName));

        // Adding the analysis object to the output stream
        outputStream.writeObject(analysis);

        // Adding an XML formatted summary of the modules and their values
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(HCExporter.prepareParametersXML(doc,analysis.getModules()));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));

        outputStream.close();

        System.out.println("File saved ("+FilenameUtils.getName(outputFileName)+")");

    }

    public HCAnalysis loadAnalysis() throws IOException, ClassNotFoundException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("*.mia");
        fileDialog.setVisible(true);

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileDialog.getFiles()[0]));

        HCAnalysis analysis = (GUIAnalysis) inputStream.readObject();
        inputStream.close();

        System.out.println("File loaded ("+FilenameUtils.getName(fileDialog.getFiles()[0].getName())+")");

        return analysis;

    }

    public HCWorkspace startAnalysis(HCAnalysis analysis) throws IOException {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);

        String inputFilePath = fileDialog.getFiles()[0].getAbsolutePath();
        String outputFilePath = FilenameUtils.removeExtension(inputFilePath);

        // Initialising the testWorkspace
        HCWorkspaceCollection workspaces = new HCWorkspaceCollection();
        HCWorkspace workspace;
        if (!inputFilePath.equals("")) {
            workspace = workspaces.getNewWorkspace(new File(inputFilePath));

        } else {
            workspace = workspaces.getNewWorkspace(null);

        }

        // Running the analysis
        if (!analysis.execute(workspace, true)) return null;

        // Exporting XLSX
        if (exportXLSX & !outputFilePath.equals("")) {
            HCExporter exporter = new HCExporter(outputFilePath, HCExporter.XLSX_EXPORT);
            exporter.exportResults(workspaces, analysis);

        }

        // Exporting XML
        if (exportXML & !outputFilePath.equals("")) {
            HCExporter exporter = new HCExporter(outputFilePath, HCExporter.XML_EXPORT);
            exporter.exportResults(workspaces, analysis);

        }

        return workspace;

    }
}
