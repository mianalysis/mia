package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedCircles2D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedSpots3D;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureImageIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement.MergeObjects;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class SplineAnalysisTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() {
        assertNotNull(new SplineAnalysis().getTitle());
    }

    @Test
    public void testRunCircle10PxRadius2DLOESS() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj = new ExpectedCircles2D().getObjects("Input_obj",true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryCircle9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        SplineAnalysis splineAnalysis = new SplineAnalysis();
        splineAnalysis.updateParameterValue(SplineAnalysis.INPUT_OBJECTS,"Input_obj");
        splineAnalysis.updateParameterValue(SplineAnalysis.REFERENCE_IMAGE,"Ref_image");
        splineAnalysis.updateParameterValue(SplineAnalysis.SPLINE_FITTING_METHOD,SplineAnalysis.SplineFittingMethods.LOESS);
        splineAnalysis.updateParameterValue(SplineAnalysis.N_NEIGHBOURS,10);
        splineAnalysis.updateParameterValue(SplineAnalysis.ITERATIONS,100);
        splineAnalysis.updateParameterValue(SplineAnalysis.ACCURACY,1d);
        splineAnalysis.updateParameterValue(SplineAnalysis.RELATE_TO_REFERENCE_POINT,false);
        splineAnalysis.updateParameterValue(SplineAnalysis.X_REF_MEASUREMENT,"");
        splineAnalysis.updateParameterValue(SplineAnalysis.Y_REF_MEASUREMENT,"");
        splineAnalysis.updateParameterValue(SplineAnalysis.DRAW_SPLINE,false);
        splineAnalysis.updateParameterValue(SplineAnalysis.MAX_CURVATURE,0d);
        splineAnalysis.updateParameterValue(SplineAnalysis.APPLY_TO_IMAGE,false);
        splineAnalysis.updateParameterValue(SplineAnalysis.SHOW_IMAGE,false);

        // Running the module
        splineAnalysis.run(workspace,false);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedCircles2D.Measures.EXP_MEAN_CURVATURE.name()).getValue();
            double actual = testObject.getMeasurement(SplineAnalysis.Measurements.MEAN_CURVATURE).getValue();
            assertEquals(expected, actual, tolerance);

        }
    }

    @Test @Ignore
    public void testRunCircle10PxRadius2DLOESSRelate() throws Exception {
    }

    @Test @Ignore
    public void testRunCircle10PxRadius2DStandard() throws Exception {
    }
}