// TODO: Could output plot of K-function as image

package io.github.mianalysis.mia.module.images.measure;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

/**
 * Created by sc13967 on 12/05/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureGreyscaleKFunction extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String FUNCTION_SEPARATOR = "K-function controls";
    public static final String MINIMUM_RADIUS_PX = "Minimum radius (px)";
    public static final String MAXIMUM_RADIUS_PX = "Maximum radius (px)";
    public static final String RADIUS_INCREMENT = "Radius increment (px)";

    public MeasureGreyscaleKFunction(Modules modules) {
        super("Measure greyscale K-function", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getDescription() {
        return "Measure's Ripley's K-function for greyscale images.  This method is re-written from the publication \"Extending Ripley’s K-Function to Quantify Aggregation in 2-D Grayscale Images\" by M. Amgad, et al. (doi: 10.1371/journal.pone.0144404).";

    }

    public static void main(String[] args) {
        ImagePlus ipl = IJ.openImage(
                "C:\\Users\\steph\\Documents\\People\\Georgie McDonald\\2022-05-26 ALP scale segmentation\\Test Ripley\\TEST_crop.tif");
        Image image = new Image("Test", ipl);
        MeasureGreyscaleKFunction.calculateGSKfunction(image, 5);

    }

    public static double calculateGSKfunction(Image image, int radius) {
        if (radius <= 0)
            return Double.NaN;

        int dia = (int) Math.ceil(2 * radius);
        if (dia % 2 == 0)
            dia++;

        int r = (int) Math.floor(dia / 2);
        System.out.println("r: " + r);

        ArrayList<Integer> x = new ArrayList<>();
        ArrayList<Integer> y = new ArrayList<>();

        for (int xx = -r; xx <= r; xx++)
            for (int yy = -r; yy <= r; yy++)
                if (Math.sqrt(xx * xx + yy * yy) <= radius) {
                    x.add(xx);
                    y.add(yy);
                }

        // Padding image with NaN
        ImagePlus ipl = image.getImagePlus();
        FloatProcessor ipr = new FloatProcessor(ipl.getWidth() + 2 * radius, ipl.getHeight() + 2 * radius);
        for (int i = 0; i < ipr.getPixelCount(); i++)
            ipr.setf(i, Float.NaN);

        for (int xx = 0; xx < ipl.getWidth(); xx++)
            for (int yy = 0; yy < ipl.getHeight(); yy++)
                ipr.setf(xx + radius, yy + radius, ipl.getProcessor().getf(xx, yy));

        FloatProcessor aggMap = new FloatProcessor(ipr.getWidth(), ipr.getHeight());

        // Creating aggregation map
        for (int xx = radius; xx < radius + ipl.getWidth(); xx++) {
            for (int yy = radius; yy < radius + ipl.getHeight(); yy++) {
                // Calculating edge correction
                int b = 0;
                for (int i = 0; i < x.size(); i++)
                    b = Float.isNaN(ipr.getf(xx + x.get(i), yy + y.get(i))) ? b : b + 1;

                double edgeCorrection = b / (Math.PI * radius * radius);

                float val = ipr.getf(xx, yy);
                if (Float.isNaN(val) || val == 0)
                    continue;

                float sum = 0;
                for (int i = 0; i < x.size(); i++) {
                    if (x.get(i) == 0 && y.get(i) == 0)
                        continue;

                    float newVal = ipr.getf(xx + x.get(i), yy + y.get(i));
                    if (Float.isNaN(newVal))
                        continue;

                    sum = sum + newVal;

                }

                float agg = (val * (val - 1) + val * sum) / Double.valueOf(edgeCorrection).floatValue();
                aggMap.setf(xx, yy, agg);

            }
        }

        new ImageJ();
        new ImagePlus("Agg", aggMap).show();

        return Double.NaN;

    }

    @Override
    public Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String minRadiusPx = parameters.getValue(MINIMUM_RADIUS_PX);
        String maxRadiusPx = parameters.getValue(MAXIMUM_RADIUS_PX);
        String radiusIncrementPx = parameters.getValue(RADIUS_INCREMENT);

        Image inputImage = workspace.getImage(inputImageName);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(FUNCTION_SEPARATOR, this));
        parameters.add(new IntegerP(MINIMUM_RADIUS_PX, this, 3));
        parameters.add(new IntegerP(MAXIMUM_RADIUS_PX, this, 15));
        parameters.add(new IntegerP(RADIUS_INCREMENT, this, 1));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}

// the cat ate the milk
// mummy is the best