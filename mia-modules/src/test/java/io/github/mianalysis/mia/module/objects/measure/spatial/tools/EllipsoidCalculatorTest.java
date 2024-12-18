// package io.github.mianalysis.mia.module.objects.measure.spatial.tools;

// import org.junit.jupiter.api.Disabled;
// import org.bonej.geometry.Ellipsoid;
// import org.junit.jupiter.api.Test;
// import io.github.mianalysis.mia.object.coordinates.volume.Volume;
// import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

// import static org.junit.jupiter.api.Assertions.assertArrayEquals;
// import static org.junit.jupiter.api.Assertions.assertEquals;

// public class EllipsoidCalculatorTest {
//     private double tolerance = 1E-2;

//     @Test
//     public void getCentroid() {
//         // Specifying spatial cal
//         double dppXY = 0.02;
//         double dppZ = 0.1;
//         String units = "um";

//         // Defining the example ellipsoid
//         double r1 = 5;
//         double r2 = 2.5;
//         double r3 = 1;
//         double x = 10;
//         double y = 12;
//         double z = 3;

//         double[][] eigenVectors = new double[][]{{0,0,-1},{0,1,0},{-1,0,0}};

//         Ellipsoid ellipsoid = new Ellipsoid(r1,r2,r3,x,y,z,eigenVectors);

//         // Initialising the calculator
//         Volume volume = new Volume(new PointListFactory(),1,1,1,dppXY,dppZ,units);
//         EllipsoidCalculator calculator = new EllipsoidCalculator(ellipsoid,volume);

//         // Testing the measured value
//         double[] actual = calculator.getCentroid();
//         double[] expected = new double[]{10,12,3};

//         assertArrayEquals(expected,actual,tolerance);

//     }

//     @Test
//     public void getRadii() {
//         // Specifying spatial cal
//         double dppXY = 0.02;
//         double dppZ = 0.1;
//         String units = "um";

//         // Defining the example ellipsoid
//         double r1 = 5;
//         double r2 = 2.5;
//         double r3 = 1;
//         double x = 10;
//         double y = 12;
//         double z = 3;

//         double[][] eigenVectors = new double[][]{{0,0,-1},{0,1,0},{-1,0,0}};

//         Ellipsoid ellipsoid = new Ellipsoid(r1,r2,r3,x,y,z,eigenVectors);

//         // Initialising the calculator
//         Volume volume = new Volume(new PointListFactory(),1,1,1,dppXY,dppZ,units);
//         EllipsoidCalculator calculator = new EllipsoidCalculator(ellipsoid,volume);

//         // Testing the measured value
//         double[] actual = calculator.getRadii();
//         double[] expected = new double[]{5,2.5,1};

//         assertArrayEquals(expected,actual,tolerance);

//     }

//     @Test @Disabled
//     public void getRotationMatrix() {
//     }

//     @Test @Disabled
//     public void getOrientationRads() {
//     }

//     @Test
//     public void getSurfaceArea() {
//         // Specifying spatial cal
//         double dppXY = 0.02;
//         double dppZ = 0.1;
//         String units = "um";

//         // Defining the example ellipsoid
//         double r1 = 5;
//         double r2 = 2.5;
//         double r3 = 1;
//         double x = 10;
//         double y = 12;
//         double z = 3;

//         double[][] eigenVectors = new double[][]{{0,0,-1},{0,1,0},{-1,0,0}};

//         Ellipsoid ellipsoid = new Ellipsoid(r1,r2,r3,x,y,z,eigenVectors);

//         // Initialising the calculator
//         Volume volume = new Volume(new PointListFactory(),1,1,1,dppXY,dppZ,units);
//         EllipsoidCalculator calculator = new EllipsoidCalculator(ellipsoid,volume);

//         // Testing the measured value
//         double actual = calculator.getSurfaceArea();
//         double expected = 93.57;

//         assertEquals(expected,actual,tolerance);

//     }

//     @Test
//     public void getVolume() {
//         // Specifying spatial cal
//         double dppXY = 0.02;
//         double dppZ = 0.1;
//         String units = "um";

//         // Defining the example ellipsoid
//         double r1 = 5;
//         double r2 = 2.5;
//         double r3 = 1;
//         double x = 10;
//         double y = 12;
//         double z = 3;

//         double[][] eigenVectors = new double[][]{{0,0,-1},{0,1,0},{-1,0,0}};

//         Ellipsoid ellipsoid = new Ellipsoid(r1,r2,r3,x,y,z,eigenVectors);

//         // Initialising the calculator
//         Volume volume = new Volume(new PointListFactory(),1,1,1,dppXY,dppZ,units);
//         EllipsoidCalculator calculator = new EllipsoidCalculator(ellipsoid,volume);

//         // Testing the measured value
//         double actual = calculator.getVolume();
//         double expected = 52.36;

//         assertEquals(expected,actual,tolerance);

//     }

//     @Test
//     public void getSphericity() {
//         // Specifying spatial cal
//         double dppXY = 0.02;
//         double dppZ = 0.1;
//         String units = "um";

//         // Defining the example ellipsoid
//         double r1 = 5;
//         double r2 = 2.5;
//         double r3 = 1;
//         double x = 10;
//         double y = 12;
//         double z = 3;

//         double[][] eigenVectors = new double[][]{{0,0,-1},{0,1,0},{-1,0,0}};

//         Ellipsoid ellipsoid = new Ellipsoid(r1,r2,r3,x,y,z,eigenVectors);

//         // Initialising the calculator
//         Volume volume = new Volume(new PointListFactory(),1,1,1,dppXY,dppZ,units);
//         EllipsoidCalculator calculator = new EllipsoidCalculator(ellipsoid,volume);

//         // Testing the measured value
//         double actual = calculator.getSphericity();
//         double expected = 0.7234;

//         assertEquals(expected,actual,tolerance);

//     }

//     @Test @Disabled
//     public void getContainedPoints() {
//     }
// }