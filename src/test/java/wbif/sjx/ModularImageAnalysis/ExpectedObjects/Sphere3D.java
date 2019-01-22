package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 23/03/2018.
 */
public class Sphere3D extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Sphere3D.csv");
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
