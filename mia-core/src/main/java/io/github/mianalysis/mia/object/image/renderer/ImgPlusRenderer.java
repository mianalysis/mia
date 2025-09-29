package io.github.mianalysis.mia.object.image.renderer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImgPlusImage;
import net.imagej.ImgPlus;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class ImgPlusRenderer implements ImageRenderer {

    @Override
    public void render(ImageI image, String title, LUT lut, boolean normalise, String displayMode, Overlay overlay) {
        ImagePlus dispIpl = ImageJFunctions.show(image.getImgPlus());
        
        // Adds the specified overlay rather than the overlay associated with this image
        // ImagePlus ipl = image.getImagePlus();
        ImgPlus img = image.getImgPlus();

        if (lut != null && dispIpl.getBitDepth() != 24)
            dispIpl.setLut(lut);

        switch (displayMode) {
            case ImageI.DisplayModes.COLOUR:
                dispIpl.setDisplayMode(CompositeImage.COLOR);
                break;

            case ImageI.DisplayModes.COMPOSITE:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Sum");
                break;

            case ImageI.DisplayModes.COMPOSITE_INVERT:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Invert");
                break;

            case ImageI.DisplayModes.COMPOSITE_MAX:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Max");
                break;

            case ImageI.DisplayModes.COMPOSITE_MIN:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Min");
                break;
        }

        ImgPlusImage.setCalibration(dispIpl, img);
        dispIpl.setOverlay(overlay);
        dispIpl.setTitle(title);

        dispIpl.show();

        dispIpl.getWindow().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                String keyText = e.getKeyText(e.getKeyCode());

                switch (keyText) {
                    case "F12":
                        dispIpl.toggleOverlay();
                        return;    
                    }
                }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

    }
}
