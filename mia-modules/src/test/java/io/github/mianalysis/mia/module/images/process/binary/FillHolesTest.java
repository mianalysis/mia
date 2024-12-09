package io.github.mianalysis.mia.module.images.process.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;


public class FillHolesTest extends ModuleTest {

    
    @Override
    public void testGetHelp() {
        assertNotNull(new FillHoles(null).getDescription());
    }

    @Test
    public void testRunWithFillHoles2DOperation2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/binaryoperations/BinaryObjects3D_8bit_whiteBG_fillHoles3D.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FillHoles binaryOperations = new FillHoles(new Modules());
        binaryOperations.updateParameterValue(FillHoles.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(FillHoles.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(FillHoles.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(FillHoles.BINARY_LOGIC, FillHoles.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}