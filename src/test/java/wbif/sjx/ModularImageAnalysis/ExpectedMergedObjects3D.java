package wbif.sjx.ModularImageAnalysis;

import java.util.HashMap;
import java.util.List;

public class ExpectedMergedObjects3D extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates3D() {
        return getCoordinates3D("/coordinates/ExpectedMergedObjects3D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
