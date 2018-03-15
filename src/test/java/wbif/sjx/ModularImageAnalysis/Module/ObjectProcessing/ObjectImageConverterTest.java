package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

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
    public void testConvertObjectsToImagebit3DWithRefImage() throws Exception {
        // Initialising parameters
        String colourMode = ObjectImageConverter.ColourModes.ID;

        // Setting object parameters
        String objectName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Initialising object store
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(objectName,false,dppXY,dppZ,calibratedUnits,false);

        // Loading a reference image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);

        // Converting objects to image
        HashMap<Integer,Float> hues = testObjects.getHue(colourMode,"","",false);
        Image testImage = testObjects.convertObjectsToImage("Test image",ipl,colourMode,hues,false);

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
            ipl.setPosition(1,z+1,1);
            testImage.getImagePlus().setPosition(1,z+1,1);

            float[][] referenceArray = ipl.getProcessor().getFloatArray();
            float[][] testArray = testImage.getImagePlus().getProcessor().getFloatArray();

            assertArrayEquals(referenceArray, testArray);

        }
    }

    /**
     * Takes provided objects and converts to an image using another image as a reference
     * @throws Exception
     */
    @Test
    public void testConvertObjectsToImagebit3DWithNoRefImage() throws Exception {
        // Initialising parameters
        String colourMode = ObjectImageConverter.ColourModes.ID;

        // Setting object parameters
        String objectName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Initialising object store
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(objectName,false,dppXY,dppZ,calibratedUnits,false);

        // Loading a reference image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects3D_32bit_NoRef.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);

        // Converting objects to image
        HashMap<Integer,Float> hues = testObjects.getHue(colourMode,"","",false);
        Image testImage = testObjects.convertObjectsToImage("Test image",ipl,colourMode,hues,false);

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

        // Running through each image, comparing the bytes to those of an expected image
        for (int z = 0;z<12;z++) {
            ipl.setPosition(1,z+1,1);
            testImage.getImagePlus().setPosition(1,z+1,1);

            float[][] referenceArray = ipl.getProcessor().getFloatArray();
            float[][] testArray = testImage.getImagePlus().getProcessor().getFloatArray();

            assertArrayEquals(referenceArray, testArray);

        }
    }

    /**
     * Tests the ability to take an image containing labelled pixels and turn it into an ObjCollection.
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
        ObjCollection actualObjects = image.convertImageToObjects(testObjectsName);

        // Checking objects have been assigned
        assertNotNull("Testing converted objects are not null",actualObjects);

        // Checking there are the expected number of objects
        assertEquals("Testing the number of converted objects",8,actualObjects.size());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new ExpectedObjects3D().getObjects("Expected",true,dppXY,dppZ,calibratedUnits,true);

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    /**
     * Tests the ability to take an image containing labelled pixels and turn it into an ObjCollection.
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
        ObjCollection actualObjects = image.convertImageToObjects(testObjectsName);

        // Checking objects have been assigned
        assertNotNull("Testing converted objects are not null",actualObjects);

        // Checking there are the expected number of objects
        assertEquals("Testing the number of converted objects",8,actualObjects.size());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new ExpectedObjects3D().getObjects("Expected",true,dppXY,dppZ,calibratedUnits,true);

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

}