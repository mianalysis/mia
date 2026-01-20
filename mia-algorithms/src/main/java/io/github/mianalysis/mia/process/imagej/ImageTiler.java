package io.github.mianalysis.mia.process.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.CanvasResizer;
import ij.process.ImageProcessor;

public class ImageTiler {
    public interface TileAxes {
        String CHANNEL = "C";
        String T = "T";
        String Z = "Z";

        String[] ALL = new String[] { CHANNEL, T, Z };

    }

    public static void copyImageCalibration(ImagePlus inputIpl, ImagePlus outputIpl) {
        // Applying new calibration
        Calibration inputCal = inputIpl.getCalibration();
        Calibration outputCal = outputIpl.getCalibration();

        outputCal.pixelWidth = inputCal.pixelWidth;
        outputCal.pixelHeight = inputCal.pixelHeight;
        outputCal.pixelDepth = inputCal.pixelDepth;
        outputCal.setUnit(inputCal.getUnit());
        outputCal.frameInterval = inputCal.frameInterval;
        outputCal.fps = inputCal.fps;

    }

    public static int getNumberOfTiles() {
        return -1;
    }

    public static int getTileSize(int imageSize, int overlap, int nTiles) {
        return (int) Math.ceil(Math.ceil((double) (imageSize - overlap) / (double) nTiles)) + overlap;
    }

    public static int getTileCount(int imageSize, int overlap, int tileSize) {
        return (int) Math.ceil(Math.ceil((double) (imageSize - overlap) / (double) tileSize));
    }

    public static ImagePlus tile(ImagePlus inputIpl, int xNumTiles, int yNumTiles, int xTileSize, int yTileSize,
            int xOverlapPx, int yOverlapPx, String tileAxis) {
        inputIpl = inputIpl.duplicate();
        String title = "Tiled_" + inputIpl.getTitle();

        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();
        int bitDepth = inputIpl.getBitDepth();

        int c0 = 0;
        int z0 = 0;
        int t0 = 0;
        switch (tileAxis) {
            case TileAxes.CHANNEL:
                c0 = inputIpl.getNChannels();
                nChannels = nChannels * xNumTiles * yNumTiles;
                break;
            case TileAxes.Z:
                z0 = inputIpl.getNSlices();
                nSlices = nSlices * xNumTiles * yNumTiles;
                break;
            case TileAxes.T:
                t0 = inputIpl.getNFrames();
                nFrames = nFrames * xNumTiles * yNumTiles;
                break;
        }

        ImagePlus outputIpl = IJ.createHyperStack(title, xTileSize, yTileSize, nChannels, nSlices, nFrames,
                bitDepth);

        ImageStack outputIst = outputIpl.getStack();
        int count = 0;
        for (int y = 0; y < yNumTiles; y++) {
            for (int x = 0; x < xNumTiles; x++) {
                int x0 = x * (xTileSize - xOverlapPx);
                int y0 = y * (yTileSize - yOverlapPx);
                inputIpl.setRoi(new Roi(x0, y0, xTileSize, yTileSize));

                ImagePlus tileIpl = inputIpl.crop("stack");
                if (tileIpl.getWidth() < xTileSize || tileIpl.getHeight() < yTileSize)
                    tileIpl.setStack(
                            new CanvasResizer().expandStack(tileIpl.getStack(), xTileSize, yTileSize, 0, 0));

                ImageStack tileIst = tileIpl.getStack();
                for (int c = 0; c < inputIpl.getNChannels(); c++) {
                    for (int z = 0; z < inputIpl.getNSlices(); z++) {
                        for (int t = 0; t < inputIpl.getNFrames(); t++) {
                            int tileIdx = tileIpl.getStackIndex(c + 1, z + 1, t + 1);
                            int outputIdx = outputIpl.getStackIndex(c + c0 * count + 1, z + z0 * count + 1,
                                    t + t0 * count + 1);

                            ImageProcessor tileIpr = tileIst.getProcessor(tileIdx);
                            outputIst.setProcessor(tileIpr, outputIdx);

                        }
                    }
                }

                count++;

            }
        }

        copyImageCalibration(inputIpl, outputIpl);

        return outputIpl;

    }

    public static ImagePlus stitch(ImagePlus inputIpl, int xNumTiles, int yNumTiles, int xTileSize, int yTileSize,
            int xOverlapPx, int yOverlapPx, int outputWidth, int outputHeight, String tileAxis) {
        inputIpl = inputIpl.duplicate();
        String title = "Stitched_" + inputIpl.getTitle();

        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();
        int bitDepth = inputIpl.getBitDepth();

        int c0 = 0;
        int z0 = 0;
        int t0 = 0;
        switch (tileAxis) {
            case TileAxes.CHANNEL:
                nChannels = nChannels / (xNumTiles * yNumTiles);
                c0 = nChannels;
                break;
            case TileAxes.Z:
                nSlices = nSlices / (xNumTiles * yNumTiles);
                z0 = nSlices;
                break;
            case TileAxes.T:
                nFrames = nFrames / (xNumTiles * yNumTiles);
                t0 = nFrames;
                break;
        }

        ImagePlus outputIpl = IJ.createHyperStack(title, outputWidth, outputHeight, nChannels, nSlices, nFrames,
                32);

        // Converting to 32 bit for this operation to get better results
        IJ.run(inputIpl, "32-bit", null);

        ImageStack inputIst = inputIpl.getStack();
        ImageStack outputIst = outputIpl.getStack();

        int count = 0;
        for (int y = 0; y < yNumTiles; y++) {
            for (int x = 0; x < xNumTiles; x++) {
                int x0 = x * (xTileSize-xOverlapPx);
                int y0 = y * (yTileSize-yOverlapPx);

                for (int c = 0; c < nChannels; c++) {
                    for (int z = 0; z < nSlices; z++) {
                        for (int t = 0; t < nFrames; t++) {
                            int inputIdx = inputIpl.getStackIndex(c + c0 * count + 1, z + z0 * count + 1,
                                    t + t0 * count + 1);

                            int outputIdx = outputIpl.getStackIndex(c + 1, z + 1, t + 1);

                            ImageProcessor inputIpr = inputIst.getProcessor(inputIdx);
                            ImageProcessor outputIpr = outputIst.getProcessor(outputIdx);

                            for (int xx = 0; xx < xTileSize; xx++) {
                                if (xx + x0 >= outputWidth)
                                    break;

                                for (int yy = 0; yy < yTileSize; yy++) {
                                    if (yy + y0 >= outputHeight)
                                        break;

                                    float multiplier = 1;

                                    if (xx < xOverlapPx && x != 0)
                                        multiplier *= (1 - (xOverlapPx - xx) / (float) xOverlapPx);

                                    if (xx >= (xTileSize-xOverlapPx) && x != xNumTiles - 1)
                                        multiplier *= (xTileSize - xx) / (float) xOverlapPx;

                                    if (yy < yOverlapPx && y != 0)
                                        multiplier *= (1 - (yOverlapPx - yy) / (float) yOverlapPx);

                                    if (yy >= (yTileSize-yOverlapPx) && y != yNumTiles - 1)
                                        multiplier *= (yTileSize - yy) / (float) yOverlapPx;

                                    outputIpr.setf(xx + x0, yy + y0,
                                            outputIpr.getf(xx + x0, yy + y0) + inputIpr.getf(xx, yy) * multiplier);

                                }
                            }
                        }
                    }
                }
                count++;
            }
        }

        switch (bitDepth) {
            case 8:
                outputIpl.setDisplayRange(0, 255);
                IJ.run(outputIpl, "8-bit", null);
                break;
            case 16:
                outputIpl.setDisplayRange(0, 65535);
                IJ.run(outputIpl, "16-bit", null);
                break;
        }

        copyImageCalibration(inputIpl, outputIpl);

        return outputIpl;

    }
}
