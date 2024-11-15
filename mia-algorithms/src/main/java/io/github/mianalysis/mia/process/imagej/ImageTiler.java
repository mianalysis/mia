package io.github.mianalysis.mia.process.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.CanvasResizer;
import ij.process.ImageProcessor;

public class ImageTiler {
    public interface TileAxes {
        String CHANNEL = "C";
        String T = "T";
        String Z = "Z";

        String[] ALL = new String[] { CHANNEL, T, Z };

    }

    public static ImagePlus tile(ImagePlus inputIpl, int xNumTiles, int yNumTiles, int xOverlapPx, int yOverlapPx,
            String tileAxis) {
        String title = "Tiled_" + inputIpl.getTitle();

        int coreTileWidth = (int) Math.round(Math.ceil((double) (inputIpl.getWidth()-xOverlapPx) / (double) xNumTiles));
        int tileWidth = coreTileWidth + xOverlapPx;
        int coreTileHeight = (int) Math.round(Math.ceil((double) (inputIpl.getHeight()-yOverlapPx) / (double) yNumTiles));
        int tileHeight = coreTileHeight + yOverlapPx;

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

        ImagePlus outputIpl = IJ.createHyperStack(title, tileWidth, tileHeight, nChannels, nSlices, nFrames, bitDepth);

        ImageStack outputIst = outputIpl.getStack();
        int count = 0;
        for (int y = 0; y < yNumTiles; y++) {
            for (int x = 0; x < xNumTiles; x++) {
                int x0 = x * coreTileWidth;
                int y0 = y * coreTileHeight;
                inputIpl.setRoi(new Roi(x0, y0, tileWidth, tileHeight));

                ImagePlus tileIpl = inputIpl.crop("stack");
                if (tileIpl.getWidth() < tileWidth || tileIpl.getHeight() < tileHeight)
                    tileIpl.setStack(new CanvasResizer().expandStack(tileIpl.getStack(), tileWidth, tileHeight, 0, 0));

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

        return outputIpl;

    }

    public static ImagePlus stitch(ImagePlus inputIpl, int xNumTiles, int yNumTiles, int xOverlapPx, int yOverlapPx,
            int outputWidth, int outputHeight,
        String tileAxis) {
        String title = "Stitched_" + inputIpl.getTitle();

        int tileWidth = inputIpl.getWidth();
        int coreTileWidth = tileWidth - xOverlapPx;
        int tileHeight = inputIpl.getHeight();
        int coreTileHeight = tileHeight - yOverlapPx;

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
        IJ.run(inputIpl,"32-bit",null);

        ImageStack inputIst = inputIpl.getStack();
        ImageStack outputIst = outputIpl.getStack();

        int count = 0;
        for (int y = 0; y < yNumTiles; y++) {
            for (int x = 0; x < xNumTiles; x++) {
                int x0 = x * coreTileWidth;
                int y0 = y * coreTileHeight;

                for (int c = 0; c < nChannels; c++) {
                    for (int z = 0; z < nSlices; z++) {
                        for (int t = 0; t < nFrames; t++) {
                            int inputIdx = inputIpl.getStackIndex(c + c0 * count + 1, z + z0 * count + 1,
                                    t + t0 * count + 1);

                            int outputIdx = outputIpl.getStackIndex(c + 1, z + 1, t + 1);

                            ImageProcessor inputIpr = inputIst.getProcessor(inputIdx);
                            ImageProcessor outputIpr = outputIst.getProcessor(outputIdx);

                            for (int xx = 0; xx < tileWidth; xx++) {
                                if (xx + x0 >= outputWidth)
                                    break;

                                for (int yy = 0; yy < tileHeight; yy++) {
                                    if (yy + y0 >= outputHeight)
                                        break;

                                    float multiplier = 1;

                                    if (xx < xOverlapPx && x != 0)
                                        multiplier *= (1 - (xOverlapPx - xx) / (float) xOverlapPx);                                        
                                    
                                    if (xx >= coreTileWidth && x != xNumTiles - 1)
                                        multiplier *= (tileWidth - xx) / (float) xOverlapPx;                                 

                                    if (yy < yOverlapPx && y != 0)
                                        multiplier *= (1 - (yOverlapPx - yy) / (float) yOverlapPx);
                                    

                                    if (yy >= coreTileHeight && y != yNumTiles - 1)
                                        multiplier *= (tileHeight - yy) / (float) yOverlapPx;
                                   
                                    outputIpr.setf(xx + x0, yy + y0, outputIpr.getf(xx+x0,yy+y0) + inputIpr.getf(xx, yy) * multiplier);
                            
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
                inputIpl.setDisplayRange(0,255);
                break;
            case 16:
                inputIpl.setDisplayRange(0,65535);
                break;
        }
        IJ.run(inputIpl,"8-bit",null);

        return outputIpl;

    }
}
