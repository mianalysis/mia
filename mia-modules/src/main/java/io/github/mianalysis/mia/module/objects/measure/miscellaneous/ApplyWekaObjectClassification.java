package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

/**
 * Apply a previously-prepared WEKA object classifier to a specified object
 * collection from the workspace. Classification can be based on a range of
 * measurements associated with the input objects. All measurements used to
 * create this model should be present in the input objects and have the same
 * names (i.e. measurement names shouldn't be changed during preparation of
 * training data).<br>
 * <br>
 * The probability of each input object belonging to each class is output as a
 * measurement associated with that object. Each object also has a class index
 * (based on the order the classes are listed in the .model file) indicating the
 * most probable class that object belongs to.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ApplyWekaObjectClassification extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Objects input";

    /**
     * Input objects from workspace which will be classified based on model
     * specified by "Classifier path" parameter.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String CLASSIFIER_SEPARATOR = "Classifier controls";

    /**
     * WEKA model (.model extension) that will be used to classify input objects
     * based on a variety of measurements. This model must be created in the
     * <a href="https://www.cs.waikato.ac.nz/ml/index.html">WEKA software</a>. All
     * measurements used to create this model should be present in the input objects
     * and have the same names (i.e. measurement names shouldn't be changed during
     * preparation of training data).
     */
    public static final String CLASSIFIER_PATH = "Classifier path";

    /**
     * When selected, measurements will be normalised (set to the range 0-1) within
     * their respective classes.
     */
    public static final String APPLY_NORMALISATION = "Apply normalisation";

    private String currClassifierPath = "";
    private Instances currInstances = null;
    private AbstractClassifier currClassifier = null;

    public interface ObjMetadataItems {
        public static final String CLASS = "CLASSIFIER // CLASS";

    }

    public ApplyWekaObjectClassification(Modules modules) {
        super("Apply Weka object classification", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Apply a previously-prepared WEKA object classifier to a specified object collection from the workspace.  Classification can be based on a range of measurements associated with the input objects.  All measurements used to create this model should be present in the input objects and have the same names (i.e. measurement names shouldn't be changed during preparation of training data).<br><br>"
                +

                "The probability of each input object belonging to each class is output as a measurement associated with that object.  Each object also has a class index (based on the order the classes are listed in the .model file) indicating the most probable class that object belongs to.";
    }

    public static String getProbabilityMeasurementName(String className) {
        return "CLASSIFIER // " + className + "_PROB";
    }

    public static String getClassMeasurementName(Instances instances) {
        StringBuilder sb = new StringBuilder("CLASSIFIER // CLASS (");
        for (int i = 0; i < instances.numClasses(); i++) {
            if (i != 0)
                sb.append(",");
            sb.append(instances.classAttribute().value(i));
        }

        sb.append(")");

        return sb.toString();

    }

    Instances getInstances(String classifierPath) throws FileNotFoundException, IOException, ClassNotFoundException {
        if (currClassifierPath.equals(classifierPath) && currInstances != null)
            return currInstances;

        currClassifierPath = classifierPath;

        if (!new File(classifierPath).exists())
            return null;

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(classifierPath));
        currClassifier = (AbstractClassifier) objectInputStream.readObject();
        currInstances = (Instances) objectInputStream.readObject();
        objectInputStream.close();

        return currInstances;

    }

    AbstractClassifier getClassifier(String classifierPath) throws FileNotFoundException, IOException, ClassNotFoundException {
        if (currClassifierPath.equals(classifierPath) && currInstances != null)
            return currClassifier;

        currClassifierPath = classifierPath;

        if (!new File(classifierPath).exists())
            return null;

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(classifierPath));
        currClassifier = (AbstractClassifier) objectInputStream.readObject();
        currInstances = (Instances) objectInputStream.readObject();
        objectInputStream.close();

        return currClassifier;

    }

    public static void addProbabilityMeasurements(Obj inputObject, Instances instances, double[] classification) {
        for (int i = 0; i < instances.numClasses(); i++) {
            String measName = getProbabilityMeasurementName(instances.classAttribute().value(i));
            Measurement measurement = new Measurement(measName, classification[i]);
            inputObject.addMeasurement(measurement);
        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting other parameters
        String classifierPath = parameters.getValue(CLASSIFIER_PATH, workspace);
        boolean applyNormalisation = parameters.getValue(APPLY_NORMALISATION, workspace);

        Instances instances = null;
        AbstractClassifier classifier = null;

        try {
            instances = getInstances(classifierPath);
            classifier = getClassifier(classifierPath);
        } catch (IOException | ClassNotFoundException e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        // Getting list of measurement names
        ArrayList<String> measurementNames = new ArrayList<>();
        for (int i = 0; i < instances.numAttributes(); i++) {
            String measName = instances.attribute(i).name();
            measName = measName.replace("ï»¿", "");
            measurementNames.add(measName);
        }

        // Adding object instances
        ArrayList<Obj> processedObjects = new ArrayList<>();
        for (Obj inputObject : inputObjects.values()) {
            double[] objAttr = new double[measurementNames.size() - 1];
            for (int i = 0; i < measurementNames.size() - 1; i++) {
                Measurement measurement = inputObject.getMeasurement(measurementNames.get(i));

                // Objects with missing measurements will cause problems for normalisation
                if (measurement == null || Double.isNaN(measurement.getValue()))
                    continue;

                objAttr[i] = measurement.getValue();
            }

            processedObjects.add(inputObject);
            instances.add(new SparseInstance(1, objAttr));
        }

        try {
            // Applying normalisation
            if (applyNormalisation) {
                Normalize normalize = new Normalize();
                normalize.setInputFormat(instances);
                instances = Filter.useFilter(instances, normalize);
            }

            // Applying classifications
            double[][] classifications = classifier.distributionsForInstances(instances);

            String classMeasName = getClassMeasurementName(instances);
            for (int i = 0; i < processedObjects.size(); i++) {
                Obj inputObject = processedObjects.get(i);
                double[] classification = classifications[i];
                addProbabilityMeasurements(inputObject, instances, classification);

                int classIndex = (int) classifier.classifyInstance(instances.get(i));
                inputObject.addMeasurement(new Measurement(classMeasName, classIndex));
                inputObject.addMetadataItem(
                        new ObjMetadata(ObjMetadataItems.CLASS, instances.classAttribute().value(classIndex)));

            }
        } catch (Exception e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        if (showOutput) {
            inputObjects.showMeasurements(this, modules);
            inputObjects.showMetadata(this, modules);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CLASSIFIER_SEPARATOR, this));
        parameters.add(new FilePathP(CLASSIFIER_PATH, this));
        parameters.add(new BooleanP(APPLY_NORMALISATION, this, true));

        addParameterDescriptions();

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
        Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        try {
            // Getting class names
            String currClassifierPath = parameters.getValue(CLASSIFIER_PATH, workspace);

            ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

            Instances instances = getInstances(currClassifierPath);
            if (instances == null)
                return returnedRefs;            

            for (int i = 0; i < instances.numClasses(); i++) {
                String className = instances.classAttribute().value(i);
                ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(getProbabilityMeasurementName(className));
                ref.setObjectsName(inputObjectsName);
                returnedRefs.add(ref);
            }

            ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(getClassMeasurementName(instances));
            ref.setObjectsName(inputObjectsName);
            returnedRefs.add(ref);

            return returnedRefs;

        } catch (IOException | ClassNotFoundException e) {
            MIA.log.writeError(e);
            return null;
        }
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        ObjMetadataRefs returnedRefs = new ObjMetadataRefs();

        ObjMetadataRef ref = objectMetadataRefs.getOrPut(ObjMetadataItems.CLASS);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

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
        parameters.get(INPUT_OBJECTS)
                .setDescription("Input objects from workspace which will be classified based on model specified by \""
                        + CLASSIFIER_PATH + "\" parameter.");

        parameters.get(CLASSIFIER_PATH).setDescription(
                "WEKA model (.model extension) that will be used to classify input objects based on a variety of measurements.  This model must be created in the <a href=\"https://www.cs.waikato.ac.nz/ml/index.html\">WEKA software</a>.  All measurements used to create this model should be present in the input objects and have the same names (i.e. measurement names shouldn't be changed during preparation of training data).");

        parameters.get(APPLY_NORMALISATION).setDescription(
                "When selected, measurements will be normalised (set to the range 0-1) within their respective classes.");

    }
}
