package io.github.mianalysis.mia.module.objects.detect.manualextensions;

import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ai.nets.samj.install.EfficientSamEnvManager;
import ai.nets.samj.install.Sam2EnvManager;
import ai.nets.samj.install.SamEnvManagerAbstract;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.models.EfficientSamJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.objects.detect.ManuallyIdentifyObjects;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;

@Plugin(type = ManualExtension.class, priority = Priority.LOW, visible = true)
public class SAMJExtension extends ManualExtension implements MouseListener {
    public static final String SAMJ_SEPARATOR = "Segment Anything (SAMJ) controls";
    public static final String USE_SAM = "Enable Segment Anything";
    public static final String MODEL = "Segment Anything model";
    public static final String ENVIRONMENT_PATH_MODE = "Environment path mode";
    public static final String ENVIRONMENT_PATH = "Environment path";
    public static final String INSTALL_IF_MISSING = "Install model if missing";

    public interface SAMJModels {
        public String EFFICIENT = "Efficient";
        public String TINY = "Tiny";

        public String[] ALL = new String[] { EFFICIENT, TINY };

    }

    public interface EnvironmentPathModes {
        String DEFAULT = "Default";
        String SPECIFIC = "Specific path";

        String[] ALL = new String[] { DEFAULT, SPECIFIC };

    }

    protected Workspace workspace;
    protected EfficientSamJ efficientSamJ;
    protected ImagePlus displayIpl;

    public SAMJExtension(Module module) {
        super(module);
    }

    @Override
    public void initialiseBeforeImageShown(Workspace workspace) {
        this.workspace = workspace;

        // Getting parameters
        boolean useSAM = parameters.getValue(USE_SAM, workspace);
        if (!useSAM)
            return;

        String modelName = parameters.getValue(MODEL, workspace);
        String envionmentPathMode = parameters.getValue(ENVIRONMENT_PATH_MODE, workspace);
        String environmentPath = parameters.getValue(ENVIRONMENT_PATH, workspace)+"/";
        boolean installIfMissing = parameters.getValue(INSTALL_IF_MISSING, workspace);

        String inputImageName = module.getParameterValue(ManuallyIdentifyObjects.INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImage(inputImageName);

        efficientSamJ = null;

        AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
        AbstractSamJ.MAX_ENCODED_SIDE = 3000;

        try {
            switch (envionmentPathMode) {
                case EnvironmentPathModes.DEFAULT:
                    environmentPath = SamEnvManagerAbstract.DEFAULT_DIR;
                    break;
            }

            SamEnvManagerAbstract manager;
            switch (modelName) {
                case SAMJModels.EFFICIENT:
                default:
                    manager = EfficientSamEnvManager.create(environmentPath);
                    break;
                case SAMJModels.TINY:
                    manager = Sam2EnvManager.create(environmentPath, "tiny");
                    break;
            }

            if (!manager.checkEverythingInstalled())
                if (installIfMissing) {
                    module.writeStatus("Installing SAM model");
                    manager.installEverything();
                } else {
                    MIA.log.writeWarning(modelName + " not available.  Please install manually or enable \""
                            + INSTALL_IF_MISSING + "\" parameter.");
                }

            efficientSamJ = EfficientSamJ.initializeSam(manager);
            efficientSamJ.setImage(inputImage.getImgPlus());

        } catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException
                | MambaInstallException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialiseAfterImageShown(@Nullable ImagePlus displayIpl) {
        // Getting parameters
        boolean useSAM = parameters.getValue(USE_SAM, workspace);
        if (!useSAM)
            return;
            
        this.displayIpl = displayIpl;
        displayIpl.getCanvas().addMouseListener(this);
    }

    @Override
    public Parameters initialiseParameters() {
        parameters.add(new SeparatorP(SAMJ_SEPARATOR, module));
        parameters.add(new BooleanP(USE_SAM, module, false));
        parameters.add(new ChoiceP(MODEL, module, SAMJModels.EFFICIENT, SAMJModels.ALL));
        parameters.add(
                new ChoiceP(ENVIRONMENT_PATH_MODE, module, EnvironmentPathModes.DEFAULT, EnvironmentPathModes.ALL));
        parameters.add(new FolderPathP(ENVIRONMENT_PATH, module));
        parameters.add(new BooleanP(INSTALL_IF_MISSING, module, true));

        return parameters;

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(SAMJ_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_SAM));

        if ((boolean) parameters.getValue(USE_SAM, workspace)) {
            returnedParameters.add(parameters.getParameter(MODEL));
            returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH_MODE));

            switch ((String) parameters.getValue(ENVIRONMENT_PATH_MODE, workspace)) {
                case EnvironmentPathModes.SPECIFIC:
                    returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH));
                    break;
            }

            returnedParameters.add(parameters.getParameter(INSTALL_IF_MISSING));

        }

        return returnedParameters;

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (efficientSamJ == null) {
            MIA.log.writeWarning("SAMJ not ready yet");
            return;
        }

        int x = (int) Math.round(displayIpl.getRoi().getXBase());
        int y = (int) Math.round(displayIpl.getRoi().getYBase());
        ArrayList<int[]> pts = new ArrayList<>();
        pts.add(new int[] { x, y });

        try {
            efficientSamJ.persistEncoding();
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }

        try {
            List<Polygon> polygons = efficientSamJ.processPoints(pts);
            for (Polygon polygon : polygons) {
                PolygonRoi polygonRoi = new PolygonRoi(polygon, PolygonRoi.POLYGON);
                displayIpl.setRoi(polygonRoi);
            }
        } catch (IOException | InterruptedException | RuntimeException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
