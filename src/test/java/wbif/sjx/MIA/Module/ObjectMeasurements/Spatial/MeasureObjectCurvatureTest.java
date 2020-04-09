package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Rings2D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Volume.VolumeType;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class MeasureObjectCurvatureTest extends ModuleTest {
    private double tolerance = 1E-1; // As these are fit values, the tolerance is larger than usual

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectCurvature(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunCircleNoRelate10PxRadius2DLOESS(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj = new Rings2D(volumeType).getObjects("Input_obj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryRing9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image", ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        MeasureObjectCurvature measureObjectCurvature = new MeasureObjectCurvature(new ModuleCollection());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_OBJECTS,"Input_obj");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_IMAGE,"Ref_image");
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

        // Running the module
        measureObjectCurvature.execute(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunCircleRelateAntiClockwise10PxRadius2DLOESS(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj = new Rings2D(volumeType).getObjects("Input_obj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryRing9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        MeasureObjectCurvature measureObjectCurvature = new MeasureObjectCurvature(new ModuleCollection());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_OBJECTS,"Input_obj");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_IMAGE,"Ref_image");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SPLINE_FITTING_METHOD, MeasureObjectCurvature.SplineFittingMethods.LOESS);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.N_NEIGHBOURS,10);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ITERATIONS,100);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ACCURACY,1d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.RELATE_TO_REFERENCE_POINT,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.X_REF_MEASUREMENT, Rings2D.Measures.EXP_REF_X_ACW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.Y_REF_MEASUREMENT, Rings2D.Measures.EXP_REF_Y_ACW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ABSOLUTE_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SIGNED_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.DRAW_SPLINE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.MAX_CURVATURE,0.2d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.APPLY_TO_IMAGE,false);

        // Running the module
        measureObjectCurvature.execute(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

            expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_REFACW_PX.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_REFACW_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunCircleRelateClockwise10PxRadius2DLOESS(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj = new Rings2D(volumeType).getObjects("Input_obj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryRing9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        MeasureObjectCurvature measureObjectCurvature = new MeasureObjectCurvature(new ModuleCollection());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_OBJECTS,"Input_obj");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.INPUT_IMAGE,"Ref_image");
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SPLINE_FITTING_METHOD, MeasureObjectCurvature.SplineFittingMethods.LOESS);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.N_NEIGHBOURS,10);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ITERATIONS,100);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ACCURACY,1d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.RELATE_TO_REFERENCE_POINT,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.X_REF_MEASUREMENT, Rings2D.Measures.EXP_REF_X_CW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.Y_REF_MEASUREMENT, Rings2D.Measures.EXP_REF_Y_CW.name());
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.ABSOLUTE_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.SIGNED_CURVATURE,true);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.DRAW_SPLINE,false);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.MAX_CURVATURE,0.2d);
        measureObjectCurvature.updateParameterValue(MeasureObjectCurvature.APPLY_TO_IMAGE,false);

        // Running the module
        measureObjectCurvature.execute(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_ABS_PX.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_ABS_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_ABSOLUTE_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

            expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_REFCW_PX.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_PX).getValue();
            assertEquals(expected, actual, tolerance);

            expected = testObject.getMeasurement(Rings2D.Measures.EXP_MEAN_CURVATURE_REFCW_CAL.name()).getValue();
            actual = testObject.getMeasurement(Units.replace(MeasureObjectCurvature.Measurements.MEAN_SIGNED_CURVATURE_CAL)).getValue();
            assertEquals(expected, actual, tolerance/dppXY);

        }
    }

    @Test @Disabled
    public void testRunCircle10PxRadius2DStandard() throws Exception {
    }
}