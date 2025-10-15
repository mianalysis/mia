package io.github.mianalysis.mia.module.objects.measure.intensity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects2D;
import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.process.analysis.TextureCalculator;


public class MeasureObjectTextureTest extends ModuleTest {
    private double tolerance = 1E-4;

    @Test
    public void testGetFullNameUncalibrated() {
        String inputImageName = "image1";
        double[] offs = new double[]{3,-2,1.2};

        String expected = "TEXTURE // image1_ASM_(3.0,-2.0,1.2 PX)";
        String actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.ASM,offs,false);
        assertEquals(expected,actual);

        expected = "TEXTURE // image1_CONTRAST_(3.0,-2.0,1.2 PX)";
        actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.CONTRAST,offs,false);
        assertEquals(expected,actual);

        expected = "TEXTURE // image1_CORRELATION_(3.0,-2.0,1.2 PX)";
        actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.CORRELATION,offs,false);
        assertEquals(expected,actual);

        expected = "TEXTURE // image1_ENTROPY_(3.0,-2.0,1.2 PX)";
        actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.ENTROPY,offs,false);
        assertEquals(expected,actual);

    }

    @Test @Disabled
    public void testConvertCalibratedOffsets() {

    }

    @Test @Disabled
    public void testGetLocalObjectRegion() {
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testProcessObject1Px(VolumeTypes volumeType) throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjsI expectedObjects = new Objects2D(VolumeTypes.getFactory(volumeType)).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Im1",ipl);

        // Initialising the TextureCalculator
        TextureCalculator calculator = new TextureCalculator();

        // Testing each object
        double[] offs = new double[]{1,0,0};
        for (ObjI obj:expectedObjects.values()) {
            MeasureObjectTexture.processObject(obj,image,calculator,offs,false);

            double expected = obj.getMeasurement(Objects2D.Measures.ASM_1PX.name()).getValue();
            double actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.ASM,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.CONTRAST_1PX.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.CONTRAST,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.CORRELATION_1PX.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.CORRELATION,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.ENTROPY_1PX.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.ENTROPY,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testProcessObject3Px(VolumeTypes volumeType) throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjsI expectedObjects = new Objects2D(VolumeTypes.getFactory(volumeType)).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Im1",ipl);

        // Initialising the TextureCalculator
        TextureCalculator calculator = new TextureCalculator();

        // Testing each object
        double[] offs = new double[]{3,0,0};
        for (ObjI obj:expectedObjects.values()) {
            MeasureObjectTexture.processObject(obj,image,calculator,offs,false);

            double expected = obj.getMeasurement(Objects2D.Measures.ASM_3PX.name()).getValue();
            double actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.ASM,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.CONTRAST_3PX.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.CONTRAST,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.CORRELATION_3PX.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.CORRELATION,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.ENTROPY_3PX.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.ENTROPY,offs,false)).getValue();
            assertEquals(expected,actual,tolerance);

        }
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectTexture(null).getDescription());
    }
}