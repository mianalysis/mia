package io.github.mianalysis.mia.module.inputoutput;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.gui.Roi;
import ij.io.RoiEncoder;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 26/06/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SaveObjectsAsROIs extends AbstractSaver {
    public static final String LOADER_SEPARATOR = "Object saving";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String ADD_TRACK_ID = "Add track ID";
    public static final String TRACK_OBJECTS = "Track objects";

    public SaveObjectsAsROIs(Modules modules) {
        super("Save objects as ROIs", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static void saveObjects(String outPath, Objs inputObjects, @Nullable String trackObjectsName) {
        String moduleName = new SaveObjectsAsROIs(null).getName();
        try {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outPath)));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);

            int total = inputObjects.size();
            int count = 0;
            for (Obj inputObject : inputObjects.values()) {
                HashMap<Integer, Roi> rois = inputObject.getRois();
                for (int z : rois.keySet()) {
                    Roi roi = rois.get(z);
                    int oid = inputObject.getID();
                    int tid = -1;
                    if (trackObjectsName != null) {
                        Obj parentObject = inputObject.getParent(trackObjectsName);
                        if (parentObject != null)
                            tid = parentObject.getID();
                    }

                    String label = "ID" + String.valueOf(oid) + "_TR" + String.valueOf(tid) + "_T"
                            + inputObject.getT() + "_Z" + z + ".roi";

                    if (inputObjects.getNFrames() > 1 && inputObjects.getNSlices() > 1)
                        roi.setPosition(1, z + 1, inputObject.getT() + 1);
                    else if (inputObjects.getNFrames() > 1 && inputObjects.getNSlices() == 1)
                        roi.setPosition(inputObject.getT() + 1);
                    else
                        roi.setPosition(z + 1);

                    zos.putNextEntry(new ZipEntry(label));
                    re.write(roi);
                    out.flush();

                }

                writeProgressStatus(++count, total, "objects", moduleName);

            }

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        boolean addTrackID = parameters.getValue(ADD_TRACK_ID, workspace);
        String parentObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

        if (!addTrackID)
            parentObjectsName = null;

        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        String outputPath = getOutputPath(modules, workspace);
        String outputName = getOutputName(modules, workspace);

        // Adding last bits to name
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);
        outputPath = outputPath + suffix + ".zip";

        saveObjects(outputPath, inputObjects, parentObjectsName);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(ADD_TRACK_ID, this, false));
        parameters.add(new ParentObjectsP(TRACK_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(ADD_TRACK_ID));
        if ((boolean) parameters.getValue(ADD_TRACK_ID, workspace)) {
            returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));
            ((ParentObjectsP) parameters.get(TRACK_OBJECTS))
                    .setChildObjectsName((String) parameters.getValue(INPUT_OBJECTS, workspace));
        }

        returnedParameters.addAll(super.updateAndGetParameters());

        return returnedParameters;

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

    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
    }
}
