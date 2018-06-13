package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 13/11/2017.
 */
public class FilterImageTest {

    // GENERAL TESTS

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new FilterImage().getTitle());
    }


    // TESTING DIFFERENCE OF GAUSSIAN FILTER

    @Test @Ignore
    public void testRunDoG2DFilter2DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunDoG2DFilter5DStack() throws Exception {
    }


    // TESTING 2D GAUSSIAN FILTER

    @Test
    public void testRunGaussian2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
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
    public void testRunGaussian2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects5D_8bit_2pxGauss2D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
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


    // TESTING 3D GAUSSIAN FILTER

    /**
     * Tests the module doesn't crash if a 2D image is passed to the 3D Gaussian filter
     * @throws Exception
     */
    @Test
    public void testRunGaussian3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxGauss3D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
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
    public void testRunGaussian3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects5D_8bit_2pxGauss3D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
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


    // TESTING 2D RANK FILTERS

    @Test
    public void testRunMedian2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxMedian2D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
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
    public void testRunMedian2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects5D_8bit_2pxMedian2D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
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
    public void testRunVariance2DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxVariance2D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.VARIANCE2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
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
    public void testRunVariance2DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects5D_8bit_2pxVariance2D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.VARIANCE2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
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


    // TESTING 3D FILTERS

    /**
     * Tests the module doesn't crash if a 2D image is passed to the 3D median filter
     * @throws Exception
     */
    @Test
    public void testRunMedian3DFilter2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects2D_8bit_2pxMedian3D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
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
    public void testRunMedian3DFilter3DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects3D_8bit_2pxMedian3D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
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
    public void testRunMedian3DFilter4DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects4D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects4D_8bit_2pxMedian3D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
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
    public void testRunMedian3DFilter5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageFilter/LabelledObjects5D_8bit_2pxMedian3D.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.MEDIAN3D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        // Running BinaryOperations
        filterImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
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


    // TESTING ROLLING FRAME FILTER

    /**
     * Tests the rolling frame filter (designed for image stacks with more than 1 timepoint)
     * @throws Exception
     */
    @Test @Ignore
    public void testRunRollingFrameFilter5DStack() throws Exception {
    }

    /**
     * Verifyig the output when the stack only contains 1 timepoint, but a 3D stack (need to check it doesn't average
     * over Z)
     * @throws Exception
     */
    @Test @Ignore
    public void testRunRollingFrameFilterOneTimepoint() throws Exception {
    }

}