package wbif.sjx.MIA.Object.Parameters;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileFolderPathPTest {

    @Test
    public void isDirectoryFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        assertFalse(fileFolderPathP.isDirectory());

    }

    @Test
    public void isDirectoryDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        assertTrue(fileFolderPathP.isDirectory());

    }

    @Test
    public void isDirectoryMissing() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath("");

        assertFalse(fileFolderPathP.isDirectory());

    }

    @Test
    public void isDirectoryNull() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(null);

        assertFalse(fileFolderPathP.isDirectory());

    }

    @Test
    public void getRawStringValueFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void getRawStringValueDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        assertEquals(temporaryFolder.getRoot().getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setValueFromString(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setValueFromString(temporaryFolder.getRoot().getAbsolutePath());

        assertEquals(temporaryFolder.getRoot().getAbsolutePath(),fileFolderPathP.getRawStringValue());

    }

    @Test
    public void verifyFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        assertTrue(fileFolderPathP.verify());

    }

    @Test
    public void verifyDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        assertTrue(fileFolderPathP.verify());

    }

    @Test
    public void verifyMissingFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        // Now, remove the file, so it's missing when tested.
        assertTrue(testFile.delete());
        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void verifyMissingDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        // Now, remove the file, so it's missing when tested.
        temporaryFolder.delete();
        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void verifyMissing() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath("");

        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void verifyNull() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(null);

        assertFalse(fileFolderPathP.verify());

    }

    @Test
    public void appendXMLAttributes() throws IOException, ParserConfigurationException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);
        fileFolderPathP.setPath(testFile.getAbsolutePath());

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        fileFolderPathP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(4,namedNodeMap.getLength());

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
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FileFolderPathP fileFolderPathP = new FileFolderPathP("Demo path",paramTest);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","C:\\Users\\Stephen\\myfile.tif");
        element.setAttribute("VISIBLE","false");

        fileFolderPathP.setAttributesFromXML(element);

        assertEquals("Demo path",fileFolderPathP.getName());
        assertEquals("New nick",fileFolderPathP.getNickname());
        assertEquals("C:\\Users\\Stephen\\myfile.tif",fileFolderPathP.getValue());
        assertFalse(fileFolderPathP.isVisible());

    }
}