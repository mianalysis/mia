// TODO: Could output plot of K-function as image

package io.github.mianalysis.mia.module.images.measure;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.CumStat;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by sc13967 on 12/05/2017.
 */

/**
* Measure the Gini coefficient for all pixels in the specified image.  The Gini coefficient measures the inequality in intensity of pixels.  A coefficient of 0 indicates perfect intensity homogeneity (all pixels with the same value), while a value of 1 indicates the maximum possible inequality (all pixels are black, except for a single bright pixel).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureGiniCoefficient<T extends RealType<T> & NativeType<T>> extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input";

	/**
	* 
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* 
	*/
    public static final String USE_MASK = "Use mask";

	/**
	* 
	*/
    public static final String MASK_IMAGE = "Mask image";

    public MeasureGiniCoefficient(Modules modules) {
        super("Measure Gini coefficient", modules);
    }

    public interface Measurements {
        String GINI_COEFFICIENT = "GINI_COEFFICIENT";
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getDescription() {
        return "Measure the Gini coefficient for all pixels in the specified image.  The Gini coefficient measures the inequality in intensity of pixels.  A coefficient of 0 indicates perfect intensity homogeneity (all pixels with the same value), while a value of 1 indicates the maximum possible inequality (all pixels are black, except for a single bright pixel).";
    }

    public static <T extends RealType<T> & NativeType<T>> double calculateGiniCoefficient(Image<T> image,
            @Nullable Image<T> maskImage) {
        Cursor<T> cursor1 = image.getImgPlus().localizingCursor();
        RandomAccess<T> randomAccessMask = maskImage == null ? null : maskImage.getImgPlus().randomAccess();

        // Calculating the summed absolute difference and mean
        double absDiff = 0;
        CumStat cs = new CumStat();
        while (cursor1.hasNext()) {
            cursor1.fwd();

            // Checking if we should evaluate this point
            if (randomAccessMask != null) {
                randomAccessMask.setPosition(cursor1);
                if ((int) Math.round(randomAccessMask.get().getRealDouble()) == 0)
                    continue;
            }

            Cursor<T> cursor2 = image.getImgPlus().localizingCursor();
            while (cursor2.hasNext()) {
                cursor2.fwd();

                // Checking if we should evaluate this point
                if (randomAccessMask != null) {
                    randomAccessMask.setPosition(cursor2);
                    if ((int) Math.round(randomAccessMask.get().getRealDouble()) == 0)
                        continue;
                }

                // Calculating the difference
                absDiff += Math.abs(cursor1.get().getRealDouble() - cursor2.get().getRealDouble());

            }

            cs.addMeasure(cursor1.get().getRealDouble());

        }

        return absDiff / (2 * cs.getN() * cs.getN() * cs.getMean());

    }

    @Override
    public Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        boolean useMask = parameters.getValue(USE_MASK, workspace);
        String maskImageName = parameters.getValue(MASK_IMAGE, workspace);

        Image<T> inputImage = workspace.getImage(inputImageName);
        Image<T> maskImage = useMask ? workspace.getImage(maskImageName) : null;

        double giniCoeff = calculateGiniCoefficient(inputImage, maskImage);

        inputImage.addMeasurement(new Measurement(Measurements.GINI_COEFFICIENT, giniCoeff));

        if (showOutput)
            inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(USE_MASK, this, false));
        parameters.add(new InputImageP(MASK_IMAGE, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(USE_MASK));
        if ((boolean) parameters.getValue(USE_MASK, workspace))
            returnedParameters.add(parameters.getParameter(MASK_IMAGE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        Workspace workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

        ImageMeasurementRef measurementRef = imageMeasurementRefs.getOrPut(Measurements.GINI_COEFFICIENT);
        measurementRef.setImageName(inputImageName);
        returnedRefs.add(measurementRef);

        return returnedRefs;
        
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
