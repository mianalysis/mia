package io.github.mianalysis.mia.object.parameters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import io.github.mianalysis.mia.module.Modules;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FolderPathPTest {
    @Test
    public void isDirectoryFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
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
    public void isDirectoryDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getAbsolutePath());

        assertTrue(folderPathP.isDirectory());

    }

    @Test
    public void isDirectoryMissing() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath("");

        assertFalse(folderPathP.isDirectory());

    }

    @Test
    public void isDirectoryNull() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(null);

        assertFalse(folderPathP.isDirectory());

    }

    @Test
    public void setPathFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),folderPathP.getPath());

    }

    @Test
    public void setPathDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getAbsolutePath());

        assertEquals(temporaryFolder.getAbsolutePath(),folderPathP.getPath());

    }

    @Test
    public void getRawStringValueFile(@TempDir Path tempPath) throws IOException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void getRawStringValueDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getAbsolutePath());

        assertEquals(temporaryFolder.getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringFile(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setValueFromString(testFile.getAbsolutePath());

        assertEquals(testFile.getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void setValueFromStringDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setValueFromString(temporaryFolder.getAbsolutePath());

        assertEquals(temporaryFolder.getAbsolutePath(),folderPathP.getRawStringValue());

    }

    @Test
    public void verifyFile(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        assertFalse(folderPathP.verify());

    }

    @Test
    public void verifyDirectory(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(temporaryFolder.getAbsolutePath());

        assertTrue(folderPathP.verify());

    }

    @Test
    public void verifyMissingFile(@TempDir Path tempPath) throws IOException {
        File temporaryFolder = tempPath.toFile();
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        // Now, remove the file, so it's missing when tested.
        assertTrue(testFile.delete());
        assertFalse(folderPathP.verify());

    }

    @Test
    public void verifyMissing() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath("");

        assertFalse(folderPathP.verify());

    }

    @Test
    public void verifyNull() throws IOException {
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(null);

        assertFalse(folderPathP.verify());

    }

    @Test
    public void appendXMLAttributes(@TempDir Path tempPath) throws IOException, ParserConfigurationException {
        File testFile = new File(tempPath+File.separator+"TestFile.zip");
        testFile.createNewFile();

        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);
        folderPathP.setPath(testFile.getAbsolutePath());

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        folderPathP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(5,namedNodeMap.getLength());

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
        Modules modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        FolderPathP folderPathP = new FolderPathP("Demo file",paramTest);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","C:\\Users\\Stephen\\myfile.zip");
        element.setAttribute("VISIBLE","false");

        folderPathP.setAttributesFromXML(element);

        assertEquals("Demo file",folderPathP.getName());
        assertEquals("New nick",folderPathP.getNickname());
        assertEquals("C:\\Users\\Stephen\\myfile.zip",folderPathP.getValue(null));
        assertFalse(folderPathP.isVisible());

    }
}