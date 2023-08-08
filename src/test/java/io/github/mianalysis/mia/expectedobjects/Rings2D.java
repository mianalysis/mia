package io.github.mianalysis.mia.expectedobjects;

import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

public class Rings2D extends ExpectedObjects {
    public Rings2D(VolumeType volumeType) {
        super(volumeType, 64, 76, 1, 1, 0.02, UNITS.SECOND);
    }

    public enum Measures {
        EXP_MEAN_CURVATURE_ABS_PX, EXP_MIN_CURVATURE_ABS_PX, EXP_MAX_CURVATURE_ABS_PX, EXP_STD_CURVATURE_ABS_PX,
        EXP_MEAN_CURVATURE_ABS_CAL, EXP_MIN_CURVATURE_ABS_CAL, EXP_MAX_CURVATURE_ABS_CAL, EXP_STD_CURVATURE_ABS_CAL,
        EXP_MEAN_CURVATURE_REFACW_PX, EXP_MIN_CURVATURE_REFACW_PX, EXP_MAX_CURVATURE_REFACW_PX, EXP_STD_CURVATURE_REFACW_PX,
        EXP_MEAN_CURVATURE_REFACW_CAL, EXP_MIN_CURVATURE_REFACW_CAL, EXP_MAX_CURVATURE_REFACW_CAL, EXP_STD_CURVATURE_REFACW_CAL,
        EXP_MEAN_CURVATURE_REFCW_PX, EXP_MIN_CURVATURE_REFCW_PX, EXP_MAX_CURVATURE_REFCW_PX, EXP_STD_CURVATURE_REFCW_PX,
        EXP_MEAN_CURVATURE_REFCW_CAL, EXP_MIN_CURVATURE_REFCW_CAL, EXP_MAX_CURVATURE_REFCW_CAL, EXP_STD_CURVATURE_REFCW_CAL,
        EXP_SPLINE_LENGTH_PX, EXP_SPLINE_LENGTH_CAL, EXP_FIRST_POINT_X_PX, EXP_FIRST_POINT_Y_PX,
        EXP_REL_LOC_OF_MIN_CURVATURE, EXP_REL_LOC_OF_MAX_CURVATURE, EXP_REF_X_ACW, EXP_REF_Y_ACW, EXP_REF_X_CW,
        EXP_REF_Y_CW
    }

    public Objs getObjects(String objectName, Mode mode, double dppXY, double dppZ, String calibratedUnits, boolean includeMeasurements) throws IntegerOverflowException {
        return super.getObjects(objectName, mode, dppXY, dppZ, calibratedUnits, includeMeasurements);
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Rings2D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        HashMap<Integer,HashMap<String,Double>> expectedValues = new HashMap<>();

        HashMap<String,Double> obj = new HashMap<>();
        obj.put(Measures.EXP_MEAN_CURVATURE_ABS_PX.name(),0.11d);
        obj.put(Measures.EXP_MIN_CURVATURE_ABS_PX.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_ABS_PX.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_ABS_PX.name(),0d);
        obj.put(Measures.EXP_MEAN_CURVATURE_ABS_CAL.name(),5.5d);
        obj.put(Measures.EXP_MIN_CURVATURE_ABS_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_ABS_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_ABS_CAL.name(),0d);

        obj.put(Measures.EXP_MEAN_CURVATURE_REFACW_PX.name(),-0.11d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFACW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFACW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFACW_PX.name(),0d);
        obj.put(Measures.EXP_MEAN_CURVATURE_REFACW_CAL.name(),-5.5d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFACW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFACW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFACW_CAL.name(),0d);

        obj.put(Measures.EXP_MEAN_CURVATURE_REFCW_PX.name(),0.11d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFCW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFCW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFCW_PX.name(),0d);
        obj.put(Measures.EXP_MEAN_CURVATURE_REFCW_CAL.name(),5.5d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFCW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFCW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFCW_CAL.name(),0d);

        obj.put(Measures.EXP_REF_X_ACW.name(),30d);
        obj.put(Measures.EXP_REF_Y_ACW.name(),20d);
        obj.put(Measures.EXP_REF_X_CW.name(),40d);
        obj.put(Measures.EXP_REF_Y_CW.name(),18d);

        obj.put(Measures.EXP_SPLINE_LENGTH_PX.name(),Double.NaN);
        obj.put(Measures.EXP_SPLINE_LENGTH_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_FIRST_POINT_X_PX.name(),Double.NaN);
        obj.put(Measures.EXP_FIRST_POINT_Y_PX.name(),Double.NaN);
        obj.put(Measures.EXP_REL_LOC_OF_MIN_CURVATURE.name(),Double.NaN);
        obj.put(Measures.EXP_REL_LOC_OF_MAX_CURVATURE.name(),Double.NaN);
        expectedValues.put(1,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_MEAN_CURVATURE_ABS_PX.name(),0.045d);
        obj.put(Measures.EXP_MIN_CURVATURE_ABS_PX.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_ABS_PX.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_ABS_PX.name(),0d);
        obj.put(Measures.EXP_MEAN_CURVATURE_ABS_CAL.name(),2.25d);
        obj.put(Measures.EXP_MIN_CURVATURE_ABS_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_ABS_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_ABS_CAL.name(),0d);

        obj.put(Measures.EXP_MEAN_CURVATURE_REFACW_PX.name(),-0.045d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFACW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFACW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFACW_PX.name(),0d);
        obj.put(Measures.EXP_MEAN_CURVATURE_REFACW_CAL.name(),-2.25d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFACW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFACW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFACW_CAL.name(),0d);

        obj.put(Measures.EXP_MEAN_CURVATURE_REFCW_PX.name(),0.045d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFCW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFCW_PX.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFCW_PX.name(),0d);
        obj.put(Measures.EXP_MEAN_CURVATURE_REFCW_CAL.name(),2.25d);
        obj.put(Measures.EXP_MIN_CURVATURE_REFCW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_MAX_CURVATURE_REFCW_CAL.name(),Double.NaN);
        obj.put(Measures.EXP_STD_CURVATURE_REFCW_CAL.name(),0d);

        obj.put(Measures.EXP_REF_X_ACW.name(),52d);
        obj.put(Measures.EXP_REF_Y_ACW.name(),38d);
        obj.put(Measures.EXP_REF_X_CW.name(),40d);
        obj.put(Measures.EXP_REF_Y_CW.name(),50d);

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
