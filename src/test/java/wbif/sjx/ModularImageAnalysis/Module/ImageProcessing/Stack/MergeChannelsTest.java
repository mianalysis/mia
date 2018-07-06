package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class MergeChannelsTest {

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() {
        assertNotNull(new MergeChannels<>().getTitle());
    }

    @Test
    public void testRun2D8Bit2D8Bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test images and adding to workspace
        String pathToImage1 = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage1);
        Image image1 = new Image("Test_image_1",ipl1);
        workspace.addImage(image1);

        String pathToImage2 = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl2 = IJ.openImage(pathToImage2);
        Image image2 = new Image("Test_image_2",ipl2);
        workspace.addImage(image2);

        String pathToExpected = URLDecoder.decode(this.getClass().getResource("/images/MergeChannels/LabObj2D8bit_NoisyGrad2D8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImagePlus = IJ.openImage(pathToExpected);
        Image expectedImage = new Image("Output_image",expectedImagePlus);

        // Initialising the module
        MergeChannels mergeChannels = new MergeChannels();
        mergeChannels.initialiseParameters();
        mergeChannels.updateParameterValue(MergeChannels.INPUT_IMAGE1,"Test_image_1");
        mergeChannels.updateParameterValue(MergeChannels.INPUT_IMAGE2,"Test_image_2");
        mergeChannels.updateParameterValue(MergeChannels.OUTPUT_IMAGE,"Output_image");

        // Running the module
        mergeChannels.run(workspace);

        // Getting the output image
        Image actualImage = workspace.getImage("Output_image");

        assertNotNull(actualImage);
        assertEquals(expectedImage,actualImage);

    }

    @Test
    public void testRun3DMultiCh8Bit2D8Bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test images and adding to workspace
        String pathToImage1 = URLDecoder.decode(this.getClass().getResource("/images/MergeChannels/LabObj2D8bit_NoisyGrad2D8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage1);
        Image image1 = new Image("Test_image_1",ipl1);
        workspace.addImage(image1);

        String pathToImage2 = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl2 = IJ.openImage(pathToImage2);
        Image image2 = new Image("Test_image_2",ipl2);
        workspace.addImage(image2);

        String pathToExpected = URLDecoder.decode(this.getClass().getResource("/images/MergeChannels/LabObj2D8bit_NoisyGrad2D8bit_ColocCh1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImagePlus = IJ.openImage(pathToExpected);
        Image expectedImage = new Image("Output_image",expectedImagePlus);

        // Initialising the module
        MergeChannels mergeChannels = new MergeChannels();
        mergeChannels.initialiseParameters();
        mergeChannels.updateParameterValue(MergeChannels.INPUT_IMAGE1,"Test_image_1");
        mergeChannels.updateParameterValue(MergeChannels.INPUT_IMAGE2,"Test_image_2");
        mergeChannels.updateParameterValue(MergeChannels.OUTPUT_IMAGE,"Output_image");

        // Running the module
        mergeChannels.run(workspace);

        // Getting the output image
        Image actualImage = workspace.getImage("Output_image");

        assertNotNull(actualImage);
        assertEquals(expectedImage,actualImage);

    }

    @Test
    public void testRun3D8Bit3D8Bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test images and adding to workspace
        String pathToImage1 = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage1);
        Image image1 = new Image("Test_image_1",ipl1);
        workspace.addImage(image1);

        String pathToImage2 = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl2 = IJ.openImage(pathToImage2);
        Image image2 = new Image("Test_image_2",ipl2);
        workspace.addImage(image2);

        String pathToExpected = URLDecoder.decode(this.getClass().getResource("/images/MergeChannels/LabObj3D8bit_NoisyGrad3D8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImagePlus = IJ.openImage(pathToExpected);
        Image expectedImage = new Image("Output_image",expectedImagePlus);

        // Initialising the module
        MergeChannels mergeChannels = new MergeChannels();
        mergeChannels.initialiseParameters();
        mergeChannels.updateParameterValue(MergeChannels.INPUT_IMAGE1,"Test_image_1");
        mergeChannels.updateParameterValue(MergeChannels.INPUT_IMAGE2,"Test_image_2");
        mergeChannels.updateParameterValue(MergeChannels.OUTPUT_IMAGE,"Output_image");

        // Running the module
        mergeChannels.run(workspace);

        // Getting the output image
        Image actualImage = workspace.getImage("Output_image");

        new ImageJ();
        actualImage.getImagePlus().show();
//        expectedImage.getImagePlus().show();
        IJ.runMacro("waitForUser");

        assertNotNull(actualImage);
        assertEquals(expectedImage,actualImage);

    }
}