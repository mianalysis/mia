package wbif.sjx.MIA.ExpectedObjects;

import util.opencsv.CSVReader;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.TSpatCal;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Track;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;


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

    /**
     * This method loads the track objects, using the second ID column as the Track ID
     * @param dppXY
     * @param dppZ
     * @param calibratedUnits
     * @return
     */
    public ObjCollection getObjects(VolumeType volumeType, String tracksName, String spotsName, double dppXY, double dppZ, String calibratedUnits) throws IntegerOverflowException {
        TSpatCal calibration = new TSpatCal(dppXY,dppZ,calibratedUnits,127,90,13,1);

        // Initialising object store
        ObjCollection trackObjects = new ObjCollection(tracksName,calibration);

        // Adding all provided coordinates to each object
        List<Integer[]> coordinates = getCoordinates5D();
        for (Integer[] coordinate:coordinates) {
            int spotID = coordinate[0];
            int trackID = coordinate[1];
            int x = coordinate[2];
            int y = coordinate[3];
            int z = coordinate[5];
            int t = coordinate[6];

            spotID = spotID+(t*65536);
            Obj spotObject = new Obj(volumeType,spotsName,spotID,calibration);
            try {
                spotObject.add(x,y,z);
            } catch (PointOutOfRangeException e) {}
            spotObject.setT(t);

            trackObjects.putIfAbsent(trackID,new Obj(volumeType,tracksName,trackID,calibration));
            Obj track = trackObjects.get(trackID);
            track.addChild(spotObject);
            spotObject.addParent(track);

        }

        return trackObjects;

    }

    public TreeMap<Integer,Track> getRawTracks(double zScaling) {
        return getTracks("/coordinates/Tracks3D.csv",zScaling);
    }

    public TreeMap<Integer,Track> getAverageTrack(double zScaling) {
        return getTracks("/coordinates/Tracks3DAverage.csv",zScaling);
    }

    public TreeMap<Integer,Track> getSubtractedTracks(double zScaling) {
        return getTracks("/coordinates/Tracks3DSubtracted.csv",zScaling);
    }

    /**
     *
     * @param path
     * @param zScaling All Z-coordinates will be multiplied by this value to convert them from the slice coordinates (as
     *                 stored in the .csv file) to true pixel coordinates.
     * @return
     */
    private TreeMap<Integer,Track> getTracks(String path, double zScaling) {
        try {
            String pathToCoordinates = URLDecoder.decode(ExpectedObjects.class.getResource(path).getPath(),"UTF-8");

            BufferedReader reader = new BufferedReader(new FileReader(pathToCoordinates));
            CSVReader csvReader = new CSVReader(reader);

            TreeMap<Integer,Track> tracks = new TreeMap<>();

            String[] coord = csvReader.readNext();
            while (coord != null) {
                int spotID = Integer.parseInt(coord[0]);
                int trackID = Integer.parseInt(coord[1]);
                double x = Double.parseDouble(coord[2]);
                double y = Double.parseDouble(coord[3]);
                double z = Double.parseDouble(coord[5])*zScaling;
                int f = Integer.parseInt(coord[6]);

                // Getting the current Track and adding the new timepoint.  Irrespective of the input calibration, Track
                // objects are always stored in pixel coordinates here.
                tracks.putIfAbsent(trackID,new Track("px"));
                Track track = tracks.get(trackID);
                track.addTimepoint(x,y,z,f);

                coord = csvReader.readNext();

            }

            return tracks;

        } catch (IOException e) {
            MIA.log.writeError(e);
            return null;
        }
    }
}