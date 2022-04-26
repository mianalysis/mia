// TODO: Rewrite this to use FitEllipsoid

// package io.github.mianalysis.mia.module.objects.measure.spatial.tools;

// import org.bonej.geometry.FitEllipse;
// import io.github.sjcross.common.exceptions.IntegerOverflowException;
// import io.github.sjcross.common.object.Point;
// import io.github.sjcross.common.object.volume.PointOutOfRangeException;
// import io.github.sjcross.common.object.volume.Volume;

// public class EllipseCalculator {
//     private final Volume volume;
//     private double[] e2d = null;
    
//     public EllipseCalculator(Volume volume) throws RuntimeException {
//         this.volume = volume;
        
//         fitEllipse(Double.MAX_VALUE);
        
//     }
    
//     public EllipseCalculator(Volume volume, double maxAxisLength) throws RuntimeException {
//         this.volume = volume;
        
//         fitEllipse(maxAxisLength);
//     }
    
//     private void fitEllipse(double maxAxisLength) throws RuntimeException {
//         if (volume.size() <= 2) {
//             e2d = null;
//             return;
//         }
        
//         //Uses FitEllipse class from BoneJ
//         double[][] coords = new double[volume.getCoordinateSet().size()][2];
//         int i = 0;
//         for (Point<Integer> point:volume.getCoordinateSet()) {
//             coords[i][0] = point.x;
//             coords[i++][1] = point.y;            
//         }
                
//         try {
//             e2d = FitEllipse.direct(coords);
//         } catch (RuntimeException e) {
//             e2d = null;
//             throw e;
//         }
        
//         // The following test prevents objects being created for ill-fit ellipses
//         double semiMinor = getSemiMinorAxis();
//         double semiMajor = getSemiMajorAxis();
//         if (semiMajor > maxAxisLength) e2d = null;
//         if (semiMinor <= 0 || semiMajor <= 0) e2d = null;
//         if (Double.isNaN(semiMinor) || Double.isNaN(semiMajor)) e2d = null;
        
//     }
    
//     public double[] getEllipseFit() {
//         return e2d;
        
//     }
    
//     public double getEllipseThetaRads() {
//         if (e2d == null) return Double.NaN;
        
//         double a = e2d[0];
//         double b = e2d[1];
//         double c = e2d[2];
        
//         if (e2d == null) return Double.NaN;
        
//         if (b == 0 & a < c) {
//             return 0;
            
//         } else if (b == 0 & a > c) {
//             return Math.PI/2;
            
//         } else if (b != 0 & a > 0) {
//             return Math.atan((c-a - Math.sqrt((a-c)*(a-c) + b*b)) / b);
            
//         } else {
//             double val = -Math.atan((c-a - Math.sqrt((a-c)*(a-c) + b*b)) / b) + Math.PI / 2;
//             return -(((val + Math.PI / 2) % Math.PI) - Math.PI / 2);
            
//         }
//     }
    
//     public double getXCentre() {
//         if (e2d == null) return Double.NaN;
        
//         double a = e2d[0];
//         double b = e2d[1];
//         double c = e2d[2];
//         double d = e2d[3];
//         double e = e2d[4];
        
//         return (2*c*d-b*e)/(b*b-4*a*c);
        
//     }
    
//     public double getYCentre() {
//         if (e2d == null) return Double.NaN;
        
//         double a = e2d[0];
//         double b = e2d[1];
//         double c = e2d[2];
//         double d = e2d[3];
//         double e = e2d[4];
        
//         return (2*a*e-b*d)/(b*b-4*a*c);
        
//     }
    
//     /**
//     * Returns the semi-major and semi-minor axes.  Note: There's no guarantee which order they will be in.
//     * @return
//     */
//     public double[] getSemiAxes() {
//         if (e2d == null) return null;
        
//         double a = e2d[0];
//         double b = e2d[1];
//         double c = e2d[2];
//         double d = e2d[3];
//         double e = e2d[4];
//         double f = e2d[5];
        
//         double part1 = 2*(a*e*e+c*d*d-b*d*e+(b*b-4*a*c)*f);
//         double part2a = a+c+Math.sqrt((a-c)*(a-c)+b*b);
//         double part2b = a+c-Math.sqrt((a-c)*(a-c)+b*b);
//         double part3 = b*b-4*a*c;
        
//         return new double[]{-Math.sqrt(part1*part2a)/part3, -Math.sqrt(part1*part2b)/part3};
        
//     }
    
//     public double getSemiMajorAxis() {
//         if (e2d == null) return Double.NaN;
        
//         double[] semiAxes = getSemiAxes();
        
//         return Math.max(semiAxes[0],semiAxes[1]);
        
//     }
    
//     public double getSemiMinorAxis() {
//         if (e2d == null) return Double.NaN;
        
//         double[] semiAxes = getSemiAxes();
        
//         return Math.min(semiAxes[0],semiAxes[1]);
        
//     }
    
//     public Volume getContainedPoints() throws IntegerOverflowException {
//         if (e2d == null) return null;
        
//         Volume insideEllipse = new Volume(volume.getVolumeType(),volume.getSpatialCalibration());
        
//         double xCent = getXCentre();
//         double yCent = getYCentre();
//         double semiMajor = getSemiMajorAxis();
        
//         // If the semiaxes come off in reverse order (i.e. [0] semi-minor, [1] semi-major) reverse the sign
//         double[] semiAxes = getSemiAxes();
//         double mult = (semiAxes[0] < semiAxes[1]) ? -1 : 1;
        
//         for (int x=(int) Math.floor(xCent-semiMajor);x<=xCent+semiMajor;x++) {
//             for (int y=(int) Math.floor(yCent-semiMajor);y<=yCent+semiMajor;y++) {
//                 if ((e2d[0]*x*x + e2d[1]*x*y + e2d[2]*y*y +e2d[3]*x + e2d[4]*y + e2d[5])*mult <= 0) {
//                     try {
//                         insideEllipse.add(x,y,0);
//                     } catch (PointOutOfRangeException e) { }
//                 }
//             }
//         }
        
//         return insideEllipse;
        
//     }
// }
