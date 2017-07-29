package wbif.sjx.ModularImageAnalysis.Object;

import org.apache.commons.math3.ml.clustering.Clusterable;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;

import java.util.ArrayList;

/**
 * Created by sc13967 on 27/07/2017.
 */
public class LocationWrapper implements Clusterable {
    private Obj object;
    private double[] location;

    public LocationWrapper(Obj object) {
        this.object = object;

        // Getting the centroid of the current object
        ArrayList<Integer> xArray = object.getCoordinates(Obj.X);
        ArrayList<Integer> yArray = object.getCoordinates(Obj.Y);
        ArrayList<Integer> zArray = object.getCoordinates(Obj.Z);
        int x = (int) MeasureObjectCentroid.calculateCentroid(xArray);
        int y = (int) MeasureObjectCentroid.calculateCentroid(yArray);
        int z = (int) MeasureObjectCentroid.calculateCentroid(zArray);

        this.location = new double[]{x,y,z};

    }

    @Override
    public double[] getPoint() {
        return location;

    }

    public Obj getObject() {
        return object;

    }
}