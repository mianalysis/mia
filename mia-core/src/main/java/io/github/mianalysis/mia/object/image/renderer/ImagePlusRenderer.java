package io.github.mianalysis.mia.object.image.renderer;

import com.drew.lang.annotations.Nullable;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.Duplicator;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.process.imagej.IntensityMinMax;

public class ImagePlusRenderer implements ImageRenderer {
    @Override
    public void render(ImageI image, String title, @Nullable LUT lut, boolean normalise, String displayMode,
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

        dispIpl.repaintWindow();
        dispIpl.show();

    }
}
