package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects2D;
import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.common.Analysis.TextureCalculator;

import java.net.URLDecoder;

import static org.junit.Assert.*;

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

    @Test @Ignore
    public void testConvertCalibratedOffsets() {

    }

    @Test @Ignore
    public void testGetLocalObjectRegion() {
    }

    @Test
    public void testProcessObject1Px() throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Im1",ipl);

        // Initialising the TextureCalculator
        TextureCalculator calculator = new TextureCalculator(1,0,0);

        // Testing each object
        double[] offs = new double[]{1,0,0};
        for (Obj obj:expectedObjects.values()) {
            MeasureObjectTexture  .processObject(obj,image,calculator,false,offs,false);

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

    @Test
    public void testProcessObject3Px() throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Im1",ipl);

        // Initialising the TextureCalculator
        TextureCalculator calculator = new TextureCalculator(3,0,0);

        // Testing each object
        double[] offs = new double[]{3,0,0};
        for (Obj obj:expectedObjects.values()) {
            MeasureObjectTexture .processObject(obj,image,calculator,false,offs,false);

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
    public void testGetTitle() {
        assertNotNull(new MeasureObjectTexture().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectTexture().getHelp());
    }
}