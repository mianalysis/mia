package io.github.mianalysis.mia.module.imagemeasurements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.Object.Image;
import io.github.mianalysis.mia.Object.Workspace;
import io.github.mianalysis.mia.Object.Workspaces;

public class MeasureImageIntensityTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageIntensity(null).getDescription());
    }

    @Test
    public void testRun2DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(null);
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");

        // Running MeasureImageIntensity
        measureImageIntensity.execute(workspace);

        // Verifying results
        assertEquals(7,image.getMeasurements().size());
        assertEquals(126.1, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(73.76, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(613349d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun2DImage16bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(null);
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");

        // Running MeasureImageIntensity
        measureImageIntensity.execute(workspace);

        // Verifying results
        assertEquals(7,image.getMeasurements().size());
        assertEquals(25209.9, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(20, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(52287, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(14743.32, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(122620952d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun3DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(null);
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");

        // Running MeasureImageIntensity
        measureImageIntensity.execute(workspace);

        // Verifying results
        assertEquals(7,image.getMeasurements().size());
        assertEquals(126.03, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(73.69, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(7355854d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun4DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_ZT_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(null);
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");

        // Running MeasureImageIntensity
        measureImageIntensity.execute(workspace);

        // Verifying results
        assertEquals(7,image.getMeasurements().size());
        assertEquals(126.10, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(73.50, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(29440126d, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }

    @Test
    public void testRun5DImage8bit() throws UnsupportedEncodingException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureImageIntensity
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(null);
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,"Test_image");

        // Running MeasureImageIntensity
        measureImageIntensity.execute(workspace);

        // Verifying results
        assertEquals(7,image.getMeasurements().size());
        assertEquals(126.10, image.getMeasurement(MeasureImageIntensity.Measurements.MEAN).getValue(),tolerance);
        assertEquals(0, image.getMeasurement(MeasureImageIntensity.Measurements.MIN).getValue(),tolerance);
        assertEquals(255, image.getMeasurement(MeasureImageIntensity.Measurements.MAX).getValue(),tolerance);
        assertEquals(72.92, image.getMeasurement(MeasureImageIntensity.Measurements.STDEV).getValue(),tolerance);
        assertEquals(58880064, image.getMeasurement(MeasureImageIntensity.Measurements.SUM).getValue(),tolerance);

    }
}