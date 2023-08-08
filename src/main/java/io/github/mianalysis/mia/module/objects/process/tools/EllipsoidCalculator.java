package io.github.mianalysis.mia.module.objects.process.tools;

import org.bonej.geometry.Ellipsoid;
import org.bonej.geometry.FitEllipsoid;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

public class EllipsoidCalculator {
    private final Volume volume;
    private Ellipsoid ell;

    public static void main(String[] args) {
        SpatCal spatCal = new SpatCal(1, 1, "px", 200, 200, 20);
        Volume vol = new Volume(VolumeType.POINTLIST, spatCal);
 
        try {
            vol.add(83, 81, 4);
            vol.add(84, 81, 4);
            vol.add(84, 82, 4);
            vol.add(85, 82, 4);
            vol.add(84, 80, 5);
            vol.add(84, 81, 5);
            vol.add(84, 82, 5);
            vol.add(85, 80, 5);
            vol.add(85, 81, 5);
            vol.add(85, 82, 5);
            vol.add(85, 83, 5);
            vol.add(85, 84, 5);
            vol.add(86, 80, 5);
            vol.add(86, 81, 5);
            vol.add(86, 82, 5);
            vol.add(86, 83, 5);
            vol.add(86, 84, 5);
            vol.add(87, 82, 5);
            vol.add(87, 83, 5);
            vol.add(87, 84, 5);
            vol.add(87, 85, 5);
            vol.add(88, 83, 5);
            vol.add(88, 84, 5);
            vol.add(88, 85, 5);
            vol.add(85, 83, 6);
            vol.add(86, 81, 6);
            vol.add(86, 82, 6);
            vol.add(86, 83, 6);
            vol.add(86, 84, 6);
            vol.add(86, 85, 6);
            vol.add(87, 82, 6);
            vol.add(87, 83, 6);
            vol.add(87, 84, 6);
            vol.add(87, 85, 6);
            vol.add(88, 84, 6);
            vol.add(88, 85, 6);
            vol.add(83, 83, 7);

            vol.add(84, 82, 7);
            vol.add(84, 83, 7);
            vol.add(84, 84, 7);
            vol.add(85, 82, 7);
            vol.add(85, 83, 7);
            vol.add(85, 84, 7);
            vol.add(85, 85, 7);
            vol.add(86, 82, 7);
            vol.add(86, 83, 7);
            vol.add(86, 84, 7);
            vol.add(86, 85, 7);
            vol.add(87, 82, 7);
            vol.add(87, 83, 7);
            vol.add(87, 84, 7);
            vol.add(87, 85, 7);
            vol.add(88, 85, 7);

            // vol.add(83,82,4);
            // vol.add(83,83,4);
            // vol.add(84,83,4);
            // vol.add(85,81,4);
            // vol.add(85,83,4);
            // vol.add(83,84,7);
            // vol.add(84,85,7);
            // vol.add(84,86,7);
            // vol.add(85,86,7);
            // vol.add(89,85,7);
            // vol.add(89,85,7);

        } catch (PointOutOfRangeException e) {
        }

        EllipsoidCalculator calculator = new EllipsoidCalculator(vol, 1000);
        double[] ori = calculator.getOrientationRads();
        System.out.println("Ori " + ori[0] + "_" + ori[1]);

        double sphericity = calculator.getSphericity();
        System.out.println("Sphericity " + sphericity);

    }

    /**
     * This constructor is package-private. As such, it's intended for testing only.
     * 
     * @param ell
     * @param volume
     */
    EllipsoidCalculator(Ellipsoid ell, Volume volume) throws RuntimeException {
        this.ell = ell;
        this.volume = volume;
    }

    public EllipsoidCalculator(Volume volume, double maxAxisLength) throws RuntimeException {
        this.volume = volume;

        double[][] coords = new double[volume.size()][3];
        int i = 0;

        int xCent = (int) Math.round(volume.getXMean(true));
        int yCent = (int) Math.round(volume.getYMean(true));
        int zCent = (int) Math.round(volume.getZMean(true, false));

        for (Point<Integer> point : volume.getCoordinateSet()) {
            coords[i][0] = point.getX() - xCent;
            coords[i][1] = point.getY() - yCent;
            coords[i++][2] = volume.getXYScaledZ(point.getZ()) - zCent;
        }

        try {
            ell = new Ellipsoid(FitEllipsoid.yuryPetrov(coords));
            double[] ellCent = ell.getCentre();
            ell.setCentroid(ellCent[0] + xCent, ellCent[1] + yCent, ellCent[2] + zCent);
        } catch (RuntimeException e) {
            ell = null;
            throw e;
        }

        double[] radii = ell.getRadii();
        if (radii[0] > maxAxisLength || radii[1] > maxAxisLength || radii[2] > maxAxisLength)
            ell = null;

    }

    public double[] getCentroid() {
        if (ell == null)
            return null;
        return ell.getCentre();

    }

    public double[] getRadii() {
        if (ell == null)
            return null;
        return ell.getRadii();

    }

    public double[][] getRotationMatrix() {
        if (ell == null)
            return null;
        return ell.getRotation();

    }

    public double[] getOrientationRads() {
        if (ell == null)
            return null;
        double[][] rot = ell.getRotation();
        double[] orien = new double[2];

        orien[0] = Math.atan(rot[1][0] / rot[0][0]); // Orientation relative to x axis (positive above, negative below)
        double xy = Math.sqrt(Math.pow(rot[0][0], 2) + Math.pow(rot[1][0], 2));
        orien[1] = Math.atan(rot[2][0] / xy); // Orientation relative to xy plane

        if (rot[0][0] <= 0) {
            orien[1] = -orien[1];
        }

        return orien;

    }

    /**
     * Gives an approximation of the surface area, which has a relative error of
     * 1.061% (Knud Thomsen's formula)
     * 
     * @return surface area
     */
    public double getSurfaceArea() {
        if (ell == null)
            return Double.NaN;

        double p = 1.6075;

        double[] r = getRadii();

        double t1 = Math.pow(r[0], p) * Math.pow(r[1], p);
        double t2 = Math.pow(r[0], p) * Math.pow(r[2], p);
        double t3 = Math.pow(r[1], p) * Math.pow(r[2], p);

        return 4 * Math.PI * Math.pow(((t1 + t2 + t3) / 3d), 1d / p);

    }

    public double getVolume() {
        if (ell == null)
            return Double.NaN;
        double[] r = getRadii();
        return (4d / 3d) * Math.PI * r[0] * r[1] * r[2];

    }

    public double getSphericity() {
        if (ell == null)
            return Double.NaN;

        double SA = getSurfaceArea();
        double V = getVolume();

        double t1 = Math.pow(Math.PI, 1d / 3d);
        double t2 = Math.pow((6 * V), 2d / 3d);

        return (t1 * t2) / SA;

    }

    public Volume getContainedPoints() throws IntegerOverflowException {
        if (ell == null)
            return null;

        double cal = volume.getDppXY() / volume.getDppZ();

        Volume insideEllipsoid = new Volume(volume.getVolumeType(), volume.getSpatialCalibration());

        // Testing which points are within the convex hull
        double[] xRange = ell.getXMinAndMax();
        double[] yRange = ell.getYMinAndMax();
        double[] zRange = ell.getZMinAndMax();

        for (int x = (int) xRange[0]; x <= xRange[1]; x++) {
            for (int y = (int) yRange[0]; y <= yRange[1]; y++) {
                for (int z = (int) zRange[0]; z <= zRange[1]; z++) {
                    if (ell.contains(x, y, z)) {
                        try {
                            insideEllipsoid.add(x, y, (int) Math.round(z * cal));
                        } catch (PointOutOfRangeException e) {
                            // If a point is outside the range, we just ignore it
                        } catch (Exception e) {
                            MIA.log.writeError(e);
                        }
                    }
                }
            }
        }

        return insideEllipsoid;

    }
}
