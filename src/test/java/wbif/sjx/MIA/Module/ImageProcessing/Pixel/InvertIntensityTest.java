package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;

/**
 * Created by sc13967 on 26/03/2018.
 */
public class InvertIntensityTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new InvertIntensity(null).getDescription());
    }

    @Test
    public void testRun3D8bit() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity(null);
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3DApplyToInput8bit() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity(null);
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,true);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(1,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3D16bit() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_16bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity(null);
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3D32bit() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Invert_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising InvertIntensity
        InvertIntensity invertIntensity = new InvertIntensity(null);
        invertIntensity.initialiseParameters();
        invertIntensity.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        invertIntensity.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        invertIntensity.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);

        // Running Module
        invertIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}