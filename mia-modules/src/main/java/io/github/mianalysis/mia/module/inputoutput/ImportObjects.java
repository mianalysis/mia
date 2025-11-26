package io.github.mianalysis.mia.module.inputoutput;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.SaveObjects.FieldKeys;
import io.github.mianalysis.mia.module.inputoutput.SaveObjects.RefKeys;
import io.github.mianalysis.mia.module.script.GeneralOutputter;
import io.github.mianalysis.mia.module.script.RunScript;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.VolumeTypesInterface;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

/**
 * Created by Stephen Cross on 24/11/25
 */

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ImportObjects extends GeneralOutputter {
    public static final String POPULATE_SEPARATOR = "Populate controls";
    public static final String POPULATE_OUTPUTS = "Populate outputs";

    public static final String OUTPUT_SEPARATOR = "Output controls";

    private String templateFilePath = null;

    public interface OutputTypes extends RunScript.OutputTypes {
    }

    public ImportObjects(Modules modules) {
        super("Import objects", modules);
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

    public static JSONObject readJSON(ZipInputStream in) throws IOException {
        int len;
        byte[] buf = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        out.close();

        String jsonString = out.toString();
        jsonString = jsonString.substring(jsonString.indexOf("{"));
        jsonString = jsonString.substring(0, jsonString.lastIndexOf("}") + 1);
        jsonString = jsonString.replaceAll("[^\\x20-\\x7E]", ""); // Remove unwanted characters

        return new JSONObject(jsonString);

    }

    private void populateOutputs() {
        String currTemplateFilePath = parameters.getValue(POPULATE_OUTPUTS, null);

        if (!new File(currTemplateFilePath).exists())
            return;

        ParameterGroup groups = parameters.getParameter(ADD_OUTPUT);
        groups.removeAllParameters();

        try {
            // Iterating over all files in the zip, checking if its an objtemplate
            // Loading multiple ROIs from .zip
            ZipInputStream in = new ZipInputStream(new FileInputStream(currTemplateFilePath));
            ZipEntry entry = in.getNextEntry();

            HashMap<String, HashSet<String>> existingParentChildRelationships = new HashMap<>();
            HashMap<String, HashSet<String>> existingPartnerRelationships = new HashMap<>();

            while (entry != null) {
                String name = entry.getName();

                if (!name.endsWith(".objtemplate")) {
                    entry = in.getNextEntry();
                    continue;
                }

                JSONObject templateJSON = readJSON(in);
                String objectsName = templateJSON.getString(FieldKeys.NAME.toString());

                Parameters currParameters = new Parameters();
                currParameters.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.OBJECTS, OutputTypes.ALL));
                currParameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this, objectsName));
                groups.addParameters(currParameters);

                JSONArray measurementArray = templateJSON.getJSONArray(FieldKeys.MEASUREMENTS.toString());
                measurementArray.forEach((v) -> {
                    Parameters measParameters = new Parameters();
                    measParameters.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.OBJECT_MEASUREMENT, OutputTypes.ALL));
                    measParameters.add(new InputObjectsInclusiveP(ASSOCIATED_OBJECTS, this, objectsName));
                    measParameters.add(new StringP(MEASUREMENT_NAME, this, (String) v));
                    groups.addParameters(measParameters);
                });

                JSONArray metadataArray = templateJSON.getJSONArray(FieldKeys.METADATA.toString());
                metadataArray.forEach((v) -> {
                    Parameters metadataParameters = new Parameters();
                    metadataParameters
                            .add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.OBJECT_METADATA_ITEM, OutputTypes.ALL));
                    metadataParameters.add(new InputObjectsInclusiveP(ASSOCIATED_OBJECTS, this, objectsName));
                    metadataParameters.add(new StringP(OBJECT_METADATA_NAME, this, (String) v));
                    groups.addParameters(metadataParameters);
                });

                JSONArray parentArray = templateJSON.getJSONArray(FieldKeys.PARENTS.toString());
                parentArray.forEach((v) -> {
                    // Checking if this relationship has already been added (first key is always
                    // parent)
                    if (existingParentChildRelationships.containsKey(v))
                        if (existingParentChildRelationships.get(v).contains(objectsName))
                            return;

                    Parameters parentParameters = new Parameters();
                    parentParameters.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.PARENT_CHILD, OutputTypes.ALL));
                    parentParameters.add(new InputObjectsInclusiveP(CHILDREN_NAME, this, objectsName));
                    parentParameters.add(new InputObjectsInclusiveP(PARENT_NAME, this, (String) v));
                    groups.addParameters(parentParameters);

                    // Adding this to the existing relationships collection
                    existingParentChildRelationships.putIfAbsent((String) v, new HashSet<>());
                    existingParentChildRelationships.get((String) v).add(objectsName);

                });

                JSONArray childArray = templateJSON.getJSONArray(FieldKeys.CHILDREN.toString());
                childArray.forEach((v) -> {
                    // Checking if this relationship has already been added (first key is always
                    // parent)
                    if (existingParentChildRelationships.containsKey(objectsName))
                        if (existingParentChildRelationships.get(objectsName).contains(v))
                            return;

                    Parameters childParameters = new Parameters();
                    childParameters.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.PARENT_CHILD, OutputTypes.ALL));
                    childParameters.add(new InputObjectsInclusiveP(CHILDREN_NAME, this, (String) v));
                    childParameters.add(new InputObjectsInclusiveP(PARENT_NAME, this, objectsName));
                    groups.addParameters(childParameters);

                    // Adding this to the existing relationships collection
                    existingParentChildRelationships.putIfAbsent(objectsName, new HashSet<>());
                    existingParentChildRelationships.get(objectsName).add((String) v);

                });

                JSONArray partnerArray = templateJSON.getJSONArray(FieldKeys.PARTNERS.toString());
                partnerArray.forEach((v) -> {
                    // Checking if this relationship has already been added. Have to check both
                    // orientations
                    if (existingPartnerRelationships.containsKey(objectsName))
                        if (existingPartnerRelationships.get(objectsName).contains(v))
                            return;

                    if (existingPartnerRelationships.containsKey(v))
                        if (existingPartnerRelationships.get(v).contains(objectsName))
                            return;

                    Parameters partnerParameters = new Parameters();
                    partnerParameters.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.PARTNERS, OutputTypes.ALL));
                    partnerParameters.add(new InputObjectsInclusiveP(PARTNERS_NAME_1, this, objectsName));
                    partnerParameters.add(new InputObjectsInclusiveP(PARTNERS_NAME_2, this, (String) v));
                    groups.addParameters(partnerParameters);

                    // Adding this to the existing relationships collection
                    existingPartnerRelationships.putIfAbsent(objectsName, new HashSet<>());
                    existingPartnerRelationships.get(objectsName).add((String) v);

                });

                entry = in.getNextEntry();

            }

            in.close();

        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        templateFilePath = currTemplateFilePath;

    }

    public void createObjects(String filePath, Workspace workspace) {
        String currTemplateFilePath = parameters.getValue(POPULATE_OUTPUTS, null);

        if (!new File(currTemplateFilePath).exists())
            return;

        ParameterGroup groups = parameters.getParameter(ADD_OUTPUT);
        groups.removeAllParameters();

        try {
            // First, adding objects, measurements and metadata
            ZipInputStream in = new ZipInputStream(new FileInputStream(currTemplateFilePath));
            ZipEntry entry = in.getNextEntry();

            while (entry != null) {
                String name = entry.getName();

                if (!name.endsWith(".objmetadata")) {
                    entry = in.getNextEntry();
                    continue;
                }

                JSONObject objJSON = readJSON(in);
                String objectsName = objJSON.getString(FieldKeys.NAME.toString());
                int objectID = objJSON.getInt(FieldKeys.ID.toString());
                VolumeType volumeType = VolumeTypesInterface
                        .getVolumeType(objJSON.getString(FieldKeys.VOLUME_TYPE.toString()));

                // Getting object collection
                if (!workspace.getObjects().containsKey(objectsName))
                    workspace.addObjects(createObjectCollection(objJSON));
                Objs outputObjects = workspace.getObjects(objectsName);
                Obj outputObject = outputObjects.createAndAddNewObject(volumeType, objectID);

                // Adding measurements
                JSONArray measurementArray = objJSON.getJSONArray(FieldKeys.MEASUREMENTS.toString());
                measurementArray.forEach((v) -> {
                    String measurementName = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    double measurementValue = ((JSONObject) v).getDouble(RefKeys.VALUE.toString());
                    outputObject.addMeasurement(new Measurement(measurementName, measurementValue));
                });

                // Adding metadata
                JSONArray metadataArray = objJSON.getJSONArray(FieldKeys.METADATA.toString());
                metadataArray.forEach((v) -> {
                    String metadataName = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    String metadataValue = ((JSONObject) v).getString(RefKeys.VALUE.toString());
                    outputObject.addMetadataItem(new ObjMetadata(metadataName, metadataValue));
                });

                entry = in.getNextEntry();

            }

            // Second, loading object relationships
            in.reset();
            while (entry != null) {
                String name = entry.getName();

                if (!name.endsWith(".objmetadata")) {
                    entry = in.getNextEntry();
                    continue;
                }

                JSONObject objJSON = readJSON(in);
                String objectsName = objJSON.getString(FieldKeys.NAME.toString());
                int objectID = objJSON.getInt(FieldKeys.ID.toString());

                Objs outputObjects = workspace.getObjects(objectsName);
                Obj outputObject = outputObjects.get(objectID);

                // Adding parents
                JSONArray parentArray = objJSON.getJSONArray(FieldKeys.PARENTS.toString());
                parentArray.forEach((v) -> {
                    String parentName = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    int parentID = ((JSONObject) v).getInt(RefKeys.VALUE.toString());
                    Obj parentObject = workspace.getObjects(parentName).get(parentID);
                    outputObject.addParent(parentObject);
                });

                // Adding children
                JSONArray childrenArray = objJSON.getJSONArray(FieldKeys.CHILDREN.toString());
                childrenArray.forEach((v) -> {
                    String childName = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    int childID = ((JSONObject) v).getInt(RefKeys.VALUE.toString());
                    Obj childObject = workspace.getObjects(childName).get(childID);
                    outputObject.addChild(childObject);
                });

                // Adding partners
                JSONArray partnerArray = objJSON.getJSONArray(FieldKeys.PARTNERS.toString());
                partnerArray.forEach((v) -> {
                    String partnerName = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    int partnerID = ((JSONObject) v).getInt(RefKeys.VALUE.toString());
                    Obj partnerObject = workspace.getObjects(partnerName).get(partnerID);
                    outputObject.addPartner(partnerObject);
                });

                entry = in.getNextEntry();

            }

            // Third, loading object coordinates
            in.reset();
            while (entry != null) {
                String name = entry.getName();

                if (!name.endsWith(".roi")) {
                    entry = in.getNextEntry();
                    continue;
                }

                byte[] buf = new byte[1024];
                int len;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
                out.close();

                byte[] bytes = out.toByteArray();
                RoiDecoder rd = new RoiDecoder(bytes, name);
                Roi roi = rd.getRoi();

                if (roi == null) {
                    entry = in.getNextEntry();
                    continue;
                }

                String[] nameParts = name.substring(0, name.lastIndexOf(".roi") - 1).split("_");
                String objectsName = nameParts[0];
                int objectID = Integer.parseInt(nameParts[1].substring(3));
                int objectZ = Integer.parseInt(nameParts[3].substring(1));
                Objs outputObjects = workspace.getObjects(objectsName);
                Obj outputObject = outputObjects.get(objectID);

                outputObject.addPointsFromRoi(roi, objectZ);

                entry = in.getNextEntry();

            }

            in.close();

        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    public Objs createObjectCollection(JSONObject objJSON) {
        String objectsName = objJSON.getString(FieldKeys.NAME.toString());

        int width = objJSON.getInt(FieldKeys.WIDTH.toString());
        int height = objJSON.getInt(FieldKeys.HEIGHT.toString());
        double dppXY = objJSON.getDouble(FieldKeys.DPPXY.toString());
        double dppZ = objJSON.getDouble(FieldKeys.DPPZ.toString());
        String units = objJSON.getString(FieldKeys.UNITS.toString());
        int nSlices = objJSON.getInt(FieldKeys.N_SLICES.toString());
        int nFrames = objJSON.getInt(FieldKeys.N_FRAMES.toString());
        double frameInterval = objJSON.getDouble(FieldKeys.FRAME_INTERVAL.toString());
        String temporalUnitString = objJSON.getString(FieldKeys.TEMPORAL_UNIT.toString());
        Unit<Time> temporalUnit = TemporalUnit.getOMEUnit(temporalUnitString);

        return new Objs(objectsName, dppXY, dppZ, units, width, height, nSlices, nFrames, frameInterval, temporalUnit);

    }

    public void addObjects(String filePath, Workspace workspace) {
        try {
            // Iterating over all files in the zip, checking if its an objtemplate
            // Loading multiple ROIs from .zip
            ZipInputStream in = new ZipInputStream(new FileInputStream(filePath));
            ZipEntry entry = in.getNextEntry();

            while (entry != null) {
                String name = entry.getName();

                if (!name.endsWith(".objmetadata")) {
                    entry = in.getNextEntry();
                    continue;
                }

                int len;
                byte[] buf = new byte[1024];
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
                out.close();

                String jsonString = out.toString();
                jsonString = jsonString.substring(jsonString.indexOf("{"));
                jsonString = jsonString.substring(0, jsonString.lastIndexOf("}") + 1);
                jsonString = jsonString.replaceAll("[^\\x20-\\x7E]", ""); // Remove unwanted characters

                JSONObject templateJSON = new JSONObject(jsonString);

                entry = in.getNextEntry();

            }

            in.close();

        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    @Override
    public Status process(Workspace workspace) {

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(POPULATE_SEPARATOR, this));
        parameters.add(new FilePathP(POPULATE_OUTPUTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));

        ((ParameterGroup) parameters.getParameter(ADD_OUTPUT)).setEditable(false);

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(POPULATE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(POPULATE_OUTPUTS));

        // Updating output
        String currTemplateFilePath = parameters.getValue(POPULATE_OUTPUTS, null);
        if (!currTemplateFilePath.equals(templateFilePath))
            populateOutputs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        if (group.getCollections(true).size() > 1) {
            returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
            returnedParameters.addAll(super.updateAndGetParameters());
        }

        // Setting all parameter controls to uneditable
        for (Parameters currParameters : group.getCollections(true).values())
            for (Parameter currParameter : currParameters.values())
                currParameter.getControl().getComponent().setEnabled(false);

        return returnedParameters;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
