package io.github.mianalysis.mia.process.imagej;

import ij.ImagePlus;
import ij.process.ImageProcessor;

public class CombingCorrector {
    public static void run(ImagePlus ipl, int offset, boolean stack) {
        if (stack) {
            // Performing the next couple of steps on all channels, slices and frames
            for (int channel = 0; channel < ipl.getNChannels(); channel++) {
                for (int slice = 0; slice < ipl.getNSlices(); slice++) {
                    for (int frame = 0; frame < ipl.getNFrames(); frame++) {
                        // Setting the current slice number
                        ipl.setPosition(channel+1,slice+1,frame+1);

                        // Applying the correction
                        ImageProcessor iprCorrected = applyCorrection(ipl.getProcessor(),offset);
                        ipl.setProcessor(iprCorrected);

                    }
                }
            }

        } else {
            ImageProcessor iprCorrected = applyCorrection(ipl.getProcessor(),offset);
            ipl.setProcessor(iprCorrected);
        }
    }

    private static ImageProcessor applyCorrection(ImageProcessor ipr, int offset) {
        ImageProcessor iprCorrected = ipr.duplicate();

        for (int y=0;y<ipr.getHeight();y += 2) {
            for (int x=0;x<ipr.getWidth();x++) {
                iprCorrected.setf(x,y,0);
            }

            for (int x=0;x<ipr.getWidth();x++) {
                int xCorr = x+offset;

                if (xCorr >= 0 && xCorr < ipr.getWidth()) {
                    iprCorrected.setf(xCorr,y,ipr.getf(x,y));
                }
            }
        }

        return iprCorrected;

    }
}
