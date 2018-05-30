package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedRings2D;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class MeasureObjectCurvatureTest {
    private double tolerance = 1E-1; // As these are fit values, the tolerance is larger than usual

    @Test
    public void testGetTitle() {
        assertNotNull(new MeasureObjectCurvature().getTitle());
    }

    @Test
    public void testRunCircleNoRelate10PxRadius2DLOESS() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj = new ExpectedRings2D().getObjects("Input_obj",true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryRing9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        MeasureObjectCurvature measureObjectCurvature = new MeasureObjectCurvature();
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_OBJECTS,"Input_obj");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.REFERENCE_IMAGE,"Ref_image");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SPLINE_FITTING_METHOD, MeasureObjectCurvature.SplineFittingMethods.LOESS);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.N_NEIGHBOURS,10);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ITERATIONS,100);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ACCURACY,1d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.RELATE_TO_REFERENCE_POINT,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.X_REF_MEASUREMENT,"");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.Y_REF_MEASUREMENT,"");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ABSOLUTE_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.DRAW_SPLINE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.MAX_CURVATURE,0d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.APPLY_TO_IMAGE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SHOW_IMAGE,false);

        // Running the module
        measureObjectCurvature.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @Test
    public void testRunCircleRelateAntiClockwise10PxRadius2DLOESS() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj = new ExpectedRings2D().getObjects("Input_obj",true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryRing9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        MeasureObjectCurvature measureObjectCurvature = new MeasureObjectCurvature();
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_OBJECTS,"Input_obj");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.REFERENCE_IMAGE,"Ref_image");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SPLINE_FITTING_METHOD, MeasureObjectCurvature.SplineFittingMethods.LOESS);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.N_NEIGHBOURS,10);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ITERATIONS,100);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ACCURACY,1d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.RELATE_TO_REFERENCE_POINT,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.X_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_X_ACW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.Y_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_Y_ACW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ABSOLUTE_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SIGNED_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.DRAW_SPLINE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.MAX_CURVATURE,0.2d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.APPLY_TO_IMAGE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SHOW_IMAGE,false);

        // Running the module
        measureObjectCurvature.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFACW_PX.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFACW_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @Test
    public void testRunCircleRelateClockwise10PxRadius2DLOESS() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj = new ExpectedRings2D().getObjects("Input_obj",true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryRing9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        MeasureObjectCurvature measureObjectCurvature = new MeasureObjectCurvature();
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_OBJECTS,"Input_obj");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.REFERENCE_IMAGE,"Ref_image");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SPLINE_FITTING_METHOD, MeasureObjectCurvature.SplineFittingMethods.LOESS);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.N_NEIGHBOURS,10);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ITERATIONS,100);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ACCURACY,1d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.RELATE_TO_REFERENCE_POINT,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.X_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_X_CW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.Y_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_Y_CW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ABSOLUTE_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SIGNED_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.DRAW_SPLINE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.MAX_CURVATURE,0.2d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.APPLY_TO_IMAGE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SHOW_IMAGE,false);

        // Running the module
        measureObjectCurvature.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFCW_PX.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFCW_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @Test @Ignore
    public void testRunCircle10PxRadius2DStandard() throws Exception {
    }
}