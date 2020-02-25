package wbif.sjx.MIA.ExpectedObjects;

import util.opencsv.CSVReader;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.TSpatCal;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static wbif.sjx.MIA.ExpectedObjects.ExpectedObjects.Mode.BINARY;
import static wbif.sjx.MIA.ExpectedObjects.ExpectedObjects.Mode.SIXTEEN_BIT;

/**
 * Created by sc13967 on 12/02/2018.
 */
public abstract class ExpectedObjects {
    public abstract List<Integer[]> getCoordinates5D();
    private final VolumeType volumeType;
    private final int width;
    private final int height;
    private final int nSlices;
    private final int nFrames;

    public enum Mode {EIGHT_BIT,SIXTEEN_BIT,BINARY};

    public ExpectedObjects(VolumeType volumeType, int width, int height, int nSlices, int nFrames) {
        this.volumeType = volumeType;
        this.width = width;
        this.height = height;
        this.nSlices = nSlices;
        this.nFrames = nFrames;
    }

    public abstract HashMap<Integer,HashMap<String,Double>> getMeasurements();

    public ObjCollection getObjects(String objectName, Mode mode, double dppXY, double dppZ, String calibratedUnits, boolean includeMeasurements) throws IntegerOverflowException {
        TSpatCal calibration = new TSpatCal(dppXY,dppZ,calibratedUnits,width,height,nSlices,nFrames);

        // Initialising object store
        ObjCollection testObjects = new ObjCollection(objectName,calibration);

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
            testObjects.putIfAbsent(ID,new Obj(volumeType,objectName,ID,calibration));

            Obj testObject = testObjects.get(ID);
            try {
                testObject.add(x,y,z);
            } catch (PointOutOfRangeException e) {}
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
            MIA.log.writeError(e);
            return null;
        }
    }
}
