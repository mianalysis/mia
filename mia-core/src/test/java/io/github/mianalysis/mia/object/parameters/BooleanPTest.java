package io.github.mianalysis.mia.object.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModulesI;

public class BooleanPTest {

    @Test
    public void testDuplicate() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,true);
        BooleanP duplicated = booleanP.duplicate(paramTest);

        assertEquals("TestBoo",duplicated.getName());
        assertEquals(paramTest,duplicated.getModule());
        assertTrue((Boolean) duplicated.getValue(null));

    }

    @Test
    public void testFlipBoolean() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,true);
        assertTrue((Boolean) booleanP.getValue(null));

        booleanP.flipBoolean();
        assertFalse((Boolean) booleanP.getValue(null));

        booleanP.flipBoolean();
        assertTrue((Boolean) booleanP.getValue(null));

    }

    @Test
    public void testGetRawStringValueTrue() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,true);

        assertEquals("true",booleanP.getRawStringValue());

    }

    @Test
    public void testGetRawStringValueFalse() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,false);

        assertEquals("false",booleanP.getRawStringValue());

    }

    @Test
    public void testSetValueFromStringTrue() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,false);
        assertFalse((Boolean) booleanP.getValue(null));

        booleanP.setValueFromString("true");
        assertTrue((Boolean) booleanP.getValue(null));

    }

    @Test
    public void testSetValueFromStringFalse() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,true);
        assertTrue((Boolean) booleanP.getValue(null));

        booleanP.setValueFromString("false");
        assertFalse((Boolean) booleanP.getValue(null));

    }

    @Test
    public void testVerifyTrue() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,true);

        assertTrue(booleanP.verify());

    }

    @Test
    public void testVerifyFalse() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,false);

        assertTrue(booleanP.verify());

    }

    @Test
    public void testAppendXMLAttributes() throws ParserConfigurationException {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,true);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        booleanP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(5,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("TestBoo",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("TestBoo",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals("true",namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("false",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void testSetAttributesFromXML() throws ParserConfigurationException {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        BooleanP booleanP = new BooleanP("TestBoo",paramTest,true);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","false");
        element.setAttribute("VISIBLE","false");

        booleanP.setAttributesFromXML(element);

        assertEquals("TestBoo",booleanP.getName());
        assertEquals("New nick",booleanP.getNickname());
        assertFalse((Boolean) booleanP.getValue(null));
        assertFalse((Boolean) booleanP.isVisible());

    }
}