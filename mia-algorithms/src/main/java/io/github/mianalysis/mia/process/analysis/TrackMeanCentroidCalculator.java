// package io.github.mianalysis.mia.process.analysis;

// import io.github.mianalysis.mia.object.coordinates.Point;
// import io.github.mianalysis.mia.object.coordinates.tracks.Timepoint;
// import io.github.mianalysis.mia.object.coordinates.tracks.Track;
// import io.github.mianalysis.mia.object.coordinates.tracks.TrackCollection;
// import io.github.mianalysis.mia.process.math.CumStat;

// public class TrackMeanCentroidCalculator {
//     public Point getMeanPoint(TrackCollection trackCollection, int frame) {
//         CumStat[] cs = new CumStat[3];

//         for (int i=0;i<3;i++) cs[i] = new CumStat();

//         for (Track track:trackCollection.values()) {
//             if (track.hasFrame(frame)) {
//                 cs[0].addMeasure(track.get(frame).getX());
//                 cs[1].addMeasure(track.get(frame).getY());
//                 cs[2].addMeasure(track.get(frame).getZ());

//             }
//         }

//         return new Timepoint(cs[0].getMean(),cs[1].getMean(),cs[2].getMean(),frame);

//     }
// }
