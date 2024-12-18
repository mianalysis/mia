package io.github.mianalysis.mia.process.analysis;

import java.util.LinkedHashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;
import io.github.mianalysis.mia.process.math.CumStat;
import io.github.mianalysis.mia.process.math.Indexer;


/**
 * Texture measures, largely from Robert M. Haralick, K. Shanmugam, and Its'hak
 * Dinstein, "Textural Features for Image Classification", IEEE Transactions on
 * Systems, Man, and Cybernetics, 1973, SMC-3 (6): 610–621
 */
public class TextureCalculator {
    private LinkedHashMap<Integer, Double> matrix = new LinkedHashMap<>();;
    private Indexer indexer = new Indexer(256, 256);
    private boolean normalised = false;


    // PUBLIC METHODS

    public void resetConfusionMatrix() {
        matrix = new LinkedHashMap<>();
        normalised = false;
    }


    public void calculate(ImageStack image, CoordinateSetI coordinateSet, int xOffs, int yOffs, int zOffs) {
        if (image.getBitDepth() != 8)
            image = convertTo8Bit(image);

        // Initialising new HashMap (acting as a sparse matrix) to store the
        // co-occurrence matrix
        resetConfusionMatrix();

        // Running through all specified positions,
        int count = 0;
        for (Point<Integer> point : coordinateSet) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            if (coordinateSet.contains(new Point<>(x + xOffs, y + yOffs, z + zOffs))) {
                addValueToConfusionMatrix(image, x, y, z, x + xOffs, y + yOffs, z + zOffs);
                count = count + 2;
            }
        }

        // Applying normalisation
        normalise();

    }

    public void calculate(ImageStack image, int xOffs, int yOffs, int zOffs) {
        if (image.getBitDepth() != 8)
            image = convertTo8Bit(image);

        // Getting image size
        int height = image.getHeight();
        int width = image.getWidth();
        int nSlices = image.size();

        // Initialising new HashMap (acting as a sparse matrix) to store the
        // co-occurrence matrix
        resetConfusionMatrix();

        // Running through all specified positions,
        int count = 0;
        for (int z = 0; z < image.size(); z++) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (x + xOffs >= 0 & x + xOffs < width & y + yOffs >= 0 & y + yOffs < height & z + zOffs >= 0
                            & z + zOffs < nSlices) {
                        addValueToConfusionMatrix(image, x, y, z, x + xOffs, y + yOffs, z + zOffs);
                        count = count + 2;
                    }
                }
            }
        }

        // Applying normalisation
        normalise();

    }

    public void addValueToConfusionMatrix(ImageStack image, int x1, int y1, int z1, int x2, int y2, int z2) {
        int v1 = 0;
        int v2 = 0;

        // Getting current pixel value
        try {
            v1 = (int) image.getVoxel(x1, y1, z1);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Out of bounds at " + x1 + "," + y1 + "," + z1);
        }

        // Getting tested pixel value
        try {
            v2 = (int) image.getVoxel(x2, y2, z2);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Out of bounds at " + x2 + "," + y2 + "," + z2);
        }

        // Storing in the HashMap
        int index1 = indexer.getIndex(new int[] { v1, v2 });
        int index2 = indexer.getIndex(new int[] { v2, v1 });
        if (!matrix.containsKey(index1)) {
            matrix.put(index1, 0d);
            matrix.put(indexer.getIndex(new int[] { v2, v1 }), 0d);
        }
        matrix.put(index1, matrix.get(index1) + 1);
        matrix.put(index2, matrix.get(index2) + 1);

    }

    /**
     * Calculates the angular second moment from the co-occurance matrix
     * 
     * @return
     */
    public double getASM() {
        if (!normalised)
            normalise();

        double ASM = 0;

        for (double val : matrix.values())
            ASM = ASM + val * val;
        
        return ASM;

    }

    public double getContrast() {
        if (!normalised)
            normalise();

        double contrast = 0;

        Indexer indexer = new Indexer(256, 256);
        for (Integer index : matrix.keySet()) {
            int[] pos = indexer.getCoord(index);

            contrast = contrast + (pos[1] - pos[0]) * (pos[1] - pos[0]) * matrix.get(index);

        }

        return contrast;

    }

    public double getCorrelation() {
        if (!normalised)
            normalise();

        double correlation = 0;

        // Getting partial probability density functions
        CumStat px = new CumStat();
        CumStat py = new CumStat();

        Indexer indexer = new Indexer(256, 256);
        for (Integer index : matrix.keySet()) {
            int[] pos = indexer.getCoord(index);

            px.addMeasure(pos[0], matrix.get(index));
            py.addMeasure(pos[1], matrix.get(index));

        }

        // Calculating the mean and standard deviations for the partial probability
        // density functions
        double xMean = px.getMean();
        double yMean = py.getMean();

        double xStd = px.getStd(CumStat.POPULATION);
        double yStd = py.getStd(CumStat.POPULATION);

        // Calculating the correlation
        for (Integer index : matrix.keySet()) {
            int[] pos = indexer.getCoord(index);
            correlation = correlation + (pos[0] - xMean) * (pos[1] - yMean) * matrix.get(index);

        }

        correlation = correlation / (xStd * yStd);

        return correlation;

    }

    public double getEntropy() {
        if (!normalised)
            normalise();

        double entropy = 0;

        for (double val : matrix.values())
            entropy = entropy + val * Math.log(val) / Math.log(2);
      
        return -entropy;

    }


    // PRIVATE METHODS

    ImageStack convertTo8Bit(ImageStack image) {
        // Duplicating the image, so the original isn't affected
        ImagePlus ipl = new ImagePlus("Temp", image.duplicate());

        // IntensityMinMax.run(ipl, true);
        IJ.run(ipl, "8-bit", null);

        return ipl.getImageStack();

    }
    
    int getTotalCount() {
        int count = 0;
        for (Double val : matrix.values())
            count = count + (int) Math.round(val);

        return count;

    }

    void normalise() {
        int finalCount = getTotalCount();
        matrix.replaceAll((k, v) -> v / finalCount);
        normalised = true;
    }

    
    // GETTERS

    public LinkedHashMap<Integer, Double> getCoOccurrenceMatrix() {
        if (!normalised)
        normalise();

        return matrix;
    }

    public Indexer getIndexer() {
        return indexer;
    }

}
