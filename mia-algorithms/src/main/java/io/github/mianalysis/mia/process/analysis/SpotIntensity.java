package io.github.mianalysis.mia.process.analysis;

//No need to use cumulative statistics, as this doesn't really make sense for a single spot.

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import ij.process.ImageProcessor;
import io.github.mianalysis.mia.object.coordinates.voxel.MidpointCircle;

public class SpotIntensity {
    private double[] int_arr;

    public SpotIntensity(ImageProcessor ipr, double x_in, double y_in, double r_in) {
        int im_w = ipr.getWidth();
        int im_h = ipr.getHeight();

        int x = (int) Math.round(x_in);
        int y = (int) Math.round(y_in);
        int r = (int) Math.round(r_in);

        MidpointCircle mpc = new MidpointCircle(r);
        int[] xx = mpc.getXCircleFill();
        int[] yy = mpc.getYCircleFill();

        ArrayList<Float> temp_al = new ArrayList<Float>();

        for (int j=0;j<xx.length;j++) {
            if (x+xx[j]>=0 & x+xx[j]<im_w & y+yy[j]>=0 & y+yy[j]<im_h) {
                float temp = ipr.getPixelValue((x+xx[j]), (y+yy[j]));
                temp_al.add(temp);
            }
        }

        int_arr = new double[temp_al.size()];
        for (int j=0;j<temp_al.size();j++) {
            int_arr[j] = temp_al.get(j);
        }
    }

    public double getMeanPointIntensity() {
        return new Mean().evaluate(int_arr);
    }

    public double getStdevPointIntensity() {
        return new StandardDeviation().evaluate(int_arr);
    }

    public double getSumPointIntensity() {
        return new Sum().evaluate(int_arr);
    }

    public double getNPointsMeasured(){
        return int_arr.length;
    }

    public double getMinimumPointIntensity() {
        return new Min().evaluate(int_arr);
    }

    public double getMaximumPointIntensity() {
        return new Max().evaluate(int_arr);
    }
}