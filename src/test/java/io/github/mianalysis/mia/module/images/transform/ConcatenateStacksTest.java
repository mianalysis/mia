package io.github.mianalysis.mia.module.images.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.Parameters;


public class ConcatenateStacksTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ConcatenateStacks<>(null).getDescription());
    }


    // TESTING 2D STACKS

    @Test
    public void testRunApplyConcatenate2D8bitX() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate2D8bitY() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks2D_8bit_Y.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate2D8bitC() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks2D_8bit_C.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate2D8bitZ() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks2D_8bit_Z.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate2D8bitT() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks2D_8bit_T.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING 3D STACKS

    @Test @Disabled
    public void testRunApplyConcatenate3D8bitX() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate3D8bitY() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_Y.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate3D8bitC() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks3D_8bit_C.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate3D8bitZ() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks3D_8bit_Z.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate3D8bitT() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects3D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks3D_8bit_T.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING 4D STACKS

    @Test @Disabled
    public void testRunApplyConcatenate4D8bitX() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate4D8bitY() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_Y.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DCT8bitC() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_CT_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_CT_8bit.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DCT_8bit_C.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DCZ8bitC() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4DCZ_8bit_C_full.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_CZ_8bit.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DCZ_8bit_C.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DZT8bitC() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects4D_8bit_2pxMedian3D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DZT_8bit_C.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DCT8bitZ() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_CT_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_CT_8bit.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DCT_8bit_Z.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DCZ8bitZ() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4DCZ_8bit_C_full.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_CZ_8bit.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DCZ_8bit_Z.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DZT8bitZ() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_8bit.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DZT_8bit_Z.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DCT8bitT() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_CT_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_CT_8bit.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DCT_8bit_T.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DCZ8bitT() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4DCZ_8bit_C_full.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects4D_CZ_8bit.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DCZ_8bit_T.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate4DZT8bitT() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects4D_8bit_2pxMedian3D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks4DZT_8bit_T.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING 5D STACKS

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitX() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitY() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_Y.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate5D8bitC() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks5D_8bit_C.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitZ() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_Z.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunApplyConcatenate5D8bitT() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects5D_8bit_2pxMean3D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/ConcatenateStacks5D_8bit_T.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING 5D UNEQUAL STACKS

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitXUnequal() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitYUnequal() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_Y.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitCUnequal() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_C.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitZUnequal() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_Z.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D8bitTUnequal() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_T.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }


    // TESTING OTHER BIT DEPTHS

    @Test @Disabled
    public void testRunApplyConcatenate5D16bitX() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D32bitX() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Disabled
    public void testRunApplyConcatenate5D8and32bitX() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/imagefilter/LabelledObjects2D_8bit_2pxGauss2D.zip").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = ImageFactory.createImage("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/concatenatestacks/NoisyGradient2D_8bit_X.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks(null);
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashMap<Integer,Parameters> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<Parameters> iterator = parameterCollections.values().iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.execute(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

}