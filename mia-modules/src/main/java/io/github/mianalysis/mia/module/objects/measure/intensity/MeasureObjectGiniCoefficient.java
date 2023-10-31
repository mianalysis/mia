// TODO: Could addRef an optional parameter to select the channel of the input image to use for measurement

package io.github.mianalysis.mia.module.objects.measure.intensity;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.measure.MeasureGiniCoefficient;
import io.github.mianalysis.mia.module.images.transform.CropImage;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by sc13967 on 08/07/2022.
 */

/**
* Measure the Gini coefficient of all pixels within each object on an object-by-object basis.  The Gini coefficient measures the inequality in intensity of pixels.  A coefficient of 0 indicates perfect intensity homogeneity (all pixels with the same value), while a value of 1 indicates the maximum possible inequality (all pixels are black, except for a single bright pixel).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectGiniCoefficient<T extends RealType<T> & NativeType<T>> extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object and image input";

	/**
	* 
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* 
	*/
    public static final String INPUT_IMAGE = "Input image";

    public interface Measurements extends MeasureGiniCoefficient.Measurements {
    };

    public MeasureObjectGiniCoefficient(Modules modules) {
        super("Measure object Gini coefficient", modules);
    }

    public static String getFullName(String imageName) {
        return "GINI_COEFFICIENT // " + imageName;
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measure the Gini coefficient of all pixels within each object on an object-by-object basis.  The Gini coefficient measures the inequality in intensity of pixels.  A coefficient of 0 indicates perfect intensity homogeneity (all pixels with the same value), while a value of 1 indicates the maximum possible inequality (all pixels are black, except for a single bright pixel).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String objectName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs objects = workspace.getObjects().get(objectName);

        Image inputImage = workspace.getImage(inputImageName);
        String measurementName = getFullName(inputImageName);

        int count = 0;
        int total = objects.size();
        for (Obj object : objects.values()) {
            int t = object.getT();

            // Getting images cropped to this object
            double[][] extents = object.getExtents(true, false);
            int top = (int) Math.round(extents[1][0]);
            int left = (int) Math.round(extents[0][0]);
            int width = (int) Math.round(extents[0][1] - left) + 1;
            int height = (int) Math.round(extents[1][1] - top) + 1;
            Image cropImage = CropImage.cropImage(inputImage, "Crop", left, top, width, height);

            // Cropping image in Z
            int minZ = (int) Math.round(extents[2][0]);
            int maxZ = (int) Math.round(extents[2][1]);
            Image subsImage = ExtractSubstack.extractSubstack(cropImage, "Substack", "1",
                    (minZ + 1) + "-" + (maxZ + 1), "1");

            // Getting mask image
            Image maskImage = object.getAsTightImage("Mask");

            double giniCoeff = MeasureGiniCoefficient.calculateGiniCoefficient(subsImage, maskImage);
            object.addMeasurement(new Measurement(measurementName,giniCoeff));

            writeProgressStatus(++count, total, "objects");

        }

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

        String name = getFullName(inputImageName);
        ObjMeasurementRef measurementRef = objectMeasurementRefs.getOrPut(name);
        measurementRef.setObjectsName(inputObjectsName);
        returnedRefs.add(measurementRef);

        return returnedRefs;
        
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

    void addParameterDescriptions() {

    }
}
