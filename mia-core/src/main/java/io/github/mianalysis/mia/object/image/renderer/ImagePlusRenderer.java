package io.github.mianalysis.mia.object.image.renderer;

import com.drew.lang.annotations.Nullable;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.Duplicator;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.IntensityMinMax;

public class ImagePlusRenderer implements ImageRenderer {
    @Override
    public void render(Image image, String title, @Nullable LUT lut, boolean normalise, boolean composite, Overlay overlay) {
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

        if (composite && dispIpl.getNChannels() > 1)
            dispIpl.setDisplayMode(CompositeImage.COMPOSITE);
        else
            dispIpl.setDisplayMode(CompositeImage.COLOR);

        dispIpl.repaintWindow();
        dispIpl.show();
        
    }    
}
