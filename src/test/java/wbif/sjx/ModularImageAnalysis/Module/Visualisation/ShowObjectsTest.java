package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Process.ColourFactory;

import java.net.URLDecoder;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 02/09/2017.
 */
public class ShowObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new ConvertObjectsToImage().getTitle());

    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ConvertObjectsToImage().getHelp());

    }

    /**
     * Takes provided objects and converts to an image using another image as a reference
     * @throws Exception
     */
    @Test
    public void testConvertObjectsToImagebit3DWithRefImage() throws Exception {
        // Initialising parameters
        String colourMode = ConvertObjectsToImage.ColourModes.ID;

        // Setting object parameters
        String objectName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Initialising object store
        ObjCollection testObjects = new Objects3D().getObjects(objectName, ExpectedObjects.Mode.SIXTEEN_BIT,dppXY,dppZ,calibratedUnits,false);

        // Loading a reference image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);

        // Converting objects to image
        HashMap<Integer,Float> hues = ColourFactory.getIDHues(testObjects,false);
        Image testImage = testObjects.convertObjectsToImage("Test image",new Image("Image",ipl),hues,32,false);

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
        String colourMode = ConvertObjectsToImage.ColourModes.ID;

        // Setting object parameters
        String objectName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Initialising object store
        ObjCollection testObjects = new Objects3D().getObjects(objectName,ExpectedObjects.Mode.SIXTEEN_BIT,dppXY,dppZ,calibratedUnits,false);

        // Loading a reference image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_32bit_NoRef.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);

        // Converting objects to image
        HashMap<Integer,Float> hues = ColourFactory.getIDHues(testObjects,false);
        Image testImage = testObjects.convertObjectsToImage("Test image",new Image("Image",ipl),hues,32,false);

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

}