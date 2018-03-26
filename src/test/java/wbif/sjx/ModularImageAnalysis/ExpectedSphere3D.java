package wbif.sjx.ModularImageAnalysis;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 23/03/2018.
 */
public class ExpectedSphere3D extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates3D() {
        return getCoordinates3D("/coordinates/ExpectedSphere3D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
