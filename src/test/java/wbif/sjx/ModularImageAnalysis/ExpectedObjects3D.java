package wbif.sjx.ModularImageAnalysis;

import wbif.sjx.ModularImageAnalysis.Object.Measurement;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.util.HashMap;

/**
 * Created by Stephen Cross on 29/08/2017.
 */
public class ExpectedObjects3D {

    public interface Measures {
        String ID_8BIT = "ID_8BIT";
        String ID_16BIT = "ID_16BIT";
        String X_MIN = "X_MIN";
        String X_MEAN = "X_MEAN";
        String X_MEDIAN = "X_MEDIAN";
        String X_MAX = "X_MAX";
        String Y_MIN = "Y_MIN";
        String Y_MEAN = "Y_MEAN";
        String Y_MEDIAN = "Y_MEDIAN";
        String Y_MAX = "Y_MAX";
        String Z_MIN = "Z_MIN";
        String Z_MEAN = "Z_MEAN";
        String Z_MEDIAN = "Z_MEDIAN";
        String Z_MAX = "Z_MAX";
        String C = "C";
        String F = "F";
        String N_VOXELS = "N_VOXELS";
        String N_VOXELS_PROJ = "N_VOXELS_PROJ";
        String I_MEAN_8BIT = "I_MEAN_8BIT";
        String I_MIN_8BIT = "I_MIN_8BIT";
        String I_MAX_8BIT = "I_MAX_8BIT";
        String I_STD_8BIT = "I_STD_8BIT";
        String I_SUM_8BIT = "I_SUM_8BIT";
        String I_MEAN_16BIT = "I_MEAN_16BIT";
        String I_MIN_16BIT = "I_MIN_16BIT";
        String I_MAX_16BIT = "I_MAX_16BIT";
        String I_STD_16BIT = "I_STD_16BIT";
        String I_SUM_16BIT = "I_SUM_16BIT";
        String I_MEAN_32BIT = "I_MEAN_32BIT";
        String I_MIN_32BIT = "I_MIN_32BIT";
        String I_MAX_32BIT = "I_MAX_32BIT";
        String I_STD_32BIT = "I_STD_32BIT";
        String I_SUM_32BIT = "I_SUM_32BIT";
        String SPOT_ID_X = "SPOT_ID_X";
        String SPOT_ID_Y = "SPOT_ID_Y";
        String SPOT_ID_Z = "SPOT_ID_Z;";
        String SPOT_PROX_CENT_X = "SPOT_PROX_CENT_X";
        String SPOT_PROX_CENT_Y = "SPOT_PROX_CENT_Y";
        String SPOT_PROX_CENT_Z = "SPOT_PROX_CENT_Z";
        String SPOT_PROX_CENT_DIST = "SPOT_PROX_CENT_DIST";
        String SPOT_PROX_CENT_20PX_X = "SPOT_PROX_CENT_20PX_X";
        String SPOT_PROX_CENT_20PX_Y = "SPOT_PROX_CENT_20PX_Y";
        String SPOT_PROX_CENT_20PX_Z = "SPOT_PROX_CENT_20PX_Z";
        String SPOT_PROX_CENT_20PX_DIST = "SPOT_PROX_CENT_20PX_DIST";
    }

    public static HashMap<Integer,HashMap<String,Object>> getExpectedValues3D() {
        HashMap<Integer,HashMap<String,Object>> expectedValues = new HashMap<>();

        HashMap<String,Object> obj = new HashMap<>();
        obj.put(Measures.ID_8BIT,3);
        obj.put(Measures.ID_16BIT, 3);
        obj.put(Measures.X_MIN, 7);
        obj.put(Measures.X_MEAN, 11.5);
        obj.put(Measures.X_MEDIAN, 11.5);
        obj.put(Measures.X_MAX, 16);
        obj.put(Measures.Y_MIN, 8);
        obj.put(Measures.Y_MEAN, 15.40);
        obj.put(Measures.Y_MEDIAN, 15.0);
        obj.put(Measures.Y_MAX, 23);
        obj.put(Measures.Z_MIN, 0);
        obj.put(Measures.Z_MEAN, 1.80);
        obj.put(Measures.Z_MEDIAN, 2.0);
        obj.put(Measures.Z_MAX, 4);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 272);
        obj.put(Measures.N_VOXELS_PROJ, 130);
        obj.put(Measures.I_MEAN_8BIT, 46.23);
        obj.put(Measures.I_MIN_8BIT, 14);
        obj.put(Measures.I_MAX_8BIT, 76);
        obj.put(Measures.I_STD_8BIT, 11.92);
        obj.put(Measures.I_SUM_8BIT, 12575);
        obj.put(Measures.I_MEAN_16BIT, 9337.54);
        obj.put(Measures.I_MIN_16BIT, 3330);
        obj.put(Measures.I_MAX_16BIT, 16290);
        obj.put(Measures.I_STD_16BIT, 2572.40);
        obj.put(Measures.I_SUM_16BIT, 2539812);
        obj.put(Measures.I_MEAN_32BIT, 0.1437);
        obj.put(Measures.I_MIN_32BIT, 0.051);
        obj.put(Measures.I_MAX_32BIT, 0.251);
        obj.put(Measures.I_STD_32BIT, 0.040);
        obj.put(Measures.I_SUM_32BIT, 39.075);
        obj.put(Measures.SPOT_ID_X, 24);
        obj.put(Measures.SPOT_ID_Y, 42);
        obj.put(Measures.SPOT_ID_Z, 1);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{19,32,8,0});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{13,18,17,31});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{0,1,2,2});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{11.96,21.05,3.98,19.41});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{19,8,0});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{13,17,31});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{0,2,2});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{11.96,3.98,19.41});
        expectedValues.put(272,obj);

        obj = new HashMap<>();
        obj.put(Measures.ID_8BIT, 4);
        obj.put(Measures.ID_16BIT, 4);
        obj.put(Measures.X_MIN, 27);
        obj.put(Measures.X_MEAN, 27.0);
        obj.put(Measures.X_MEDIAN, 27.0);
        obj.put(Measures.X_MAX, 27);
        obj.put(Measures.Y_MIN, 16);
        obj.put(Measures.Y_MEAN, 16.0);
        obj.put(Measures.Y_MEDIAN, 16.0);
        obj.put(Measures.Y_MAX, 16);
        obj.put(Measures.Z_MIN, 5);
        obj.put(Measures.Z_MEAN, 5.5);
        obj.put(Measures.Z_MEDIAN, 5.5);
        obj.put(Measures.Z_MAX, 6);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 2);
        obj.put(Measures.N_VOXELS_PROJ, 1);
        obj.put(Measures.I_MEAN_8BIT, 106.0);
        obj.put(Measures.I_MIN_8BIT, 98);
        obj.put(Measures.I_MAX_8BIT, 114);
        obj.put(Measures.I_STD_8BIT, 11.31);
        obj.put(Measures.I_SUM_8BIT, 212);
        obj.put(Measures.I_MEAN_16BIT, 22377.5);
        obj.put(Measures.I_MIN_16BIT, 20432);
        obj.put(Measures.I_MAX_16BIT, 24323);
        obj.put(Measures.I_STD_16BIT, 2751.35);
        obj.put(Measures.I_SUM_16BIT, 44755);
        obj.put(Measures.I_MEAN_32BIT, 0.344);
        obj.put(Measures.I_MIN_32BIT, 0.314);
        obj.put(Measures.I_MAX_32BIT, 0.374);
        obj.put(Measures.I_STD_32BIT, 0.0424);
        obj.put(Measures.I_SUM_32BIT, 0.688);
        obj.put(Measures.SPOT_ID_X, 24);
        obj.put(Measures.SPOT_ID_Y, 44);
        obj.put(Measures.SPOT_ID_Z, 1);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{39,15,21,36,17});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{27,36,5,21,10});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{3,5,6,7,8});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{20.52,23.46,12.78,12.74,17.10});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{21,36,17});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{5,21,10});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{6,7,8});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{12.78,12.74,17.10});
        expectedValues.put(2,obj);

        obj = new HashMap<>();
        obj.put(Measures.ID_8BIT, 7);
        obj.put(Measures.ID_16BIT, 1155);
        obj.put(Measures.X_MIN, 42);
        obj.put(Measures.X_MEAN, 49.81);
        obj.put(Measures.X_MEDIAN, 50.0);
        obj.put(Measures.X_MAX, 57);
        obj.put(Measures.Y_MIN, 3);
        obj.put(Measures.Y_MEAN, 11.73);
        obj.put(Measures.Y_MEDIAN, 12.0);
        obj.put(Measures.Y_MAX, 20);
        obj.put(Measures.Z_MIN, 3);
        obj.put(Measures.Z_MEAN, 6.31);
        obj.put(Measures.Z_MEDIAN, 7.0);
        obj.put(Measures.Z_MAX, 9);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 742);
        obj.put(Measures.N_VOXELS_PROJ, 234);
        obj.put(Measures.I_MEAN_8BIT, 199.43);
        obj.put(Measures.I_MIN_8BIT, 149);
        obj.put(Measures.I_MAX_8BIT, 244);
        obj.put(Measures.I_STD_8BIT, 17.23);
        obj.put(Measures.I_SUM_8BIT, 147979);
        obj.put(Measures.I_MEAN_16BIT, 39871.31);
        obj.put(Measures.I_MIN_16BIT, 28151);
        obj.put(Measures.I_MAX_16BIT, 52362);
        obj.put(Measures.I_STD_16BIT, 3625.46);
        obj.put(Measures.I_SUM_16BIT, 29584511);
        obj.put(Measures.I_MEAN_32BIT, 0.6134);
        obj.put(Measures.I_MIN_32BIT, 0.433);
        obj.put(Measures.I_MAX_32BIT, 0.806);
        obj.put(Measures.I_STD_32BIT, 0.0558);
        obj.put(Measures.I_SUM_32BIT, 455.15);
        obj.put(Measures.SPOT_ID_X, 19);
        obj.put(Measures.SPOT_ID_Y, 13);
        obj.put(Measures.SPOT_ID_Z, 0);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{52,44,44});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{4,14,14});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{0,2,7});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{32.56,22.43,7.13});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{44});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{14});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{7});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{7.13});
        expectedValues.put(742,obj);

        obj = new HashMap<>();
        obj.put(Measures.ID_8BIT, 8);
        obj.put(Measures.ID_16BIT, 8);
        obj.put(Measures.X_MIN, 28);
        obj.put(Measures.X_MEAN, 34.8);
        obj.put(Measures.X_MEDIAN, 35.0);
        obj.put(Measures.X_MAX, 41);
        obj.put(Measures.Y_MIN, 72);
        obj.put(Measures.Y_MEAN, 73.57);
        obj.put(Measures.Y_MEDIAN, 74.0);
        obj.put(Measures.Y_MAX, 75);
        obj.put(Measures.Z_MIN, 1);
        obj.put(Measures.Z_MEAN, 2.0);
        obj.put(Measures.Z_MEDIAN, 2.0);
        obj.put(Measures.Z_MAX, 3);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 90);
        obj.put(Measures.N_VOXELS_PROJ, 48);
        obj.put(Measures.I_MEAN_8BIT, 139.42);
        obj.put(Measures.I_MIN_8BIT, 94);
        obj.put(Measures.I_MAX_8BIT, 172);
        obj.put(Measures.I_STD_8BIT, 16.40);
        obj.put(Measures.I_SUM_8BIT, 12548);
        obj.put(Measures.I_MEAN_16BIT, 28101.38);
        obj.put(Measures.I_MIN_16BIT, 20448);
        obj.put(Measures.I_MAX_16BIT, 34928);
        obj.put(Measures.I_STD_16BIT, 3317.57);
        obj.put(Measures.I_SUM_16BIT, 2529124);
        obj.put(Measures.I_MEAN_32BIT, 0.432);
        obj.put(Measures.I_MIN_32BIT, 0.315);
        obj.put(Measures.I_MAX_32BIT, 0.537);
        obj.put(Measures.I_STD_32BIT, 0.051);
        obj.put(Measures.I_SUM_32BIT, 38.911);
        obj.put(Measures.SPOT_ID_X, 0);
        obj.put(Measures.SPOT_ID_Y, 31);
        obj.put(Measures.SPOT_ID_Z, 2);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{});
        expectedValues.put(90,obj);

        obj = new HashMap<>();
        obj.put(Measures.ID_8BIT, 9);
        obj.put(Measures.ID_16BIT, 9);
        obj.put(Measures.X_MIN, 17);
        obj.put(Measures.X_MEAN, 18.0);
        obj.put(Measures.X_MEDIAN, 18.0);
        obj.put(Measures.X_MAX, 19);
        obj.put(Measures.Y_MIN, 60);
        obj.put(Measures.Y_MEAN, 61.0);
        obj.put(Measures.Y_MEDIAN, 61.0);
        obj.put(Measures.Y_MAX, 62);
        obj.put(Measures.Z_MIN, 2);
        obj.put(Measures.Z_MEAN, 2.0);
        obj.put(Measures.Z_MEDIAN, 2.0);
        obj.put(Measures.Z_MAX, 2);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 9);
        obj.put(Measures.N_VOXELS_PROJ, 9);
        obj.put(Measures.I_MEAN_8BIT, 71.56);
        obj.put(Measures.I_MIN_8BIT, 64);
        obj.put(Measures.I_MAX_8BIT, 83);
        obj.put(Measures.I_STD_8BIT, 6.06);
        obj.put(Measures.I_SUM_8BIT, 644);
        obj.put(Measures.I_MEAN_16BIT, 14648.11);
        obj.put(Measures.I_MIN_16BIT, 11653);
        obj.put(Measures.I_MAX_16BIT, 17281);
        obj.put(Measures.I_STD_16BIT, 1823.12);
        obj.put(Measures.I_SUM_16BIT, 131833);
        obj.put(Measures.I_MEAN_32BIT, 0.225);
        obj.put(Measures.I_MIN_32BIT, 0.179);
        obj.put(Measures.I_MAX_32BIT, 0.266);
        obj.put(Measures.I_STD_32BIT, 0.028);
        obj.put(Measures.I_SUM_32BIT, 2.028);
        obj.put(Measures.SPOT_ID_X, 0);
        obj.put(Measures.SPOT_ID_Y, 75);
        obj.put(Measures.SPOT_ID_Z, 5);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{24,24,15,0});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{42,44,57,75});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{1,1,2,5});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{20.54,18.71,5,27.29});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{24,15});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{44,57});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{1,2});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{18.71,5});
        expectedValues.put(9,obj);

        obj = new HashMap<>();
        obj.put(Measures.ID_8BIT, 13);
        obj.put(Measures.ID_16BIT, 13);
        obj.put(Measures.X_MIN, 34);
        obj.put(Measures.X_MEAN, 40.57);
        obj.put(Measures.X_MEDIAN, 40.0);
        obj.put(Measures.X_MAX, 48);
        obj.put(Measures.Y_MIN, 35);
        obj.put(Measures.Y_MEAN, 40.26);
        obj.put(Measures.Y_MEDIAN, 40.0);
        obj.put(Measures.Y_MAX, 46);
        obj.put(Measures.Z_MIN, 6);
        obj.put(Measures.Z_MEAN, 8.39);
        obj.put(Measures.Z_MEDIAN, 9.0);
        obj.put(Measures.Z_MAX, 11);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 375);
        obj.put(Measures.N_VOXELS_PROJ, 137);
        obj.put(Measures.I_MEAN_8BIT, 161.93);
        obj.put(Measures.I_MIN_8BIT, 119);
        obj.put(Measures.I_MAX_8BIT, 218);
        obj.put(Measures.I_STD_8BIT, 17.51);
        obj.put(Measures.I_SUM_8BIT, 60724);
        obj.put(Measures.I_MEAN_16BIT, 32363.46);
        obj.put(Measures.I_MIN_16BIT, 23471);
        obj.put(Measures.I_MAX_16BIT, 43399);
        obj.put(Measures.I_STD_16BIT, 3629.69);
        obj.put(Measures.I_SUM_16BIT, 12136296);
        obj.put(Measures.I_MEAN_32BIT, 0.498);
        obj.put(Measures.I_MIN_32BIT, 0.361);
        obj.put(Measures.I_MAX_32BIT, 0.668);
        obj.put(Measures.I_STD_32BIT, 0.0558);
        obj.put(Measures.I_SUM_32BIT, 186.716);
        obj.put(Measures.SPOT_ID_X, 39);
        obj.put(Measures.SPOT_ID_Y, 27);
        obj.put(Measures.SPOT_ID_Z, 3);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{46,51,56});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{44,36,40});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{6,10,10});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{13.63,13.86,17.42});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{46,51,56});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{44,36,40});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{6,10,10});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{13.63,13.86,17.42});
        expectedValues.put(375,obj);

        obj = new HashMap<>();
        obj.put(Measures.ID_8BIT, 20);
        obj.put(Measures.ID_16BIT, 20);
        obj.put(Measures.X_MIN, 29);
        obj.put(Measures.X_MEAN, 32.38);
        obj.put(Measures.X_MEDIAN, 32.5);
        obj.put(Measures.X_MAX, 35);
        obj.put(Measures.Y_MIN, 51);
        obj.put(Measures.Y_MEAN, 54.5);
        obj.put(Measures.Y_MEDIAN, 54.5);
        obj.put(Measures.Y_MAX, 58);
        obj.put(Measures.Z_MIN, 4);
        obj.put(Measures.Z_MEAN, 7.38);
        obj.put(Measures.Z_MEDIAN, 7.5);
        obj.put(Measures.Z_MAX, 10);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 8);
        obj.put(Measures.N_VOXELS_PROJ, 8);
        obj.put(Measures.I_MEAN_8BIT, 130.88);
        obj.put(Measures.I_MIN_8BIT, 124);
        obj.put(Measures.I_MAX_8BIT, 139);
        obj.put(Measures.I_STD_8BIT, 4.94);
        obj.put(Measures.I_SUM_8BIT, 1047);
        obj.put(Measures.I_MEAN_16BIT, 25965.75);
        obj.put(Measures.I_MIN_16BIT, 23278);
        obj.put(Measures.I_MAX_16BIT, 27482);
        obj.put(Measures.I_STD_16BIT, 1381.49);
        obj.put(Measures.I_SUM_16BIT, 207726);
        obj.put(Measures.I_MEAN_32BIT, 0.3995);
        obj.put(Measures.I_MIN_32BIT, 0.358);
        obj.put(Measures.I_MAX_32BIT, 0.423);
        obj.put(Measures.I_STD_32BIT, 0.021);
        obj.put(Measures.I_SUM_32BIT, 3.196);
        obj.put(Measures.SPOT_ID_X, 15);
        obj.put(Measures.SPOT_ID_Y, 57);
        obj.put(Measures.SPOT_ID_Z, 2);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{35,35,45,21});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{41,45,71,70});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{3,5,7,8});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{25.84,15.43,20.86,19.48});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{35,21});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{45,70});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{5,8});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{15.43,19.48});
        expectedValues.put(8,obj);
        obj = new HashMap<>();
        obj.put(Measures.ID_8BIT, 23);
        obj.put(Measures.ID_16BIT, 23);
        obj.put(Measures.X_MIN, 55);
        obj.put(Measures.X_MEAN, 55.0);
        obj.put(Measures.X_MEDIAN, 55.0);
        obj.put(Measures.X_MAX, 55);
        obj.put(Measures.Y_MIN, 61);
        obj.put(Measures.Y_MEAN, 61.0);
        obj.put(Measures.Y_MEDIAN, 61.0);
        obj.put(Measures.Y_MAX, 61);
        obj.put(Measures.Z_MIN, 11);
        obj.put(Measures.Z_MEAN, 11.0);
        obj.put(Measures.Z_MEDIAN, 11.0);
        obj.put(Measures.Z_MAX, 11);
        obj.put(Measures.C, 0);
        obj.put(Measures.F, 0);
        obj.put(Measures.N_VOXELS, 1);
        obj.put(Measures.N_VOXELS_PROJ, 1);
        obj.put(Measures.I_MEAN_8BIT, 237.0);
        obj.put(Measures.I_MIN_8BIT, 237);
        obj.put(Measures.I_MAX_8BIT, 237);
        obj.put(Measures.I_STD_8BIT, Double.NaN);
        obj.put(Measures.I_SUM_8BIT, 237);
        obj.put(Measures.I_MEAN_16BIT, 48606.0);
        obj.put(Measures.I_MIN_16BIT, 48606);
        obj.put(Measures.I_MAX_16BIT, 48606);
        obj.put(Measures.I_STD_16BIT, Double.NaN);
        obj.put(Measures.I_SUM_16BIT, 48606);
        obj.put(Measures.I_MEAN_32BIT, 0.748);
        obj.put(Measures.I_MIN_32BIT, 0.748);
        obj.put(Measures.I_MAX_32BIT, 0.748);
        obj.put(Measures.I_STD_32BIT, Double.NaN);
        obj.put(Measures.I_SUM_32BIT, 0.748);
        obj.put(Measures.SPOT_ID_X, 36);
        obj.put(Measures.SPOT_ID_Y, 21);
        obj.put(Measures.SPOT_ID_Z, 7);
        obj.put(Measures.SPOT_PROX_CENT_X, new int[]{51,63});
        obj.put(Measures.SPOT_PROX_CENT_Y, new int[]{65,64});
        obj.put(Measures.SPOT_PROX_CENT_Z, new int[]{10,11});
        obj.put(Measures.SPOT_PROX_CENT_DIST, new double[]{7.55,8.54});
        obj.put(Measures.SPOT_PROX_CENT_20PX_X, new int[]{51,63});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Y, new int[]{65,64});
        obj.put(Measures.SPOT_PROX_CENT_20PX_Z, new int[]{10,11});
        obj.put(Measures.SPOT_PROX_CENT_20PX_DIST, new double[]{7.55,8.54});
        expectedValues.put(1,obj);

        return expectedValues;

    }

    public static int[][] getCoordinates3D() {
        return new int[][]{{3,3,11,12,0,0,0},
                {3,3,12,12,0,0,0},
                {3,3,11,13,0,0,0},
                {3,3,12,13,0,0,0},
                {3,3,10,14,0,0,0},
                {3,3,11,14,0,0,0},
                {3,3,12,14,0,0,0},
                {3,3,13,14,0,0,0},
                {3,3,10,15,0,0,0},
                {3,3,11,15,0,0,0},
                {3,3,12,15,0,0,0},
                {3,3,13,15,0,0,0},
                {3,3,10,16,0,0,0},
                {3,3,11,16,0,0,0},
                {3,3,12,16,0,0,0},
                {3,3,13,16,0,0,0},
                {3,3,10,17,0,0,0},
                {3,3,11,17,0,0,0},
                {3,3,12,17,0,0,0},
                {3,3,13,17,0,0,0},
                {3,3,11,18,0,0,0},
                {3,3,12,18,0,0,0},
                {3,3,10,10,0,1,0},
                {3,3,11,10,0,1,0},
                {3,3,12,10,0,1,0},
                {3,3,13,10,0,1,0},
                {3,3,9,11,0,1,0},
                {3,3,10,11,0,1,0},
                {3,3,11,11,0,1,0},
                {3,3,12,11,0,1,0},
                {3,3,13,11,0,1,0},
                {3,3,14,11,0,1,0},
                {3,3,9,12,0,1,0},
                {3,3,10,12,0,1,0},
                {3,3,11,12,0,1,0},
                {3,3,12,12,0,1,0},
                {3,3,13,12,0,1,0},
                {3,3,14,12,0,1,0},
                {3,3,9,13,0,1,0},
                {3,3,10,13,0,1,0},
                {3,3,11,13,0,1,0},
                {3,3,12,13,0,1,0},
                {3,3,13,13,0,1,0},
                {3,3,14,13,0,1,0},
                {3,3,8,14,0,1,0},
                {3,3,9,14,0,1,0},
                {3,3,10,14,0,1,0},
                {3,3,11,14,0,1,0},
                {3,3,12,14,0,1,0},
                {3,3,13,14,0,1,0},
                {3,3,14,14,0,1,0},
                {3,3,15,14,0,1,0},
                {3,3,8,15,0,1,0},
                {3,3,9,15,0,1,0},
                {3,3,10,15,0,1,0},
                {3,3,11,15,0,1,0},
                {3,3,12,15,0,1,0},
                {3,3,13,15,0,1,0},
                {3,3,14,15,0,1,0},
                {3,3,15,15,0,1,0},
                {3,3,8,16,0,1,0},
                {3,3,9,16,0,1,0},
                {3,3,10,16,0,1,0},
                {3,3,11,16,0,1,0},
                {3,3,12,16,0,1,0},
                {3,3,13,16,0,1,0},
                {3,3,14,16,0,1,0},
                {3,3,15,16,0,1,0},
                {3,3,8,17,0,1,0},
                {3,3,9,17,0,1,0},
                {3,3,10,17,0,1,0},
                {3,3,11,17,0,1,0},
                {3,3,12,17,0,1,0},
                {3,3,13,17,0,1,0},
                {3,3,14,17,0,1,0},
                {3,3,15,17,0,1,0},
                {3,3,9,18,0,1,0},
                {3,3,10,18,0,1,0},
                {3,3,11,18,0,1,0},
                {3,3,12,18,0,1,0},
                {3,3,13,18,0,1,0},
                {3,3,14,18,0,1,0},
                {3,3,9,19,0,1,0},
                {3,3,10,19,0,1,0},
                {3,3,11,19,0,1,0},
                {3,3,12,19,0,1,0},
                {3,3,13,19,0,1,0},
                {3,3,14,19,0,1,0},
                {3,3,9,20,0,1,0},
                {3,3,10,20,0,1,0},
                {3,3,11,20,0,1,0},
                {3,3,12,20,0,1,0},
                {3,3,13,20,0,1,0},
                {3,3,14,20,0,1,0},
                {3,3,10,21,0,1,0},
                {3,3,11,21,0,1,0},
                {3,3,12,21,0,1,0},
                {3,3,13,21,0,1,0},
                {8,8,28,73,0,1,0},
                {8,8,29,73,0,1,0},
                {8,8,30,73,0,1,0},
                {8,8,31,73,0,1,0},
                {8,8,32,73,0,1,0},
                {8,8,33,73,0,1,0},
                {8,8,34,73,0,1,0},
                {8,8,35,73,0,1,0},
                {8,8,36,73,0,1,0},
                {8,8,37,73,0,1,0},
                {8,8,38,73,0,1,0},
                {8,8,28,74,0,1,0},
                {8,8,29,74,0,1,0},
                {8,8,30,74,0,1,0},
                {8,8,31,74,0,1,0},
                {8,8,32,74,0,1,0},
                {8,8,33,74,0,1,0},
                {8,8,34,74,0,1,0},
                {8,8,35,74,0,1,0},
                {8,8,36,74,0,1,0},
                {8,8,37,74,0,1,0},
                {8,8,38,74,0,1,0},
                {8,8,30,75,0,1,0},
                {8,8,31,75,0,1,0},
                {8,8,32,75,0,1,0},
                {8,8,33,75,0,1,0},
                {8,8,34,75,0,1,0},
                {8,8,35,75,0,1,0},
                {8,8,36,75,0,1,0},
                {8,8,37,75,0,1,0},
                {3,3,10,8,0,2,0},
                {3,3,11,8,0,2,0},
                {3,3,12,8,0,2,0},
                {3,3,13,8,0,2,0},
                {3,3,9,9,0,2,0},
                {3,3,10,9,0,2,0},
                {3,3,11,9,0,2,0},
                {3,3,12,9,0,2,0},
                {3,3,13,9,0,2,0},
                {3,3,14,9,0,2,0},
                {3,3,8,10,0,2,0},
                {3,3,9,10,0,2,0},
                {3,3,10,10,0,2,0},
                {3,3,11,10,0,2,0},
                {3,3,12,10,0,2,0},
                {3,3,13,10,0,2,0},
                {3,3,14,10,0,2,0},
                {3,3,15,10,0,2,0},
                {3,3,8,11,0,2,0},
                {3,3,9,11,0,2,0},
                {3,3,10,11,0,2,0},
                {3,3,11,11,0,2,0},
                {3,3,12,11,0,2,0},
                {3,3,13,11,0,2,0},
                {3,3,14,11,0,2,0},
                {3,3,15,11,0,2,0},
                {3,3,7,12,0,2,0},
                {3,3,8,12,0,2,0},
                {3,3,9,12,0,2,0},
                {3,3,10,12,0,2,0},
                {3,3,11,12,0,2,0},
                {3,3,12,12,0,2,0},
                {3,3,13,12,0,2,0},
                {3,3,14,12,0,2,0},
                {3,3,15,12,0,2,0},
                {3,3,16,12,0,2,0},
                {3,3,7,13,0,2,0},
                {3,3,8,13,0,2,0},
                {3,3,9,13,0,2,0},
                {3,3,10,13,0,2,0},
                {3,3,11,13,0,2,0},
                {3,3,12,13,0,2,0},
                {3,3,13,13,0,2,0},
                {3,3,14,13,0,2,0},
                {3,3,15,13,0,2,0},
                {3,3,16,13,0,2,0},
                {3,3,7,14,0,2,0},
                {3,3,8,14,0,2,0},
                {3,3,9,14,0,2,0},
                {3,3,10,14,0,2,0},
                {3,3,11,14,0,2,0},
                {3,3,12,14,0,2,0},
                {3,3,13,14,0,2,0},
                {3,3,14,14,0,2,0},
                {3,3,15,14,0,2,0},
                {3,3,16,14,0,2,0},
                {3,3,7,15,0,2,0},
                {3,3,8,15,0,2,0},
                {3,3,9,15,0,2,0},
                {3,3,10,15,0,2,0},
                {3,3,11,15,0,2,0},
                {3,3,12,15,0,2,0},
                {3,3,13,15,0,2,0},
                {3,3,14,15,0,2,0},
                {3,3,15,15,0,2,0},
                {3,3,16,15,0,2,0},
                {3,3,7,16,0,2,0},
                {3,3,8,16,0,2,0},
                {3,3,9,16,0,2,0},
                {3,3,10,16,0,2,0},
                {3,3,11,16,0,2,0},
                {3,3,12,16,0,2,0},
                {3,3,13,16,0,2,0},
                {3,3,14,16,0,2,0},
                {3,3,15,16,0,2,0},
                {3,3,16,16,0,2,0},
                {3,3,7,17,0,2,0},
                {3,3,8,17,0,2,0},
                {3,3,9,17,0,2,0},
                {3,3,10,17,0,2,0},
                {3,3,11,17,0,2,0},
                {3,3,12,17,0,2,0},
                {3,3,13,17,0,2,0},
                {3,3,14,17,0,2,0},
                {3,3,15,17,0,2,0},
                {3,3,16,17,0,2,0},
                {3,3,7,18,0,2,0},
                {3,3,8,18,0,2,0},
                {3,3,9,18,0,2,0},
                {3,3,10,18,0,2,0},
                {3,3,11,18,0,2,0},
                {3,3,12,18,0,2,0},
                {3,3,13,18,0,2,0},
                {3,3,14,18,0,2,0},
                {3,3,15,18,0,2,0},
                {3,3,16,18,0,2,0},
                {3,3,8,19,0,2,0},
                {3,3,9,19,0,2,0},
                {3,3,10,19,0,2,0},
                {3,3,11,19,0,2,0},
                {3,3,12,19,0,2,0},
                {3,3,13,19,0,2,0},
                {3,3,14,19,0,2,0},
                {3,3,15,19,0,2,0},
                {3,3,8,20,0,2,0},
                {3,3,9,20,0,2,0},
                {3,3,10,20,0,2,0},
                {3,3,11,20,0,2,0},
                {3,3,12,20,0,2,0},
                {3,3,13,20,0,2,0},
                {3,3,14,20,0,2,0},
                {3,3,15,20,0,2,0},
                {3,3,8,21,0,2,0},
                {3,3,9,21,0,2,0},
                {3,3,10,21,0,2,0},
                {3,3,11,21,0,2,0},
                {3,3,12,21,0,2,0},
                {3,3,13,21,0,2,0},
                {3,3,14,21,0,2,0},
                {3,3,15,21,0,2,0},
                {3,3,9,22,0,2,0},
                {3,3,10,22,0,2,0},
                {3,3,11,22,0,2,0},
                {3,3,12,22,0,2,0},
                {3,3,13,22,0,2,0},
                {3,3,14,22,0,2,0},
                {3,3,10,23,0,2,0},
                {3,3,11,23,0,2,0},
                {3,3,12,23,0,2,0},
                {3,3,13,23,0,2,0},
                {9,9,17,60,0,2,0},
                {9,9,18,60,0,2,0},
                {9,9,19,60,0,2,0},
                {9,9,17,61,0,2,0},
                {9,9,18,61,0,2,0},
                {9,9,19,61,0,2,0},
                {9,9,17,62,0,2,0},
                {9,9,18,62,0,2,0},
                {9,9,19,62,0,2,0},
                {8,8,30,73,0,2,0},
                {8,8,31,73,0,2,0},
                {8,8,32,73,0,2,0},
                {8,8,33,73,0,2,0},
                {8,8,34,73,0,2,0},
                {8,8,35,73,0,2,0},
                {8,8,36,73,0,2,0},
                {8,8,37,73,0,2,0},
                {8,8,38,73,0,2,0},
                {8,8,39,73,0,2,0},
                {8,8,40,73,0,2,0},
                {8,8,30,74,0,2,0},
                {8,8,31,74,0,2,0},
                {8,8,32,74,0,2,0},
                {8,8,33,74,0,2,0},
                {8,8,34,74,0,2,0},
                {8,8,35,74,0,2,0},
                {8,8,36,74,0,2,0},
                {8,8,37,74,0,2,0},
                {8,8,38,74,0,2,0},
                {8,8,39,74,0,2,0},
                {8,8,40,74,0,2,0},
                {8,8,32,75,0,2,0},
                {8,8,33,75,0,2,0},
                {8,8,34,75,0,2,0},
                {8,8,35,75,0,2,0},
                {8,8,36,75,0,2,0},
                {8,8,37,75,0,2,0},
                {8,8,38,75,0,2,0},
                {8,8,39,75,0,2,0},
                {7,1155,48,10,0,3,0},
                {7,1155,49,10,0,3,0},
                {7,1155,50,10,0,3,0},
                {7,1155,51,10,0,3,0},
                {7,1155,47,11,0,3,0},
                {7,1155,48,11,0,3,0},
                {7,1155,49,11,0,3,0},
                {7,1155,50,11,0,3,0},
                {7,1155,51,11,0,3,0},
                {7,1155,52,11,0,3,0},
                {3,3,11,12,0,3,0},
                {3,3,12,12,0,3,0},
                {7,1155,47,12,0,3,0},
                {7,1155,48,12,0,3,0},
                {7,1155,49,12,0,3,0},
                {7,1155,50,12,0,3,0},
                {7,1155,51,12,0,3,0},
                {7,1155,52,12,0,3,0},
                {7,1155,53,12,0,3,0},
                {3,3,11,13,0,3,0},
                {3,3,12,13,0,3,0},
                {7,1155,47,13,0,3,0},
                {7,1155,48,13,0,3,0},
                {7,1155,49,13,0,3,0},
                {7,1155,50,13,0,3,0},
                {7,1155,51,13,0,3,0},
                {7,1155,52,13,0,3,0},
                {7,1155,53,13,0,3,0},
                {3,3,10,14,0,3,0},
                {3,3,11,14,0,3,0},
                {3,3,12,14,0,3,0},
                {3,3,13,14,0,3,0},
                {7,1155,47,14,0,3,0},
                {7,1155,48,14,0,3,0},
                {7,1155,49,14,0,3,0},
                {7,1155,50,14,0,3,0},
                {7,1155,51,14,0,3,0},
                {7,1155,52,14,0,3,0},
                {7,1155,53,14,0,3,0},
                {3,3,10,15,0,3,0},
                {3,3,11,15,0,3,0},
                {3,3,12,15,0,3,0},
                {3,3,13,15,0,3,0},
                {7,1155,47,15,0,3,0},
                {7,1155,48,15,0,3,0},
                {7,1155,49,15,0,3,0},
                {7,1155,50,15,0,3,0},
                {7,1155,51,15,0,3,0},
                {7,1155,52,15,0,3,0},
                {3,3,10,16,0,3,0},
                {3,3,11,16,0,3,0},
                {3,3,12,16,0,3,0},
                {3,3,13,16,0,3,0},
                {7,1155,49,16,0,3,0},
                {7,1155,50,16,0,3,0},
                {3,3,10,17,0,3,0},
                {3,3,11,17,0,3,0},
                {3,3,12,17,0,3,0},
                {3,3,13,17,0,3,0},
                {3,3,11,18,0,3,0},
                {3,3,12,18,0,3,0},
                {8,8,31,72,0,3,0},
                {8,8,32,72,0,3,0},
                {8,8,33,72,0,3,0},
                {8,8,34,72,0,3,0},
                {8,8,35,72,0,3,0},
                {8,8,36,72,0,3,0},
                {8,8,37,72,0,3,0},
                {8,8,38,72,0,3,0},
                {8,8,39,72,0,3,0},
                {8,8,40,72,0,3,0},
                {8,8,41,72,0,3,0},
                {8,8,31,73,0,3,0},
                {8,8,32,73,0,3,0},
                {8,8,33,73,0,3,0},
                {8,8,34,73,0,3,0},
                {8,8,35,73,0,3,0},
                {8,8,36,73,0,3,0},
                {8,8,37,73,0,3,0},
                {8,8,38,73,0,3,0},
                {8,8,39,73,0,3,0},
                {8,8,40,73,0,3,0},
                {8,8,41,73,0,3,0},
                {8,8,33,74,0,3,0},
                {8,8,34,74,0,3,0},
                {8,8,35,74,0,3,0},
                {8,8,36,74,0,3,0},
                {8,8,37,74,0,3,0},
                {8,8,38,74,0,3,0},
                {8,8,39,74,0,3,0},
                {8,8,40,74,0,3,0},
                {7,1155,48,8,0,4,0},
                {7,1155,49,8,0,4,0},
                {7,1155,50,8,0,4,0},
                {7,1155,51,8,0,4,0},
                {7,1155,52,8,0,4,0},
                {7,1155,53,8,0,4,0},
                {7,1155,47,9,0,4,0},
                {7,1155,48,9,0,4,0},
                {7,1155,49,9,0,4,0},
                {7,1155,50,9,0,4,0},
                {7,1155,51,9,0,4,0},
                {7,1155,52,9,0,4,0},
                {7,1155,53,9,0,4,0},
                {7,1155,54,9,0,4,0},
                {7,1155,46,10,0,4,0},
                {7,1155,47,10,0,4,0},
                {7,1155,48,10,0,4,0},
                {7,1155,49,10,0,4,0},
                {7,1155,50,10,0,4,0},
                {7,1155,51,10,0,4,0},
                {7,1155,52,10,0,4,0},
                {7,1155,53,10,0,4,0},
                {7,1155,54,10,0,4,0},
                {7,1155,55,10,0,4,0},
                {7,1155,46,11,0,4,0},
                {7,1155,47,11,0,4,0},
                {7,1155,48,11,0,4,0},
                {7,1155,49,11,0,4,0},
                {7,1155,50,11,0,4,0},
                {7,1155,51,11,0,4,0},
                {7,1155,52,11,0,4,0},
                {7,1155,53,11,0,4,0},
                {7,1155,54,11,0,4,0},
                {7,1155,55,11,0,4,0},
                {3,3,11,12,0,4,0},
                {3,3,12,12,0,4,0},
                {7,1155,46,12,0,4,0},
                {7,1155,47,12,0,4,0},
                {7,1155,48,12,0,4,0},
                {7,1155,49,12,0,4,0},
                {7,1155,50,12,0,4,0},
                {7,1155,51,12,0,4,0},
                {7,1155,52,12,0,4,0},
                {7,1155,53,12,0,4,0},
                {7,1155,54,12,0,4,0},
                {3,3,11,13,0,4,0},
                {3,3,12,13,0,4,0},
                {7,1155,46,13,0,4,0},
                {7,1155,47,13,0,4,0},
                {7,1155,48,13,0,4,0},
                {7,1155,49,13,0,4,0},
                {7,1155,50,13,0,4,0},
                {7,1155,51,13,0,4,0},
                {7,1155,52,13,0,4,0},
                {7,1155,53,13,0,4,0},
                {7,1155,54,13,0,4,0},
                {3,3,10,14,0,4,0},
                {3,3,11,14,0,4,0},
                {3,3,12,14,0,4,0},
                {3,3,13,14,0,4,0},
                {7,1155,46,14,0,4,0},
                {7,1155,47,14,0,4,0},
                {7,1155,48,14,0,4,0},
                {7,1155,49,14,0,4,0},
                {7,1155,50,14,0,4,0},
                {7,1155,51,14,0,4,0},
                {7,1155,52,14,0,4,0},
                {7,1155,53,14,0,4,0},
                {7,1155,54,14,0,4,0},
                {3,3,10,15,0,4,0},
                {3,3,11,15,0,4,0},
                {3,3,12,15,0,4,0},
                {3,3,13,15,0,4,0},
                {7,1155,46,15,0,4,0},
                {7,1155,47,15,0,4,0},
                {7,1155,48,15,0,4,0},
                {7,1155,49,15,0,4,0},
                {7,1155,50,15,0,4,0},
                {7,1155,51,15,0,4,0},
                {7,1155,52,15,0,4,0},
                {7,1155,53,15,0,4,0},
                {7,1155,54,15,0,4,0},
                {3,3,10,16,0,4,0},
                {3,3,11,16,0,4,0},
                {3,3,12,16,0,4,0},
                {3,3,13,16,0,4,0},
                {7,1155,47,16,0,4,0},
                {7,1155,48,16,0,4,0},
                {7,1155,49,16,0,4,0},
                {7,1155,50,16,0,4,0},
                {7,1155,51,16,0,4,0},
                {7,1155,52,16,0,4,0},
                {7,1155,53,16,0,4,0},
                {3,3,10,17,0,4,0},
                {3,3,11,17,0,4,0},
                {3,3,12,17,0,4,0},
                {3,3,13,17,0,4,0},
                {7,1155,49,17,0,4,0},
                {7,1155,50,17,0,4,0},
                {7,1155,51,17,0,4,0},
                {3,3,11,18,0,4,0},
                {3,3,12,18,0,4,0},
                {20,20,29,51,0,4,0},
                {7,1155,50,5,0,5,0},
                {7,1155,51,5,0,5,0},
                {7,1155,52,5,0,5,0},
                {7,1155,53,5,0,5,0},
                {7,1155,54,5,0,5,0},
                {7,1155,48,6,0,5,0},
                {7,1155,49,6,0,5,0},
                {7,1155,50,6,0,5,0},
                {7,1155,51,6,0,5,0},
                {7,1155,52,6,0,5,0},
                {7,1155,53,6,0,5,0},
                {7,1155,54,6,0,5,0},
                {7,1155,55,6,0,5,0},
                {7,1155,48,7,0,5,0},
                {7,1155,49,7,0,5,0},
                {7,1155,50,7,0,5,0},
                {7,1155,51,7,0,5,0},
                {7,1155,52,7,0,5,0},
                {7,1155,53,7,0,5,0},
                {7,1155,54,7,0,5,0},
                {7,1155,55,7,0,5,0},
                {7,1155,47,8,0,5,0},
                {7,1155,48,8,0,5,0},
                {7,1155,49,8,0,5,0},
                {7,1155,50,8,0,5,0},
                {7,1155,51,8,0,5,0},
                {7,1155,52,8,0,5,0},
                {7,1155,53,8,0,5,0},
                {7,1155,54,8,0,5,0},
                {7,1155,55,8,0,5,0},
                {7,1155,56,8,0,5,0},
                {7,1155,46,9,0,5,0},
                {7,1155,47,9,0,5,0},
                {7,1155,48,9,0,5,0},
                {7,1155,49,9,0,5,0},
                {7,1155,50,9,0,5,0},
                {7,1155,54,9,0,5,0},
                {7,1155,55,9,0,5,0},
                {7,1155,56,9,0,5,0},
                {7,1155,46,10,0,5,0},
                {7,1155,47,10,0,5,0},
                {7,1155,48,10,0,5,0},
                {7,1155,49,10,0,5,0},
                {7,1155,54,10,0,5,0},
                {7,1155,55,10,0,5,0},
                {7,1155,56,10,0,5,0},
                {7,1155,57,10,0,5,0},
                {7,1155,46,11,0,5,0},
                {7,1155,47,11,0,5,0},
                {7,1155,48,11,0,5,0},
                {7,1155,54,11,0,5,0},
                {7,1155,55,11,0,5,0},
                {7,1155,56,11,0,5,0},
                {7,1155,57,11,0,5,0},
                {7,1155,46,12,0,5,0},
                {7,1155,47,12,0,5,0},
                {7,1155,48,12,0,5,0},
                {7,1155,54,12,0,5,0},
                {7,1155,55,12,0,5,0},
                {7,1155,56,12,0,5,0},
                {7,1155,57,12,0,5,0},
                {7,1155,46,13,0,5,0},
                {7,1155,47,13,0,5,0},
                {7,1155,48,13,0,5,0},
                {7,1155,54,13,0,5,0},
                {7,1155,55,13,0,5,0},
                {7,1155,56,13,0,5,0},
                {7,1155,57,13,0,5,0},
                {7,1155,46,14,0,5,0},
                {7,1155,47,14,0,5,0},
                {7,1155,48,14,0,5,0},
                {7,1155,49,14,0,5,0},
                {7,1155,53,14,0,5,0},
                {7,1155,54,14,0,5,0},
                {7,1155,55,14,0,5,0},
                {7,1155,56,14,0,5,0},
                {7,1155,57,14,0,5,0},
                {7,1155,46,15,0,5,0},
                {7,1155,47,15,0,5,0},
                {7,1155,48,15,0,5,0},
                {7,1155,49,15,0,5,0},
                {7,1155,50,15,0,5,0},
                {7,1155,51,15,0,5,0},
                {7,1155,52,15,0,5,0},
                {7,1155,53,15,0,5,0},
                {7,1155,54,15,0,5,0},
                {7,1155,55,15,0,5,0},
                {7,1155,56,15,0,5,0},
                {7,1155,57,15,0,5,0},
                {4,4,27,16,0,5,0},
                {7,1155,46,16,0,5,0},
                {7,1155,47,16,0,5,0},
                {7,1155,48,16,0,5,0},
                {7,1155,49,16,0,5,0},
                {7,1155,50,16,0,5,0},
                {7,1155,51,16,0,5,0},
                {7,1155,52,16,0,5,0},
                {7,1155,53,16,0,5,0},
                {7,1155,54,16,0,5,0},
                {7,1155,55,16,0,5,0},
                {7,1155,56,16,0,5,0},
                {7,1155,47,17,0,5,0},
                {7,1155,48,17,0,5,0},
                {7,1155,49,17,0,5,0},
                {7,1155,50,17,0,5,0},
                {7,1155,51,17,0,5,0},
                {7,1155,52,17,0,5,0},
                {7,1155,53,17,0,5,0},
                {7,1155,54,17,0,5,0},
                {7,1155,55,17,0,5,0},
                {7,1155,48,18,0,5,0},
                {7,1155,49,18,0,5,0},
                {7,1155,50,18,0,5,0},
                {7,1155,51,18,0,5,0},
                {7,1155,52,18,0,5,0},
                {7,1155,53,18,0,5,0},
                {7,1155,54,18,0,5,0},
                {20,20,30,52,0,5,0},
                {7,1155,48,5,0,6,0},
                {7,1155,49,5,0,6,0},
                {7,1155,50,5,0,6,0},
                {7,1155,51,5,0,6,0},
                {7,1155,52,5,0,6,0},
                {7,1155,47,6,0,6,0},
                {7,1155,48,6,0,6,0},
                {7,1155,49,6,0,6,0},
                {7,1155,50,6,0,6,0},
                {7,1155,51,6,0,6,0},
                {7,1155,52,6,0,6,0},
                {7,1155,53,6,0,6,0},
                {7,1155,54,6,0,6,0},
                {7,1155,47,7,0,6,0},
                {7,1155,48,7,0,6,0},
                {7,1155,49,7,0,6,0},
                {7,1155,50,7,0,6,0},
                {7,1155,51,7,0,6,0},
                {7,1155,52,7,0,6,0},
                {7,1155,53,7,0,6,0},
                {7,1155,54,7,0,6,0},
                {7,1155,46,8,0,6,0},
                {7,1155,47,8,0,6,0},
                {7,1155,48,8,0,6,0},
                {7,1155,49,8,0,6,0},
                {7,1155,50,8,0,6,0},
                {7,1155,51,8,0,6,0},
                {7,1155,52,8,0,6,0},
                {7,1155,53,8,0,6,0},
                {7,1155,54,8,0,6,0},
                {7,1155,55,8,0,6,0},
                {7,1155,45,9,0,6,0},
                {7,1155,46,9,0,6,0},
                {7,1155,47,9,0,6,0},
                {7,1155,48,9,0,6,0},
                {7,1155,49,9,0,6,0},
                {7,1155,50,9,0,6,0},
                {7,1155,51,9,0,6,0},
                {7,1155,52,9,0,6,0},
                {7,1155,53,9,0,6,0},
                {7,1155,54,9,0,6,0},
                {7,1155,55,9,0,6,0},
                {7,1155,45,10,0,6,0},
                {7,1155,46,10,0,6,0},
                {7,1155,47,10,0,6,0},
                {7,1155,48,10,0,6,0},
                {7,1155,49,10,0,6,0},
                {7,1155,53,10,0,6,0},
                {7,1155,54,10,0,6,0},
                {7,1155,55,10,0,6,0},
                {7,1155,56,10,0,6,0},
                {7,1155,45,11,0,6,0},
                {7,1155,46,11,0,6,0},
                {7,1155,47,11,0,6,0},
                {7,1155,48,11,0,6,0},
                {7,1155,54,11,0,6,0},
                {7,1155,55,11,0,6,0},
                {7,1155,56,11,0,6,0},
                {7,1155,45,12,0,6,0},
                {7,1155,46,12,0,6,0},
                {7,1155,47,12,0,6,0},
                {7,1155,48,12,0,6,0},
                {7,1155,54,12,0,6,0},
                {7,1155,55,12,0,6,0},
                {7,1155,56,12,0,6,0},
                {7,1155,45,13,0,6,0},
                {7,1155,46,13,0,6,0},
                {7,1155,47,13,0,6,0},
                {7,1155,48,13,0,6,0},
                {7,1155,54,13,0,6,0},
                {7,1155,55,13,0,6,0},
                {7,1155,56,13,0,6,0},
                {7,1155,45,14,0,6,0},
                {7,1155,46,14,0,6,0},
                {7,1155,47,14,0,6,0},
                {7,1155,48,14,0,6,0},
                {7,1155,54,14,0,6,0},
                {7,1155,55,14,0,6,0},
                {7,1155,56,14,0,6,0},
                {7,1155,45,15,0,6,0},
                {7,1155,46,15,0,6,0},
                {7,1155,47,15,0,6,0},
                {7,1155,48,15,0,6,0},
                {7,1155,49,15,0,6,0},
                {7,1155,52,15,0,6,0},
                {7,1155,53,15,0,6,0},
                {7,1155,54,15,0,6,0},
                {7,1155,55,15,0,6,0},
                {7,1155,56,15,0,6,0},
                {4,4,27,16,0,6,0},
                {7,1155,46,16,0,6,0},
                {7,1155,47,16,0,6,0},
                {7,1155,48,16,0,6,0},
                {7,1155,49,16,0,6,0},
                {7,1155,50,16,0,6,0},
                {7,1155,51,16,0,6,0},
                {7,1155,52,16,0,6,0},
                {7,1155,53,16,0,6,0},
                {7,1155,54,16,0,6,0},
                {7,1155,55,16,0,6,0},
                {7,1155,56,16,0,6,0},
                {7,1155,46,17,0,6,0},
                {7,1155,47,17,0,6,0},
                {7,1155,48,17,0,6,0},
                {7,1155,49,17,0,6,0},
                {7,1155,50,17,0,6,0},
                {7,1155,51,17,0,6,0},
                {7,1155,52,17,0,6,0},
                {7,1155,53,17,0,6,0},
                {7,1155,54,17,0,6,0},
                {7,1155,55,17,0,6,0},
                {7,1155,47,18,0,6,0},
                {7,1155,48,18,0,6,0},
                {7,1155,49,18,0,6,0},
                {7,1155,50,18,0,6,0},
                {7,1155,51,18,0,6,0},
                {7,1155,52,18,0,6,0},
                {7,1155,53,18,0,6,0},
                {7,1155,54,18,0,6,0},
                {7,1155,50,19,0,6,0},
                {7,1155,51,19,0,6,0},
                {7,1155,52,19,0,6,0},
                {7,1155,53,19,0,6,0},
                {7,1155,50,20,0,6,0},
                {7,1155,51,20,0,6,0},
                {7,1155,52,20,0,6,0},
                {7,1155,53,20,0,6,0},
                {13,13,43,37,0,6,0},
                {13,13,37,38,0,6,0},
                {13,13,38,38,0,6,0},
                {13,13,39,38,0,6,0},
                {13,13,42,38,0,6,0},
                {13,13,43,38,0,6,0},
                {13,13,37,39,0,6,0},
                {13,13,38,39,0,6,0},
                {13,13,39,39,0,6,0},
                {13,13,42,39,0,6,0},
                {13,13,43,39,0,6,0},
                {13,13,37,40,0,6,0},
                {13,13,38,40,0,6,0},
                {13,13,39,40,0,6,0},
                {13,13,40,40,0,6,0},
                {13,13,41,40,0,6,0},
                {13,13,42,40,0,6,0},
                {13,13,43,40,0,6,0},
                {13,13,37,41,0,6,0},
                {13,13,38,41,0,6,0},
                {13,13,39,41,0,6,0},
                {13,13,40,41,0,6,0},
                {13,13,41,41,0,6,0},
                {13,13,42,41,0,6,0},
                {13,13,43,41,0,6,0},
                {20,20,31,53,0,6,0},
                {7,1155,47,3,0,7,0},
                {7,1155,48,3,0,7,0},
                {7,1155,49,3,0,7,0},
                {7,1155,50,3,0,7,0},
                {7,1155,51,3,0,7,0},
                {7,1155,45,4,0,7,0},
                {7,1155,46,4,0,7,0},
                {7,1155,47,4,0,7,0},
                {7,1155,48,4,0,7,0},
                {7,1155,49,4,0,7,0},
                {7,1155,50,4,0,7,0},
                {7,1155,51,4,0,7,0},
                {7,1155,44,5,0,7,0},
                {7,1155,45,5,0,7,0},
                {7,1155,46,5,0,7,0},
                {7,1155,47,5,0,7,0},
                {7,1155,48,5,0,7,0},
                {7,1155,49,5,0,7,0},
                {7,1155,50,5,0,7,0},
                {7,1155,51,5,0,7,0},
                {7,1155,52,5,0,7,0},
                {7,1155,43,6,0,7,0},
                {7,1155,44,6,0,7,0},
                {7,1155,45,6,0,7,0},
                {7,1155,46,6,0,7,0},
                {7,1155,47,6,0,7,0},
                {7,1155,48,6,0,7,0},
                {7,1155,49,6,0,7,0},
                {7,1155,50,6,0,7,0},
                {7,1155,51,6,0,7,0},
                {7,1155,52,6,0,7,0},
                {7,1155,53,6,0,7,0},
                {7,1155,43,7,0,7,0},
                {7,1155,44,7,0,7,0},
                {7,1155,45,7,0,7,0},
                {7,1155,46,7,0,7,0},
                {7,1155,47,7,0,7,0},
                {7,1155,48,7,0,7,0},
                {7,1155,49,7,0,7,0},
                {7,1155,50,7,0,7,0},
                {7,1155,51,7,0,7,0},
                {7,1155,52,7,0,7,0},
                {7,1155,53,7,0,7,0},
                {7,1155,43,8,0,7,0},
                {7,1155,44,8,0,7,0},
                {7,1155,45,8,0,7,0},
                {7,1155,46,8,0,7,0},
                {7,1155,47,8,0,7,0},
                {7,1155,48,8,0,7,0},
                {7,1155,49,8,0,7,0},
                {7,1155,50,8,0,7,0},
                {7,1155,51,8,0,7,0},
                {7,1155,52,8,0,7,0},
                {7,1155,53,8,0,7,0},
                {7,1155,54,8,0,7,0},
                {7,1155,55,8,0,7,0},
                {7,1155,42,9,0,7,0},
                {7,1155,43,9,0,7,0},
                {7,1155,44,9,0,7,0},
                {7,1155,45,9,0,7,0},
                {7,1155,46,9,0,7,0},
                {7,1155,47,9,0,7,0},
                {7,1155,48,9,0,7,0},
                {7,1155,49,9,0,7,0},
                {7,1155,50,9,0,7,0},
                {7,1155,51,9,0,7,0},
                {7,1155,52,9,0,7,0},
                {7,1155,53,9,0,7,0},
                {7,1155,54,9,0,7,0},
                {7,1155,55,9,0,7,0},
                {7,1155,42,10,0,7,0},
                {7,1155,43,10,0,7,0},
                {7,1155,44,10,0,7,0},
                {7,1155,45,10,0,7,0},
                {7,1155,46,10,0,7,0},
                {7,1155,47,10,0,7,0},
                {7,1155,52,10,0,7,0},
                {7,1155,53,10,0,7,0},
                {7,1155,54,10,0,7,0},
                {7,1155,55,10,0,7,0},
                {7,1155,56,10,0,7,0},
                {7,1155,42,11,0,7,0},
                {7,1155,43,11,0,7,0},
                {7,1155,44,11,0,7,0},
                {7,1155,45,11,0,7,0},
                {7,1155,46,11,0,7,0},
                {7,1155,52,11,0,7,0},
                {7,1155,53,11,0,7,0},
                {7,1155,54,11,0,7,0},
                {7,1155,55,11,0,7,0},
                {7,1155,56,11,0,7,0},
                {7,1155,42,12,0,7,0},
                {7,1155,43,12,0,7,0},
                {7,1155,44,12,0,7,0},
                {7,1155,45,12,0,7,0},
                {7,1155,46,12,0,7,0},
                {7,1155,52,12,0,7,0},
                {7,1155,53,12,0,7,0},
                {7,1155,54,12,0,7,0},
                {7,1155,55,12,0,7,0},
                {7,1155,56,12,0,7,0},
                {7,1155,42,13,0,7,0},
                {7,1155,43,13,0,7,0},
                {7,1155,44,13,0,7,0},
                {7,1155,45,13,0,7,0},
                {7,1155,46,13,0,7,0},
                {7,1155,52,13,0,7,0},
                {7,1155,53,13,0,7,0},
                {7,1155,54,13,0,7,0},
                {7,1155,55,13,0,7,0},
                {7,1155,56,13,0,7,0},
                {7,1155,42,14,0,7,0},
                {7,1155,43,14,0,7,0},
                {7,1155,44,14,0,7,0},
                {7,1155,45,14,0,7,0},
                {7,1155,46,14,0,7,0},
                {7,1155,52,14,0,7,0},
                {7,1155,53,14,0,7,0},
                {7,1155,54,14,0,7,0},
                {7,1155,55,14,0,7,0},
                {7,1155,56,14,0,7,0},
                {7,1155,42,15,0,7,0},
                {7,1155,43,15,0,7,0},
                {7,1155,44,15,0,7,0},
                {7,1155,45,15,0,7,0},
                {7,1155,46,15,0,7,0},
                {7,1155,47,15,0,7,0},
                {7,1155,52,15,0,7,0},
                {7,1155,53,15,0,7,0},
                {7,1155,54,15,0,7,0},
                {7,1155,55,15,0,7,0},
                {7,1155,56,15,0,7,0},
                {7,1155,42,16,0,7,0},
                {7,1155,43,16,0,7,0},
                {7,1155,44,16,0,7,0},
                {7,1155,45,16,0,7,0},
                {7,1155,46,16,0,7,0},
                {7,1155,47,16,0,7,0},
                {7,1155,48,16,0,7,0},
                {7,1155,49,16,0,7,0},
                {7,1155,50,16,0,7,0},
                {7,1155,51,16,0,7,0},
                {7,1155,52,16,0,7,0},
                {7,1155,53,16,0,7,0},
                {7,1155,54,16,0,7,0},
                {7,1155,55,16,0,7,0},
                {7,1155,56,16,0,7,0},
                {7,1155,43,17,0,7,0},
                {7,1155,44,17,0,7,0},
                {7,1155,45,17,0,7,0},
                {7,1155,46,17,0,7,0},
                {7,1155,47,17,0,7,0},
                {7,1155,48,17,0,7,0},
                {7,1155,49,17,0,7,0},
                {7,1155,50,17,0,7,0},
                {7,1155,51,17,0,7,0},
                {7,1155,52,17,0,7,0},
                {7,1155,53,17,0,7,0},
                {7,1155,54,17,0,7,0},
                {7,1155,55,17,0,7,0},
                {7,1155,43,18,0,7,0},
                {7,1155,44,18,0,7,0},
                {7,1155,45,18,0,7,0},
                {7,1155,46,18,0,7,0},
                {7,1155,47,18,0,7,0},
                {7,1155,48,18,0,7,0},
                {7,1155,49,18,0,7,0},
                {7,1155,50,18,0,7,0},
                {7,1155,51,18,0,7,0},
                {7,1155,52,18,0,7,0},
                {7,1155,53,18,0,7,0},
                {7,1155,54,18,0,7,0},
                {7,1155,44,19,0,7,0},
                {7,1155,45,19,0,7,0},
                {7,1155,46,19,0,7,0},
                {7,1155,47,19,0,7,0},
                {7,1155,48,19,0,7,0},
                {7,1155,49,19,0,7,0},
                {7,1155,50,19,0,7,0},
                {7,1155,51,19,0,7,0},
                {7,1155,52,19,0,7,0},
                {7,1155,53,19,0,7,0},
                {7,1155,44,20,0,7,0},
                {7,1155,45,20,0,7,0},
                {7,1155,46,20,0,7,0},
                {7,1155,47,20,0,7,0},
                {7,1155,48,20,0,7,0},
                {7,1155,49,20,0,7,0},
                {7,1155,50,20,0,7,0},
                {7,1155,51,20,0,7,0},
                {7,1155,52,20,0,7,0},
                {7,1155,53,20,0,7,0},
                {13,13,43,35,0,7,0},
                {13,13,43,36,0,7,0},
                {13,13,44,36,0,7,0},
                {13,13,37,37,0,7,0},
                {13,13,38,37,0,7,0},
                {13,13,42,37,0,7,0},
                {13,13,43,37,0,7,0},
                {13,13,44,37,0,7,0},
                {13,13,45,37,0,7,0},
                {13,13,36,38,0,7,0},
                {13,13,37,38,0,7,0},
                {13,13,38,38,0,7,0},
                {13,13,39,38,0,7,0},
                {13,13,40,38,0,7,0},
                {13,13,41,38,0,7,0},
                {13,13,42,38,0,7,0},
                {13,13,43,38,0,7,0},
                {13,13,44,38,0,7,0},
                {13,13,45,38,0,7,0},
                {13,13,46,38,0,7,0},
                {13,13,36,39,0,7,0},
                {13,13,37,39,0,7,0},
                {13,13,38,39,0,7,0},
                {13,13,39,39,0,7,0},
                {13,13,40,39,0,7,0},
                {13,13,41,39,0,7,0},
                {13,13,42,39,0,7,0},
                {13,13,43,39,0,7,0},
                {13,13,44,39,0,7,0},
                {13,13,45,39,0,7,0},
                {13,13,46,39,0,7,0},
                {13,13,35,40,0,7,0},
                {13,13,36,40,0,7,0},
                {13,13,37,40,0,7,0},
                {13,13,38,40,0,7,0},
                {13,13,39,40,0,7,0},
                {13,13,40,40,0,7,0},
                {13,13,41,40,0,7,0},
                {13,13,42,40,0,7,0},
                {13,13,43,40,0,7,0},
                {13,13,44,40,0,7,0},
                {13,13,45,40,0,7,0},
                {13,13,46,40,0,7,0},
                {13,13,35,41,0,7,0},
                {13,13,36,41,0,7,0},
                {13,13,37,41,0,7,0},
                {13,13,38,41,0,7,0},
                {13,13,39,41,0,7,0},
                {13,13,40,41,0,7,0},
                {13,13,41,41,0,7,0},
                {13,13,42,41,0,7,0},
                {13,13,43,41,0,7,0},
                {13,13,44,41,0,7,0},
                {13,13,45,41,0,7,0},
                {13,13,46,41,0,7,0},
                {13,13,35,42,0,7,0},
                {13,13,36,42,0,7,0},
                {13,13,37,42,0,7,0},
                {13,13,38,42,0,7,0},
                {13,13,39,42,0,7,0},
                {13,13,40,42,0,7,0},
                {13,13,41,42,0,7,0},
                {13,13,42,42,0,7,0},
                {13,13,43,42,0,7,0},
                {13,13,44,42,0,7,0},
                {13,13,45,42,0,7,0},
                {13,13,37,43,0,7,0},
                {13,13,38,43,0,7,0},
                {13,13,39,43,0,7,0},
                {13,13,40,43,0,7,0},
                {13,13,41,43,0,7,0},
                {13,13,42,43,0,7,0},
                {13,13,43,43,0,7,0},
                {13,13,44,43,0,7,0},
                {13,13,38,44,0,7,0},
                {13,13,39,44,0,7,0},
                {13,13,40,44,0,7,0},
                {13,13,41,44,0,7,0},
                {13,13,38,45,0,7,0},
                {13,13,39,45,0,7,0},
                {13,13,40,45,0,7,0},
                {13,13,41,45,0,7,0},
                {20,20,32,54,0,7,0},
                {7,1155,47,4,0,8,0},
                {7,1155,48,4,0,8,0},
                {7,1155,49,4,0,8,0},
                {7,1155,50,4,0,8,0},
                {7,1155,51,4,0,8,0},
                {7,1155,46,5,0,8,0},
                {7,1155,47,5,0,8,0},
                {7,1155,48,5,0,8,0},
                {7,1155,49,5,0,8,0},
                {7,1155,50,5,0,8,0},
                {7,1155,51,5,0,8,0},
                {7,1155,52,5,0,8,0},
                {7,1155,53,5,0,8,0},
                {7,1155,46,6,0,8,0},
                {7,1155,47,6,0,8,0},
                {7,1155,48,6,0,8,0},
                {7,1155,49,6,0,8,0},
                {7,1155,50,6,0,8,0},
                {7,1155,51,6,0,8,0},
                {7,1155,52,6,0,8,0},
                {7,1155,53,6,0,8,0},
                {7,1155,54,6,0,8,0},
                {7,1155,46,7,0,8,0},
                {7,1155,47,7,0,8,0},
                {7,1155,48,7,0,8,0},
                {7,1155,49,7,0,8,0},
                {7,1155,50,7,0,8,0},
                {7,1155,51,7,0,8,0},
                {7,1155,52,7,0,8,0},
                {7,1155,53,7,0,8,0},
                {7,1155,54,7,0,8,0},
                {7,1155,45,8,0,8,0},
                {7,1155,46,8,0,8,0},
                {7,1155,47,8,0,8,0},
                {7,1155,48,8,0,8,0},
                {7,1155,49,8,0,8,0},
                {7,1155,50,8,0,8,0},
                {7,1155,51,8,0,8,0},
                {7,1155,52,8,0,8,0},
                {7,1155,53,8,0,8,0},
                {7,1155,54,8,0,8,0},
                {7,1155,45,9,0,8,0},
                {7,1155,46,9,0,8,0},
                {7,1155,47,9,0,8,0},
                {7,1155,48,9,0,8,0},
                {7,1155,49,9,0,8,0},
                {7,1155,50,9,0,8,0},
                {7,1155,51,9,0,8,0},
                {7,1155,52,9,0,8,0},
                {7,1155,53,9,0,8,0},
                {7,1155,54,9,0,8,0},
                {7,1155,44,10,0,8,0},
                {7,1155,45,10,0,8,0},
                {7,1155,46,10,0,8,0},
                {7,1155,47,10,0,8,0},
                {7,1155,48,10,0,8,0},
                {7,1155,49,10,0,8,0},
                {7,1155,50,10,0,8,0},
                {7,1155,51,10,0,8,0},
                {7,1155,52,10,0,8,0},
                {7,1155,53,10,0,8,0},
                {7,1155,54,10,0,8,0},
                {7,1155,55,10,0,8,0},
                {7,1155,44,11,0,8,0},
                {7,1155,45,11,0,8,0},
                {7,1155,46,11,0,8,0},
                {7,1155,47,11,0,8,0},
                {7,1155,48,11,0,8,0},
                {7,1155,49,11,0,8,0},
                {7,1155,50,11,0,8,0},
                {7,1155,51,11,0,8,0},
                {7,1155,52,11,0,8,0},
                {7,1155,53,11,0,8,0},
                {7,1155,54,11,0,8,0},
                {7,1155,55,11,0,8,0},
                {7,1155,44,12,0,8,0},
                {7,1155,45,12,0,8,0},
                {7,1155,46,12,0,8,0},
                {7,1155,47,12,0,8,0},
                {7,1155,48,12,0,8,0},
                {7,1155,49,12,0,8,0},
                {7,1155,50,12,0,8,0},
                {7,1155,51,12,0,8,0},
                {7,1155,52,12,0,8,0},
                {7,1155,53,12,0,8,0},
                {7,1155,54,12,0,8,0},
                {7,1155,55,12,0,8,0},
                {7,1155,44,13,0,8,0},
                {7,1155,45,13,0,8,0},
                {7,1155,46,13,0,8,0},
                {7,1155,47,13,0,8,0},
                {7,1155,48,13,0,8,0},
                {7,1155,49,13,0,8,0},
                {7,1155,50,13,0,8,0},
                {7,1155,51,13,0,8,0},
                {7,1155,52,13,0,8,0},
                {7,1155,53,13,0,8,0},
                {7,1155,54,13,0,8,0},
                {7,1155,55,13,0,8,0},
                {7,1155,45,14,0,8,0},
                {7,1155,46,14,0,8,0},
                {7,1155,47,14,0,8,0},
                {7,1155,48,14,0,8,0},
                {7,1155,49,14,0,8,0},
                {7,1155,50,14,0,8,0},
                {7,1155,51,14,0,8,0},
                {7,1155,52,14,0,8,0},
                {7,1155,53,14,0,8,0},
                {7,1155,54,14,0,8,0},
                {7,1155,45,15,0,8,0},
                {7,1155,46,15,0,8,0},
                {7,1155,47,15,0,8,0},
                {7,1155,48,15,0,8,0},
                {7,1155,49,15,0,8,0},
                {7,1155,50,15,0,8,0},
                {7,1155,51,15,0,8,0},
                {7,1155,52,15,0,8,0},
                {7,1155,53,15,0,8,0},
                {7,1155,54,15,0,8,0},
                {7,1155,45,16,0,8,0},
                {7,1155,46,16,0,8,0},
                {7,1155,47,16,0,8,0},
                {7,1155,48,16,0,8,0},
                {7,1155,49,16,0,8,0},
                {7,1155,50,16,0,8,0},
                {7,1155,51,16,0,8,0},
                {7,1155,52,16,0,8,0},
                {7,1155,53,16,0,8,0},
                {7,1155,47,17,0,8,0},
                {7,1155,48,17,0,8,0},
                {7,1155,49,17,0,8,0},
                {7,1155,50,17,0,8,0},
                {7,1155,51,17,0,8,0},
                {7,1155,52,17,0,8,0},
                {7,1155,53,17,0,8,0},
                {7,1155,50,18,0,8,0},
                {13,13,43,36,0,8,0},
                {13,13,42,37,0,8,0},
                {13,13,43,37,0,8,0},
                {13,13,44,37,0,8,0},
                {13,13,35,38,0,8,0},
                {13,13,36,38,0,8,0},
                {13,13,37,38,0,8,0},
                {13,13,38,38,0,8,0},
                {13,13,39,38,0,8,0},
                {13,13,40,38,0,8,0},
                {13,13,41,38,0,8,0},
                {13,13,42,38,0,8,0},
                {13,13,43,38,0,8,0},
                {13,13,44,38,0,8,0},
                {13,13,35,39,0,8,0},
                {13,13,36,39,0,8,0},
                {13,13,37,39,0,8,0},
                {13,13,38,39,0,8,0},
                {13,13,39,39,0,8,0},
                {13,13,40,39,0,8,0},
                {13,13,41,39,0,8,0},
                {13,13,42,39,0,8,0},
                {13,13,43,39,0,8,0},
                {13,13,44,39,0,8,0},
                {13,13,34,40,0,8,0},
                {13,13,35,40,0,8,0},
                {13,13,36,40,0,8,0},
                {13,13,37,40,0,8,0},
                {13,13,38,40,0,8,0},
                {13,13,39,40,0,8,0},
                {13,13,40,40,0,8,0},
                {13,13,41,40,0,8,0},
                {13,13,42,40,0,8,0},
                {13,13,43,40,0,8,0},
                {13,13,44,40,0,8,0},
                {13,13,45,40,0,8,0},
                {13,13,35,41,0,8,0},
                {13,13,36,41,0,8,0},
                {13,13,37,41,0,8,0},
                {13,13,38,41,0,8,0},
                {13,13,39,41,0,8,0},
                {13,13,40,41,0,8,0},
                {13,13,41,41,0,8,0},
                {13,13,42,41,0,8,0},
                {13,13,43,41,0,8,0},
                {13,13,44,41,0,8,0},
                {13,13,45,41,0,8,0},
                {13,13,46,41,0,8,0},
                {13,13,35,42,0,8,0},
                {13,13,36,42,0,8,0},
                {13,13,37,42,0,8,0},
                {13,13,38,42,0,8,0},
                {13,13,39,42,0,8,0},
                {13,13,40,42,0,8,0},
                {13,13,41,42,0,8,0},
                {13,13,42,42,0,8,0},
                {13,13,43,42,0,8,0},
                {13,13,44,42,0,8,0},
                {13,13,37,43,0,8,0},
                {13,13,38,43,0,8,0},
                {13,13,39,43,0,8,0},
                {13,13,40,43,0,8,0},
                {13,13,41,43,0,8,0},
                {13,13,42,43,0,8,0},
                {13,13,43,43,0,8,0},
                {13,13,38,44,0,8,0},
                {13,13,39,44,0,8,0},
                {13,13,40,44,0,8,0},
                {13,13,41,44,0,8,0},
                {13,13,38,45,0,8,0},
                {13,13,39,45,0,8,0},
                {13,13,40,45,0,8,0},
                {13,13,41,45,0,8,0},
                {20,20,33,55,0,8,0},
                {7,1155,47,6,0,9,0},
                {7,1155,48,6,0,9,0},
                {7,1155,49,6,0,9,0},
                {7,1155,50,6,0,9,0},
                {7,1155,51,6,0,9,0},
                {7,1155,47,7,0,9,0},
                {7,1155,48,7,0,9,0},
                {7,1155,49,7,0,9,0},
                {7,1155,50,7,0,9,0},
                {7,1155,51,7,0,9,0},
                {7,1155,47,8,0,9,0},
                {7,1155,48,8,0,9,0},
                {7,1155,49,8,0,9,0},
                {7,1155,50,8,0,9,0},
                {7,1155,51,8,0,9,0},
                {7,1155,52,8,0,9,0},
                {7,1155,46,9,0,9,0},
                {7,1155,47,9,0,9,0},
                {7,1155,48,9,0,9,0},
                {7,1155,49,9,0,9,0},
                {7,1155,50,9,0,9,0},
                {7,1155,51,9,0,9,0},
                {7,1155,52,9,0,9,0},
                {7,1155,46,10,0,9,0},
                {7,1155,47,10,0,9,0},
                {7,1155,48,10,0,9,0},
                {7,1155,49,10,0,9,0},
                {7,1155,50,10,0,9,0},
                {7,1155,51,10,0,9,0},
                {7,1155,52,10,0,9,0},
                {7,1155,46,11,0,9,0},
                {7,1155,47,11,0,9,0},
                {7,1155,48,11,0,9,0},
                {7,1155,49,11,0,9,0},
                {7,1155,50,11,0,9,0},
                {7,1155,51,11,0,9,0},
                {7,1155,52,11,0,9,0},
                {7,1155,47,12,0,9,0},
                {7,1155,48,12,0,9,0},
                {7,1155,49,12,0,9,0},
                {7,1155,50,12,0,9,0},
                {7,1155,51,12,0,9,0},
                {7,1155,47,13,0,9,0},
                {7,1155,48,13,0,9,0},
                {7,1155,49,13,0,9,0},
                {7,1155,50,13,0,9,0},
                {7,1155,51,13,0,9,0},
                {7,1155,47,14,0,9,0},
                {7,1155,48,14,0,9,0},
                {7,1155,49,14,0,9,0},
                {7,1155,50,14,0,9,0},
                {7,1155,48,15,0,9,0},
                {7,1155,49,15,0,9,0},
                {13,13,37,36,0,9,0},
                {13,13,38,36,0,9,0},
                {13,13,39,36,0,9,0},
                {13,13,40,36,0,9,0},
                {13,13,41,36,0,9,0},
                {13,13,42,36,0,9,0},
                {13,13,36,37,0,9,0},
                {13,13,37,37,0,9,0},
                {13,13,38,37,0,9,0},
                {13,13,39,37,0,9,0},
                {13,13,40,37,0,9,0},
                {13,13,41,37,0,9,0},
                {13,13,42,37,0,9,0},
                {13,13,43,37,0,9,0},
                {13,13,44,37,0,9,0},
                {13,13,45,37,0,9,0},
                {13,13,35,38,0,9,0},
                {13,13,36,38,0,9,0},
                {13,13,37,38,0,9,0},
                {13,13,38,38,0,9,0},
                {13,13,39,38,0,9,0},
                {13,13,40,38,0,9,0},
                {13,13,41,38,0,9,0},
                {13,13,42,38,0,9,0},
                {13,13,43,38,0,9,0},
                {13,13,44,38,0,9,0},
                {13,13,45,38,0,9,0},
                {13,13,46,38,0,9,0},
                {13,13,35,39,0,9,0},
                {13,13,36,39,0,9,0},
                {13,13,37,39,0,9,0},
                {13,13,38,39,0,9,0},
                {13,13,39,39,0,9,0},
                {13,13,40,39,0,9,0},
                {13,13,41,39,0,9,0},
                {13,13,42,39,0,9,0},
                {13,13,43,39,0,9,0},
                {13,13,44,39,0,9,0},
                {13,13,45,39,0,9,0},
                {13,13,46,39,0,9,0},
                {13,13,35,40,0,9,0},
                {13,13,36,40,0,9,0},
                {13,13,37,40,0,9,0},
                {13,13,38,40,0,9,0},
                {13,13,39,40,0,9,0},
                {13,13,40,40,0,9,0},
                {13,13,41,40,0,9,0},
                {13,13,42,40,0,9,0},
                {13,13,43,40,0,9,0},
                {13,13,44,40,0,9,0},
                {13,13,45,40,0,9,0},
                {13,13,46,40,0,9,0},
                {13,13,47,40,0,9,0},
                {13,13,48,40,0,9,0},
                {13,13,34,41,0,9,0},
                {13,13,35,41,0,9,0},
                {13,13,36,41,0,9,0},
                {13,13,37,41,0,9,0},
                {13,13,38,41,0,9,0},
                {13,13,39,41,0,9,0},
                {13,13,40,41,0,9,0},
                {13,13,41,41,0,9,0},
                {13,13,42,41,0,9,0},
                {13,13,43,41,0,9,0},
                {13,13,44,41,0,9,0},
                {13,13,45,41,0,9,0},
                {13,13,46,41,0,9,0},
                {13,13,47,41,0,9,0},
                {13,13,48,41,0,9,0},
                {13,13,34,42,0,9,0},
                {13,13,35,42,0,9,0},
                {13,13,36,42,0,9,0},
                {13,13,37,42,0,9,0},
                {13,13,38,42,0,9,0},
                {13,13,39,42,0,9,0},
                {13,13,40,42,0,9,0},
                {13,13,41,42,0,9,0},
                {13,13,42,42,0,9,0},
                {13,13,43,42,0,9,0},
                {13,13,44,42,0,9,0},
                {13,13,45,42,0,9,0},
                {13,13,46,42,0,9,0},
                {13,13,47,42,0,9,0},
                {13,13,35,43,0,9,0},
                {13,13,36,43,0,9,0},
                {13,13,37,43,0,9,0},
                {13,13,38,43,0,9,0},
                {13,13,39,43,0,9,0},
                {13,13,40,43,0,9,0},
                {13,13,41,43,0,9,0},
                {13,13,42,43,0,9,0},
                {13,13,43,43,0,9,0},
                {13,13,44,43,0,9,0},
                {13,13,45,43,0,9,0},
                {13,13,46,43,0,9,0},
                {13,13,47,43,0,9,0},
                {13,13,37,44,0,9,0},
                {13,13,38,44,0,9,0},
                {13,13,39,44,0,9,0},
                {13,13,40,44,0,9,0},
                {13,13,41,44,0,9,0},
                {13,13,42,44,0,9,0},
                {13,13,43,44,0,9,0},
                {13,13,44,44,0,9,0},
                {13,13,45,44,0,9,0},
                {13,13,46,44,0,9,0},
                {13,13,47,44,0,9,0},
                {13,13,37,45,0,9,0},
                {13,13,38,45,0,9,0},
                {13,13,39,45,0,9,0},
                {13,13,40,45,0,9,0},
                {13,13,41,45,0,9,0},
                {13,13,42,45,0,9,0},
                {13,13,43,45,0,9,0},
                {13,13,44,45,0,9,0},
                {13,13,45,45,0,9,0},
                {13,13,46,45,0,9,0},
                {13,13,47,45,0,9,0},
                {13,13,39,46,0,9,0},
                {13,13,40,46,0,9,0},
                {13,13,41,46,0,9,0},
                {13,13,42,46,0,9,0},
                {13,13,43,46,0,9,0},
                {13,13,44,46,0,9,0},
                {13,13,45,46,0,9,0},
                {13,13,46,46,0,9,0},
                {20,20,34,56,0,9,0},
                {13,13,36,36,0,10,0},
                {13,13,37,36,0,10,0},
                {13,13,38,36,0,10,0},
                {13,13,39,36,0,10,0},
                {13,13,40,36,0,10,0},
                {13,13,45,36,0,10,0},
                {13,13,46,36,0,10,0},
                {13,13,35,37,0,10,0},
                {13,13,36,37,0,10,0},
                {13,13,37,37,0,10,0},
                {13,13,38,37,0,10,0},
                {13,13,39,37,0,10,0},
                {13,13,40,37,0,10,0},
                {13,13,44,37,0,10,0},
                {13,13,45,37,0,10,0},
                {13,13,46,37,0,10,0},
                {13,13,35,38,0,10,0},
                {13,13,36,38,0,10,0},
                {13,13,37,38,0,10,0},
                {13,13,38,38,0,10,0},
                {13,13,39,38,0,10,0},
                {13,13,40,38,0,10,0},
                {13,13,43,38,0,10,0},
                {13,13,44,38,0,10,0},
                {13,13,45,38,0,10,0},
                {13,13,46,38,0,10,0},
                {13,13,47,38,0,10,0},
                {13,13,35,39,0,10,0},
                {13,13,36,39,0,10,0},
                {13,13,37,39,0,10,0},
                {13,13,38,39,0,10,0},
                {13,13,39,39,0,10,0},
                {13,13,40,39,0,10,0},
                {13,13,43,39,0,10,0},
                {13,13,44,39,0,10,0},
                {13,13,45,39,0,10,0},
                {13,13,46,39,0,10,0},
                {13,13,47,39,0,10,0},
                {13,13,35,40,0,10,0},
                {13,13,36,40,0,10,0},
                {13,13,37,40,0,10,0},
                {13,13,38,40,0,10,0},
                {13,13,39,40,0,10,0},
                {13,13,40,40,0,10,0},
                {13,13,43,40,0,10,0},
                {13,13,44,40,0,10,0},
                {13,13,45,40,0,10,0},
                {13,13,46,40,0,10,0},
                {13,13,47,40,0,10,0},
                {13,13,36,41,0,10,0},
                {13,13,37,41,0,10,0},
                {13,13,38,41,0,10,0},
                {13,13,39,41,0,10,0},
                {13,13,43,41,0,10,0},
                {13,13,44,41,0,10,0},
                {13,13,45,41,0,10,0},
                {20,20,35,57,0,10,0},
                {20,20,35,58,0,10,0},
                {13,13,36,37,0,11,0},
                {13,13,37,37,0,11,0},
                {13,13,45,37,0,11,0},
                {13,13,36,38,0,11,0},
                {13,13,37,38,0,11,0},
                {13,13,38,38,0,11,0},
                {13,13,44,38,0,11,0},
                {13,13,45,38,0,11,0},
                {13,13,36,39,0,11,0},
                {13,13,37,39,0,11,0},
                {13,13,38,39,0,11,0},
                {13,13,44,39,0,11,0},
                {13,13,45,39,0,11,0},
                {23,23,55,61,0,11,0}};

    }

    public static ObjCollection getObjects(String objectName, boolean eightBit, double dppXY, double dppZ, String calibratedUnits) {
        // Initialising object store
        ObjCollection testObjects = new ObjCollection(objectName);

        // Adding all provided coordinates to each object
        int[][] coordinates = getCoordinates3D();
        for (int i = 0;i<coordinates.length;i++) {
            int ID = eightBit ? coordinates[i][0] : coordinates[i][1];
            int x = coordinates[i][2];
            int y = coordinates[i][3];
            int z = coordinates[i][5];
            int t = coordinates[i][6];

            testObjects.putIfAbsent(ID,new Obj(objectName,ID,dppXY,dppZ,calibratedUnits));

            Obj testObject = testObjects.get(ID);
            testObject.addCoord(x,y,z);
            testObject.setT(t);

        }

        // Adding measurements to each Obj.  Run through each collection of measurements and add it to the Obj with the same ID number.  Use the measurement names from the interface in this class.

        return testObjects;

    }
}

