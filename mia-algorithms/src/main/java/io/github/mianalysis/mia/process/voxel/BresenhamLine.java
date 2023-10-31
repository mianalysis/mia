package io.github.mianalysis.mia.process.voxel;

/**
 * Implements Bresenham's line algorithm to draw a straight line between two points.
 */
public class BresenhamLine {
    public static int[][] getLine(int x0, int x1, int y0, int y1) {
        int w = Math.abs(x1-x0);
        int h = Math.abs(y1-y0);

        int sx = x1<x0 ? -1 : 1;
        int sy = y1<y0 ? -1 : 1;

        int[][] xy;

        if (w > h) {
            double derr = Math.abs((double) h/(double) w);
            double error = derr - 0.5;

            xy = new int[w][2];
            int currY = y0;

            for (int i = 0; i < w; i++) {
                xy[i][0] = x0 + i*sx;
                xy[i][1] = currY;
                error = error + derr;

                if (error >= 0.5) {
                    currY = currY + sy;
                    error = error - 1;

                }
            }

        } else {
            double derr = Math.abs((double) w/(double) h);
            double error = derr - 0.5;

            xy = new int[h][2];
            int currX = x0;

            for (int i = 0; i < h; i++) {
                xy[i][0] = currX;
                xy[i][1] = y0 + i*sy;
                error = error + derr;

                if (error >= 0.5) {
                    currX = currX + sx;
                    error = error - 1;

                }
            }

        }

        return xy;

    }
}
