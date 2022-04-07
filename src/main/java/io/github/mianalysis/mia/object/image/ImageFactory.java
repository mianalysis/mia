package io.github.mianalysis.mia.object.image;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.system.Preferences;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImageFactory {
    public static <T extends RealType<T> & NativeType<T>> Image<T> createImage(String name, ImagePlus imagePlus) {
        switch (MIA.preferences.getDataStorageMode()) {
            case Preferences.DataStorageModes.KEEP_IN_RAM:
            default:
                return new ImagePlusImage(name, imagePlus);
            case Preferences.DataStorageModes.STREAM_FROM_DRIVE:
                return new ImgPlusImage(name, imagePlus);
        }       
    }

    public static <T extends RealType<T> & NativeType<T>> Image<T> createImage(String name, ImgPlus img) {
        switch (MIA.preferences.getDataStorageMode()) {
            case Preferences.DataStorageModes.KEEP_IN_RAM:
            default:
                return new ImagePlusImage(name, img);
            case Preferences.DataStorageModes.STREAM_FROM_DRIVE:
                return new ImgPlusImage(name, img);
        }               
    }
}
