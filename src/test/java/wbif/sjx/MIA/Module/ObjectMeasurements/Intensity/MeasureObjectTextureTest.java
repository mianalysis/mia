package wbif.sjx.MIA.Module.ObjectMeasurements.Intensity;

import ij.IJ;
import ij.ImagePlus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects2D;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.common.Analysis.TextureCalculator;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.net.URLDecoder;

import static org.junit.jupiter.api.Assertions.*;

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
    @EnumSource(VolumeType.class)
    public void testProcessObject1Px(VolumeType volumeType) throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Im1",ipl);

        // Initialising the TextureCalculator
        TextureCalculator calculator = new TextureCalculator(1,0,0);

        // Testing each object
        double[] offs = new double[]{1,0,0};
        for (Obj obj:expectedObjects.values()) {
            MeasureObjectTexture.processObject(obj,obj,image,calculator,offs,false);

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
    @EnumSource(VolumeType.class)
    public void testProcessObject3Px(VolumeType volumeType) throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Im1",ipl);

        // Initialising the TextureCalculator
        TextureCalculator calculator = new TextureCalculator(3,0,0);

        // Testing each object
        double[] offs = new double[]{3,0,0};
        for (Obj obj:expectedObjects.values()) {
            MeasureObjectTexture.processObject(obj,obj,image,calculator,offs,false);

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