package io.github.mianalysis.mia.process.voxel;

public class SphereSolid extends AbstractSphere {
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
