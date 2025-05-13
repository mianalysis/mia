package io.github.mianalysis.mia.object.image.renderer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.drew.lang.annotations.Nullable;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.Duplicator;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.process.imagej.IntensityMinMax;

public class ImagePlusRenderer implements ImageRenderer {
    @Override
    public void render(Image image, String title, @Nullable LUT lut, boolean normalise, String displayMode,
            Overlay overlay) {
        ImagePlus imagePlus = image.getImagePlus();

        // Adds the specified overlay rather than the overlay associated with this image
        ImagePlus dispIpl = new Duplicator().run(imagePlus);
        dispIpl.setOverlay(overlay);
        dispIpl.setTitle(title);
        if (normalise)
            IntensityMinMax.run(dispIpl, true);

        dispIpl.setPosition(1, 1, 1);
        dispIpl.updateChannelAndDraw();
        if (lut != null && dispIpl.getBitDepth() != 24)
            dispIpl.setLut(lut);

        switch (displayMode) {
            case Image.DisplayModes.COLOUR:
                dispIpl.setDisplayMode(CompositeImage.COLOR);
                break;

            case Image.DisplayModes.COMPOSITE:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Sum");
                break;

            case Image.DisplayModes.COMPOSITE_INVERT:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Invert");
                break;

            case Image.DisplayModes.COMPOSITE_MAX:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Max");
                break;

            case Image.DisplayModes.COMPOSITE_MIN:
                dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
                dispIpl.setProp("CompositeProjection", "Min");
                break;
        }

        dispIpl.repaintWindow();
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
