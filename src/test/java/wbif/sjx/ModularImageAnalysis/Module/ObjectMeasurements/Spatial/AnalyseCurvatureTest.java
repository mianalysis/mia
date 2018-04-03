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
public class AnalyseCurvatureTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() {
        assertNotNull(new AnalyseCurvature().getTitle());
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
        ObjCollection inputObj = new ExpectedRings2D().getObjects("Input_obj",true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj);

        // Loading the reference image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryRing9p5pxRadius2D.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Ref_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        AnalyseCurvature analyseCurvature = new AnalyseCurvature();
        analyseCurvature.updateParameterValue(AnalyseCurvature.INPUT_OBJECTS,"Input_obj");
        analyseCurvature.updateParameterValue(AnalyseCurvature.REFERENCE_IMAGE,"Ref_image");
        analyseCurvature.updateParameterValue(AnalyseCurvature.SPLINE_FITTING_METHOD, AnalyseCurvature.SplineFittingMethods.LOESS);
        analyseCurvature.updateParameterValue(AnalyseCurvature.N_NEIGHBOURS,10);
        analyseCurvature.updateParameterValue(AnalyseCurvature.ITERATIONS,100);
        analyseCurvature.updateParameterValue(AnalyseCurvature.ACCURACY,1d);
        analyseCurvature.updateParameterValue(AnalyseCurvature.RELATE_TO_REFERENCE_POINT,false);
        analyseCurvature.updateParameterValue(AnalyseCurvature.X_REF_MEASUREMENT,"");
        analyseCurvature.updateParameterValue(AnalyseCurvature.Y_REF_MEASUREMENT,"");
        analyseCurvature.updateParameterValue(AnalyseCurvature.DRAW_SPLINE,false);
        analyseCurvature.updateParameterValue(AnalyseCurvature.MAX_CURVATURE,0d);
        analyseCurvature.updateParameterValue(AnalyseCurvature.APPLY_TO_IMAGE,false);
        analyseCurvature.updateParameterValue(AnalyseCurvature.SHOW_IMAGE,false);

        // Running the module
        analyseCurvature.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:inputObj.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedRings2D.Measures.EXP_MEAN_CURVATURE.name()).getValue();
            double actual = testObject.getMeasurement(AnalyseCurvature.Measurements.MEAN_CURVATURE).getValue();
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