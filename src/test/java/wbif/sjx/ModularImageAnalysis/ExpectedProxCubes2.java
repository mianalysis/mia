package wbif.sjx.ModularImageAnalysis;

import util.opencsv.CSVReader;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ExpectedProxCubes2 extends ExpectedObjects {
    @Override
    public List<Integer[]> getCoordinates3D() {
        return getCoordinates3D("/coordinates/ExpectedProxCubes2.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
