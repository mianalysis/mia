package io.github.mianalysis.mia.process.math;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public class ArrayFunc
{
    public static int[] uniqueVals (int[] col_vals) {
        //Unique values from the input int array "col_vals"
        ArrayList<Integer> un_al = new ArrayList<Integer>();
        for (int i=0;i<col_vals.length;i++){
            if (!un_al.contains(col_vals[i])) {
                un_al.add(col_vals[i]);
            }
        }

        int[] un_ar = new int[un_al.size()];
        for (int i=0;i<un_al.size();i++) {
            un_ar[i] = (int) un_al.get(i);
        }
        return un_ar;
    }

    public static float[] uniqueVals (float[] col_vals) {
        //Unique values from the input float array "col_vals"
        ArrayList<Float> un_al = new ArrayList<Float>();
        for (int i=0;i<col_vals.length;i++){
            if (!un_al.contains(col_vals[i])) {
                un_al.add(col_vals[i]);
            }
        }

        float[] un_ar = new float[un_al.size()];
        for (int i=0;i<un_al.size();i++) {
            un_ar[i] = (float) un_al.get(i);
        }
        return un_ar;
    }

    public static double[] uniqueVals (double[] col_vals) {
        //Unique values from the input double array "col_vals"
        ArrayList<Double> un_al = new ArrayList<Double>();
        for (int i=0;i<col_vals.length;i++){
            if (!un_al.contains(col_vals[i])) {
                un_al.add(col_vals[i]);
            }
        }

        double[] un_ar = new double[un_al.size()];
        for (int i=0;i<un_al.size();i++) {
            un_ar[i] = (double) un_al.get(i);
        }
        return un_ar;
    }

    public static String[] uniqueVals (String[] col_vals) {
        //Unique values from the input double array "col_vals"
        ArrayList<String> un_al = new ArrayList<String>();
        for (int i=0;i<col_vals.length;i++){
            if (!un_al.contains(col_vals[i])) {
                un_al.add(col_vals[i]);
            }
        }

        String[] un_ar = new String[un_al.size()];
        for (int i=0;i<un_al.size();i++) {
            un_ar[i] = (String) un_al.get(i);
        }

        return un_ar;
    }

    public static int[] uniqueRows(int[] col_vals) {
        //Rows corresponding to unique values from the input int array "col_vals"
        ArrayList<Integer> un_al = new ArrayList<Integer>();
        ArrayList<Integer> r_al = new ArrayList<Integer>();
        for (int i=0;i<col_vals.length;i++){
            if (!un_al.contains(col_vals[i])) {
                un_al.add(col_vals[i]);
                r_al.add(i);
            }
        }

        int[] r_ar = new int[r_al.size()];
        for (int i=0;i<r_al.size();i++) {
            r_ar[i] = (int) r_al.get(i);
        }
        return r_ar;
    }

    public static double[][] uniqueRows(double[][] col_vals) {
        //This still doesn't seem to work right.  It gives true for locating a row in the ArrayList, even if there isn't one
        ArrayList<double[]>  un_al = new ArrayList<double[]>();

        for (int i=0;i<col_vals.length;i++){
            double[] row = new double[col_vals[i].length];
            System.arraycopy(col_vals[i],0,row,0,col_vals[i].length);

            boolean found = false;
            for (int j=0;j<un_al.size();j++) {
                if (Arrays.equals(row,un_al.get(j))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                un_al.add(row);
            }
        }

        double[][] un_ar = new double[un_al.size()][col_vals[0].length];
        for (int i=0;i<un_al.size();i++) {
            for (int k=0;k<col_vals[i].length;k++) {
                un_ar[i][k] = un_al.get(i)[k];
            }
        }

        return un_ar;

    }

    public static int[] correspondingRows(double[] col_vals, double tar) {
        //Rows corresponding to target ("tar") values from the input float array "col_vals"
        ArrayList<Integer> corr_al = new ArrayList<Integer>();
        for (int i=0;i<col_vals.length;i++){
            if (col_vals[i]==tar) {
                corr_al.add(i);
            }
        }

        int[] corr_ar = new int[corr_al.size()];
        for (int i=0;i<corr_al.size();i++) {
            corr_ar[i] = (int) corr_al.get(i);
        }

        return corr_ar;
    }

    public static double[] getSubArray(double[] col_vals, int[] rows) {
        double[] arr_out = new double[rows.length];
        for (int i=0;i<rows.length;i++) {
            arr_out[i] = col_vals[rows[i]];
        }
        return arr_out;
    }

    public static double getMinIncrement(double[] col_vals) {
        double inc = Double.POSITIVE_INFINITY;

        for (int i=0;i<col_vals.length-1;i++) {
            if (Math.abs(col_vals[i+1]-col_vals[i])<inc) {
                BigDecimal a = BigDecimal.valueOf(col_vals[i+1]);
                BigDecimal b = BigDecimal.valueOf(col_vals[i]);
                double c = a.subtract(b).doubleValue();
                inc = Math.abs(c);
            }
        }
        return inc;
    }

    public static double[] padArray(double[] arr_in, int l, double pad) {
        double[] arr_out = new double[l];
        for (int i=0;i<l;i++) {
            if (i < arr_in.length) {
                arr_out[i] = arr_in[i];
            } else {
                arr_out[i] = pad;
            }
        }
        return arr_out;
    }

    public static double[] padAlignArray(double[] arr_in, double[] tar_in, double[] tar_fix, double pad) {
        double[] arr_out = new double[tar_fix.length];
        Arrays.fill(arr_out, 0);

        for (int i=0;i<tar_in.length;i++) {
            for (int j=0;j<tar_fix.length;j++) {
                if (tar_in[i]==tar_fix[j]) {
                    arr_out[j] = arr_in[i];
                    break;
                }
            }
        }

        return arr_out;
    }

    public static String[] concatenateArrays(String[] arr_1, String[] arr_2) {
        String[] arr_out = new String[arr_1.length+arr_2.length];

        for (int i=0;i<arr_1.length;i++) {
            arr_out[i] = arr_1[i];
        }
        for (int i=0;i<arr_2.length;i++) {
            arr_out[i+arr_1.length] = arr_2[i];
        }

        return arr_out;
    }

    public static boolean contains(int[] list, int test) {
        Boolean boo = false;

        for (int i=0;i<list.length;i++) {
            if (list[i]==test) {
                boo = true;
                break; //Once we know it's there we can terminate
            }
        }

        return boo;
    }

    public static boolean contains(ArrayList<Integer> list, int test) {
        Boolean boo = false;

        for (int i=0;i<list.size();i++) {
            if (list.get(i) == test) {
                boo = true;
                break; //Once we know it's there we can terminate
            }
        }

        return boo;
    }

    public static int sum(int[] arr) {
        int out = 0;
        for (int i=0;i<arr.length;i++) {
            out += arr[i];
        }
        return out;
    }

    public static int max(int[] arr) {
        int out = 0;
        for (int i=0;i<arr.length;i++) {
            if (arr[i]>out) {
                out = arr[i];
            }
        }
        return out;
    }

    public static double[] arrayListToArray(ArrayList<Double> al) {
        double[] ar = new double[al.size()];
        for (int i=0;i<ar.length;i++) {
            ar[i] = al.get(i);
        }
        return ar;
    }

    public static int[] incrementedNumbers(int start, int end) {
        int[] out = new int[end-start];
        for(int i=0;i<out.length;i++) {
            out[i] = start+i;
        }
        return out;
    }

    public static double[] incrementedNumbers(double start, double end) {
        double[] out = new double[(int) (end-start)];
        for(int i=0;i<out.length;i++) {
            out[i] = start+i;
        }
        return out;
    }
}