package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjSet;

import java.net.URLDecoder;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 02/09/2017.
 */
public class ObjectImageConverterTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ObjectImageConverter().getTitle());

    }

    /**
     * Takes provided objects and converts to an image using another image as a reference
     * @throws Exception
     */
    @Test
    public void testConvertObjectsToImage16bit3DWithRefImage() throws Exception {
        // Initialising parameters
        String colourMode = ObjectImageConverter.ColourModes.ID;
        String colourSource = ""; // This isn't required for COLOUR_MODES[3] (ID)
        boolean hideMissing = false;

        // Setting object parameters
        String objectName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Initialising object store
        ObjSet testObjects = ExpectedObjects3D.getObjects(objectName,false,dppXY,dppZ,calibratedUnits);

        // Loading a reference image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image referenceImage = new Image("Reference image",ipl);

        // Converting objects to image
        Image testImage = ObjectImageConverter.convertObjectsToImage(testObjects,"Test image",referenceImage,colourMode,colourSource,hideMissing);

        // Testing the resultant image is the expected size
        ImagePlus testImagePlus = testImage.getImagePlus();
        assertEquals(64,testImagePlus.getWidth());
        assertEquals(76,testImagePlus.getHeight());
        assertEquals(1,testImagePlus.getNFrames());
        assertEquals(12,testImagePlus.getNSlices());
        assertEquals(1,testImagePlus.getNChannels());

        // Testing the spatial calibration of the new image
        assertEquals(0.02,testImagePlus.getCalibration().getX(1),tolerance);
        assertEquals(0.02,testImagePlus.getCalibration().getY(1),tolerance);
        assertEquals(0.1,testImagePlus.getCalibration().getZ(1),tolerance);

        // Running through each image, comparing the bytes to those of an expected image
        for (int z = 0;z<12;z++) {
            referenceImage.getImagePlus().setPosition(1,z+1,1);
            testImage.getImagePlus().setPosition(1,z+1,1);

            int[][] referenceArray = referenceImage.getImagePlus().getProcessor().getIntArray();
            int[][] testArray = testImage.getImagePlus().getProcessor().getIntArray();

            assertArrayEquals(referenceArray, testArray);

        }
    }

    /**
     * Takes provided objects and converts to an image using another image as a reference
     * @throws Exception
     */
    @Test
    public void testConvertObjectsToImage16bit3DWithNoRefImage() throws Exception {
        // Initialising parameters
        String colourMode = ObjectImageConverter.ColourModes.ID;
        String colourSource = ""; // This isn't required for COLOUR_MODES[3] (ID)
        boolean hideMissing = false;

        // Setting object parameters
        String objectName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Initialising object store
        ObjSet testObjects = ExpectedObjects3D.getObjects(objectName,false,dppXY,dppZ,calibratedUnits);

        // Loading a reference image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects3D_16bit_NoRef.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image referenceImage = new Image("Reference image",ipl);

        // Converting objects to image
        Image testImage = ObjectImageConverter.convertObjectsToImage(testObjects,"Test image",null,colourMode,colourSource,hideMissing);

        // Testing the resultant image is the expected size
        ImagePlus testImagePlus = testImage.getImagePlus();
        assertEquals(58,testImagePlus.getWidth());
        assertEquals(76,testImagePlus.getHeight());
        assertEquals(1,testImagePlus.getNFrames());
        assertEquals(12,testImagePlus.getNSlices());
        assertEquals(1,testImagePlus.getNChannels());

        // Testing the spatial calibration of the new image
        assertEquals(0.02,testImagePlus.getCalibration().getX(1),tolerance);
        assertEquals(0.02,testImagePlus.getCalibration().getY(1),tolerance);
        assertEquals(0.1,testImagePlus.getCalibration().getZ(1),tolerance);
        new ImageJ();

        // Running through each image, comparing the bytes to those of an expected image
        for (int z = 0;z<12;z++) {
            referenceImage.getImagePlus().setPosition(1,z+1,1);
            testImage.getImagePlus().setPosition(1,z+1,1);

            int[][] referenceArray = referenceImage.getImagePlus().getProcessor().getIntArray();
            int[][] testArray = testImage.getImagePlus().getProcessor().getIntArray();

            assertArrayEquals(referenceArray, testArray);

        }
    }

    /**
     * Tests the ability to take an image containing labelled pixels and turn it into an ObjSet.
     * @throws Exception
     */
    @Test
    public void testConvertImageToObjects8bit3D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);

        // Setting other parameters
        String testObjectsName = "Test objects";

        // Running the method to be tested
        ObjSet testObjects = ObjectImageConverter.convertImageToObjects(image, testObjectsName);

        // Checking objects have been assigned
        assertNotNull("Testing converted objects are not null",testObjects);

        // Checking there are the expected number of objects
        assertEquals("Testing the number of converted objects",8,testObjects.size());

        // Checking the spatial calibration and coordinate limits of each object
        HashMap<Integer, HashMap<ExpectedObjects3D.Measures, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        for (Obj object:testObjects.values()) {
            // Getting the number of voxels in this object (this is used as the key for the expected values map)
            int nVoxels = object.getNVoxels();

            // Getting the relevant measures
            HashMap<ExpectedObjects3D.Measures, Object> expected = expectedValues.get(nVoxels);
            assertNotNull("Null means no expected object with the specified number of voxels",expected);

            // Testing coordinate ranges
            int[][] coordinateRange = object.getCoordinateRange();
            assertEquals("X-min",(int) expected.get(ExpectedObjects3D.Measures.X_MIN),coordinateRange[0][0],tolerance);
            assertEquals("X-max",(int) expected.get(ExpectedObjects3D.Measures.X_MAX),coordinateRange[0][1],tolerance);
            assertEquals("Y-min",(int) expected.get(ExpectedObjects3D.Measures.Y_MIN),coordinateRange[1][0],tolerance);
            assertEquals("Y-max",(int) expected.get(ExpectedObjects3D.Measures.Y_MAX),coordinateRange[1][1],tolerance);
            assertEquals("X-min",(int) expected.get(ExpectedObjects3D.Measures.Z_MIN),coordinateRange[2][0],tolerance);
            assertEquals("Y-max",(int) expected.get(ExpectedObjects3D.Measures.Z_MAX),coordinateRange[2][1],tolerance);
            assertEquals("F",(int) expected.get(ExpectedObjects3D.Measures.F),object.getT(),tolerance);

            // Checking the objects have the correct spatial calibration
            double dppXY = object.getDistPerPxXY();
            double dppZ = object.getDistPerPxZ();
            assertEquals("Spatial calibration in XY",0.02,dppXY,tolerance);
            assertEquals("Spatial calibration in Z",0.1,dppZ,tolerance);

        }

    }

    /**
     * Tests the ability to take an image containing labelled pixels and turn it into an ObjSet.
     * @throws Exception
     */
    @Test
    public void testConvertImageToObjects16bit3D() throws Exception {
        // Loading the test image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);

        // Setting other parameters
        String testObjectsName = "Test objects";

        // Running the method to be tested
        ObjSet testObjects = ObjectImageConverter.convertImageToObjects(image, testObjectsName);

        // Checking objects have been assigned
        assertNotNull("Testing converted objects are not null",testObjects);

        // Checking there are the expected number of objects
        assertEquals("Testing the number of converted objects",8,testObjects.size());

        // Checking the spatial calibration and coordinate limits of each object
        HashMap<Integer, HashMap<ExpectedObjects3D.Measures, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        for (Obj object:testObjects.values()) {
            // Getting the number of voxels in this object (this is used as the key for the expected values map)
            int nVoxels = object.getNVoxels();

            // Getting the relevant measures
            HashMap<ExpectedObjects3D.Measures, Object> expected = expectedValues.get(nVoxels);
            assertNotNull("Null means no expected object with the specified number of voxels",expected);

            // Testing coordinate ranges
            int[][] coordinateRange = object.getCoordinateRange();
            assertEquals("X-min",(int) expected.get(ExpectedObjects3D.Measures.X_MIN),coordinateRange[0][0],tolerance);
            assertEquals("X-max",(int) expected.get(ExpectedObjects3D.Measures.X_MAX),coordinateRange[0][1],tolerance);
            assertEquals("Y-min",(int) expected.get(ExpectedObjects3D.Measures.Y_MIN),coordinateRange[1][0],tolerance);
            assertEquals("Y-max",(int) expected.get(ExpectedObjects3D.Measures.Y_MAX),coordinateRange[1][1],tolerance);
            assertEquals("X-min",(int) expected.get(ExpectedObjects3D.Measures.Z_MIN),coordinateRange[2][0],tolerance);
            assertEquals("Y-max",(int) expected.get(ExpectedObjects3D.Measures.Z_MAX),coordinateRange[2][1],tolerance);
            assertEquals("F",(int) expected.get(ExpectedObjects3D.Measures.F),object.getT(),tolerance);

            // Checking the objects have the correct spatial calibration
            double dppXY = object.getDistPerPxXY();
            double dppZ = object.getDistPerPxZ();
            assertEquals("Spatial calibration in XY",0.02,dppXY,tolerance);
            assertEquals("Spatial calibration in Z",0.1,dppZ,tolerance);

        }

    }

    @Test @Ignore
    public void testRunObjectToImage() throws Exception {

    }

    @Test @Ignore
    public void testRunImageToObject() throws Exception {

    }
}