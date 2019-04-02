package wbif.sjx.MIA.Module.ImageMeasurements;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects2D;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class MeasureImageColocalisationTest extends ModuleTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetFullName() {
        String inputImage2Name = "Im2";
        String measurement = MeasureImageColocalisation.Measurements.MEAN_PCC;

        String expected = "COLOCALISATION // Im2_MEAN_PCC";
        String actual = MeasureImageColocalisation.getFullName(inputImage2Name,measurement);

        assertEquals(expected,actual);
    }

    @Test
    public void testGetMaskImageNone() throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage);
        Image image1 = new Image("Im1",ipl1);

        Image actual = MeasureImageColocalisation.getMaskImage(expectedObjects,image1,MeasureImageColocalisation.MaskingModes.NONE);

        assertNull(actual);

    }

    @Test
    public void testGetMaskImageInsideObjects() throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage);
        Image image1 = new Image("Im1",ipl1);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus expectedIpl = IJ.openImage(pathToImage);
        Image expectedImage = new Image("Expected",expectedIpl);

        Image actual = MeasureImageColocalisation.getMaskImage(expectedObjects,image1,MeasureImageColocalisation.MaskingModes.MEASURE_INSIDE_OBJECTS);

        assertEquals(expectedImage,actual);

    }

    @Test
    public void testGetMaskImageOutsideObjects() throws Exception {
        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage);
        Image image1 = new Image("Im1",ipl1);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus expectedIpl = IJ.openImage(pathToImage);
        Image expectedImage = new Image("Expected",expectedIpl);

        Image actual = MeasureImageColocalisation.getMaskImage(expectedObjects,image1,MeasureImageColocalisation.MaskingModes.MEASURE_OUTSIDE_OBJECTS);

        assertEquals(expectedImage,actual);

    }

    @Test
    public void testMeasurePCCWholeImage() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage);
        Image image1 = new Image("Im1",ipl1);
        workspace.addImage(image1);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel2_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl2 = IJ.openImage(pathToImage);
        Image image2 = new Image("Im2",ipl2);
        workspace.addImage(image2);

        // Running the analysis
        MeasureImageColocalisation.measurePCC(image1,image2,null);

        // Calculating PCC (expected value from Coloc2)
        double expected = 0.44;
        double actual = image1.getMeasurement(MeasureImageColocalisation.getFullName("Im2",MeasureImageColocalisation.Measurements.MEAN_PCC)).getValue();

        // Testing returned value
        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testMeasurePCCMaskImage() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage);
        Image image1 = new Image("Im1",ipl1);
        workspace.addImage(image1);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel2_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl2 = IJ.openImage(pathToImage);
        Image image2 = new Image("Im2",ipl2);
        workspace.addImage(image2);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus maskIpl = IJ.openImage(pathToImage);
        Image maskImage = new Image("Mask",maskIpl);
        workspace.addImage(maskImage);

        // Running the analysis
        MeasureImageColocalisation.measurePCC(image1,image2,maskImage);

        // Calculating PCC (expected value from Coloc2)
        double expected = 0.28;
        double actual = image1.getMeasurement(MeasureImageColocalisation.getFullName("Im2",MeasureImageColocalisation.Measurements.MEAN_PCC)).getValue();

        // Testing returned value
        assertEquals(expected,actual,tolerance);

    }

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureImageColocalisation().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageColocalisation().getHelp());
    }
}