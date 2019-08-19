package wbif.sjx.MIA.Object.Parameters;

import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Module.ModuleCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FolderPathPTest {
    @Test
    public void isDirectoryFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        assertFalse(folderPathP.isDirectory());

    }

    /**
     * There's nothing to stop a file being defined in the path.  The main difference between this class and the folder
     * or file and folder classes is the parameter verification and GUI control object.
     * @throws IOException
     */
    @Test
    public void isDirectoryDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        assertTrue(folderPathP.isDirectory());

    }

    @Test
    public void isDirectoryMissing() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath("");

        assertFalse(folderPathP.isDirectory());

    }

    @Test
    public void isDirectoryNull() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(null);

        assertFalse(folderPathP.isDirectory());

    }

    @Test
    public void setPathFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),folderPathP.getPath());

    }

    @Test
    public void setPathDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        assertEquals(temporaryFolder.getRoot().getAbsolutePath(),folderPathP.getPath());

    }

    @Test
    public void getRawStringValueFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void getRawStringValueDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        assertEquals(temporaryFolder.getRoot().getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setValueFromString(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setValueFromString(temporaryFolder.getRoot().getAbsolutePath());

        assertEquals(temporaryFolder.getRoot().getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void verifyFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        assertFalse(folderPathP.verify());

    }

    @Test
    public void verifyDirectory() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getRoot().getAbsolutePath());

        assertTrue(folderPathP.verify());

    }

    @Test
    public void verifyMissingFile() throws IOException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        // Now, remove the file, so it's missing when tested.
        assertTrue(testFile.delete());
        assertFalse(folderPathP.verify());

    }

    @Test
    public void verifyMissing() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath("");

        assertFalse(folderPathP.verify());

    }

    @Test
    public void verifyNull() throws IOException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(null);

        assertFalse(folderPathP.verify());

    }

    @Test
    public void appendXMLAttributes() throws IOException, ParserConfigurationException {
        // Create a temporary folder and tell the workspace that's where the input was (even though it wasn't)
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        File testFile = temporaryFolder.newFile("TestFile.tif");

        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        folderPathP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(4,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("Demo file",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("Demo file",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals(testFile.getAbsolutePath(),namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("false",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void setAttributesFromXML() throws ParserConfigurationException {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","C:\\Users\\Stephen\\myfile.tif");
        element.setAttribute("VISIBLE","false");

        folderPathP.setAttributesFromXML(element);

        assertEquals("Demo file",folderPathP.getName());
        assertEquals("New nick",folderPathP.getNickname());
        assertEquals("C:\\Users\\Stephen\\myfile.tif",folderPathP.getValue());
        assertFalse(folderPathP.isVisible());

    }
}