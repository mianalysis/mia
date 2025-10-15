package io.github.mianalysis.mia.expectedobjects;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.ObjFactories;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.tracks.Track;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import ome.units.UNITS;
import util.opencsv.CSVReader;

/**
 * Created by Stephen Cross on 09/08/2018.
 */
public class Tracks3D {
    public List<Integer[]> getCoordinates5D() {
        return ExpectedObjects.getCoordinates5D("/coordinates/Tracks3D.csv");
    }

    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }

    public ObjsI getObjects(VolumeTypes volumeType, String tracksName, String spotsName, double dppXY, double dppZ,
            String calibratedUnits) throws IntegerOverflowException {
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 127, 90, 13);

        // Initialising object store
        ObjsI spotObjects = ObjsFactories.getDefaultFactory().createFromSpatCal("Spots", calibration, 10, 0.02, UNITS.SECOND);
        ObjsI trackObjects = ObjsFactories.getDefaultFactory().createFromSpatCal(tracksName, calibration, 10, 0.02, UNITS.SECOND);

        // Adding all provided coordinates to each object
        List<Integer[]> coordinates = getCoordinates5D();
        for (Integer[] coordinate : coordinates) {
            int spotID = coordinate[0];
            int trackID = coordinate[1];
            int x = coordinate[2];
            int y = coordinate[3];
            int z = coordinate[5];
            int t = coordinate[6];

            spotID = spotID + (t * 65536);
            ObjI spotObject = spotObjects.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), spotID);
            try {
                spotObject.addCoord(x, y, z);
            } catch (PointOutOfRangeException e) {
            }
            spotObject.setT(t);

            trackObjects.putIfAbsent(trackID, ObjFactories.getDefaultFactory().createObj(trackObjects,
                    VolumeTypes.getFactory(volumeType), trackID));
            ObjI track = trackObjects.get(trackID);
            track.addChild(spotObject);
            spotObject.addParent(track);

        }

        return trackObjects;

    }

    public TreeMap<Integer, Track> getRawTracks(double zScaling) {
        return getTracks("/coordinates/Tracks3D.csv", zScaling);
    }

    public TreeMap<Integer, Track> getAverageTrack(double zScaling) {
        return getTracks("/coordinates/Tracks3DAverage.csv", zScaling);
    }

    public TreeMap<Integer, Track> getSubtractedTracks(double zScaling) {
        return getTracks("/coordinates/Tracks3DSubtracted.csv", zScaling);
    }

    private TreeMap<Integer, Track> getTracks(String path, double zScaling) {
        try {
            String pathToCoordinates = URLDecoder.decode(ExpectedObjects.class.getResource(path).getPath(), "UTF-8");

            BufferedReader reader = new BufferedReader(new FileReader(pathToCoordinates));
            CSVReader csvReader = new CSVReader(reader);

            TreeMap<Integer, Track> tracks = new TreeMap<>();

            String[] coord = csvReader.readNext();
            while (coord != null) {
                int trackID = Integer.parseInt(coord[1]);
                double x = Double.parseDouble(coord[2]);
                double y = Double.parseDouble(coord[3]);
                double z = Double.parseDouble(coord[5]) * zScaling;
                int f = Integer.parseInt(coord[6]);

                // Getting the current Track and adding the new timepoint. Irrespective of the
                // input calibration, Track
                // objects are always stored in pixel coordinates here.
                tracks.putIfAbsent(trackID, new Track("px"));
                Track track = tracks.get(trackID);
                track.addTimepoint(x, y, z, f);

                coord = csvReader.readNext();

            }

            return tracks;

        } catch (IOException e) {
            MIA.log.writeError(e);
            return null;
        }
    }
}