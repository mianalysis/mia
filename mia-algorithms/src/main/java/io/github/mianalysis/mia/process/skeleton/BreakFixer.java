package io.github.mianalysis.mia.process.skeleton;

import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Convolver;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.process.voxel.BresenhamLine;

public class BreakFixer {
    private double angleWeight = 1;
    private double distanceWeight = 1;
    private double endWeight = 20; // We want to heavily favour linking to another end point


    static ArrayList<int[]> getEndPoints(ImageProcessor iprConn, int x, int y, int nPx) {
        // Getting the current point and nPx adjacent points.  A line will be fit to these.
        ArrayList<int[]> c = new ArrayList<>();
        int[] lc = new int[]{x,y};
        iprConn.set(lc[0],lc[1],0);
        c.add(lc);

        int nAdded = 1;
        while (nAdded < nPx) {
            nAdded++;

            // Check that all points around the current one are accessible
            if (lc[0] <= 0 || lc[0] >= iprConn.getWidth()-1 || lc[1] <= 0 || lc[1] >= iprConn.getHeight()-1) break;

            // Terminate process if a junction is reached
            if (iprConn.get(lc[0]-1,lc[1]+1) > 3 | iprConn.get(lc[0]-1,lc[1]) > 3
                    | iprConn.get(lc[0]-1,lc[1]-1) > 3 | iprConn.get(lc[0],lc[1]+1) > 3
                    | iprConn.get(lc[0],lc[1]-1) > 3 | iprConn.get(lc[0]+1,lc[1]+1) > 3
                    | iprConn.get(lc[0]+1,lc[1]) > 3 | iprConn.get(lc[0]+1,lc[1]-1) > 3) break;

            if (iprConn.get(lc[0]-1,lc[1]+1) == 3) {
                lc = new int[]{lc[0]-1,lc[1]+1};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
                continue;
            }

            if (iprConn.get(lc[0]-1,lc[1]) == 3) {
                lc = new int[]{lc[0]-1,lc[1]};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
                continue;
            }

            if (iprConn.get(lc[0]-1,lc[1]-1) == 3) {
                lc = new int[]{lc[0]-1,lc[1]-1};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
                continue;
            }

            if (iprConn.get(lc[0],lc[1]+1) == 3) {
                lc = new int[]{lc[0],lc[1]+1};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
                continue;
            }

            if (iprConn.get(lc[0],lc[1]-1) == 3) {
                lc = new int[]{lc[0],lc[1]-1};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
                continue;
            }

            if (iprConn.get(lc[0]+1,lc[1]+1) == 3) {
                lc = new int[]{lc[0]+1,lc[1]+1};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
                continue;
            }

            if (iprConn.get(lc[0]+1,lc[1]) == 3) {
                lc = new int[]{lc[0]+1,lc[1]};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
                continue;
            }

            if (iprConn.get(lc[0]+1,lc[1]-1) == 3) {
                lc = new int[]{lc[0]+1,lc[1]-1};
                c.add(lc);
                iprConn.set(lc[0],lc[1],0);
            }
        }

        return c;

    }

    public static double getEndAngleRads(ArrayList<int[]> c) {
        double[][] dataX = new double[c.size()][2];
        double[][] dataY = new double[c.size()][2];

        double cumulative = 0;

        dataX[0][0] = cumulative;
        dataX[0][1] = c.get(0)[0];
        dataY[0][0] = cumulative;
        dataY[0][1] = c.get(0)[1];

        for (int i=1;i<c.size();i++) {
            int x1 = c.get(i-1)[0];
            int y1 = c.get(i-1)[1];
            int x2 = c.get(i)[0];
            int y2 = c.get(i)[1];

            cumulative = cumulative + getDist(x1,x2,y1,y2);

            dataX[i][0] = cumulative;
            dataX[i][1] = x2;
            dataY[i][0] = cumulative;
            dataY[i][1] = y2;

        }

        // Fitting straight line to each.  We take the negative slope, because we want the direction the end points.
        SimpleRegression srX = new SimpleRegression();
        srX.addData(dataX);
        double slopeX = -srX.getSlope();

        SimpleRegression srY = new SimpleRegression();
        srY.addData(dataY);
        double slopeY = -srY.getSlope();

        return Math.atan2(slopeY,slopeX);

    }

    static double getDist(int x1, int x2, int y1, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    static ArrayList<Link> getPotentialLinks(ImageProcessor iprConn, ArrayList<int[]> c, int x, int y, int distLim, double angleLim, boolean endToEndOnly) {
        // Calculating the angle of the line using linear regression
        double endAngle = Math.toDegrees(getEndAngleRads(c));
        Point<Integer> p1 = new Point<>(x,y,0);
        ArrayList<Link> links = new ArrayList<>();

        for (int xx = x- distLim; xx<x+ distLim; xx++) {
            for (int yy = y- distLim; yy<y+ distLim; yy++) {
                if (xx < 0 || xx >= iprConn.getWidth() || yy < 0 || yy >= iprConn.getHeight()) continue;

                if (endToEndOnly && iprConn.get(xx, yy) != 2) {
                    continue;
                } else if (iprConn.get(xx,yy) == 0) {
                    continue;
                }

                // Checking that the position being tested isn't closer to the next pixel in the tube
                // (i.e. a connection would be made in the opposite direction to the tube)
                double dist = getDist(x,xx,y,yy);

                if (dist > getDist(c.get(1)[0],xx,c.get(1)[1],yy)) continue;

                // Checking the position being tested is within an acceptable distance
                if (dist > distLim) continue;

                Point<Integer> p2 = new Point<>(xx,yy,0);
                double connectionAngle = Math.toDegrees(p1.calculateAngle2D(p2));

                double phi = Math.abs(connectionAngle - endAngle) % 360;
                double dAngle = phi > 180 ? 360 - phi : phi;

                // Checking the position being tested is within an acceptable angle
                if (dAngle > angleLim) continue;

                links.add(new Link(xx,yy,dAngle,dist,(iprConn.get(xx, yy) == 2)));

            }
        }

        return links;

    }

    public void process(ImageProcessor iprOrig, int nPx, int distLim, double angleLim, boolean endToEndOnly) {
        process(iprOrig,nPx,distLim,angleLim,endToEndOnly,angleWeight,distanceWeight,endWeight);

    }

    public static void process(ImageProcessor iprOrig, int nPx, int distLim, double angleLim, boolean endToEndOnly, double angleWeight, double distanceWeight, double endWeight) {
        // Inverting image, so the the logic is skeleton (255) and background (0)
        iprOrig.invert();

        // Dividing by 255, so the logic is skeleton (1) and background (0)
        iprOrig.multiply(1d/255d);

        // Duplicating the image
        ImageProcessor iprConn = iprOrig.duplicate();

        // Convolving with connectivity kernel
        Convolver convolver = new Convolver();
        convolver.setNormalize(false);
        convolver.convolve(iprConn,new float[]{1,1,1,1,1,1,1,1,1},3,3);

        // Multiplying the convolved image with the original image
        new ImageCalculator().run("Multiply",new ImagePlus("Conn",iprConn),new ImagePlus("Orig",iprOrig));

        // For each pixel with connectivity = 2, any other connectivity = 2 are searched for in the local vicinity.  The
        // points to be tested are only within an accepted angle from the current end (estimated by the adjacent 5
        // pixels).
        for (int x=0;x<iprConn.getWidth();x++) {
            for (int y = 0; y < iprConn.getHeight(); y++) {
                if (iprConn.get(x,y) != 2) continue;

                // Getting end points for fitting
                ArrayList<int[]> c = getEndPoints(iprConn,x,y,nPx);

                // We need more than 1 pixel to determine the orientation
                if (c.size() < 2) continue;

                // Getting a list of all potential links
                ArrayList<Link> links = getPotentialLinks(iprConn,c,x,y,distLim,angleLim,endToEndOnly);

                // Quit if no links found
                if (links.size() == 0) continue;

                // Getting the link with the smallest angle
                double minScore = Double.MAX_VALUE;
                int xx = 0;
                int yy = 0;
                for (Link link:links) {
                    if (link.getAngle() < minScore) {
                        minScore = link.getScore(angleWeight,distanceWeight,endWeight);
                        xx = link.getX();
                        yy = link.getY();
                    }
                }

                // Doing the linking
                int[][] line = BresenhamLine.getLine(x, xx, y, yy);
                for (int[] px : line) iprOrig.set(px[0], px[1], 1);

                iprOrig.set(x,y,10);
                iprOrig.set(xx,yy,10);

                // Updating connectivity on the joined ends
                iprConn.set(x,y,3);
                iprConn.set(xx,yy,3);

            }
        }

        // Returning to standard levels for ImageJ
        iprOrig.multiply(255);
        iprOrig.invert();

    }

    public double getAngleWeight() {
        return angleWeight;
    }

    public void setAngleWeight(double angleWeight) {
        this.angleWeight = angleWeight;
    }

    public double getDistanceWeight() {
        return distanceWeight;
    }

    public void setDistanceWeight(double distanceWeight) {
        this.distanceWeight = distanceWeight;
    }

    public double getEndWeight() {
        return endWeight;
    }

    public void setEndWeight(double endWeight) {
        this.endWeight = endWeight;
    }
}

class Link {
    private int x;
    private int y;
    private double angle;
    private double distance;
    private boolean isEnd;

    public Link(int x, int y, double angle, double distance, boolean isEnd) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.distance = distance;
        this.isEnd = isEnd;

    }

    public double getScore(double angleWeight, double distanceWeight, double endWeight) {
        double endVal = isEnd ? -endWeight : 0;
        return Math.abs(angle) * angleWeight + distance * distanceWeight + endVal;
        
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
