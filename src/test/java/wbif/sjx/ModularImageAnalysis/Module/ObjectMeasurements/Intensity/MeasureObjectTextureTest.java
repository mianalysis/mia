package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects2D;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.common.Analysis.TextureCalculator;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class MeasureObjectTextureTest {
    private double tolerance = 1E-4;

    @Test
    public void testGetFullName() {
        String inputImageName = "image1";

        String expected = "TEXTURE // image1_ASM";
        String actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.ASM);
        assertEquals(expected,actual);

        expected = "TEXTURE // image1_CONTRAST";
        actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.CONTRAST);
        assertEquals(expected,actual);

        expected = "TEXTURE // image1_CORRELATION";
        actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.CORRELATION);
        assertEquals(expected,actual);

        expected = "TEXTURE // image1_ENTROPY";
        actual = MeasureObjectTexture.getFullName(inputImageName,MeasureObjectTexture.Measurements.ENTROPY);
        assertEquals(expected,actual);

    }

    @Test @Ignore
    public void testConvertCalibratedOffsets() {
    }

    @Test @Ignore
    public void testGetLocalObjectRegion() {
    }

    @Test
    public void testProcessObject() throws Exception {
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
        for (Obj obj:expectedObjects.values()) {
            MeasureObjectTexture  .processObject(obj,image,calculator,false);

            double expected = obj.getMeasurement(Objects2D.Measures.ASM.name()).getValue();
            double actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.ASM)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.CONTRAST.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.CONTRAST)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.CORRELATION.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.CORRELATION)).getValue();
            assertEquals(expected,actual,tolerance);

            expected = obj.getMeasurement(Objects2D.Measures.ENTROPY.name()).getValue();
            actual = obj.getMeasurement(MeasureObjectTexture.getFullName("Im1",MeasureObjectTexture.Measurements.ENTROPY)).getValue();
            assertEquals(expected,actual,tolerance);

        }
    }

    @Test
    public void testGetTitle() {
        assertNotNull(new MeasureObjectTexture().getTitle());
    }
}