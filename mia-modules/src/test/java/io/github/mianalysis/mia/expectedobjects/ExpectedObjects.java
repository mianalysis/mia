package io.github.mianalysis.mia.expectedobjects;

import static io.github.mianalysis.mia.expectedobjects.ExpectedObjects.Mode.BINARY;
import static io.github.mianalysis.mia.expectedobjects.ExpectedObjects.Mode.SIXTEEN_BIT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import ome.units.quantity.Time;
import ome.units.unit.Unit;
import util.opencsv.CSVReader;

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
    private final double frameInterval;
    private Unit<Time> temporalUnit;

    public enum Mode {EIGHT_BIT,SIXTEEN_BIT,BINARY};

    public ExpectedObjects(VolumeType volumeType, int width, int height, int nSlices, int nFrames, double frameInterval, Unit<Time> temporalUnit) {
        this.volumeType = volumeType;
        this.width = width;
        this.height = height;
        this.nSlices = nSlices;
        this.nFrames = nFrames;
        this.frameInterval = frameInterval;
        this.temporalUnit = temporalUnit;
    }

    public abstract HashMap<Integer,HashMap<String,Double>> getMeasurements();

    public Objs getObjects(String objectName, Mode mode, double dppXY, double dppZ, String calibratedUnits, boolean includeMeasurements) throws IntegerOverflowException {
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,width,height,nSlices);

        // Initialising object store
        Objs testObjects = new Objs(objectName,calibration,nFrames,frameInterval,temporalUnit);

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
            testObjects.putIfAbsent(ID,new Obj(testObjects,volumeType,ID));

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
