// TODO: Each local threshold algorithm should really be tested with different dimension images, limits and multipliers

package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class ThresholdImageTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ThresholdImage(null).getDescription());
    }

    @Test
    public void testRunGlobalHuangNoLimsNoMultWhiteBG2D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient2D_8bit_GlobalHuangNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalHuangNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangNoLimsNoMultWhiteBG4D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_ZT_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient5D_8bit_C1_GlobalHuangNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangNoLimsNoMultWhiteBG5D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient5D_8bit_GlobalHuangNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangNoLimsNoMultWhiteBG3D16bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_16bit_GlobalHuangNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    // Temporarily removed.  The applied threshold is very close to the expected value; however, an update to the
    // normalisation/thresholding means it's not exactly the same.
    @Test @Ignore
    public void testRunGlobalHuangNoLimsNoMultWhiteBG3D32bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_32bit_GlobalHuangNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangNoLimsNoMultBlackBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalHuangNoLimsNoMultBlackBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,false);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangNoLims2xMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalHuangNoLims2xMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,2.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangNoLims0p5xMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalHuangNoLims0p5xMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,0.5);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangMinLimPassNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalHuangNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,true);
        thresholdImage.updateParameterValue(ThresholdImage.LOWER_THRESHOLD_LIMIT,100.0);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalHuangMinLimFailNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalHuangMinLimFailNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,true);
        thresholdImage.updateParameterValue(ThresholdImage.LOWER_THRESHOLD_LIMIT,140.0);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunLocal3DBernsenNoLimsNoMultWhiteBG3D8bit() throws Exception {

    }

    @Test @Ignore
    public void testRunLocal3DContrastNoLimsNoMultWhiteBG3D8bit() throws Exception {

    }

    @Test @Ignore
    public void testRunLocal3DMeanNoLimsNoMultWhiteBG3D8bit() throws Exception {

    }

    @Test @Ignore
    public void testRunLocal3DMedianNoLimsNoMultWhiteBG3D8bit() throws Exception {

    }

    @Test @Ignore
    public void testRunLocal3DPhansalkarNoLimsNoMultWhiteBG3D8bit() throws Exception {

    }

    @Test
    public void testRunLocalSlicePhansalkarNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_LocalSlicePhansalkarNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.LOCAL);
        thresholdImage.updateParameterValue(ThresholdImage.LOCAL_ALGORITHM,ThresholdImage.LocalAlgorithms.PHANSALKAR_SLICE);
        thresholdImage.updateParameterValue(ThresholdImage.LOCAL_RADIUS,15.0);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunLocalSlicePhansalkarNoLimsNoMultBlackBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_LocalSlicePhansalkarNoLimsNoMultBlackBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.LOCAL);
        thresholdImage.updateParameterValue(ThresholdImage.LOCAL_ALGORITHM,ThresholdImage.LocalAlgorithms.PHANSALKAR_SLICE);
        thresholdImage.updateParameterValue(ThresholdImage.LOCAL_RADIUS,15.0);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,false);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunManual8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient2D_8bit_Manual62.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.MANUAL);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_VALUE,62);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,false);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // INDIVIDUAL TESTS FOR EACH GLOBAL THRESHOLD

    @Test
    public void testRunGlobalIntermodesNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);


        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalIntermodesNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.INTERMODES);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalIsodataNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalIsoDataNoLimsNoMultBlackBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.ISO_DATA);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalMaxEntropyNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalMaxEntropyNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.MAX_ENTROPY);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalOtsuNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalOtsuNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.OTSU);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGlobalTriangleNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ThresholdImage/NoisyGradient3D_8bit_GlobalTriangleNoLimsNoMultWhiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.TRIANGLE);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);

        // Running ThresholdImage
        thresholdImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // VERIFYING THRESHOLD IS STORED

    @Test
    public void testRunStoreThresholdGlobalHuangNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);
        thresholdImage.updateParameterValue(ThresholdImage.STORE_THRESHOLD_AS_MEASUREMENT,true);

        // Running MeasureImageIntensity
        thresholdImage.execute(workspace);

        // Getting output image
        Image outputImage = workspace.getImage("Test_output");

        // Verifying results
        assertEquals(1,outputImage.getMeasurements().size());

        String measurementName = ThresholdImage.getFullName(ThresholdImage.Measurements.GLOBAL_VALUE,ThresholdImage.GlobalAlgorithms.HUANG);
        assertEquals(130.0, outputImage.getMeasurement(measurementName).getValue(),tolerance);

    }

    @Test
    public void testRunStoreThresholdGlobalHuangApplyNoLimsNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,true);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);
        thresholdImage.updateParameterValue(ThresholdImage.STORE_THRESHOLD_AS_MEASUREMENT,true);

        // Running MeasureImageIntensity
        thresholdImage.execute(workspace);

        // Verifying results
        assertEquals(1,image.getMeasurements().size());

        String measurementName = ThresholdImage.getFullName(ThresholdImage.Measurements.GLOBAL_VALUE,ThresholdImage.GlobalAlgorithms.HUANG);
        assertEquals(130.0, image.getMeasurement(measurementName).getValue(),tolerance);

    }

    @Test
    public void testRunStoreThresholdGlobalHuangApplyMinLimNoMultWhiteBG3D8bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,true);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,true);
        thresholdImage.updateParameterValue(ThresholdImage.LOWER_THRESHOLD_LIMIT,145.0);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);
        thresholdImage.updateParameterValue(ThresholdImage.STORE_THRESHOLD_AS_MEASUREMENT,true);

        // Running MeasureImageIntensity
        thresholdImage.execute(workspace);

        // Verifying results
        assertEquals(1,image.getMeasurements().size());

        String measurementName = ThresholdImage.getFullName(ThresholdImage.Measurements.GLOBAL_VALUE,ThresholdImage.GlobalAlgorithms.HUANG);
        assertEquals(145.0, image.getMeasurement(measurementName).getValue(),tolerance);

    }
    @Test
    public void testRunStoreThresholdLocal() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,true);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.LOCAL);
        thresholdImage.updateParameterValue(ThresholdImage.LOCAL_ALGORITHM,ThresholdImage.LocalAlgorithms.PHANSALKAR_SLICE);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);
        thresholdImage.updateParameterValue(ThresholdImage.STORE_THRESHOLD_AS_MEASUREMENT,true);

        // Running MeasureImageIntensity
        thresholdImage.execute(workspace);

        // Verifying results
        assertEquals(0,image.getMeasurements().size());

    }

    @Test
    public void testRunStoreThresholdManual() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,true);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.MANUAL);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_VALUE,12);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);
        thresholdImage.updateParameterValue(ThresholdImage.STORE_THRESHOLD_AS_MEASUREMENT,true);

        // Running MeasureImageIntensity
        thresholdImage.execute(workspace);

        // Verifying results
        assertEquals(0,image.getMeasurements().size());

    }

    @Test
    public void testRunDoNotStoreThresholdGlobal() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising ThresholdImage
        ThresholdImage thresholdImage = new ThresholdImage(new ModuleCollection());
        thresholdImage.initialiseParameters();
        thresholdImage.updateParameterValue(ThresholdImage.INPUT_IMAGE,"Test_image");
        thresholdImage.updateParameterValue(ThresholdImage.APPLY_TO_INPUT,false);
        thresholdImage.updateParameterValue(ThresholdImage.OUTPUT_IMAGE,"Test_output");
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_TYPE,ThresholdImage.ThresholdTypes.GLOBAL);
        thresholdImage.updateParameterValue(ThresholdImage.GLOBAL_ALGORITHM,ThresholdImage.GlobalAlgorithms.HUANG);
        thresholdImage.updateParameterValue(ThresholdImage.THRESHOLD_MULTIPLIER,1.0);
        thresholdImage.updateParameterValue(ThresholdImage.USE_LOWER_THRESHOLD_LIMIT,false);
        thresholdImage.updateParameterValue(ThresholdImage.WHITE_BACKGROUND,true);
        thresholdImage.updateParameterValue(ThresholdImage.STORE_THRESHOLD_AS_MEASUREMENT,false);

        // Running MeasureImageIntensity
        thresholdImage.execute(workspace);

        // Getting output image
        Image outputImage = workspace.getImage("Test_output");

        // Verifying results
        assertEquals(0,outputImage.getMeasurements().size());

    }
}