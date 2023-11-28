package io.github.mianalysis.mia.object.image.renderer;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImgPlusImage;
import net.imagej.ImgPlus;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class ImgPlusRenderer implements ImageRenderer {

    @Override
    public void render(Image image, String title, LUT lut, boolean normalise, boolean composite, Overlay overlay) {
        ImagePlus ipl = ImageJFunctions.show(image.getImgPlus());
        // Adds the specified overlay rather than the overlay associated with this image
        // ImagePlus ipl = image.getImagePlus();
        ImgPlus img = image.getImgPlus();

        if (lut != null && ipl.getBitDepth() != 24)
            ipl.setLut(lut);

        if (composite && ipl.getNChannels() > 1)
            ipl.setDisplayMode(CompositeImage.COMPOSITE);
        else
            ipl.setDisplayMode(CompositeImage.COLOR);

        ImgPlusImage.setCalibration(ipl, img);
        ipl.setOverlay(overlay);

        ipl.setTitle(title);

        ipl.show();

    }
}
