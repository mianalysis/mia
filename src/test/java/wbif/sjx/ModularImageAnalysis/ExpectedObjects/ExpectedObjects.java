package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import util.opencsv.CSVReader;
import wbif.sjx.ModularImageAnalysis.Object.Measurement;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects.Mode.BINARY;
import static wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects.Mode.SIXTEEN_BIT;

/**
 * Created by sc13967 on 12/02/2018.
 */
public abstract class ExpectedObjects {
    private boolean is2D;
    public abstract List<Integer[]> getCoordinates5D();
    public abstract boolean is2D();

    public enum Mode {EIGHT_BIT,SIXTEEN_BIT,BINARY};

    public ExpectedObjects() {
        this.is2D = is2D();
    }

    public abstract HashMap<Integer,HashMap<String,Double>> getMeasurements();

    public ObjCollection getObjects(String objectName, Mode mode, double dppXY, double dppZ, String calibratedUnits, boolean includeMeasurements) {
        // Initialising object store
        ObjCollection testObjects = new ObjCollection(objectName);

        // Adding all provided coordinates to each object
        List<Integer[]> coordinates = getCoordinates5D();
        for (Integer[] coordinate:coordinates) {
            int ID = 255;
            switch (mode) {
                case BINARY:
                    ID = 255;
                    break;

                case EIGHT_BIT:
                    ID = coordinate[0];
                    break;

                case SIXTEEN_BIT:
                    ID = coordinate[1];
                    break;
            }

            int x = coordinate[2];
            int y = coordinate[3];
            int z = coordinate[5];
            int t = coordinate[6];

            ID = ID+(t*65536);
            testObjects.putIfAbsent(ID,new Obj(objectName,ID,dppXY,dppZ,calibratedUnits,is2D));

            Obj testObject = testObjects.get(ID);
            testObject.addCoord(x,y,z);
            testObject.setT(t);

        }

        // Adding measurements to each object
        if (includeMeasurements &! mode.equals(BINARY) &! mode.equals(SIXTEEN_BIT)) {
            HashMap<Integer, HashMap<String, Double>> measurements = getMeasurements();
            if (measurements != null) {
                for (Obj testObject : testObjects.values()) {
                    int ID = testObject.getID();
                    HashMap<String, Double> measurement = measurements.get(ID);

                    for (String measurementName : measurement.keySet()) {
                        testObject.addMeasurement(new Measurement(measurementName, measurement.get(measurementName)));
                    }
                }
            }
        }

        return testObjects;

    }

    protected static List<Integer[]> getCoordinates5D(String path) {
        try {
            String pathToCoordinates = URLDecoder.decode(ExpectedObjects.class.getResource(path).getPath(),"UTF-8");

            BufferedReader reader = new BufferedReader(new FileReader(pathToCoordinates));
            CSVReader csvReader = new CSVReader(reader);

            List<Integer[]> coords = new ArrayList<>();

            String[] coord = csvReader.readNext();
            while (coord != null) {
                Integer[] thisCoord = new Integer[coord.length];

                for (int j=0;j<coord.length;j++) thisCoord[j] = Integer.parseInt(coord[j]);

                coords.add(thisCoord);
                coord = csvReader.readNext();
            }

            return coords;

        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
}
