package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class WekaProbabilityMapsTest {

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new WekaProbabilityMaps().getTitle());
    }

    @Test
    public void testRun2D8Bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_2D_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_2D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_2D_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_2D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_2D_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_3D_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_4D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_4D_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_5D_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_3D_channels_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_3D_channels_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_4D_channels-slice_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_4D_channels-slice_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_4D_channels-time_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/WekaProbabilityMaps/NoisyObjects_4D_channels-time_probability.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        WekaProbabilityMaps wekaProbabilityMaps = new WekaProbabilityMaps();
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.INPUT_IMAGE,"Test_image");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.OUTPUT_IMAGE,"Test_output");
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.CLASSIFIER_FILE,this.getClass().getResource("/images/WekaProbabilityMaps/Example_classifier.model").toURI().getPath());
        wekaProbabilityMaps.updateParameterValue(WekaProbabilityMaps.SHOW_IMAGE,false);

        // Running BinaryOperations
        wekaProbabilityMaps.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}