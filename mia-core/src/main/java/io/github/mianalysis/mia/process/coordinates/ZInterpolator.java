package io.github.mianalysis.mia.process.coordinates;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.SubHyperstackMaker;
import ij.process.BinaryInterpolator;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImagePlusImage;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

public class ZInterpolator {
    public static void applySpatialInterpolation(Objs inputObjects, CoordinateSetFactoryI factory) throws IntegerOverflowException {
        for (Obj inputObj : inputObjects.values()) {
            ImageI binaryImage = inputObj.getAsTightImage("BinaryTight");

            // We need at least 3 slices to make interpolation worthwhile
            if (binaryImage.getImagePlus().getNSlices() < 3)
                continue;

            applySpatialInterpolation(binaryImage);

            // Converting binary image back to objects
            Obj interpObj = binaryImage.convertImageToObjects(factory, inputObj.getName(), true).getFirst();
            interpObj.setSpatialCalibration(inputObj.getSpatialCalibration());
            double[][] extents = inputObj.getExtents(true, false);

            interpObj.translateCoords((int) Math.round(extents[0][0]), (int) Math.round(extents[1][0]),
                    (int) Math.round(extents[2][0]));
            inputObj.setCoordinateSet(interpObj.getCoordinateSet());

        }
    }

    static void applySpatialInterpolation(ImageI binaryImage) {
        ImagePlus binaryIpl = binaryImage.getImagePlus();
        int nSlices = binaryIpl.getNSlices();
        int nFrames = binaryIpl.getNFrames();

        BinaryInterpolator binaryInterpolator = new BinaryInterpolator();

        // We only want to interpolate in z, so need to processAutomatic each timepoint
        // separately
        for (int t = 1; t <= nFrames; t++) {
            // Extracting the slice and interpolating
            ImagePlus sliceIpl = SubHyperstackMaker.makeSubhyperstack(binaryIpl, "1-1", "1-" + nSlices, t + "-" + t);
            if (!checkStackForInterpolation(sliceIpl.getStack()))
                continue;
            binaryInterpolator.run(sliceIpl.getStack());
            ImagePlusImage.getSetStack(binaryIpl, t, 1, sliceIpl.getStack());
        }
    }

    static boolean checkStackForInterpolation(ImageStack stack) {
        int count = 0;
        for (int i = 1; i <= stack.getSize(); i++) {
            if (stack.getProcessor(i).getStatistics().max > 0)
                count++;
        }

        return count >= 2;

    }
}
