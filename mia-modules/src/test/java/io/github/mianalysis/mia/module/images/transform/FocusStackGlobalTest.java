package io.github.mianalysis.mia.module.images.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;

/**
 * Created by Stephen Cross on 28/02/2019.
 */

public class FocusStackGlobalTest extends ModuleTest {
    private double tolerance = 1E-10;

    @Override
    public void testGetHelp() {
        assertNotNull(new FocusStackGlobal<>(null).getDescription());
    }


    @Test
    public void testGetMaxStatSliceStdevFirstChannel5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);

        int actual = FocusStackGlobal.getOptimalStatSlice(image,0,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(5,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,1,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(6,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,2,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(3,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,3,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(10,actual);

    }

    @Test
    public void testGetMaxStatSliceStdevSecondChannel5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);

        int actual = FocusStackGlobal.getOptimalStatSlice(image,0,1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(0,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,1,1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(8,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,2,1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(7,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,3,1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(11,actual);

    }

    @Test
    public void testGetMaxStatSliceStdevBothChannels5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);

        int actual = FocusStackGlobal.getOptimalStatSlice(image,0,-1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(0,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,1,-1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(6,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,2,-1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(7,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,3,-1, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(10,actual);

    }

    @Test
    public void testGetMaxStatSliceStdevOnlyChannel() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_C1_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);

        int actual = FocusStackGlobal.getOptimalStatSlice(image,0,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(5,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,1,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(6,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,2,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(3,actual);

        actual = FocusStackGlobal.getOptimalStatSlice(image,3,0, FocusStackGlobal.Stat.STDEV, FocusStackGlobal.MinMaxMode.MAX);
        assertEquals(10,actual);

    }



    @Test
    public void testGetEmptyImageBelowAndAbove5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,-3,4);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(2,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(8,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Z));
        assertEquals(0.1,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageBelowOnly5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,-3,0);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(2,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Z));
        assertEquals(0.1,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageAboveOnly5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,0,4);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(2,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(5,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Z));
        assertEquals(0.1,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageSinglePlane5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,0,0);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(2,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageAllBelow5D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,-5,-2);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(2,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageBelowAndAboveSingleChannel4D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_C1_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,-3,4);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(8,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Z));
        assertEquals(0.1,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageSinglePlaneSingleChannel4D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_C1_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,0,0);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(4,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageBelowAndAboveSingleTimepoint4D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus4D_T1_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,-3,4);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(2,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(8,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Z));
        assertEquals(0.1,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageSinglePlaneSingleTimepoint4D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus4D_T1_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,0,0);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(2,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageBelowAndAbove3D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,-3,4);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(8,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Z));
        assertEquals(0.1,axis.calibratedValue(1),tolerance);

    }

    @Test
    public void testGetEmptyImageSinglePlane3D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        ImgPlus inputImg = inputImage.getImgPlus();

        ImgPlus actualImg = FocusStackGlobal.getEmptyImage(inputImg,0,0);

        assertNotNull(actualImg);
        assertEquals(64,actualImg.dimension(actualImg.dimensionIndex(Axes.X)));
        assertEquals(76,actualImg.dimension(actualImg.dimensionIndex(Axes.Y)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.CHANNEL)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.Z)));
        assertEquals(1,actualImg.dimension(actualImg.dimensionIndex(Axes.TIME)));

        DefaultLinearAxis axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.X));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

        axis = (DefaultLinearAxis) actualImg.axis(actualImg.dimensionIndex(Axes.Y));
        assertEquals(0.02,axis.calibratedValue(1),tolerance);

    }



    @Test
    public void testRunFirstChannelBelowAndAbove5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_C1_belowabove.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,-3);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL,1);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunSecondChannelBelowAndAbove5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_C2_belowabove.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,-3);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL,2);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunBothChannelsBelowAndAbove5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_bothC_belowabove.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,-3);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_ALL);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunFirstChannelBothBelow5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_C1_bothbelow.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,-5);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,-2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL,1);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunFirstChannelSinglePlane5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_C1_singleplane.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,0);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,0);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL,1);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunFirstChannelBelowAndAboveIndicesInverted5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_C1_belowabove.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,-3);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL, 1);

        // Running Module O
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunFirstChannelBelowAndAboveSingleChannel4D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_C1_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_C1_belowabove_C1.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,-3);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL,1);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunFirstChannelBelowAndAboveSingleTimepoint4D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus4D_T1_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus5D_8bit_C1_belowabove_T1.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,-3);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL,1);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunFirstChannelBelowAndAbove3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image inputImage = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(inputImage);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/bestfocussubstack/BestFocus3D_8bit_C1_belowabove.zip").getPath(),"UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        FocusStackGlobal bestFocusSubstack = new FocusStackGlobal(new Modules());
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.INPUT_IMAGE,"Test_image");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.OUTPUT_IMAGE,"Test_output");
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.BEST_FOCUS_CALCULATION, FocusStackGlobal.BestFocusCalculations.MAX_STDEV);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_START_SLICE,-3);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.RELATIVE_END_SLICE,2);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL_MODE, FocusStackGlobal.ChannelModes.USE_SINGLE);
        bestFocusSubstack.updateParameterValue(FocusStackGlobal.CHANNEL,1);

        // Running Module
        bestFocusSubstack.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}