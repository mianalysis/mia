package io.github.mianalysis.mia.module.objects.detect.manualextensions;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ai.nets.samj.install.EfficientSamEnvManager;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.models.EfficientSamJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.objects.detect.ManuallyIdentifyObjects;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.Parameters;

@Plugin(type = ManuallyIdentifyObjectsExtension.class, priority = Priority.LOW, visible = true)
public class SAMJExtension extends ManuallyIdentifyObjectsExtension implements MouseListener {
    protected ManuallyIdentifyObjects module;
    protected Workspace workspace;
    protected EfficientSamJ efficientSamJ;
    protected ImagePlus displayIpl; 

    @Override
    public void initialiseBeforeImageShown(ManuallyIdentifyObjects module, Workspace workspace) {
        this.module = module;
        this.workspace = workspace;

        String inputImageName = module.getParameterValue(ManuallyIdentifyObjects.INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImage(inputImageName);

        efficientSamJ = null;

        AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
        AbstractSamJ.MAX_ENCODED_SIDE = 3000;
        
        try {
            EfficientSamEnvManager manager = EfficientSamEnvManager.create("C:\\Users\\sc13967\\Programs\\Fiji.app\\appose_x86_64\\"); 
            efficientSamJ = EfficientSamJ.initializeSam(manager);
            efficientSamJ.setImage(inputImage.getImgPlus());

        } catch (IOException | RuntimeException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialiseAfterImageShown(@Nullable ImagePlus displayIpl) {
        displayIpl.getCanvas().addMouseListener(this);
        
        try {
            EfficientSamEnvManager manager = EfficientSamEnvManager.create("C:\\Users\\sc13967\\Programs\\Fiji.app\\appose_x86_64\\"); 
            efficientSamJ = EfficientSamJ.initializeSam(manager);

        } catch (IOException | RuntimeException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Parameters getParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParameters'");
    }

    @Override
    public Parameters updateAndGetParameters() {
        throw new UnsupportedOperationException("Unimplemented method 'updateAndGetParameters'");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (efficientSamJ == null) {
            MIA.log.writeWarning("SAMJ not ready yet");
            return;
        }

        int x = (int) Math.round(displayIpl.getRoi().getXBase());
        int y = (int) Math.round(displayIpl.getRoi().getYBase());
        ArrayList<int[]> pts = new ArrayList<>();
        pts.add(new int[]{x,y});

        try {
            efficientSamJ.persistEncoding();
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
