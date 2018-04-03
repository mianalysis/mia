package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedRings2D;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class MeasureCurvatureTest {
    private double tolerance = 1E-1; // As these are fit values, the tolerance is larger than usual

    @Test
    public void testGetTitle() {
        assertNotNull(new MeasureCurvature().getTitle());
    }

    @Test
    public void testRunCircleNoRelate10PxRadius2DLOESS() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

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
        MeasureCurvature measureCurvature = new MeasureCurvature();
        measureCurvature.updateParameterValue(MeasureCurvature.INPUT_OBJECTS,"Input_obj");
        measureCurvature.updateParameterValue(MeasureCurvature.REFERENCE_IMAGE,"Ref_image");
        measureCurvature.updateParameterValue(MeasureCurvature.SPLINE_FITTING_METHOD, MeasureCurvature.SplineFittingMethods.LOESS);
        measureCurvature.updateParameterValue(MeasureCurvature.N_NEIGHBOURS,10);
        measureCurvature.updateParameterValue(MeasureCurvature.ITERATIONS,100);
        measureCurvature.updateParameterValue(MeasureCurvature.ACCURACY,1d);
        measureCurvature.updateParameterValue(MeasureCurvature.RELATE_TO_REFERENCE_POINT,false);
        measureCurvature.updateParameterValue(MeasureCurvature.X_REF_MEASUREMENT,"");
        measureCurvature.updateParameterValue(MeasureCurvature.Y_REF_MEASUREMENT,"");
        measureCurvature.updateParameterValue(MeasureCurvature.ABSOLUTE_CURVATURE,true);
        measureCurvature.updateParameterValue(MeasureCurvature.DRAW_SPLINE,false);
        measureCurvature.updateParameterValue(MeasureCurvature.MAX_CURVATURE,0d);
        measureCurvature.updateParameterValue(MeasureCurvature.APPLY_TO_IMAGE,false);
        measureCurvature.updateParameterValue(MeasureCurvature.SHOW_IMAGE,false);

        // Running the module
        measureCurvature.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @Test
    public void testRunCircleRelateAntiClockwise10PxRadius2DLOESS() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

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
        MeasureCurvature measureCurvature = new MeasureCurvature();
        measureCurvature.updateParameterValue(MeasureCurvature.INPUT_OBJECTS,"Input_obj");
        measureCurvature.updateParameterValue(MeasureCurvature.REFERENCE_IMAGE,"Ref_image");
        measureCurvature.updateParameterValue(MeasureCurvature.SPLINE_FITTING_METHOD, MeasureCurvature.SplineFittingMethods.LOESS);
        measureCurvature.updateParameterValue(MeasureCurvature.N_NEIGHBOURS,10);
        measureCurvature.updateParameterValue(MeasureCurvature.ITERATIONS,100);
        measureCurvature.updateParameterValue(MeasureCurvature.ACCURACY,1d);
        measureCurvature.updateParameterValue(MeasureCurvature.RELATE_TO_REFERENCE_POINT,true);
        measureCurvature.updateParameterValue(MeasureCurvature.X_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_X_ACW.name());
        measureCurvature.updateParameterValue(MeasureCurvature.Y_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_Y_ACW.name());
        measureCurvature.updateParameterValue(MeasureCurvature.ABSOLUTE_CURVATURE,true);
        measureCurvature.updateParameterValue(MeasureCurvature.SIGNED_CURVATURE,true);
        measureCurvature.updateParameterValue(MeasureCurvature.DRAW_SPLINE,false);
        measureCurvature.updateParameterValue(MeasureCurvature.MAX_CURVATURE,0.2d);
        measureCurvature.updateParameterValue(MeasureCurvature.APPLY_TO_IMAGE,false);
        measureCurvature.updateParameterValue(MeasureCurvature.SHOW_IMAGE,false);

        // Running the module
        measureCurvature.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFACW_PX.name()).getValue();
            actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_SIGNED_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFACW_CAL.name()).getValue();
            actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_SIGNED_CURVATURE_CAL).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @Test
    public void testRunCircleRelateClockwise10PxRadius2DLOESS() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

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
        MeasureCurvature measureCurvature = new MeasureCurvature();
        measureCurvature.updateParameterValue(MeasureCurvature.INPUT_OBJECTS,"Input_obj");
        measureCurvature.updateParameterValue(MeasureCurvature.REFERENCE_IMAGE,"Ref_image");
        measureCurvature.updateParameterValue(MeasureCurvature.SPLINE_FITTING_METHOD, MeasureCurvature.SplineFittingMethods.LOESS);
        measureCurvature.updateParameterValue(MeasureCurvature.N_NEIGHBOURS,10);
        measureCurvature.updateParameterValue(MeasureCurvature.ITERATIONS,100);
        measureCurvature.updateParameterValue(MeasureCurvature.ACCURACY,1d);
        measureCurvature.updateParameterValue(MeasureCurvature.RELATE_TO_REFERENCE_POINT,true);
        measureCurvature.updateParameterValue(MeasureCurvature.X_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_X_CW.name());
        measureCurvature.updateParameterValue(MeasureCurvature.Y_REF_MEASUREMENT,ExpectedRings2D.Measures.EXP_REF_Y_CW.name());
        measureCurvature.updateParameterValue(MeasureCurvature.ABSOLUTE_CURVATURE,true);
        measureCurvature.updateParameterValue(MeasureCurvature.SIGNED_CURVATURE,true);
        measureCurvature.updateParameterValue(MeasureCurvature.DRAW_SPLINE,false);
        measureCurvature.updateParameterValue(MeasureCurvature.MAX_CURVATURE,0.2d);
        measureCurvature.updateParameterValue(MeasureCurvature.APPLY_TO_IMAGE,false);
        measureCurvature.updateParameterValue(MeasureCurvature.SHOW_IMAGE,false);

        // Running the module
        measureCurvature.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFCW_PX.name()).getValue();
            actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_SIGNED_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE_REFCW_CAL.name()).getValue();
            actual = testObject.getMeasurement(MeasureCurvature.Measurements.MEAN_SIGNED_CURVATURE_CAL).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @Test @Ignore
    public void testRunCircle10PxRadius2DStandard() throws Exception {
    }
}