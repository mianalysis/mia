package wbif.sjx.MIA.Object.Parameters;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureImageIntensity;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureImageTexture;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.ModuleCollection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ImageMeasurementPTest {
    @Test
    public void duplicate() {
        ModuleCollection modules = new ModuleCollection();
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
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        ImageMeasurementP duplicated = imageMeasurementP.duplicate(paramTest);

        assertEquals("Test meas",duplicated.getName());
        assertEquals("",duplicated.getImageName());
        assertEquals(paramTest,duplicated.getModule());

    }

    @Test
    public void getRawStringValueBlank() {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");

        assertEquals("",imageMeasurementP.getRawStringValue());

    }

    @Test
    public void getRawStringValue() {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setChoice("Image measurement choice");

        assertEquals("Image measurement choice",imageMeasurementP.getRawStringValue());

    }

    @Test
    public void getRawStringValueNull() {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName(null);

        assertEquals("",imageMeasurementP.getRawStringValue());

    }

    @Test
    public void setValueFromString() {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setValueFromString("Image measurement choice");

        assertEquals("Image measurement choice",imageMeasurementP.getChoice());

    }

    @Test
    public void setValueFromStringBlank() {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas",paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setValueFromString("");

        assertEquals("",imageMeasurementP.getChoice());

    }

    @Test
    public void setValueFromStringNull() {
        ModuleCollection modules = new ModuleCollection();
        ParamTest paramTest = new ParamTest(modules);

        ImageMeasurementP imageMeasurementP = new ImageMeasurementP("Test meas", paramTest);
        imageMeasurementP.setImageName("My im");
        imageMeasurementP.setValueFromString(null);

        assertEquals("",imageMeasurementP.getChoice());

    }

    @Test
    public void getChoicesWithChoices() {
        ModuleCollection modules = new ModuleCollection();

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
        String[] expected = new String[]{"INTENSITY // MEAN",
                "INTENSITY // MIN",
                "INTENSITY // MAX",
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
        ModuleCollection modules = new ModuleCollection();

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
        ModuleCollection modules = new ModuleCollection();

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
        ModuleCollection modules = new ModuleCollection();

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
        ModuleCollection modules = new ModuleCollection();

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
        ModuleCollection modules = new ModuleCollection();
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
        assertEquals(4,namedNodeMap.getLength());

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
        ModuleCollection modules = new ModuleCollection();
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
        assertEquals("Demo meas", imageMeasurementP.getValue());
        assertFalse(imageMeasurementP.isVisible());

    }
}