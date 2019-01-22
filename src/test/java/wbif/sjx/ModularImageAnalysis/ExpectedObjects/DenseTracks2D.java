package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

public class DenseTracks2D extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/DenseTracks2D.csv");
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
