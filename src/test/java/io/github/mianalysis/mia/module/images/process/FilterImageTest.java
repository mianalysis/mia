package io.github.mianalysis.mia.module.images.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;

/**
 * Created by sc13967 on 13/11/2017.
 */

public class FilterImageTest extends ModuleTest {

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }


    // GENERAL TESTS

    @Override
    public void testGetHelp() {
        assertNotNull(new FilterImage(null).getDescription());
    }


    // TESTING DIFFERENCE OF GAUSSIAN FILTER

    @Test @Disabled
    public void testRunDoG2DFilter2DStack() throws Exception {
    }

    @Test @Disabled
    public void testRunDoG2DFilter5DStack() throws Exception {
    }

    @Test @Disabled
    public void testRunDoG2DFilter2DStackCalibrated() throws Exception {

    }


    // TESTING 2D GAUSSIAN FILTER

    @Test
    public void testRunGaussian2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGaussian2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGaussian2DFilter2DStackCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,true);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,0.04d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING 3D GAUSSIAN FILTER

    /**
     * Tests the module doesn't crash if a 2D image is passed to the 3D Gaussian filter
     * @throws Exception
     */
    @Test
    public void testRunGaussian3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGaussian3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxGauss3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunGaussian3DFilter2DStackCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,true);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,0.04d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING 2D RANK FILTERS

    @Test
    public void testRunMax2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMax2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MAXIMUM2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRunMax2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMax2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MAXIMUM2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMax2DFilter5DStackCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMax2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MAXIMUM2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,true);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,0.04d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMean2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMean2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRunMean2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMean2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMedian2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMedian2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRunMedian2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMedian2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMin2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMin2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MINIMUM2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRunMin2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMin2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MINIMUM2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunVariance2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxVariance2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.VARIANCE2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunVariance2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxVariance2D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.VARIANCE2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING 3D FILTERS

    @Test
    public void testRunMax3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMax3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MAXIMUM3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");

        assertEquals(outputImage, expectedImage);

    }

    @Test
    public void testRunMax3DFilter3DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxMax3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MAXIMUM3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMax3DFilter2DStackCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMax3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MAXIMUM3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,true);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,0.04d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");

        assertEquals(outputImage, expectedImage);

    }

    @Test
    public void testRunMax3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMax3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MAXIMUM3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");

        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMean3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMean3DFilter3DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMean3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");

        assertEquals(expectedImage,outputImage);

    }

    /**
     * Tests the module doesn't crash if a 2D image is passed to the 3D median filter
     * @throws Exception
     */
    @Test
    public void testRunMedian3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMedian3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMedian3DFilter3DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxMedian3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMedian3DFilter4DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects4D_8bit_2pxMedian3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMedian3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMedian3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMin3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxMin3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MINIMUM3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMin3DFilter3DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxMin3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MINIMUM3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMin3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMin3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MINIMUM3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");

        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunVar3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxVar3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.VARIANCE3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");

        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunVar3DFilter3DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxVar3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.VARIANCE3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunVar3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxVar3D.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.VARIANCE3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running Module
        filterImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");

        assertEquals(expectedImage,outputImage);

    }


    // TESTING ROLLING FRAME FILTER

    /**
     * Tests the rolling frame filter (designed for image stacks with more than 1 timepoint)
     * @throws Exception
     */
    @Test @Disabled
    public void testRunRollingFrameFilter5DStack() throws Exception {
    }

    /**
     * Verifyig the output when the stack only contains 1 timepoint, but a 3D stack (need to check it doesn't average
     * over Z)
     * @throws Exception
     */
    @Test @Disabled
    public void testRunRollingFrameFilterOneTimepoint() throws Exception {
    }

    @Test @Disabled
    public void testRunRollingFrameFilter5DStackCalibrated() throws Exception {
    }

}