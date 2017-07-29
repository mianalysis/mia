package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Process.StackComparator;
import wbif.sjx.common.MetadataExtractors.NameExtractor;
import wbif.sjx.common.Object.HCMetadata;

import javax.naming.Name;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by steph on 30/04/2017.
 */
public class ImageStackLoader extends HCModule {
    public static final String EXTRACTOR = "Extractor";
    public static final String ORDER_FIELD = "Order field";
    public static final String STATIC_FIELDS = "Static fields";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SET_FIELDS = "Set fields";

    @Override
    public String getTitle() {
        return "Image stack loader";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting parameters
        NameExtractor extractor = parameters.getValue(EXTRACTOR);
        String orderField = parameters.getValue(ORDER_FIELD);
        ArrayList<String> staticFields = parameters.getValue(STATIC_FIELDS);
        HashMap<String,String> setFields = parameters.getValue(SET_FIELDS);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting files
        File referenceFile = workspace.getMetadata().getFile();
        File[] files = referenceFile.getParentFile().listFiles();

        // Creating a Result object holding parameters about the reference file
        HCMetadata referenceResult = new HCMetadata();
        referenceResult.setFile(referenceFile);
        extractor.extract(referenceResult,referenceFile.getName());

        // Creating a structure to store only Result objects containing file parameters matching those specified
        ArrayList<HCMetadata> results = new ArrayList<>();

        // Running through all provided files, extracting parameters
        for (File file:files) {
            HCMetadata result = new HCMetadata();
            result.setFile(file);
            if (extractor.extract(result,file.getName())) {
                // Checking if fixed fields are the same as for the template
                boolean addResult = true;
                for (String field:staticFields) {
                    if (!result.getAsString(field).equals(referenceResult.getAsString(field))) {
                        addResult = false;
                    }
                }

                // Checking if fields with a specific value (not necessarily same a template) have that value
                for (String key:setFields.keySet()) {
                    if (!result.getAsString(key).equals(setFields.get(key))) {
                        addResult = false;
                    }
                }

                // If the previous conditions were met the result is added to the ArrayList
                if (addResult) {
                    results.add(result);
                }
            }
        }

        // Before importing images they need to be ordered based on the orderField parameter
        StackComparator stackComparator = new StackComparator();
        if (orderField != null) stackComparator.setField(orderField);
        results.sort(new StackComparator());

        // Loading the images and storing them as an ImagePlus
        Opener opener = new Opener();
        ImagePlus refIpl = opener.openImage(referenceFile.getAbsolutePath());
        ImagePlus ipl = IJ.createHyperStack("STACK_"+referenceFile.getName(),refIpl.getWidth(),refIpl.getHeight(),1,results.size(),1,refIpl.getBitDepth());

        int iter = 1;
        for (HCMetadata res:results) {
            ipl.setPosition(iter);

            ImagePlus singleIpl = opener.openImage(res.getFile().getAbsolutePath());
            ipl.setProcessor(singleIpl.getProcessor());

            iter++;

        }

        ipl.setPosition(1);

        workspace.addImage(new Image(outputImageName,ipl));

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        // Setting the input image stack name
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(EXTRACTOR, Parameter.OBJECT,null));
        parameters.addParameter(new Parameter(ORDER_FIELD, Parameter.STRING,""));
        parameters.addParameter(new Parameter(STATIC_FIELDS, Parameter.OBJECT,new ArrayList<String>()));
        parameters.addParameter(new Parameter(SET_FIELDS, Parameter.OBJECT,new HashMap<String,String>()));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}