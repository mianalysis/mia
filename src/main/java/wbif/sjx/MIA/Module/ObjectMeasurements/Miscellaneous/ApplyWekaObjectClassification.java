package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import java.util.Iterator;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import weka.classifiers.Classifier;
import weka.core.Capabilities.Capability;

public class ApplyWekaObjectClassification extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLASSIFIER_PATH = "Classifier path";

    public ApplyWekaObjectClassification(ModuleCollection modules) {
        super("Apply Weka object classification",modules);
    }

    
    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting other parameters
        String classifierPath = parameters.getValue(CLASSIFIER_PATH);
        
        // Load classifier
        Classifier classifier = null;
        try {
            classifier = (Classifier) weka.core.SerializationHelper.read(classifierPath);
        } catch (Exception e) {
            MIA.log.writeError(e.getStackTrace());
            return Status.FAIL;
        }
        
            
        // Determine measurements required
        

        // Unless it can be loaded from classifier, create FastVector of features (and class)
        // FastVector outlookClasses = new FastVector(2);
        // outlookClasses.addElement("sunny");
        // outlookClasses.addElement("rainy");
        // Attribute outlook = new Attribute("outlook", outlookClasses);
        // Attribute temperature = new Attribute("temperature");
        // Attribute humidity = new Attribute("humidity");
        // FastVector windyClasses = new FastVector(2);
        // windyClasses.addElement("TRUE");
        // windyClasses.addElement("FALSE");
        // Attribute windy = new Attribute("windy",windyClasses);
        // FastVector playClasses = new FastVector(2);
        // playClasses.addElement("yes");
        // playClasses.addElement("no");
        // Attribute play = new Attribute("play",playClasses);

        // FastVector fvWekaAttributes = new FastVector(5);
        // fvWekaAttributes.addElement(outlook);
        // fvWekaAttributes.addElement(temperature);
        // fvWekaAttributes.addElement(humidity);
        // fvWekaAttributes.addElement(windy);
        // fvWekaAttributes.addElement(play);

        // Create instances for objects to classify
        // Instances dataset = new Instances("whatever", fvWekaAttributes, 0);
        // dataset.setClassIndex(0);
        // Instance instance = new SparseInstance(1, new double[]{0,75,70,0});
        // dataset.add(instance);

        // MAYBE USES "CLASSIFIER.DISTRIBUTIONFORINSTANCE" ?


        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new FilePathP(CLASSIFIER_PATH,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
