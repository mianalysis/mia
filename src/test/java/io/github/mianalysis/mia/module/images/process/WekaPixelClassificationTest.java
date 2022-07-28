package io.github.mianalysis.mia.module.images.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;

public class WekaPixelClassificationTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new WekaPixelClassification(null).getDescription());
    }

    @Test
    public void testRun2D8Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_2D_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_2D_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");

    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun2D16Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_2D_16bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_2D_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun2D32Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_2D_32bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_2D_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3D8Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_3D_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_3D_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun4D8Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun5D8Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_5D_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_5D_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3DChannels8Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_3D_channels_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_3D_channels_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun3DChannels8Bit1Block() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_3D_channels_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_3D_channels_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,1);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun4DChannelsSlice8Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-slice_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-slice_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");

    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun4DChannelsSlice8Bit1Block() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-slice_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-slice_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,1);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");

    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun4DChannelsSlice8Bit3Block() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-slice_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-slice_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,3);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");

    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun4DChannelsTime8Bit() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-time_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-time_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,this.getClass().getResource("/images/wekaprobabilitymaps/Example_classifier.model").toURI().getPath());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    wekaProbabilityMaps.execute(workspace);

    // Checking the images in the workspace
    assertEquals(2,workspace.getImages().size());
    assertNotNull(workspace.getImage("Test_image"));
    assertNotNull(workspace.getImage("Test_output"));

    // Checking the output image has the expected calibration
    Image outputImage = workspace.getImage("Test_output");
    assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMissingClassifier() throws Exception {
    // Creating a new workspace
    Workspaces workspaces = new Workspaces();
    Workspace workspace = workspaces.getNewWorkspace(null,1);

    // Loading the test image and adding to workspace
    String pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-time_8bit.zip").getPath(),"UTF-8");
    ImagePlus ipl = IJ.openImage(pathToImage);
    Image image = new Image("Test_image",ipl);
    workspace.addImage(image);

    pathToImage =
    URLDecoder.decode(this.getClass().getResource("/images/wekaprobabilitymaps/NoisyObjects_4D_channels-time_probability.zip").getPath(),"UTF-8");
    Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

    // Initialising BinaryOperations
    WekaPixelClassification wekaProbabilityMaps = new WekaPixelClassification(new
    Modules());
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.INPUT_IMAGE,"Test_image");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.OUTPUT_IMAGE,"Test_output");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.CLASSIFIER_FILE,"");
    wekaProbabilityMaps.updateParameterValue(WekaPixelClassification.SIMULTANEOUS_SLICES,20);

    // Running Module
    Status status = wekaProbabilityMaps.execute(workspace);

    // Checking the module failed to generateModuleList (returned false)
    assertEquals(Status.FAIL,status);

    }
}