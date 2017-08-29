package wbif.sjx.ModularImageAnalysis.Module;

import java.util.HashMap;

/**
 * Created by Stephen Cross on 29/08/2017.
 */
public class ExpectedObjects {
    public static final int ID_8BIT = 0;
    public static final int ID_16BIT = 1;
    public static final int N_VOXELS = 2;
    public static final int X_MIN = 3;
    public static final int X_MEAN = 4;
    public static final int X_MAX = 5;
    public static final int Y_MIN = 6;
    public static final int Y_MEAN = 7;
    public static final int Y_MAX = 8;
    public static final int Z_MIN = 9;
    public static final int Z_MEAN = 10;
    public static final int Z_MAX = 11;
    public static final int C = 12;
    public static final int F = 13;

    public static HashMap<Integer,double[]> get3D() {
        HashMap<Integer,double[]> expectedValues = new HashMap<>();

        double[] obj1 = new double[14];
        obj1[ID_8BIT] = 3;
        obj1[ID_16BIT] = 3;
        obj1[N_VOXELS] = 272;
        obj1[X_MIN] = 7;
        obj1[X_MEAN] = 11.5;
        obj1[X_MAX] = 16;
        obj1[Y_MIN] = 8;
        obj1[Y_MEAN] = 15.40;
        obj1[Y_MAX] = 23;
        obj1[Z_MIN] = 0;
        obj1[Z_MEAN] = 1.80;
        obj1[Z_MAX] = 4;
        obj1[C] = 0;
        obj1[F] = 0;
        expectedValues.put(272,obj1);

        double[] obj2 = new double[14];
        obj2[ID_8BIT] = 4;
        obj2[ID_16BIT] = 4;
        obj2[N_VOXELS] = 2;
        obj2[X_MIN] = 27;
        obj2[X_MEAN] = 27;
        obj2[X_MAX] = 27;
        obj2[Y_MIN] = 16;
        obj2[Y_MEAN] = 16;
        obj2[Y_MAX] = 16;
        obj2[Z_MIN] = 5;
        obj2[Z_MEAN] = 5.5;
        obj2[Z_MAX] = 6;
        obj2[C] = 0;
        obj2[F] = 0;
        expectedValues.put(2,obj2);

        double[] obj3 = new double[14];
        obj3[ID_8BIT] = 7;
        obj3[ID_16BIT] = 1155;
        obj3[N_VOXELS] = 742;
        obj3[X_MIN] = 42;
        obj3[X_MEAN] = 49.81;
        obj3[X_MAX] = 57;
        obj3[Y_MIN] = 3;
        obj3[Y_MEAN] = 11.73;
        obj3[Y_MAX] = 20;
        obj3[Z_MIN] = 3;
        obj3[Z_MEAN] = 6.31;
        obj3[Z_MAX] = 9;
        obj3[C] = 0;
        obj3[F] = 0;
        expectedValues.put(742,obj3);

        double[] obj4 = new double[14];
        obj4[ID_8BIT] = 8;
        obj4[ID_16BIT] = 8;
        obj4[N_VOXELS] = 90;
        obj4[X_MIN] = 28;
        obj4[X_MEAN] = 34.8;
        obj4[X_MAX] = 41;
        obj4[Y_MIN] = 72;
        obj4[Y_MEAN] = 73.57;
        obj4[Y_MAX] = 75;
        obj4[Z_MIN] = 1;
        obj4[Z_MEAN] = 2;
        obj4[Z_MAX] = 3;
        obj4[C] = 0;
        obj4[F] = 0;
        expectedValues.put(90,obj4);

        double[] obj5 = new double[14];
        obj5[ID_8BIT] = 9;
        obj5[ID_16BIT] = 9;
        obj5[N_VOXELS] = 9;
        obj5[X_MIN] = 17;
        obj5[X_MEAN] = 18;
        obj5[X_MAX] = 19;
        obj5[Y_MIN] = 60;
        obj5[Y_MEAN] = 61;
        obj5[Y_MAX] = 62;
        obj5[Z_MIN] = 2;
        obj5[Z_MEAN] = 2;
        obj5[Z_MAX] = 2;
        obj5[C] = 0;
        obj5[F] = 0;
        expectedValues.put(9,obj5);

        double[] obj6 = new double[14];
        obj6[ID_8BIT] = 13;
        obj6[ID_16BIT] = 13;
        obj6[N_VOXELS] = 375;
        obj6[X_MIN] = 34;
        obj6[X_MEAN] = 40.65;
        obj6[X_MAX] = 48;
        obj6[Y_MIN] = 35;
        obj6[Y_MEAN] = 40.3;
        obj6[Y_MAX] = 46;
        obj6[Z_MIN] = 6;
        obj6[Z_MEAN] = 8.78;
        obj6[Z_MAX] = 11;
        obj6[C] = 0;
        obj6[F] = 0;
        expectedValues.put(375,obj6);

        double[] obj7 = new double[14];
        obj7[ID_8BIT] = 20;
        obj7[ID_16BIT] = 20;
        obj7[N_VOXELS] = 8;
        obj7[X_MIN] = 29;
        obj7[X_MEAN] = 32.38;
        obj7[X_MAX] = 35;
        obj7[Y_MIN] = 51;
        obj7[Y_MEAN] = 54.5;
        obj7[Y_MAX] = 58;
        obj7[Z_MIN] = 4;
        obj7[Z_MEAN] = 7.38;
        obj7[Z_MAX] = 10;
        obj7[C] = 0;
        obj7[F] = 0;
        expectedValues.put(8,obj7);

        double[] obj8 = new double[14];
        obj8[ID_8BIT] = 23;
        obj8[ID_16BIT] = 23;
        obj8[N_VOXELS] = 1;
        obj8[X_MIN] = 55;
        obj8[X_MEAN] = 55;
        obj8[X_MAX] = 55;
        obj8[Y_MIN] = 61;
        obj8[Y_MEAN] = 61;
        obj8[Y_MAX] = 61;
        obj8[Z_MIN] = 11;
        obj8[Z_MEAN] = 11;
        obj8[Z_MAX] = 11;
        obj8[C] = 0;
        obj8[F] = 0;
        expectedValues.put(1,obj8);

        return expectedValues;

    }
}
