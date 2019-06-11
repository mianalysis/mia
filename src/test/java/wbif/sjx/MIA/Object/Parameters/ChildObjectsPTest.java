package wbif.sjx.MIA.Object.Parameters;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ExtractObjectEdges;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.IdentifyObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ProjectObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.TrackObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.ExpandShrinkObjects;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.*;

public class ChildObjectsPTest {
    @Test
    public void testDuplicate() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Par_name");
        ChildObjectsP duplicated = childObjectsP.duplicate();

        assertEquals("Test param",duplicated.getName());
        assertEquals(filterImage,duplicated.getModule());
                

    }

    @Test
    public void testGetRawStringValueBlank() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);

        assertEquals("",childObjectsP.getRawStringValue());

    }

    @Test
    public void testGetRawStringValue() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setChoice("Par_name");

        assertEquals("Par_name",childObjectsP.getRawStringValue());

    }

    @Test
    public void testGetChoicesWithChoices() {
        ModuleCollection modules = new ModuleCollection();

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

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[]{"Flat obj","Obj int"};

        assertEquals(2,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetChoicesWithChoicesMultiGeneration() {
        ModuleCollection modules = new ModuleCollection();

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

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[]{"Flat obj","Flat obj // Obj int"};

        assertEquals(2,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetChoicesNoChoices() {
        ModuleCollection modules = new ModuleCollection();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[0];

        assertEquals(0,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetChoicesDisabledModule() {
        ModuleCollection modules = new ModuleCollection();

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

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");

        String[] actual = childObjectsP.getChoices();
        String[] expected = new String[]{"Flat obj"};

        assertEquals(1,actual.length);
        assertArrayEquals(expected,actual);

    }

    @Test
    public void testVerifyPresent() {
        ModuleCollection modules = new ModuleCollection();

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

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj");

        assertTrue(childObjectsP.verify());

    }

    @Test
    public void testVerifyPresentMultiGeneration() {
        ModuleCollection modules = new ModuleCollection();

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

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj // Obj int");

        assertTrue(childObjectsP.verify());

    }

    @Test
    public void testVerifyDisabledModule() {
        ModuleCollection modules = new ModuleCollection();

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

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj");

        assertFalse(childObjectsP.verify());

    }

    @Test
    public void testVerifyNoChildren() {
        ModuleCollection modules = new ModuleCollection();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Demo im");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Obj out");
        modules.add(identifyObjects);

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");

        assertFalse(childObjectsP.verify());

    }

    @Test
    public void testVerifyMissingChild() {
        ModuleCollection modules = new ModuleCollection();

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

        FilterImage filterImage = new FilterImage(modules);
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Demo im");
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Wrong obj");

        assertFalse(childObjectsP.verify());

    }

    @Test
    public void appendXMLAttributes() throws ParserConfigurationException {
        ModuleCollection modules = new ModuleCollection();

        FilterImage filterImage = new FilterImage(modules);
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Obj out");
        childObjectsP.setChoice("Flat obj");
        childObjectsP.setVisible(true);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        childObjectsP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(4,namedNodeMap.getLength());

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
        ModuleCollection modules = new ModuleCollection();

        FilterImage filterImage = new FilterImage(modules);
        modules.add(filterImage);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","Demo obj");
        element.setAttribute("VISIBLE","false");

        childObjectsP.setAttributesFromXML(element);

        assertEquals("Test param",childObjectsP.getName());
        assertEquals("New nick",childObjectsP.getNickname());
        assertEquals("Demo obj", childObjectsP.getValue());
        assertFalse(childObjectsP.isVisible());

    }
}