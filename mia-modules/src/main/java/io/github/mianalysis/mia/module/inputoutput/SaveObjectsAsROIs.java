package io.github.mianalysis.mia.module.inputoutput;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
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
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 26/06/2017.
 */

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SaveObjectsAsROIs extends AbstractSaver {

    /**
    * 
    */
    public static final String LOADER_SEPARATOR = "Object output";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String ADD_TRACK_ID = "Add track ID";

    /**
    * 
    */
    public static final String TRACK_OBJECTS = "Track objects";

    /**
    * 
    */
    public static final String ADD_OBJECT_CLASS = "Add object class";

    /**
    * 
    */
    public static final String METADATA_ITEM = "Metadata item";

    /**
    * 
    */
    public static final String FILE_MODE = "File mode";

    public interface FileModes {
        String PER_OBJECT = "Per object";
        String PER_OBJECT_PER_SLICE = "Per slice per object";
        String SINGLE_FILE = "Single file";

        String[] ALL = new String[] { PER_OBJECT, PER_OBJECT_PER_SLICE, SINGLE_FILE };

    }

    public SaveObjectsAsROIs(Modules modules) {
        super("Save objects as ROIs", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static void saveObjects(String outPath, String fileMode, ObjsI inputObjects,
            @Nullable String trackObjectsName, @Nullable String metadataItem) {
        String moduleName = new SaveObjectsAsROIs(null).getName();
        try {
            ZipOutputStream zos = null;
            DataOutputStream out = null;
            RoiEncoder re = null;
            if (fileMode.equals(FileModes.SINGLE_FILE)) {
                zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outPath + ".zip")));
                out = new DataOutputStream(new BufferedOutputStream(zos));
                re = new RoiEncoder(out);
            }

            int total = inputObjects.size();
            int count = 0;
            for (ObjI inputObject : inputObjects.values()) {
                int oid = inputObject.getID();
                int tid = -1;
                if (trackObjectsName != null) {
                    ObjI parentObject = inputObject.getParent(trackObjectsName);
                    if (parentObject != null)
                        tid = parentObject.getID();
                }

                String classStr = "";
                if (metadataItem != null) {
                    ObjMetadata metadata = inputObject.getMetadataItem(metadataItem);
                    if (metadata != null)
                        classStr = "_" + metadata.getValue();
                }

                if (fileMode.equals(FileModes.PER_OBJECT)) {
                    zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                            outPath + "_ID" + String.valueOf(oid) + "_TR" + String.valueOf(tid) + classStr + ".zip")));
                    out = new DataOutputStream(new BufferedOutputStream(zos));
                    re = new RoiEncoder(out);
                }

                HashMap<Integer, Roi> rois = inputObject.getRois();
                for (int z : rois.keySet()) {
                    Roi roi = rois.get(z);

                    String label = "ID" + String.valueOf(oid) + "_TR" + String.valueOf(tid) + "_T"
                            + (inputObject.getT() + 1) + "_Z" + (z + 1) + classStr + ".roi";

                    roi.setName(label);

                    if (inputObjects.getNFrames() > 1 && inputObjects.getNSlices() > 1)
                        roi.setPosition(1, z + 1, inputObject.getT() + 1);
                    else if (inputObjects.getNFrames() > 1 && inputObjects.getNSlices() == 1)
                        roi.setPosition(inputObject.getT() + 1);
                    else
                        roi.setPosition(z + 1);

                    if (fileMode.equals(FileModes.PER_OBJECT_PER_SLICE)) {
                        zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                                outPath + "_ID" + String.valueOf(oid) + "_TR" + String.valueOf(tid) + "_T"
                                        + (inputObject.getT() + 1) + "_Z" + (z + 1) + classStr + ".zip")));
                        out = new DataOutputStream(new BufferedOutputStream(zos));
                        re = new RoiEncoder(out);
                    }

                    zos.putNextEntry(new ZipEntry(label));
                    re.write(roi);
                    out.flush();

                    if (fileMode.equals(FileModes.PER_OBJECT_PER_SLICE))
                        out.close();

                }

                if (fileMode.equals(FileModes.PER_OBJECT))
                    out.close();

                writeProgressStatus(++count, total, "objects", moduleName);

            }

            if (fileMode.equals(FileModes.SINGLE_FILE))
                out.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        boolean addTrackID = parameters.getValue(ADD_TRACK_ID, workspace);
        boolean addClass = parameters.getValue(ADD_OBJECT_CLASS, workspace);
        String metadataItem = parameters.getValue(METADATA_ITEM, workspace);
        String fileMode = parameters.getValue(FILE_MODE, workspace);
        String parentObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

        if (!addTrackID)
            parentObjectsName = null;

        if (!addClass)
            metadataItem = null;

        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        String outputPath = getOutputPath(modules, workspace);
        String outputName = getOutputName(modules, workspace);

        // Ensuring folders have been created
        new File(outputPath).mkdirs();

        // Adding last bits to name
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);
        outputPath = outputPath + suffix;

        saveObjects(outputPath, fileMode, inputObjects, parentObjectsName, metadataItem);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(ADD_TRACK_ID, this, false));
        parameters.add(new ParentObjectsP(TRACK_OBJECTS, this));
        parameters.add(new BooleanP(ADD_OBJECT_CLASS, this, false));
        parameters.add(new ObjectMetadataP(METADATA_ITEM, this));
        parameters.add(new ChoiceP(FILE_MODE, this, FileModes.SINGLE_FILE, FileModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(ADD_TRACK_ID));
        if ((boolean) parameters.getValue(ADD_TRACK_ID, workspace)) {
            returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));
            ((ParentObjectsP) parameters.get(TRACK_OBJECTS))
                    .setChildObjectsName((String) parameters.getValue(INPUT_OBJECTS, workspace));
        }
        returnedParameters.add(parameters.getParameter(ADD_OBJECT_CLASS));
        if ((boolean) parameters.getValue(ADD_OBJECT_CLASS, workspace)) {
            returnedParameters.add(parameters.getParameter(METADATA_ITEM));
            ((ObjectMetadataP) parameters.get(METADATA_ITEM))
                    .setObjectName((String) parameters.getValue(INPUT_OBJECTS, workspace));
        }
        returnedParameters.add(parameters.getParameter(FILE_MODE));

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

    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
    }
}
