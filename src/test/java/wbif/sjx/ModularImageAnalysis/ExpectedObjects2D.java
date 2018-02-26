package wbif.sjx.ModularImageAnalysis;

import wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 21/02/2018.
 */
public class ExpectedObjects2D extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates3D() {
        return getCoordinates3D("/coordinates/ExpectedObjects2D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
