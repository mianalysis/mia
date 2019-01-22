package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Stephen Cross on 10/09/2017.
 */
public class Spots3D extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Spots3D.csv");
    }

    @Override
    public boolean is2D() {
        return false;
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
