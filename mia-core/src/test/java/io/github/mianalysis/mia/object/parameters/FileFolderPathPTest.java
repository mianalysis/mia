package io.github.mianalysis.mia.object.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import ij.IJ;
import io.github.mianalysis.mia.module.Modules;

public class FileFolderPathPTest {

    @Test
    public void isDirectoryFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        assertFalse(fileFolderPathP.isDirectory());

    }

    @Test
    public void isDirectoryDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getAbsolutePath());

        assertTrue(fileFolderPathP.isDirectory());

    }

    @Test
    public void isDirectoryMissing() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath("");

        assertFalse(fileFolderPathP.isDirectory());

    }

    @Test
    public void isDirectoryNull() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(null);

        assertFalse(fileFolderPathP.isDirectory());

    }

    @Test
    public void getRawStringValueFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void getRawStringValueDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getAbsolutePath());

        assertEquals(temporaryFolder.getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setValueFromString(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setValueFromString(temporaryFolder.getAbsolutePath());

        assertEquals(temporaryFolder.getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void verifyFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        assertTrue(fileFolderPathP.verify());

    }

    @Test
    public void verifyDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getAbsolutePath());

        assertTrue(fileFolderPathP.verify());

    }

    @Test
    public void verifyMissingFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        // Now, remove the file, so it's missing when tested.
        assertTrue(testFile.delete());
        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void verifyMissingDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getAbsolutePath());

        // Now, remove the file, so it's missing when tested.
        temporaryFolder.delete();
        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void verifyMissing() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath("");

        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void verifyNull() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(null);

        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void appendXMLAttributes(@TempDir Path tempPath) throws IOException, ParserConfigurationException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        fileFolderPathP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(5,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("Demo path",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("Demo path",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals(testFile.getAbsolutePath(),namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("false",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void setAttributesFromXML() throws ParserConfigurationException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","C:\\Users\\Stephen\\myfile.zip");
        element.setAttribute("VISIBLE","false");

        fileFolderPathP.setAttributesFromXML(element);

        assertEquals("Demo path",fileFolderPathP.getName());
        assertEquals("New nick",fileFolderPathP.getNickname());
        assertEquals("C:\\Users\\Stephen\\myfile.zip",fileFolderPathP.getValue(null));
        assertFalse(fileFolderPathP.isVisible());

    }
}