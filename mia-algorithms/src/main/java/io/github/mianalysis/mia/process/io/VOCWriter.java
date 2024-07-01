package io.github.mianalysis.mia.process.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ij.ImagePlus;

public class VOCWriter {
    private final Document document;
    private final Element root;

    public VOCWriter() throws ParserConfigurationException {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        root = document.createElement("annotation");
        document.appendChild(root);

    }

    public void addImagePath(String path) {
        File file = new File(path);

        addSimpleTextElement(root, "folder", file.getParentFile().getName());
        addSimpleTextElement(root, "filename", file.getName());
        addSimpleTextElement(root, "path", path);
        
        Element sourceElement = document.createElement("source");
        root.appendChild(sourceElement);

        addSimpleTextElement(sourceElement, "database", "Unknown");

    }

    public void addImageSize(ImagePlus ipl) {
        Element sizeElement = document.createElement("size");
        root.appendChild(sizeElement);

        addSimpleTextElement(sizeElement, "width", String.valueOf(ipl.getWidth()));
        addSimpleTextElement(sizeElement, "height", String.valueOf(ipl.getHeight()));
        addSimpleTextElement(sizeElement, "depth", String.valueOf(ipl.getStackSize()));

    }

    public void addOther(String name, String value) {
        addSimpleTextElement(root, name, value);

    }

    public void addObject(String name, String pose, boolean truncated, boolean difficult, int[][] boundingBox) {
        Element objectElement = document.createElement("object");
        root.appendChild(objectElement);

        addSimpleTextElement(objectElement, "name", name);
        addSimpleTextElement(objectElement, "pose", pose);
        addSimpleTextElement(objectElement, "truncated", truncated ? "1" : "0");
        addSimpleTextElement(objectElement, "difficult", difficult ? "1" : "0");

        Element bndboxElement = document.createElement("bndbox");
        objectElement.appendChild(bndboxElement);

        addSimpleTextElement(bndboxElement, "xmin", String.valueOf(boundingBox[0][0]));
        addSimpleTextElement(bndboxElement, "ymin", String.valueOf(boundingBox[1][0]));
        addSimpleTextElement(bndboxElement, "xmax", String.valueOf(boundingBox[0][1]));
        addSimpleTextElement(bndboxElement, "ymax", String.valueOf(boundingBox[1][1]));

    }
    

    public void write(String outputPath) throws TransformerException, IOException {
        FileOutputStream outputStream = new FileOutputStream(outputPath);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        outputStream.close();

    }

    Element addSimpleTextElement(Element parentElement, String name, String value) {
        Element newElement = document.createElement(name);
        newElement.setTextContent(value);
        parentElement.appendChild(newElement);

        return newElement;

    }

}
