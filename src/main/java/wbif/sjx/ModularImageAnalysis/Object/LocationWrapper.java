//package wbif.sjx.ModularImageAnalysis.Object;
//
//import org.apache.commons.math3.ml.clustering.Clusterable;
//import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
//
//import java.util.ArrayList;
//
///**
// * Created by sc13967 on 27/07/2017.
// */
//public class LocationWrapper implements Clusterable {
//    private Obj object;
//    private double[] location;
//
//    public LocationWrapper(Obj object) {
//        this.object = object;
//
//        // Getting the centroid of the current object
//        int x = (int) Math.round(object.getXMean(true));
//        int y = (int) Math.round(object.getYMean(true));
//        int z = (int) Math.round(object.getZMean(true,true));
//
//        this.location = new double[]{x,y,z};
//
//    }
//
//    @Override
//    public double[] getPoint() {
//        return location;
//
//    }
//
//    public Obj getObject() {
//        return object;
//
//    }
//}