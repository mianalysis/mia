package io.github.mianalysis.mia.object.image.renderer;

import com.drew.lang.annotations.Nullable;

import ij.gui.Overlay;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.Image;

public interface ImageRenderer {
    public void render(Image image, String title, @Nullable LUT lut, boolean normalise, boolean composite, Overlay overlay);
}
