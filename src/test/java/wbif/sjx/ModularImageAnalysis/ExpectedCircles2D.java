package wbif.sjx.ModularImageAnalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ExpectedCircles2D extends ExpectedObjects {
    public enum Measures { EXP_MEAN_CURVATURE,EXP_MIN_CURVATURE, EXP_MAX_CURVATURE, EXP_STD_CURVATURE,
        EXP_SPLINE_LENGTH_PX, EXP_SPLINE_LENGTH_CAL, EXP_FIRST_POINT_X_PX, EXP_FIRST_POINT_Y_PX,
        EXP_REL_LOC_OF_MIN_CURVATURE, EXP_REL_LOC_OF_MAX_CURVATURE
    }

    @Override
    public List<Integer[]> getCoordinates3D() {
        return getCoordinates3D("/coordinates/ExpectedCircles2D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        HashMap<Integer,HashMap<String,Double>> expectedValues = new HashMap<>();

        HashMap<String,Double> obj = new HashMap<>();
        obj.put(Measures.EXP_MEAN_CURVATURE.name(),0.11d);
        obj.put(Measures.EXP_MIN_CURVATURE.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE.name(),0d);
        obj.put(Measures.EXP_SPLINE_LENGTH_PX.name(),Double.NaN);
        obj.put(Measures.EXP_SPLINE_LENGTH_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_FIRST_POINT_X_PX.name(),Double.NaN);
        obj.put(Measures.EXP_FIRST_POINT_Y_PX.name(),Double.NaN);
        obj.put(Measures.EXP_REL_LOC_OF_MIN_CURVATURE.name(),Double.NaN);
        obj.put(Measures.EXP_REL_LOC_OF_MAX_CURVATURE.name(),Double.NaN);
        expectedValues.put(1,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_MEAN_CURVATURE.name(),0.045d);
        obj.put(Measures.EXP_MIN_CURVATURE.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE.name(),0d);
        obj.put(Measures.EXP_SPLINE_LENGTH_PX.name(),Double.NaN);
        obj.put(Measures.EXP_SPLINE_LENGTH_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_FIRST_POINT_X_PX.name(),Double.NaN);
        obj.put(Measures.EXP_FIRST_POINT_Y_PX.name(),Double.NaN);
        obj.put(Measures.EXP_REL_LOC_OF_MIN_CURVATURE.name(),Double.NaN);
        obj.put(Measures.EXP_REL_LOC_OF_MAX_CURVATURE.name(),Double.NaN);
        expectedValues.put(2,obj);

        return expectedValues;

    }
}
