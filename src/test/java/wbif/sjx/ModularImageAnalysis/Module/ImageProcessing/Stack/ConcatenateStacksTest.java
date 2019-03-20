package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputObjectsP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedHashSet;

import static org.junit.Assert.*;

public class ConcatenateStacksTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ConcatenateStacks<>().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ConcatenateStacks<>().getHelp());
    }


    // TESTING 2D STACKS

    @Test
    public void testRunApplyConcatenate2D8bitX() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks2D_8bit_Y.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks2D_8bit_C.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks2D_8bit_Z.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks2D_8bit_T.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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

    @Test @Ignore
    public void testRunApplyConcatenate3D8bitX() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate3D8bitY() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_Y.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects3D_8bit_2pxMean3D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks3D_8bit_C.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects3D_8bit_2pxMean3D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks3D_8bit_Z.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects3D_8bit_2pxMean3D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks3D_8bit_T.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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

    @Test @Ignore
    public void testRunApplyConcatenate4D8bitX() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate4D8bitY() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_Y.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_CT_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects4D_CT_8bit.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DCT_8bit_C.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4DCZ_8bit_C_full.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects4D_CZ_8bit.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DCZ_8bit_C.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_ZT_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects4D_8bit_2pxMedian3D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DZT_8bit_C.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_CT_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects4D_CT_8bit.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DCT_8bit_Z.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4DCZ_8bit_C_full.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects4D_CZ_8bit.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DCZ_8bit_Z.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_ZT_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects4D_8bit.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DZT_8bit_Z.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_CT_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects4D_CT_8bit.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DCT_8bit_T.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4DCZ_8bit_C_full.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects4D_CZ_8bit.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DCZ_8bit_T.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_ZT_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects4D_8bit_2pxMedian3D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks4DZT_8bit_T.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitX() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitY() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_Y.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects5D_8bit_2pxMean3D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks5D_8bit_C.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitZ() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_Z.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects5D_8bit_2pxMean3D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/ConcatenateStacks5D_8bit_T.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitXUnequal() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitYUnequal() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_Y.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Y);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitCUnequal() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_C.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.CHANNEL);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitZUnequal() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_Z.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.Z);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D8bitTUnequal() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_T.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.TIME);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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

    @Test @Ignore
    public void testRunApplyConcatenate5D16bitX() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D32bitX() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

        // Checking the images in the workspace
        assertEquals(3,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image1"));
        assertNotNull(workspace.getImage("Test_image2"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunApplyConcatenate5D8and32bitX() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = new Image("Test_image1",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ipl = IJ.openImage(pathToImage);
        inputImage = new Image("Test_image2",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ConcatenateStacks/NoisyGradient2D_8bit_X.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.updateParameterValue(ConcatenateStacks.OUTPUT_IMAGE,"Test_output");
        concatenateStacks.updateParameterValue(ConcatenateStacks.AXIS_MODE, ConcatenateStacks.AxisModes.X);

        LinkedHashSet<ParameterCollection> parameterCollections = concatenateStacks.getParameterValue(ConcatenateStacks.ADD_INPUT_IMAGE);
        Iterator<ParameterCollection> iterator = parameterCollections.iterator();
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image1");
        iterator.next().updateValue(ConcatenateStacks.INPUT_IMAGE,"Test_image2");

        // Running Module
        concatenateStacks.run(workspace);

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