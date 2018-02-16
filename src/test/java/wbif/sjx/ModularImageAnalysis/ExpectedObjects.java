package wbif.sjx.ModularImageAnalysis;

import util.opencsv.CSVReader;
import wbif.sjx.ModularImageAnalysis.Object.Measurement;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public abstract class ExpectedObjects {
    public abstract List<Integer[]> getCoordinates3D();

    public abstract HashMap<Integer,HashMap<String,Double>> getMeasurements();

    public ObjCollection getObjects(String objectName, boolean eightBit, double dppXY, double dppZ, String calibratedUnits) {
        // Initialising object store
        ObjCollection testObjects = new ObjCollection(objectName);

        // Adding all provided coordinates to each object
        List<Integer[]> coordinates = getCoordinates3D();
        for (Integer[] coordinate:coordinates) {
            int ID = eightBit ? coordinate[0] : coordinate[1];
            int x = coordinate[2];
            int y = coordinate[3];
            int z = coordinate[5];
            int t = coordinate[6];

            testObjects.putIfAbsent(ID,new Obj(objectName,ID,dppXY,dppZ,calibratedUnits));

            Obj testObject = testObjects.get(ID);
            testObject.addCoord(x,y,z);
            testObject.setT(t);

        }

        // Adding measurements to each object
        HashMap<Integer,HashMap<String,Double>> measurements = getMeasurements();
        if (measurements != null) {
            for (Obj testObject : testObjects.values()) {
                int size = testObject.getNVoxels();
                HashMap<String, Double> measurement = measurements.get(size);

                for (String measurementName : measurement.keySet()) {
                    testObject.addMeasurement(new Measurement(measurementName, measurement.get(measurementName)));
                }
            }
        }

        return testObjects;

    }

    protected List<Integer[]> getCoordinates3D(String path) {
        try {
            String pathToCoordinates = URLDecoder.decode(this.getClass().getResource(path).getPath(),"UTF-8");

            BufferedReader reader = new BufferedReader(new FileReader(pathToCoordinates));
            CSVReader csvReader = new CSVReader(reader);

            List<Integer[]> coords = new ArrayList<>();

            String[] coord = csvReader.readNext();
            while (coord != null) {
                Integer[] thisCoord = new Integer[coord.length];

                for (int j=0;j<coord.length;j++) {
                    thisCoord[j] = Integer.parseInt(coord[j]);
                }

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
