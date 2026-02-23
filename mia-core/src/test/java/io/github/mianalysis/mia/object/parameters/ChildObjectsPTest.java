package io.github.mianalysis.mia.object.parameters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import io.github.mianalysis.mia.module.testmodules.ExtractObjectEdges;
import io.github.mianalysis.mia.module.testmodules.IdentifyObjects;
import io.github.mianalysis.mia.module.testmodules.ImageLoader;
import io.github.mianalysis.mia.module.testmodules.ProjectObjects;

public class ChildObjectsPTest {
    @Test
    public void testDuplicate() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Par_name");
        ChildObjectsP duplicated = childObjectsP.duplicate(paramTest);

        assertEquals("Test param",duplicated.getName());
        assertEquals(paramTest,duplicated.getModule());

    }

    @Test
    public void testGetRawStringValueBlank() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);

        assertEquals("",childObjectsP.getRawStringValue());

    }

    @Test
    public void testGetRawStringValue() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setChoice("Par_name");

        assertEquals("Par_name",childObjectsP.getRawStringValue());

    }

    @Test
    public void testGetRawStringValueNull() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setChoice(null);

        assertEquals("",childObjectsP.getRawStringValue());

    }

    @Test
    public void testGetChoicesWithChoices() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Obj out");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[]{"Flat obj","Obj int"};

        assertEquals(2,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetChoicesWithChoicesMultiGeneration() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Flat obj");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[]{"Flat obj","Flat obj // Obj int"};

        assertEquals(2,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetChoicesNoChoices() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[0];

        assertEquals(0,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetChoicesDisabledModule() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Obj out");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        extractObjectEdges.setEnabled(false);
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[]{"Flat obj"};

        assertEquals(1,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testVerifyPresent() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Obj out");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj");

        assertTrue(childObjectsP.verify());

    }

    @Test
    public void testVerifyPresentMultiGeneration() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Flat obj");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj // Obj int");

        assertTrue(childObjectsP.verify());

    }

    @Test
    public void testVerifyDisabledModule() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        projectObjects.setEnabled(false);
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Obj out");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj");

        assertFalse(childObjectsP.verify());

    }

    @Test
    public void testVerifyNoChildren() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");

        assertFalse(childObjectsP.verify());

    }

    @Test
    public void testVerifyMissingChild() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        projectObjects.setEnabled(false);
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Obj out");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Wrong obj");

        assertFalse(childObjectsP.verify());

    }

    @Test
    public void testVerifyNoParentSpecified() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        ProjectObjects projectObjects = new ProjectObjects(modules);
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,"Obj out");
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,"Flat obj");
        modules.add(projectObjects);

        ExtractObjectEdges extractObjectEdges = new ExtractObjectEdges(modules);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.INPUT_OBJECTS,"Obj out");
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_EDGE_OBJECTS,false);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.CREATE_INTERIOR_OBJECTS,true);
        extractObjectEdges.updateParameterValue(ExtractObjectEdges.OUTPUT_INTERIOR_OBJECTS,"Obj int");
        modules.add(extractObjectEdges);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setChoice("Flat obj");

        assertFalse(childObjectsP.verify());

    }

    @Test
    public void appendXMLAttributes() throws ParserConfigurationException {
        ModulesI modules = new Modules();

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj");
        childObjectsP.setVisible(true);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        childObjectsP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(5,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("Test param",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("Test param",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals("Flat obj",namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("true",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void setAttributesFromXML() throws ParserConfigurationException {
        ModulesI modules = new Modules();

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",paramTest);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","Demo obj");
        element.setAttribute("VISIBLE","false");
        
        childObjectsP.setAttributesFromXML(element);

        assertEquals("Test param",childObjectsP.getName());
        assertEquals("New nick",childObjectsP.getNickname());
        assertEquals("Demo obj", childObjectsP.getValue(null));
        assertFalse(childObjectsP.isVisible());

    }
}