package io.github.mianalysis.mia.module.images.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;

/**
 * Created by Stephen Cross on 09/09/2017.
 */

public class ChannelExtractorTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ChannelExtractor(null).getDescription());

    }

    @Test
    public void testRun8bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor(new Modules());
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);

        MIA.log.writeMessage(channelExtractor.getParameter(ChannelExtractor.CHANNEL_TO_EXTRACT).toString());

        // Running ChannelExtractor
        channelExtractor.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRun8bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_ZT_8bit_C2.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor(new Modules());
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);

        // Running ChannelExtractor
        channelExtractor.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun16bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_16bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_16bit_C1.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor(new Modules());
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);

        // Running ChannelExtractor
        channelExtractor.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun16bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_16bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_16bit_C2.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor(new Modules());
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);

        // Running ChannelExtractor
        channelExtractor.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun32bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_32bit_C1.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor(new Modules());
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);

        // Running ChannelExtractor
        channelExtractor.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun32bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_32bit_C2.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor(new Modules());
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);

        // Running ChannelExtractor
        channelExtractor.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}