package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 09/09/2017.
 */
public class ChannelExtractorTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ChannelExtractor().getTitle());

    }

    @Test
    public void testRun8bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));
    }

    @Test
    public void testRun8bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit_C2.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRun16bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_16bit_C1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRun16bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_16bit_C2.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRun32bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_32bit_C1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRun32bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_32bit_C2.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }
}