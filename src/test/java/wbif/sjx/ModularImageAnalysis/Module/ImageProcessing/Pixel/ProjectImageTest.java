package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.InputOutput.ImageLoader;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.junit.Assert.*;

public class ProjectImageTest {
    @Test
    public void testGetTitle() {
        assertNotNull(new ProjectImage().getTitle());
    }

    @Test
    public void testRunMaxZ2D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient2D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRunMaxZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient3D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRunMaxZ4D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(4,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test @Ignore
    public void testRunMaxZ5D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient5D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(4,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRunMaxZ3D16bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient3D_ZMaxProj_16bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(16,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRunMaxZ3D32bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient3D_ZMaxProj_32bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(32,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test @Ignore
    public void testRunMinZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZMinProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MIN);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test @Ignore
    public void testRunAverageZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZAvProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.AVERAGE);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test @Ignore
    public void testRunMedianZ3D() throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZMedProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MEDIAN);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test @Ignore
    public void testRunStdevZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZStdevProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.STDEV);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test @Ignore
    public void testRunSumZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZSumProj_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Output_image");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.SUM);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output_image"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output_image").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }
}