package io.github.mianalysis.mia.object.image;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import net.imagej.ImgPlus;

public class ImagePlusImageFactory implements ImageFactoryI {

    @Override
    public String getName() {
        return "ImagePlus image factory";
    }

    @Override
    public ImageFactoryI duplicate() {
        return new ImagePlusImageFactory();
    }

    @Override
    public ImageI create(String name, Object rawImage) {
        if (rawImage instanceof ImagePlus)
            return new ImagePlusImage<>(name, (ImagePlus) rawImage);
        else if (rawImage instanceof ImgPlus)
            return new ImagePlusImage<>(name, (ImgPlus) rawImage);
        else {
            MIA.log.writeError("Can't convert "+rawImage+" to ImagePlusImage");
            return null;
        }
    }
}
