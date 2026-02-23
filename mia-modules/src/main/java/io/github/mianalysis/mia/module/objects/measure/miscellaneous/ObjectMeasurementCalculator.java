package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import java.util.Collection;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.images.measure.abstrakt.MeasurementCalculator;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.measurements.MeasurementProvider;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen Cross on 19/03/2019.
 */

/**
* Perform a mathematical operation on measurements associated with each object of an object collection in the workspace.  The calculation can replace either or both values with fixed values, measurements associated with an image or a statistic of all measurements associated with another object collection (e.g. the mean volume of all objects).  The resulting measurements are associated with the corresponding input objects as new measurements.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ObjectMeasurementCalculator extends MeasurementCalculator {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* Object collection from the workspace to perform the measurement calculation for.  The specified calculation will be performed once per object in the collection.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


    public interface ValueModes extends MeasurementCalculator.ValueModes{};

    public interface CalculationModes extends MeasurementCalculator.CalculationModes{};

    public interface StatisticModes extends MeasurementCalculator.StatisticModes{};

    
    public ObjectMeasurementCalculator(ModulesI modules) {
        super("Object measurement calculator", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Perform a mathematical operation on measurements associated with each object of an object collection in the workspace.  The calculation can replace either or both values with fixed values, measurements associated with an image or a statistic of all measurements associated with another object collection (e.g. the mean volume of all objects).  The resulting measurements are associated with the corresponding input objects as new measurements.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        Status status = process(workspace, ImageObjectMode.OBJECT, inputObjects.values());

        if (status != Status.PASS)
            return status;

        // Showing results
        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(VALUE_SEPARATOR_1, this));
        parameters.add(new ChoiceP(VALUE_MODE_1, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_1, this, 0));
        parameters.add(new InputImageP(IMAGE_1, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_1, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_1, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_1, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_1, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_1, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new SeparatorP(VALUE_SEPARATOR_2, this));
        parameters.add(new ChoiceP(VALUE_MODE_2, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_2, this, 0));
        parameters.add(new InputImageP(IMAGE_2, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_2, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_2, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_2, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_2, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_2, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new StringP(OUTPUT_MEASUREMENT, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Parameters returnedParams = new Parameters();

        returnedParams.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParams.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_1));
        returnedParams.add(parameters.getParameter(VALUE_MODE_1));
        switch ((String) parameters.getValue(VALUE_MODE_1, workspace)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_1));
                break;

            case ValueModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.getParameter(IMAGE_1));
                returnedParams.add(parameters.getParameter(IMAGE_MEASUREMENT_1));
                String imageName1 = parameters.getValue(IMAGE_1, workspace);
                ((ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_1)).setImageName(imageName1);
                break;

            case ValueModes.MEASUREMENT:
                returnedParams.add(parameters.getParameter(MEASUREMENT_1));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_1)).setObjectName(inputObjectsName);
                break;

            case ValueModes.OBJECT_COLLECTION_STATISTIC:
                returnedParams.add(parameters.getParameter(REFERENCE_OBJECTS_1));
                returnedParams.add(parameters.getParameter(REFERENCE_MEASUREMENT_1));
                returnedParams.add(parameters.getParameter(STATISTIC_MODE_1));
                String referenceObjectsName1 = parameters.getValue(REFERENCE_OBJECTS_1, workspace);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_MEASUREMENT_1))
                        .setObjectName(referenceObjectsName1);
                break;
        }

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_2));
        returnedParams.add(parameters.getParameter(VALUE_MODE_2));
        switch ((String) parameters.getValue(VALUE_MODE_2, workspace)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_2));
                break;

            case ValueModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.getParameter(IMAGE_2));
                returnedParams.add(parameters.getParameter(IMAGE_MEASUREMENT_2));
                String imageName2 = parameters.getValue(IMAGE_2, workspace);
                ((ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_2)).setImageName(imageName2);
                break;

            case ValueModes.MEASUREMENT:
                returnedParams.add(parameters.getParameter(MEASUREMENT_2));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_2)).setObjectName(inputObjectsName);
                break;

            case ValueModes.OBJECT_COLLECTION_STATISTIC:
                returnedParams.add(parameters.getParameter(REFERENCE_OBJECTS_2));
                returnedParams.add(parameters.getParameter(REFERENCE_MEASUREMENT_2));
                returnedParams.add(parameters.getParameter(STATISTIC_MODE_2));
                String referenceObjectsName2 = parameters.getValue(REFERENCE_OBJECTS_2, workspace);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_MEASUREMENT_2))
                        .setObjectName(referenceObjectsName2);
                break;
        }

        returnedParams.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParams.add(parameters.getParameter(OUTPUT_MEASUREMENT));
        returnedParams.add(parameters.getParameter(CALCULATION_MODE));

        return returnedParams;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        // Creating new MeasurementRef
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        String measurementName = parameters.getValue(OUTPUT_MEASUREMENT, workspace);
        returnedRefs.add(objectMeasurementRefs.getOrPut(measurementName).setObjectsName(inputObjectsName));

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Object collection from the workspace to perform the measurement calculation for.  The specified calculation will be performed once per object in the collection.");

        parameters.get(VALUE_MODE_1)
                .setDescription("Controls how the first value in the calculation is defined:<br><ul>"

                        + "<li>\"" + ValueModes.FIXED + "\" A single, fixed value defined by \"" + FIXED_VALUE_1
                        + "\"is used.</li>"

                        + "<li>\"" + ValueModes.IMAGE_MEASUREMENT
                        + "\" A measurement associated with an image (specified by \"" + IMAGE_1
                        + "\") and defined by \"" + IMAGE_MEASUREMENT_1 + "\" is used.</li>"

                        + "<li>\"" + ValueModes.MEASUREMENT
                        + "\" A measurement associated with the input object and defined by \"" + MEASUREMENT_1
                        + "\" is used.</li>"

                        + "<li>\"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" A statistic (specified by \""
                        + STATISTIC_MODE_1 + "\") of a measurement (specified by \"" + REFERENCE_MEASUREMENT_1
                        + "\") associated with all objects in an object collection (specified by \""
                        + REFERENCE_OBJECTS_1
                        + "\") is used.  For example, the mean object volume.  Note: The object collection used to calculate the statistic doesn't have to be the same as the input object collection.</li></ul>");

        parameters.get(FIXED_VALUE_1).setDescription("Fixed value to use in the calculation when \"" + VALUE_MODE_1
                + "\" is in \"" + ValueModes.FIXED + "\" mode.");

        parameters.get(IMAGE_1)
                .setDescription("A measurement associated with this image will be used in the calculated when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(IMAGE_MEASUREMENT_1)
                .setDescription("Measurement associated with an image (specified by \"" + IMAGE_1
                        + "\") to use in the calculation when \"" + VALUE_MODE_1 + "\" is in \""
                        + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(MEASUREMENT_1)
                .setDescription("Measurement, associated with the current object, to use in the calculation when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.MEASUREMENT + "\" mode.");

        parameters.get(REFERENCE_OBJECTS_1).setDescription(
                "Object collection for which a statistic of an associated measurement will be used in the calculation when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(REFERENCE_MEASUREMENT_1)
                .setDescription("Measurement associated with the objects in the collection specified by \""
                        + REFERENCE_OBJECTS_1
                        + "\".  A statistic of all object measurements will be used in the calculation when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(STATISTIC_MODE_1)
                .setDescription("Statistic to apply to all measurements (specified by \"" + REFERENCE_MEASUREMENT_1
                        + "\") of an object collection (specified by \"" + REFERENCE_OBJECTS_1
                        + "\").  The resulting value will be used in the calculation when \"" + VALUE_MODE_1
                        + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.  Choices are: "
                        + String.join(", ", StatisticModes.ALL) + ".");

        parameters.get(VALUE_MODE_2)
                .setDescription("Controls how the second value in the calculation is defined:<br><ul>"

                        + "<li>\"" + ValueModes.FIXED + "\" A single, fixed value defined by \"" + FIXED_VALUE_2
                        + "\"is used.</li>"

                        + "<li>\"" + ValueModes.IMAGE_MEASUREMENT
                        + "\" A measurement associated with an image (specified by \"" + IMAGE_2
                        + "\") and defined by \"" + IMAGE_MEASUREMENT_2 + "\" is used.</li>"

                        + "<li>\"" + ValueModes.MEASUREMENT
                        + "\" A measurement associated with the input object and defined by \"" + MEASUREMENT_2
                        + "\" is used.</li>"

                        + "<li>\"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" A statistic (specified by \""
                        + STATISTIC_MODE_2 + "\") of a measurement (specified by \"" + REFERENCE_MEASUREMENT_2
                        + "\") associated with all objects in an object collection (specified by \""
                        + REFERENCE_OBJECTS_2
                        + "\") is used.  For example, the mean object volume.  Note: The object collection used to calculate the statistic doesn't have to be the same as the input object collection.</li></ul>");

        parameters.get(FIXED_VALUE_2).setDescription("Fixed value to use in the calculation when \"" + VALUE_MODE_2
                + "\" is in \"" + ValueModes.FIXED + "\" mode.");

        parameters.get(IMAGE_2)
                .setDescription("A measurement associated with this image will be used in the calculated when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(IMAGE_MEASUREMENT_2)
                .setDescription("Measurement associated with an image (specified by \"" + IMAGE_2
                        + "\") to use in the calculation when \"" + VALUE_MODE_2 + "\" is in \""
                        + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(MEASUREMENT_2)
                .setDescription("Measurement, associated with the current object, to use in the calculation when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.MEASUREMENT + "\" mode.");

        parameters.get(REFERENCE_OBJECTS_2).setDescription(
                "Object collection for which a statistic of an associated measurement will be used in the calculation when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(REFERENCE_MEASUREMENT_2)
                .setDescription("Measurement associated with the objects in the collection specified by \""
                        + REFERENCE_OBJECTS_2
                        + "\".  A statistic of all object measurements will be used in the calculation when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(STATISTIC_MODE_2)
                .setDescription("Statistic to apply to all measurements (specified by \"" + REFERENCE_MEASUREMENT_2
                        + "\") of an object collection (specified by \"" + REFERENCE_OBJECTS_2
                        + "\").  The resulting value will be used in the calculation when \"" + VALUE_MODE_2
                        + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.  Choices are: "
                        + String.join(", ", StatisticModes.ALL) + ".");

        parameters.get(OUTPUT_MEASUREMENT).setDescription(
                "The value resulting from the calculation will be stored as a new measurement with this name.  This output measurement will be associated with the corresponding object from the input object collection.");

        parameters.get(CALCULATION_MODE).setDescription(
                "Calculation to perform.  Choices are: " + String.join(", ", CalculationModes.ALL) + ".");

    }
}
