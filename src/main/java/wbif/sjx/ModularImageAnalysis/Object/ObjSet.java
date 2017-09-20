package wbif.sjx.ModularImageAnalysis.Object;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class ObjSet extends LinkedHashMap<Integer,Obj> {
    private String name;
    private int maxID = 0;

    public ObjSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Obj object) {
        put(object.getID(),object);

    }

    public int getNextID() {
        maxID++;
        return maxID;
    }

    public int[][] getSpatialLimits() {
        int[][] limits = new int[][]{
                {Integer.MAX_VALUE,Integer.MIN_VALUE},
                {Integer.MAX_VALUE,Integer.MIN_VALUE},
                {Integer.MAX_VALUE,Integer.MIN_VALUE}};

        for (Obj object:values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            for (int i=0;i<x.size();i++) {
                limits[0][0] = Math.min(limits[0][0],x.get(i));
                limits[0][1] = Math.max(limits[0][1],x.get(i));
                limits[1][0] = Math.min(limits[1][0],y.get(i));
                limits[1][1] = Math.max(limits[1][1],y.get(i));
                limits[2][0] = Math.min(limits[2][0],z.get(i));
                limits[2][1] = Math.max(limits[2][1],z.get(i));

            }
        }

        return limits;

    }

    public int[] getTimepointLimits() {
        // Finding the first and last frame of all objects in the inputObjects set
        int[] limits = new int[2];
        limits[0] = Integer.MAX_VALUE;
        limits[1] = Integer.MIN_VALUE;

        for (Obj object:values()) {
            if (object.getT() < limits[0]) limits[0] = object.getT();
            if (object.getT() > limits[1]) limits[1] = object.getT();

        }

        return limits;

    }
}
