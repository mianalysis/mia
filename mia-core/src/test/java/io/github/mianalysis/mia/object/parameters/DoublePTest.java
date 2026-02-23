package io.github.mianalysis.mia.object.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;

public class DoublePTest {
    private static double tolerance = 1E-10;

    @Test
    public void getRawStringValueProvidedInteger() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,2);

        assertEquals("2.0",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvided4DP() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,3.142);

        assertEquals("3.142",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvided10DP() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,1.2345678901);

        assertEquals("1.2345678901",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvidedScientific() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,6.46E-42);

        assertEquals("6.46E-42",doubleP.getRawStringValue());

    }

    @Test
    public void getRawStringValueProvidedBlank() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,"");

        assertEquals("",doubleP.getRawStringValue());

    }

    @Test
    public void getValueProvidedInteger() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,2);

        assertEquals(2,doubleP.getValue(null),tolerance);

    }

    @Test
    public void getValueProvided4dp() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,3.142);

        assertEquals(3.142,doubleP.getValue(null),tolerance);

    }

    @Test
    public void getValueProvided10DP() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,1.2345678901);

        assertEquals(1.2345678901,doubleP.getValue(null),tolerance);

    }

    @Test
    public void getValueProvidedScientific() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,6.46E-42);

        assertEquals(6.46E-42,doubleP.getValue(null),tolerance);

    }

    @Test
    public void getValueProvidedBlank() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,"");

        // The following should throw the NumberFormatException
        assertThrows(NumberFormatException.class,() -> doubleP.getValue(null));

    }

    @Test
    public void duplicate() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,42.54);
        DoubleP duplicated = doubleP.duplicate(paramTest);

        assertEquals("Test val",duplicated.getName());
        assertEquals(paramTest,duplicated.getModule());
        assertFalse(duplicated.isVisible());
        assertEquals(42.54,duplicated.getValue(null),tolerance);

    }

    @Test
    public void setValueFromStringProvidedInteger() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,"");
        doubleP.setValueFromString("2");

        assertEquals(2,doubleP.getValue(null),tolerance);

    }

    @Test
    public void setValueFromStringProvided4dp() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,"");
        doubleP.setValueFromString("3.142");

        assertEquals(3.142,doubleP.getValue(null),tolerance);

    }

    @Test
    public void setValueFromStringProvided10DP() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,"");
        doubleP.setValueFromString("1.2345678901");

        assertEquals(1.2345678901,doubleP.getValue(null),tolerance);

    }

    @Test
    public void setValueFromStringProvidedScientific() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,"");
        doubleP.setValueFromString("6.46E-42");

        assertEquals(6.46E-42,doubleP.getValue(null),tolerance);

    }

    @Test
    public void setValueFromStringProvidedBlank() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,12);
        doubleP.setValueFromString("");

        // The following should throw the NumberFormatException
        assertThrows(NumberFormatException.class, () -> doubleP.getValue(null));        

    }

    @Test
    public void verifyDouble() {
        // It shouldn't be possible to get a false value here.
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,-23.5423423);

        assertTrue(doubleP.verify());

    }

    @Test
    public void appendXMLAttributes() throws ParserConfigurationException {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,-23.5423423);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        doubleP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(4,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("Test val",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("Test val",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals("-23.5423423",namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("false",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void setAttributesFromXML() throws ParserConfigurationException {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        DoubleP doubleP = new DoubleP("Test val",paramTest,-23.5423423);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","-3.142E4");
        element.setAttribute("VISIBLE","false");

        doubleP.setAttributesFromXML(element);

        assertEquals("Test val",doubleP.getName());
        assertEquals("New nick",doubleP.getNickname());
        assertEquals(-3.142E4,doubleP.getValue(null),tolerance);
        assertFalse(doubleP.isVisible());

    }
}