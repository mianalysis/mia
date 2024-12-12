package io.github.mianalysis.mia.object.image.renderer;

import com.drew.lang.annotations.Nullable;

import ij.gui.Overlay;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.ImageI;

public interface ImageRenderer {
    public void render(ImageI image, String title, @Nullable LUT lut, boolean normalise, String displayMode, Overlay overlay);
}
