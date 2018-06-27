package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects2D;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class MeasureImageColocalisationTest {
    private double tolerance = 1E-2;

    @Test
    public void getFullName() {
        String inputImage2Name = "Im2";
        String measurement = MeasureImageColocalisation.Measurements.MEAN_PCC;

        String expected = "COLOCALISATION // Im2_MEAN_PCC";
        String actual = MeasureImageColocalisation.getFullName(inputImage2Name,measurement);

        assertEquals(expected,actual);
    }

    @Test
    public void measurePCCWholeImage() throws Exception {
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

        // Initialising the module
        MeasureImageColocalisation measureImageColocalisation = new MeasureImageColocalisation();
        measureImageColocalisation.updateParameterValue(MeasureImageColocalisation.INPUT_IMAGE_1,"Im1");
        measureImageColocalisation.updateParameterValue(MeasureImageColocalisation.INPUT_IMAGE_2,"Im2");
        measureImageColocalisation.updateParameterValue(MeasureImageColocalisation.MASKING_MODE,false);

        // Running the analysis
        measureImageColocalisation.measurePCC(image1,image2,null);

        // Calculating PCC (expected value from Coloc2)
        double expected = 0.44;
        double actual = image1.getMeasurement(MeasureImageColocalisation.getFullName("Im2",MeasureImageColocalisation.Measurements.MEAN_PCC)).getValue();

        // Testing returned value
        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void measurePCCMaskImage() throws Exception {
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

    @Test
    public void getTitle() {
        assertNotNull(new MeasureImageColocalisation().getTitle());
    }
}