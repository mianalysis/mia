package io.github.mianalysis.mia.module.objects.detect;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ai.nets.samj.annotation.Mask;
import ai.nets.samj.install.EfficientSamEnvManager;
import ai.nets.samj.install.SamEnvManagerAbstract;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.models.EfficientSamJ;
import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.detect.manualextensions.SAMJExtension.EnvironmentPathModes;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

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
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String SAMJ_SEPARATOR = "Segment Anything (SAMJ) controls";
    public static final String ENVIRONMENT_PATH_MODE = "Environment path mode";
    public static final String ENVIRONMENT_PATH = "Environment path";
    public static final String INSTALL_IF_MISSING = "Install model if missing";
    public static final String PREINITIALISE = "Preinitialise (batch only)";

    protected AbstractSamJ samJ = null;
    protected AbstractSamJ preinitSamJ = null;
    protected AbstractSamJ tempSamJ = null;
    protected String preinitPath = null;
    protected boolean preinitComplete = false;
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

    // public AbstractSamJ initialiseSAMJ() {
    //     AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
    //     AbstractSamJ.MAX_ENCODED_SIDE = 3000;

    //     AbstractSamJ loadedSamJ = null;
    //     try {
    //         String environmentPath = SamEnvManagerAbstract.DEFAULT_DIR;
    //         SamEnvManagerAbstract manager = EfficientSamEnvManager.create(environmentPath);

    //         if (!manager.checkEverythingInstalled())
    //             manager.installEverything();

    //         loadedSamJ = EfficientSamJ.initializeSam(manager);

    //     } catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException
    //             | MambaInstallException e) {
    //         e.printStackTrace();
    //     }

    //     return loadedSamJ;

    // }

    public AbstractSamJ initialiseSAMJ(String environmentPath, boolean installIfMissing) {        
        AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
        AbstractSamJ.MAX_ENCODED_SIDE = 3000;

        AbstractSamJ loadedSamJ = null;
        try {
            SamEnvManagerAbstract manager = EfficientSamEnvManager.create(environmentPath);

            if (!manager.checkEverythingInstalled())
                if (installIfMissing) {
                    writeStatus("Installing SAM model");
                    MIA.log.writeDebug("Installing SAM model to "+environmentPath);
                    manager.installEverything();
                } else {
                    MIA.log.writeWarning("Model not available.  Please install manually or enable \""
                            + INSTALL_IF_MISSING + "\" parameter.");
                }

            loadedSamJ = EfficientSamJ.initializeSam(manager);

        } catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException
                | MambaInstallException e) {
            e.printStackTrace();
        }

        return loadedSamJ;

    }

    public List<Mask> getPolygonsFromObjectCentroid(Obj inputObject, AbstractSamJ samJ) {
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
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String envionmentPathMode = parameters.getValue(ENVIRONMENT_PATH_MODE, workspace);
        String environmentPath = parameters.getValue(ENVIRONMENT_PATH, workspace) + "/";
        boolean installIfMissing = parameters.getValue(INSTALL_IF_MISSING, workspace);
        boolean preinitialise = parameters.getValue(PREINITIALISE, workspace);

        Image inputImage = workspace.getImages().get(inputImageName);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        switch (envionmentPathMode) {
            case EnvironmentPathModes.DEFAULT:
                environmentPath = SamEnvManagerAbstract.DEFAULT_DIR;
                break;
        }

        if (samJ == null | !environmentPath.equals(prevEnvironmentPath))
            samJ = initialiseSAMJ(environmentPath, installIfMissing);

        if (preinitialise && preinitSamJ == null)
            // No need to install as this is the same environment as the main environment
            preinitSamJ = initialiseSAMJ(environmentPath, false);

            // Checking if SamJ has been preinitialised for this file
        String path = workspace.getMetadata().getFile().getAbsolutePath();
        try {
            if (path.equals(preinitPath)) {
                while (!preinitComplete)
                    Thread.sleep(100);

                tempSamJ = samJ;
                samJ = preinitSamJ;
                preinitSamJ = tempSamJ;
            } else {
                samJ.setImage(workspace.getImage(inputImageName).getImgPlus());
            }
        } catch (InterruptedException | IOException | RuntimeException e) {
            e.printStackTrace();
            return Status.FAIL;
        }

        if (preinitialise) {
            // Preinitialise next file on a separate thread
            preinitComplete = false;
            boolean isNext = false;
            Workspace nextWorkspace = null;
            for (Workspace currWorkspace : workspace.getWorkspaces()) {
                if (isNext) {
                    nextWorkspace = currWorkspace;
                    break;
                } else if (currWorkspace == workspace) {
                    isNext = true;
                }
            }

            if (nextWorkspace != null) {
                preinitPath = nextWorkspace.getMetadata().getFile().getAbsolutePath();
                Workspace finalNextWorkspace = nextWorkspace;
                Thread t = new Thread(() -> {
                    // Running modules up to this point
                    Modules modules = getModules();
                    for (Module currModule : modules) {
                        if (currModule == this)
                            break;
                        currModule.execute(finalNextWorkspace);
                    }

                    try {
                        preinitSamJ.setImage(finalNextWorkspace.getImage(inputImageName).getImgPlus());
                    } catch (IOException | RuntimeException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    finalNextWorkspace.clearAllImages(false);
                    finalNextWorkspace.clearAllObjects(false);
                    finalNextWorkspace.clearMetadata();

                    preinitComplete = true;

                });
                t.start();
            }
        }

        prevEnvironmentPath = environmentPath;

        // AbstractSamJ samJ = initialiseSAMJ();
        try {
            samJ.setImage(inputImage.getImgPlus());
        } catch (IOException | RuntimeException | InterruptedException e) {
            MIA.log.writeError(e);
        }

        // Creating output objects
        Objs outputObjects = new Objs(outputObjectsName, inputImage.getImagePlus());

        for (Obj inputObject : inputObjects.values()) {
            List<Mask> masks = getPolygonsFromObjectCentroid(inputObject, samJ);
            if (masks == null)
                return Status.PASS;

            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
            for (Mask mask : masks)
                try {
                    outputObject.addPointsFromPolygon(mask.getContour(), 0);
                } catch (PointOutOfRangeException e) {
                }
        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(SAMJ_SEPARATOR, this));
        parameters.add(
                new ChoiceP(ENVIRONMENT_PATH_MODE, this, EnvironmentPathModes.DEFAULT, EnvironmentPathModes.ALL));
        parameters.add(new FolderPathP(ENVIRONMENT_PATH, this));
        parameters.add(new BooleanP(INSTALL_IF_MISSING, this, true));
        parameters.add(new BooleanP(PREINITIALISE, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        
        
        returnedParameters.add(parameters.getParameter(SAMJ_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH_MODE));

        switch ((String) parameters.getValue(ENVIRONMENT_PATH_MODE, null)) {
            case EnvironmentPathModes.SPECIFIC:
                returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(INSTALL_IF_MISSING));
        // returnedParameters.add(parameters.getParameter(PREINITIALISE));

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
