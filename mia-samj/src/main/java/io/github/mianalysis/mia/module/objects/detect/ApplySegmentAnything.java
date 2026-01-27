package io.github.mianalysis.mia.module.objects.detect;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ai.nets.samj.annotation.Mask;
import ai.nets.samj.install.SamEnvManagerAbstract;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.models.EfficientSamJ;
import ij.gui.PolygonRoi;
import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.objects.detect.manualextensions.SAMJExtension.EnvironmentPathModes;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.SAMJUtils;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.Img;

/**
 * Created by Stephen Cross on 31/10/2024.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ApplySegmentAnything extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/object output";

    /**
     * 
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * 
     */
    public static final String INPUT_OBJECTS = "Input objects (centroids)";

    /**
     * 
     */
    public static final String INPUT_OBJECTS_MODE = "Input objects mode";

    /**
     * 
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
     * 
     */
    public static final String OBJECT_SEPARATOR = "Output object controls";

    /**
     * 
     */
    public static final String LIMIT_OBJECT_SIZE = "Limit object size";

    /**
     * 
     */
    public static final String MAXIMUM_AREA = "Maximum area";

    /**
     * 
     */
    public static final String CALIBRATED_UNITS = "Calibrated units";

    /**
     * 
     */
    public static final String SAMJ_SEPARATOR = "Segment Anything (SAMJ) controls";

    /**
     * 
     */
    public static final String ENVIRONMENT_PATH_MODE = "Environment path mode";

    /**
     * 
     */
    public static final String ENVIRONMENT_PATH = "Environment path";

    /**
     * 
     */
    public static final String INSTALL_IF_MISSING = "Install model if missing";

    public interface InputObjectsModes {
        String BOUNDING_BOX = "Bounding box";
        String CENTROIDS = "Centroids";

        String[] ALL = new String[] { BOUNDING_BOX, CENTROIDS };

    }

    // protected AbstractSamJ samJ = null;
    protected SamEnvManagerAbstract envManager = null;
    protected String prevEnvironmentPath = "";

    public static void main(String[] args) throws Exception {
        try {
            LegacyInjector.preinit();
        } catch (Exception e) {
        }

        try {
            new ij.ImageJ();
            new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);
            AvailableModules.addModuleName(ApplySegmentAnything.class);

        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    public ApplySegmentAnything(Modules modules) {
        super("Apply Segment Anything", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";

    }

    public AbstractSamJ initialiseSAMJ(String environmentPath, boolean installIfMissing) {
        AbstractSamJ.MAX_ENCODED_AREA_RS = 10000;
        AbstractSamJ.MAX_ENCODED_SIDE = AbstractSamJ.MAX_ENCODED_AREA_RS * 3;

        AbstractSamJ loadedSamJ = null;
        try {
            if (envManager == null || !environmentPath.equals(prevEnvironmentPath)) {
                envManager = SAMJUtils.installSAMJ(environmentPath);
                prevEnvironmentPath = environmentPath;
            }

            loadedSamJ = EfficientSamJ.initializeSam(envManager);

        } catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException
                | MambaInstallException e) {
            e.printStackTrace();
        }

        return loadedSamJ;

    }

    public List<Mask> getPolygonsFromObjectBoundingBox(ObjI inputObject, AbstractSamJ samJ) {
        double[][] extents = inputObject.getExtents(true, false);
        int[] boundingBox = new int[4];
        boundingBox[0] = (int) Math.round(extents[0][0]);
        boundingBox[1] = (int) Math.round(extents[1][0]);
        boundingBox[2] = (int) Math.round(extents[0][1]);
        boundingBox[3] = (int) Math.round(extents[1][1]);

        try {
            return samJ.processBox(boundingBox);
        } catch (IOException | InterruptedException | RuntimeException e) {
            MIA.log.writeError(e);
        }

        return null;

    }

    public List<Mask> getPolygonsFromObjectCentroid(ObjI inputObject, AbstractSamJ samJ) {
        ArrayList<int[]> pts = new ArrayList<>();
        pts.add(new int[] { (int) Math.round(inputObject.getXMean(true)),
                (int) Math.round(inputObject.getYMean(true)) });

        try {
            return samJ.processPoints(pts);
        } catch (IOException | InterruptedException | RuntimeException e) {
            MIA.log.writeError(e);
        }

        return null;

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String inputObjectMode = parameters.getValue(INPUT_OBJECTS_MODE, workspace);
        boolean limitObjectSize = parameters.getValue(LIMIT_OBJECT_SIZE, workspace);
        double maxObjectArea = parameters.getValue(MAXIMUM_AREA, workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS, workspace);
        String envionmentPathMode = parameters.getValue(ENVIRONMENT_PATH_MODE, workspace);
        String environmentPath = parameters.getValue(ENVIRONMENT_PATH, workspace) + "/";
        boolean installIfMissing = parameters.getValue(INSTALL_IF_MISSING, workspace);

        ImageI inputImage = workspace.getImages().get(inputImageName);

        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        if (calibratedUnits)
            maxObjectArea = maxObjectArea / (inputObjects.getDppXY() * inputObjects.getDppXY());

        switch (envionmentPathMode) {
            case EnvironmentPathModes.DEFAULT:
                environmentPath = SAMJUtils.getDefaultEnvironmentPath();
                break;
        }

        // if (samJ == null | !environmentPath.equals(prevEnvironmentPath))
        AbstractSamJ samJ = initialiseSAMJ(environmentPath, installIfMissing);

        // Gathering objects by timepoint and slice
        HashMap<Integer, HashMap<Integer, ArrayList<ObjI>>> inputObjectsBySliceAndTime = new HashMap<>();
        for (ObjI inputObject : inputObjects.values()) {
            int t = inputObject.getT();
            int z = (int) Math.round(inputObject.getZMean(true, false));

            inputObjectsBySliceAndTime.putIfAbsent(t, new HashMap<Integer, ArrayList<ObjI>>());
            HashMap<Integer, ArrayList<ObjI>> inputObjectsBySlice = inputObjectsBySliceAndTime.get(t);

            inputObjectsBySlice.putIfAbsent(z, new ArrayList<ObjI>());
            inputObjectsBySlice.get(z).add(inputObject);

        }

        // Creating output objects
        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createFromImage(outputObjectsName, inputImage);

        int count = 0;
        int total = inputObjects.size();
        for (int t : inputObjectsBySliceAndTime.keySet()) {
            HashMap<Integer, ArrayList<ObjI>> inputObjectsBySlice = inputObjectsBySliceAndTime.get(t);
            for (int z : inputObjectsBySlice.keySet()) {
                Img img = ExtractSubstack
                        .extractSubstack(inputImage, "Substack", "1-end", String.valueOf(z + 1), String.valueOf(t + 1))
                        .getImgPlus();

                try {
                    samJ.setImage(img);
                } catch (IOException | RuntimeException | InterruptedException e) {
                    // If it runs out of memory, close SAMJ and initialise a new one
                    MIA.log.writeDebug("Initialising new SAMJ");
                    samJ.close();
                    samJ = initialiseSAMJ(environmentPath, installIfMissing);
                    try {
                        samJ.setImage(img);
                    } catch (IOException | RuntimeException | InterruptedException e1) {
                        MIA.log.writeError(e);
                        continue;
                    }
                }

                for (ObjI inputObject : inputObjectsBySlice.get(z)) {
                    List<Mask> masks = null;
                    switch (inputObjectMode) {
                        case InputObjectsModes.BOUNDING_BOX:
                            masks = getPolygonsFromObjectBoundingBox(inputObject, samJ);
                            break;

                        case InputObjectsModes.CENTROIDS:
                            masks = getPolygonsFromObjectCentroid(inputObject, samJ);
                            break;
                    }

                    if (masks == null)
                        return Status.PASS;

                    ObjI outputObject = outputObjects.createAndAddNewObject(new QuadtreeFactory());
                    for (Mask mask : masks)
                        try {
                            if (limitObjectSize && new PolygonRoi(mask.getContour(), PolygonRoi.FREEROI)
                                    .getStatistics().area > maxObjectArea) {
                                continue;
                            }

                            outputObject.addPointsFromPolygon(mask.getContour(), z);
                            outputObject.setT(t);
                        } catch (PointOutOfRangeException e) {
                        }

                    // Checking this object has volume. If not, removing it
                    if (outputObject.size() == 0)
                        outputObjects.remove(outputObject.getID());

                    writeProgressStatus(++count, total, "objects");

                }
            }
        }

        samJ.close();

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(INPUT_OBJECTS_MODE, this, InputObjectsModes.CENTROIDS, InputObjectsModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OBJECT_SEPARATOR, this));
        parameters.add(new BooleanP(LIMIT_OBJECT_SIZE, this, false));
        parameters.add(new DoubleP(MAXIMUM_AREA, this, 0));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        parameters.add(new SeparatorP(SAMJ_SEPARATOR, this));
        parameters.add(
                new ChoiceP(ENVIRONMENT_PATH_MODE, this, EnvironmentPathModes.DEFAULT, EnvironmentPathModes.ALL));
        parameters.add(new FolderPathP(ENVIRONMENT_PATH, this));
        parameters.add(new BooleanP(INSTALL_IF_MISSING, this, true));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_MODE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OBJECT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LIMIT_OBJECT_SIZE));
        if ((Boolean) parameters.getValue(LIMIT_OBJECT_SIZE, null)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_AREA));
            returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        }

        returnedParameters.add(parameters.getParameter(SAMJ_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH_MODE));

        switch ((String) parameters.getValue(ENVIRONMENT_PATH_MODE, null)) {
            case EnvironmentPathModes.SPECIFIC:
                returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(INSTALL_IF_MISSING));

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
}
