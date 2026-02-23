package io.github.mianalysis.mia.object.parameters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.testmodules.ImageLoader;
import io.github.mianalysis.mia.module.testmodules.MeasureImageIntensity;
import io.github.mianalysis.mia.module.testmodules.MeasureImageTexture;

public class ImageMeasurementPTest {
    @Test
    public void duplicate() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");
        ImageMeasurementP duplicated = imageMeasurementP.duplicate(paramTest);

        assertEquals("Test meas",duplicated.getName());
        assertEquals("My im",duplicated.getImageName());
        assertEquals(paramTest,duplicated.getModule());

    }

    @Test
    public void duplicateNoImageSpecified() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        ImageMeasurementP duplicated = imageMeasurementP.duplicate(paramTest);

        assertEquals("Test meas",duplicated.getName());
        assertEquals("",duplicated.getImageName());
        assertEquals(paramTest,duplicated.getModule());

    }

    @Test
    public void getRawStringValueBlank() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");

        assertEquals("",imageMeasurementP.getRawStringValue());

    }

    @Test
    public void getRawStringValue() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setChoice("Image measurement choice");

        assertEquals("Image measurement choice",imageMeasurementP.getRawStringValue());

    }

    @Test
    public void getRawStringValueNull() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName(null);

        assertEquals("",imageMeasurementP.getRawStringValue());

    }

    @Test
    public void setValueFromString() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setValueFromString("Image measurement choice");

        assertEquals("Image measurement choice",imageMeasurementP.getValue(null));

    }

    @Test
    public void setValueFromStringBlank() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setValueFromString("");

        assertEquals("",imageMeasurementP.getValue(null));

    }

    @Test
    public void setValueFromStringNull() {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas", paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setValueFromString(null);

        assertEquals("",imageMeasurementP.getValue(null));

    }

    @Test
    public void getChoicesWithChoices() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(modules);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Demo im");
        modules.add(measureImageIntensity);

        MeasureImageTexture measureImageTexture = new MeasureImageTexture(modules);
        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,"Demo im")
                .updateParameterValue(MeasureImageTexture.X_OFFSET,1)
                .updateParameterValue(MeasureImageTexture.Y_OFFSET,0)
                .updateParameterValue(MeasureImageTexture.Z_OFFSET,0);
        modules.add(measureImageTexture);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("Demo im");

        String[] actual = imageMeasurementP.getChoices();
        String[] expected = new String[] { "INTENSITY // MEAN",
                "INTENSITY // MEDIAN",
                "INTENSITY // MIN",
                "INTENSITY // MAX",
                "INTENSITY // MODE",
                "INTENSITY // SUM",
                "INTENSITY // STDEV",
                "TEXTURE // ASM",
                "TEXTURE // CONTRAST",
                "TEXTURE // CORRELATION",
                "TEXTURE // ENTROPY"};

        // Sorting arrays into the same order
        Arrays.sort(actual);
        Arrays.sort(expected);

        assertArrayEquals(expected,actual);

    }

    @Test
    public void getChoicesNoChoices() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("Demo im");

        String[] actual = imageMeasurementP.getChoices();
        String[] expected = new String[0];

        // Sorting arrays into the same order
        Arrays.sort(actual);
        Arrays.sort(expected);

        assertArrayEquals(expected,actual);

    }

    @Test
    public void verifyPresent() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(modules);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Demo im");
        modules.add(measureImageIntensity);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("Demo im");
        imageMeasurementP.setChoice("INTENSITY // STDEV");

        assertTrue(imageMeasurementP.verify());

    }

    @Test
    public void verifyNoMeasurements() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("Demo im");

        assertFalse(imageMeasurementP.verify());

    }

    @Test
    public void verifyNoImageSpecified() {
        ModulesI modules = new Modules();

        ImageLoader imageLoader = new ImageLoader(modules);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Demo im");
        modules.add(imageLoader);

        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(modules);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Demo im");
        modules.add(measureImageIntensity);

        ParamTest paramTest = new ParamTest(modules);
        modules.add(paramTest);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setChoice("INTENSITY // STDEV");

        assertFalse(imageMeasurementP.verify());

    }

    @Test
    public void appendXMLAttributes() throws ParserConfigurationException {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas", paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setChoice("Im meas");
        imageMeasurementP.setNickname("Nickname meas");
        imageMeasurementP.setVisible(true);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        imageMeasurementP.appendXMLAttributes(element);

        NamedNodeMap namedNodeMap = element.getAttributes();
        assertEquals(5,namedNodeMap.getLength());

        assertNotNull(namedNodeMap.getNamedItem("NAME"));
        assertNotNull(namedNodeMap.getNamedItem("NICKNAME"));
        assertNotNull(namedNodeMap.getNamedItem("VALUE"));
        assertNotNull(namedNodeMap.getNamedItem("VISIBLE"));

        assertEquals("Test meas",namedNodeMap.getNamedItem("NAME").getNodeValue());
        assertEquals("Nickname meas",namedNodeMap.getNamedItem("NICKNAME").getNodeValue());
        assertEquals("Im meas",namedNodeMap.getNamedItem("VALUE").getNodeValue());
        assertEquals("true",namedNodeMap.getNamedItem("VISIBLE").getNodeValue());

    }

    @Test
    public void setAttributesFromXML() throws ParserConfigurationException {
        ModulesI modules = new Modules();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas", paramTest);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("Test");
        element.setAttribute("NAME","New name");
        element.setAttribute("NICKNAME","New nick");
        element.setAttribute("VALUE","Demo meas");
        element.setAttribute("VISIBLE","false");

        imageMeasurementP.setAttributesFromXML(element);

        assertEquals("Test meas",imageMeasurementP.getName());
        assertEquals("New nick",imageMeasurementP.getNickname());
        assertEquals("Demo meas", imageMeasurementP.getValue(null));
        assertFalse(imageMeasurementP.isVisible());

    }
}