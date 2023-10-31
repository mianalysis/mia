package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

import io.github.mianalysis.mia.object.coordinates.tracks.Track;
import io.github.mianalysis.mia.object.coordinates.tracks.TrackCollection;

/**
 * Created by sc13967 on 22/01/2018.
 */
public class NearestNeighbourCalculator {
    public TreeMap<Integer,double[]> calculate(Track track, TrackCollection testTracks) {
        TreeMap<Integer,double[]> links = new TreeMap<>();

        // Iterating over all frames in this track
        for (int f:track.getF()) {
            // Getting coordinates for this track
            double x1 = track.getX(f);
            double y1 = track.getY(f);
            double z1 = track.getZ(f);

            double minDist = Double.MAX_VALUE;
            double nnID = -1;

            // Iterating over all other tracks
            for (int ID:testTracks.keySet()) {
                Track testTrack = testTracks.get(ID);

                // We don't want to compare our track to itself
                if (track == testTrack) continue;

                // If the test track doesn't have this time frame, skip it
                if (!testTrack.containsKey(f)) continue;

                // Getting coordinates for the comparison (test) track at the same frame
                double x2 = testTrack.getX(f);
                double y2 = testTrack.getY(f);
                double z2 = testTrack.getZ(f);

                // Calculating the distance
                double distance = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));

                if (distance < minDist) {
                    minDist = distance;
                    nnID = ID;
                }
            }

            // Adding the link to the results structure
            links.put(f,new double[]{nnID,minDist});
        }

        return links;

    }
}