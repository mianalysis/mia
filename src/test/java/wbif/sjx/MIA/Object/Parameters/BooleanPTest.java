package wbif.sjx.MIA.Object.Parameters;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.*;

public class BooleanPTest {

    @Test
    public void testDuplicate() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,true);
        BooleanP duplicated = booleanP.duplicate();

        assertEquals("TestBoo",duplicated.getName());
        assertEquals(filterImage,duplicated.getModule());
        assertTrue(duplicated.getValue());

    }

    @Test
    public void testFlipBoolean() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,true);
        assertTrue(booleanP.getValue());

        booleanP.flipBoolean();
        assertFalse(booleanP.getValue());

        booleanP.flipBoolean();
        assertTrue(booleanP.getValue());

    }

    @Test
    public void testGetRawStringValueTrue() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,true);

        assertEquals("true",booleanP.getRawStringValue());

    }

    @Test
    public void testGetRawStringValueFalse() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,false);

        assertEquals("false",booleanP.getRawStringValue());

    }

    @Test
    public void testSetValueFromStringTrue() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,false);
        assertFalse(booleanP.getValue());

        booleanP.setValueFromString("true");
        assertTrue(booleanP.getValue());

    }

    @Test
    public void testSetValueFromStringFalse() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,true);
        assertTrue(booleanP.getValue());

        booleanP.setValueFromString("false");
        assertFalse(booleanP.getValue());

    }

    @Test
    public void testVerifyTrue() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,true);

        assertTrue(booleanP.verify());

    }

    @Test
    public void testVerifyFalse() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,false);

        assertTrue(booleanP.verify());

    }

    @Test
    public void testAppendXMLAttributes() throws ParserConfigurationException {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,true);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        booleanP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(4,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("TestBoo",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("TestBoo",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals("true",namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("true",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void testSetAttributesFromXML() throws ParserConfigurationException {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        BooleanP booleanP = new BooleanP("TestBoo",filterImage,true);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","false");
        element.setAttribute("VISIBLE","false");

        booleanP.setAttributesFromXML(element);

        assertEquals("TestBoo",booleanP.getName());
        assertEquals("New nick",booleanP.getNickname());
        assertFalse(booleanP.getValue());
        assertFalse(booleanP.isVisible());

    }
}