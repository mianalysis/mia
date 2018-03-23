package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class ImageMathTest {
    @Test
    public void getTitle() {
        assertNotNull(new ImageMath().getTitle());
    }

    @Test @Ignore
    public void testRunAddPositive2D() throws Exception {

    }

    @Test
    public void testRunAddPositive3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Add50_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Output_image");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test @Ignore
    public void testRunAddPositive3D16bit() throws Exception {

    }

    @Test @Ignore
    public void testRunAddPositive3D32bit() throws Exception {

    }

    @Test @Ignore
    public void testRunAddPositive4D() throws Exception {

    }

    @Test @Ignore
    public void testRunAddPositive5D() throws Exception {

    }

    @Test @Ignore
    public void testRunAddPositiveToInput5D() throws Exception {

    }

    @Test @Ignore
    public void testRunAddMeasurement5D() throws Exception {

    }

    @Test @Ignore
    public void testRunAddNegative3D() throws Exception {

    }

    @Test @Ignore
    public void testRunSubtractPositive3D() throws Exception {

    }

    @Test @Ignore
    public void testRunSubtractNegative3D() throws Exception {

    }

    @Test @Ignore
    public void testRunMultiplyPositive3D() throws Exception {

    }

    @Test @Ignore
    public void testRunMultiplyNegative3D() throws Exception {

    }

    @Test @Ignore
    public void testRunDividePositive3D() throws Exception {

    }

    @Test @Ignore
    public void testRuDivideNegative3D() throws Exception {

    }

    @Test @Ignore
    public void testRunInvert3D() throws Exception {

    }
}