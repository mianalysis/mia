package io.github.mianalysis.mia.process.voxel;

import java.util.ArrayList;

public abstract class AbstractSphere {
    protected ArrayList<Integer> x = new ArrayList<>();
    protected ArrayList<Integer> y = new ArrayList<>();
    protected ArrayList<Integer> z = new ArrayList<>();

    public static boolean inSphere(int xx, int yy, int zz, int r) {
        return Math.sqrt(xx * xx + yy * yy + zz * zz) <= r;
    }

    public int[] getX() {
        int[] xx = new int[x.size()];
        int i = 0;
        for (int xxx : x)
            xx[i++] = xxx;

        return xx;

    }

    public int[] getY() {
        int[] yy = new int[y.size()];
        int i = 0;
        for (int yyy : y)
            yy[i++] = yyy;

        return yy;

    }

    public int[] getZ() {
        int[] zz = new int[z.size()];
        int i = 0;
        for (int zzz : z)
            zz[i++] = zzz;

        return zz;

    }

    public int[][] getSphere() {
        int[] x_sph = getX();
        int[] y_sph = getY();
        int[] z_sph = getZ();

        int[][] sph = new int[x_sph.length][3];
        for (int i = 0; i < x_sph.length; i++) {
            sph[i][0] = x_sph[i];
            sph[i][1] = y_sph[i];
            sph[i][2] = z_sph[i];
        }

        return sph;

    }
}