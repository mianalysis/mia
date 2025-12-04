package io.github.mianalysis.mia.module.inputoutput;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import io.github.mianalysis.mia.module.inputoutput.SaveObjectsAsMOC.FieldKeys;
import io.github.mianalysis.mia.module.inputoutput.SaveObjectsAsMOC.RefKeys;
import io.github.mianalysis.mia.module.script.GeneralOutputter;
import io.github.mianalysis.mia.module.script.RunScript;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.VolumeTypesInterface;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.PartnerRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.system.FileTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

/**
 * Created by Stephen Cross on 24/11/25
 */

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class LoadObjectsFromMOC extends GeneralOutputter {
    public static final String PATH_SEPARATOR = "File path controls";
    public static final String FILE_PATH_MODE = "File path mode";
    public static final String SPECIFIC_FILE_PATH = "Specific file path";
    public static final String GENERIC_FILE_PATH = "Generic file path";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

    public static final String POPULATE_SEPARATOR = "Populate controls";
    public static final String POPULATE_OUTPUTS = "Populate outputs";

    public static final String OUTPUT_SEPARATOR = "Output controls";
    public static final String EXPORT = "Export";

    private String templateFilePathDefault = "Click to select template";

    public interface FilePathModes {
        String GENERIC_PATH = "Generic path";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[] { GENERIC_PATH, SPECIFIC_FILE };

    }

    public interface OutputTypes extends RunScript.OutputTypes {
    }

    public LoadObjectsFromMOC(Modules modules) {
        super("Load objects from MOC (MIA collection)", modules);
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
            // Iterating over all files in the .moc, checking if its an objtemplate
            // Loading multiple ROIs from .moc
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
                currParameters.add(new BooleanP(EXPORT, this, true));
                groups.addParameters(currParameters);

                JSONArray measurementArray = templateJSON.getJSONArray(FieldKeys.MEASUREMENTS.toString());
                measurementArray.forEach((v) -> {
                    Parameters measParameters = new Parameters();
                    measParameters.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.OBJECT_MEASUREMENT, OutputTypes.ALL));
                    measParameters.add(new InputObjectsInclusiveP(ASSOCIATED_OBJECTS, this, objectsName));
                    measParameters.add(new StringP(MEASUREMENT_NAME, this, (String) v));
                    measParameters.add(new BooleanP(EXPORT, this, true));
                    groups.addParameters(measParameters);
                });

                JSONArray metadataArray = templateJSON.getJSONArray(FieldKeys.METADATA.toString());
                metadataArray.forEach((v) -> {
                    Parameters metadataParameters = new Parameters();
                    metadataParameters
                            .add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.OBJECT_METADATA_ITEM, OutputTypes.ALL));
                    metadataParameters.add(new InputObjectsInclusiveP(ASSOCIATED_OBJECTS, this, objectsName));
                    metadataParameters.add(new StringP(OBJECT_METADATA_NAME, this, (String) v));
                    metadataParameters.add(new BooleanP(EXPORT, this, true));
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
                    parentParameters.add(new BooleanP(EXPORT, this, true));
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
                    childParameters.add(new BooleanP(EXPORT, this, true));
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
                    partnerParameters.add(new BooleanP(EXPORT, this, true));
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
    }

    public void createObjects(String filePath, Workspace workspace) {
        if (!new File(filePath).exists()) {
            MIA.log.writeWarning("Input objects file \"" + filePath + "\" can't be found");
            return;
        }

        // Getting lists of exported objects, measurements, metadata and relationships
        ParameterGroup groups = parameters.getParameter(ADD_OUTPUT);
        ArrayList<String> exportedObjects = new ArrayList<>();
        HashMap<String, ArrayList<String>> exportedMeasurements = new HashMap<>();
        HashMap<String, ArrayList<String>> exportedMetadata = new HashMap<>();
        HashMap<String, ArrayList<String>> exportedParents = new HashMap<>();
        HashMap<String, ArrayList<String>> exportedChildren = new HashMap<>();
        HashMap<String, ArrayList<String>> exportedPartners = new HashMap<>();
        LinkedHashMap<Integer, Parameters> collections = groups.getCollections(true);
        for (Parameters collection : collections.values()) {
            if (!(Boolean) collection.getParameter(EXPORT).getValue(workspace))
                continue;

            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.OBJECTS:
                    exportedObjects.add(collection.getValue(OUTPUT_OBJECTS, workspace));
                    break;
                case OutputTypes.OBJECT_MEASUREMENT:
                    String objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                    String measurementsName = collection.getValue(MEASUREMENT_NAME, workspace);
                    exportedMeasurements.putIfAbsent(objectsName, new ArrayList<>());
                    exportedMeasurements.get(objectsName).add(measurementsName);
                    break;
                case OutputTypes.OBJECT_METADATA_ITEM:
                    objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                    String metadataName = collection.getValue(METADATA_NAME, workspace);
                    exportedMetadata.putIfAbsent(objectsName, new ArrayList<>());
                    exportedMetadata.get(objectsName).add(metadataName);
                    break;
                case OutputTypes.PARENT_CHILD:
                    String parentName = collection.getValue(PARENT_NAME, workspace);
                    String childName = collection.getValue(CHILDREN_NAME, workspace);
                    exportedParents.putIfAbsent(childName, new ArrayList<>());
                    exportedParents.get(childName).add(parentName);
                    exportedChildren.putIfAbsent(parentName, new ArrayList<>());
                    exportedChildren.get(parentName).add(childName);
                    break;
                case OutputTypes.PARTNERS:
                    String partnerName1 = collection.getValue(PARTNERS_NAME_1, workspace);
                    String partnerName2 = collection.getValue(PARTNERS_NAME_2, workspace);
                    exportedPartners.putIfAbsent(partnerName1, new ArrayList<>());
                    exportedPartners.get(partnerName1).add(partnerName2);
                    exportedPartners.putIfAbsent(partnerName2, new ArrayList<>());
                    exportedPartners.get(partnerName2).add(partnerName1);
                    break;
            }
        }

        try {
            // First, adding objects, measurements and metadata
            ZipInputStream in = new ZipInputStream(new FileInputStream(filePath));
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

                if (!exportedObjects.contains(objectsName))
                    continue;

                Objs outputObjects = workspace.getObjects(objectsName);
                Obj outputObject = outputObjects.createAndAddNewObject(volumeType, objectID);

                // Adding measurements
                JSONArray measurementArray = objJSON.getJSONArray(FieldKeys.MEASUREMENTS.toString());
                measurementArray.forEach((v) -> {
                    ArrayList<String> currExportedMeasurements = exportedMeasurements.get(objectsName);
                    String measurementNickname = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    if (currExportedMeasurements == null || !currExportedMeasurements.contains(measurementNickname))
                        return;

                    String measurementName = objectsName + " // " + measurementNickname;
                    double measurementValue = ((JSONObject) v).getDouble(RefKeys.VALUE.toString());
                    outputObject.addMeasurement(new Measurement(measurementName, measurementValue));
                });

                // Adding metadata
                JSONArray metadataArray = objJSON.getJSONArray(FieldKeys.METADATA.toString());
                metadataArray.forEach((v) -> {
                    ArrayList<String> currExportedMetadata = exportedMetadata.get(objectsName);
                    String metadataNickname = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    if (currExportedMetadata == null || !currExportedMetadata.contains(metadataNickname))
                        return;

                    String metadataName = objectsName + " // " + metadataNickname;
                    String metadataValue = ((JSONObject) v).getString(RefKeys.VALUE.toString());
                    outputObject.addMetadataItem(new ObjMetadata(metadataName, metadataValue));
                });

                entry = in.getNextEntry();

            }

            // Second, loading object relationships
            in.close();
            in = new ZipInputStream(new FileInputStream(filePath));
            entry = in.getNextEntry();

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
                    Objs parentObjects = workspace.getObjects(parentName);
                    if (parentObjects == null)
                        return;

                    ArrayList<String> currExportedParents = exportedParents.get(objectsName);
                    if (currExportedParents == null || !currExportedParents.contains(parentName))
                        return;

                    int parentID = ((JSONObject) v).getInt(RefKeys.ID.toString());
                    Obj parentObject = parentObjects.get(parentID);
                    outputObject.addParent(parentObject);
                });

                // Adding children
                JSONArray childrenArray = objJSON.getJSONArray(FieldKeys.CHILDREN.toString());
                childrenArray.forEach((v) -> {
                    String childName = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    Objs childObjects = workspace.getObjects(childName);
                    if (childObjects == null)
                        return;

                    ArrayList<String> currExportedChildren = exportedChildren.get(objectsName);
                    if (currExportedChildren == null || !currExportedChildren.contains(childName))
                        return;

                    JSONArray childIDs = ((JSONObject) v).getJSONArray(RefKeys.IDS.toString());
                    childIDs.forEach((childID) -> {
                        Obj childObject = childObjects.get(childID);
                        outputObject.addChild(childObject);
                    });
                });

                // Adding partners
                JSONArray partnerArray = objJSON.getJSONArray(FieldKeys.PARTNERS.toString());
                partnerArray.forEach((v) -> {
                    String partnerName = ((JSONObject) v).getString(RefKeys.NAME.toString());
                    Objs partnerObjects = workspace.getObjects(partnerName);
                    if (partnerObjects == null)
                        return;

                    ArrayList<String> currExportedPartners = exportedPartners.get(objectsName);
                    if (currExportedPartners == null || !currExportedPartners.contains(partnerName))
                        return;

                    JSONArray partnerIDs = ((JSONObject) v).getJSONArray(RefKeys.IDS.toString());
                    partnerIDs.forEach((partnerID) -> {
                        Obj partnerObject = partnerObjects.get(partnerID);
                        outputObject.addPartner(partnerObject);
                    });
                });

                entry = in.getNextEntry();

            }

            // Third, loading object coordinates
            in.close();
            in = new ZipInputStream(new FileInputStream(filePath));
            entry = in.getNextEntry();

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

                String[] nameParts = name.substring(0, name.lastIndexOf(".roi")).split("_");
                String objectsName = nameParts[0];
                int objectID = Integer.parseInt(nameParts[1].substring(2));
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

    @Override
    public Status process(Workspace workspace) {
        String filePathMode = parameters.getValue(FILE_PATH_MODE, workspace);
        String filePath = parameters.getValue(SPECIFIC_FILE_PATH, workspace);
        String genericFilePath = parameters.getValue(GENERIC_FILE_PATH, workspace);

        // Getting the final file path
        switch (filePathMode) {
            case FilePathModes.GENERIC_PATH:
                try {
                    filePath = FileTools.getGenericName(workspace.getMetadata(), genericFilePath);
                } catch (ServiceException | DependencyException | FormatException | IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        createObjects(filePath, workspace);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(PATH_SEPARATOR, this));
        parameters.add(new ChoiceP(FILE_PATH_MODE, this, FilePathModes.GENERIC_PATH, FilePathModes.ALL));
        parameters.add(new StringP(GENERIC_FILE_PATH, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, ParameterState.MESSAGE, 170));
        parameters.add(new FilePathP(SPECIFIC_FILE_PATH, this));

        parameters.add(new SeparatorP(POPULATE_SEPARATOR, this));
        parameters.add(new FilePathP(POPULATE_OUTPUTS, this) {
            @Override
            public boolean verify() {
                return true;
            }
        });

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        group.setEditable(false);
        group.getTemplateParameters().add(new BooleanP(EXPORT, this, true));

    }

    @Override
    protected ParameterUpdaterAndGetter getOutputUpdaterAndGetter() {
        return new OutputterParameterUpdaterAndGetter() {
            @Override
            public Parameters updateAndGet(Parameters parameters) {
                Parameters outputParameters = super.updateAndGet(parameters);

                outputParameters.add(parameters.getParameter(EXPORT));

                return outputParameters;

            }
        };
    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(PATH_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILE_PATH_MODE));
        switch ((String) parameters.getValue(FILE_PATH_MODE, null)) {
            case FilePathModes.GENERIC_PATH:
                returnedParameters.add(parameters.getParameter(GENERIC_FILE_PATH));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;
            case FilePathModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(SPECIFIC_FILE_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(POPULATE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(POPULATE_OUTPUTS));

        // Updating output
        String currTemplateFilePath = parameters.getValue(POPULATE_OUTPUTS, null);
        if (!currTemplateFilePath.equals(templateFilePathDefault)) {
            populateOutputs();
            parameters.updateValue(POPULATE_OUTPUTS, templateFilePathDefault);
        }

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        if (group.getCollections(true).size() > 1) {
            returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
            returnedParameters.addAll(super.updateAndGetParameters());
        }

        // Setting all parameter controls to uneditable
        for (Parameters currParameters : group.getCollections(true).values())
            for (Parameter currParameter : currParameters.values()) {
                if (currParameter.getName().equals(EXPORT))
                    continue;

                currParameter.getControl().getComponent().setEnabled(false);
            }

        return returnedParameters;

    }

    @Override
    public <T extends Parameter> void addParameterGroupParameters(ParameterGroup parameterGroup, Class<T> type,
            LinkedHashSet<T> parameters) {
        LinkedHashMap<Integer, Parameters> collections = parameterGroup.getCollections(true);
        for (Parameters collection : collections.values()) {
            if (collection.containsKey(EXPORT))
                if (!(Boolean) collection.getValue(EXPORT, null))
                    continue;

            for (Parameter currParameter : collection.values()) {
                if (type.isInstance(currParameter))
                    parameters.add((T) currParameter);

                if (currParameter instanceof ParameterGroup)
                    addParameterGroupParameters((ParameterGroup) currParameter, type, parameters);

            }
        }
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.OBJECT_MEASUREMENT)
                    && (Boolean) collection.getValue(EXPORT, workspace)) {
                String objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                String measurementNickname = collection.getValue(MEASUREMENT_NAME, workspace);
                String measurementName = objectsName + " // " + measurementNickname;

                ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
                ref.setNickname(measurementNickname);
                ref.setObjectsName(objectsName);
                returnedRefs.add(ref);

            }
        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        Workspace workspace = null;
        ObjMetadataRefs returnedRefs = new ObjMetadataRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.OBJECT_METADATA_ITEM)
                    && (Boolean) collection.getValue(EXPORT, workspace)) {
                String objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                String metadataNickname = collection.getValue(OBJECT_METADATA_NAME, workspace);
                String metadataName = objectsName + " // " + metadataNickname;

                ObjMetadataRef ref = objectMetadataRefs.getOrPut(metadataName);
                ref.setNickname(metadataNickname);
                ref.setObjectsName(objectsName);
                returnedRefs.add(ref);
            }
        }

        return returnedRefs;

    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRefs = new ParentChildRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (!(Boolean) collection.getValue(EXPORT, workspace))
                continue;

            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.PARENT_CHILD:
                    String parentsName = collection.getValue(PARENT_NAME, workspace);
                    String childrenName = collection.getValue(CHILDREN_NAME, workspace);

                    ParentChildRef ref = parentChildRefs.getOrPut(parentsName, childrenName);
                    returnedRefs.add(ref);
                    break;
            }
        }

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        Workspace workspace = null;
        PartnerRefs returnedRefs = new PartnerRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (!(Boolean) collection.getValue(EXPORT, workspace))
                continue;

            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.PARTNERS:
                    String partnersName1 = collection.getValue(PARTNERS_NAME_1, workspace);
                    String partnersName2 = collection.getValue(PARTNERS_NAME_2, workspace);

                    PartnerRef ref = partnerRefs.getOrPut(partnersName1, partnersName2);
                    returnedRefs.add(ref);
                    break;
            }
        }

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
