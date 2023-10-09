package io.github.mianalysis.mia.object.coordinates.voxel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;

public class SphereSolid extends AbstractSphere {
    public static void main(String[] args) {
        SphereSolid sphereSolid = new SphereSolid(45);
        int[] x = sphereSolid.getX();
        int[] y = sphereSolid.getY();
        int[] z = sphereSolid.getZ();

        new ImageJ();
        ImagePlus ipl = IJ.createImage("", 100, 100, 100, 8);
        ImageStack ist = ipl.getStack();
        for (int i = 0; i < x.length; i++) {
            ist.setVoxel(x[i] + 50, y[i] + 50, z[i] + 50, 255);
        }
        ipl.show();
    }

    public SphereSolid(int r) {
        for (int xx = -r; xx < r; xx++) {
            for (int yy = -r; yy < r; yy++) {
                for (int zz= -r; zz < r; zz++) {    
                    if (inSphere(xx,yy,zz,r)) {
                        x.add(xx);
                        y.add(yy);
                        z.add(zz);
                    }
                }
            }
        }
    }
}
