package io.github.mianalysis.mia.module.objects.relate;

import java.util.ArrayList;
import java.util.Map;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.tracking.jaqaman.JaqamanLinker;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.DefaultCostMatrixCreator;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ObjectSlicesTo3D extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String LINKING_SEPARATOR = "Linking controls";
    public static final String LINKING_DISTANCE_XY = "Linking distance XY";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MAX_MISSING_SLICES = "Max number of missing slices";

    public ObjectSlicesTo3D(Modules modules) {
        super("Object slices to 3D", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE_XY, workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS, workspace);
        int maxMissingSlices = parameters.getValue(MAX_MISSING_SLICES, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectsName);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        if (calibratedUnits)
            linkingDistance = linkingDistance / inputObjects.getDppXY();

        // Only linking objects in the same frame
        for (int t = 0; t < inputObjects.getNFrames(); t++) {
            // Creating a store for existing objects. Each cluster has a collection
            // of the slice objects that make it up
            ArrayList<ArrayList<Obj>> clusters = new ArrayList<>();

            // Assigning any objects in the first slice as new clusters
            for (Obj inputObject : inputObjects.values()) {
                if (inputObject.getT() != t)
                    continue;

                if (inputObject.getZMean(true, false) > 0)
                    continue;

                // Adding current object as new cluster
                ArrayList<Obj> cluster = new ArrayList<>();
                cluster.add(inputObject);
                clusters.add(cluster);

            }

            // Iterating over each slice, getting the objects within it and
            // trying to link them to the existing clusters
            for (int z = 1; z < inputObjects.getNSlices(); z++) {
                // Getting any available clusters (those with a top slice
                // within range)
                ArrayList<Obj> candidates1 = new ArrayList<>();
                for (ArrayList<Obj> cluster : clusters) {
                    Obj topSlice = cluster.get(cluster.size() - 1);

                    if (topSlice.getT() != t)
                        if (topSlice.getZMean(true, false) < z - maxMissingSlices)
                            continue;

                    candidates1.add(topSlice);

                }

                // Getting objects in current frame
                ArrayList<Obj> candidates2 = new ArrayList<>();
                for (Obj inputObject : inputObjects.values()) {
                    if (inputObject.getT() != t)
                        continue;

                    if (inputObject.getZMean(true, false) != z)
                        continue;

                    candidates2.add(inputObject);

                }

                // Creating the ArrayList containing linkables
                ArrayList<Linkable> linkables = new ArrayList<>();
                for (int prev = 0; prev < candidates1.size(); prev++) {
                    for (int curr = 0; curr < candidates2.size(); curr++) {
                        Obj prevObj = candidates1.get(prev);
                        Obj currObj = candidates2.get(curr);

                        // Calculating main spatial cost
                        double spatialCost = prevObj.getCentroidSeparation(currObj, true, false);
                        boolean linkValid = spatialCost < linkingDistance;

                        if (linkValid) {
                            double cost = spatialCost;

                            // Linker occasionally fails on zero-costs, so adding 0.1 to all values
                            cost = cost + 0.1;

                            linkables.add(new Linkable(cost, currObj.getID(), prevObj.getID()));

                        }
                    }
                }

                // Check if there are potential links, if not, skip to the next frame
                if (linkables.size() > 0) {
                    DefaultCostMatrixCreator<Integer, Integer> creator = RelateOneToOne.getCostMatrixCreator(linkables);

                    JaqamanLinker<Integer, Integer> linker = new JaqamanLinker<>(creator);
                    if (!linker.checkInput()) {
                        MIA.log.writeError(linker.getErrorMessage());
                        return Status.FAIL;
                    }

                    if (!linker.process()) {
                        MIA.log.writeError(linker.getErrorMessage());
                        return Status.FAIL;
                    }
                    Map<Integer, Integer> assignment = linker.getResult();

                    // Applying the calculated assignments as relationships
                    for (int ID1 : assignment.keySet()) {
                        int ID2 = assignment.get(ID1);
                        Obj currObj = inputObjects.get(ID1);
                        Obj prevObj = inputObjects.get(ID2);

                        for (ArrayList<Obj> cluster : clusters) {
                            if (cluster.contains(prevObj)) {
                                cluster.add(currObj);
                                break;
                            }
                        }
                    }
                }

                // Assigning any objects in the current slice without a cluster
                for (Obj currObj : candidates2) {
                    boolean alreadyPlaced = false;
                    for (ArrayList<Obj> cluster : clusters) {
                        if (cluster.contains(currObj)) {
                            alreadyPlaced = true;
                            break;
                        }
                    }

                    if (alreadyPlaced)
                        continue;

                    ArrayList<Obj> cluster = new ArrayList<>();
                    cluster.add(currObj);
                    clusters.add(cluster);
                }
            }

            for (ArrayList<Obj> cluster : clusters) {
                Obj outputObject = outputObjects.createAndAddNewObject(cluster.get(0).getCoordinateSetFactory());
                outputObject.setT(t);
                for (Obj clusterSlice : cluster)
                    outputObject.getCoordinateSet().addAll(clusterSlice.getCoordinateSet());

            }

            writeProgressStatus(t+1, inputObjects.getNFrames(), "frames");

        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(LINKING_SEPARATOR, this));
        parameters.add(new DoubleP(LINKING_DISTANCE_XY, this, 1.0));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new IntegerP(MAX_MISSING_SLICES, this, 0));

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
}
