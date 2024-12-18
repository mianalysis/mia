package io.github.mianalysis.mia.module.visualisation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;
import java.util.HashMap;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.convert.ConvertObjectsToImage;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.process.ColourFactory;

/**
 * Created by Stephen Cross on 02/09/2017.
 */

public class ShowObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;


    @Override
    public void testGetHelp() {
        assertNotNull(new ConvertObjectsToImage(null).getDescription());

    }

    /**
     * Takes provided objects and converts to an image using another image as a reference
     * @throws Exception
     */
    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testConvertObjectsToImagebit3D(VolumeTypes volumeType) throws Exception {
        // Initialising parameters
        String colourMode = ConvertObjectsToImage.ColourModes.ID;

        // Setting object parameters
        String objectName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Initialising object store
        Objs testObjects = new Objects3D(VolumeTypes.getFactory(volumeType)).getObjects(objectName, ExpectedObjects.Mode.SIXTEEN_BIT,dppXY,dppZ,calibratedUnits,false);

        // Loading a reference image
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);

        // Converting objects to image
        HashMap<Integer,Float> hues = ColourFactory.getIDHues(testObjects,false);
        ImageI testImage = testObjects.convertToImage("Test image",hues,32,false);

        // Testing the resultant image is the expected size
        ImagePlus testImagePlus = testImage.getImagePlus();
        assertEquals(64,testImagePlus.getWidth());
        assertEquals(76,testImagePlus.getHeight());
        assertEquals(1,testImagePlus.getNFrames());
        assertEquals(12,testImagePlus.getNSlices());
        assertEquals(1,testImagePlus.getNChannels());

        // Testing the spatial calibration of the new image
        assertEquals(0.02,testImagePlus.getCalibration().pixelWidth,tolerance);
        assertEquals(0.02,testImagePlus.getCalibration().pixelHeight,tolerance);
        assertEquals(0.1,testImagePlus.getCalibration().pixelDepth,tolerance);

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