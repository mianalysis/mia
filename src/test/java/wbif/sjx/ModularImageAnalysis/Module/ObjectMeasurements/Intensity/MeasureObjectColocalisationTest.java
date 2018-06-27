package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects2D;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class MeasureObjectColocalisationTest {
    private double tolerance  = 1E-2;

    @Test
    public void getFullName() {
        String inputImage1Name = "image1";
        String inputImage2Name = "Im2";
        String measurement = MeasureObjectColocalisation.Measurements.PCC;

        String expected = "COLOCALISATION // image1_Im2_PCC";
        String actual = MeasureObjectColocalisation.getFullName(inputImage1Name,inputImage2Name,measurement);

        assertEquals(expected,actual);

    }

    @Test
    public void measurePCC() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(expectedObjects);

        // Loading images
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel1_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl1 = IJ.openImage(pathToImage);
        Image image1 = new Image("Im1",ipl1);
        workspace.addImage(image1);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureColocalisation/ColocalisationChannel2_2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl2 = IJ.openImage(pathToImage);
        Image image2 = new Image("Im2",ipl2);
        workspace.addImage(image2);

        // Initialising the Module
        MeasureObjectColocalisation measureObjectColocalisation = new MeasureObjectColocalisation();
        measureObjectColocalisation.initialiseParameters();
        measureObjectColocalisation.updateParameterValue(MeasureObjectColocalisation.INPUT_OBJECTS,"Expected");
        measureObjectColocalisation.updateParameterValue(MeasureObjectColocalisation.INPUT_IMAGE_1,"Im1");
        measureObjectColocalisation.updateParameterValue(MeasureObjectColocalisation.INPUT_IMAGE_2,"Im2");

        // Running the module
        measureObjectColocalisation.run(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet("Expected"));
        assertEquals(8,workspace.getObjectSet("Expected").size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        String measurementName = MeasureObjectColocalisation.getFullName("Im1","Im2",MeasureObjectColocalisation.Measurements.PCC);
        for (Obj testObject:expectedObjects.values()) {
            double expected = testObject.getMeasurement(Objects2D.Measures.PCC.name()).getValue();
            double actual = testObject.getMeasurement(measurementName).getValue();
            assertEquals("Measurement value", expected, actual, tolerance);

        }
    }

    @Test
    public void getTitle() {
        assertNotNull(new MeasureObjectColocalisation().getTitle());
    }
}