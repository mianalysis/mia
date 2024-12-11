package io.github.mianalysis.mia.gui.svg;

import java.awt.Color;
import java.awt.Paint;

import com.drew.lang.annotations.NotNull;
import com.github.weisj.jsvg.attributes.paint.SimplePaintSVGPaint;

public class DynamicAWTSvgPaint implements SimplePaintSVGPaint {

    private @NotNull Color color;

    DynamicAWTSvgPaint(@NotNull Color color) {
        this.color = color;
    }

    public void setColor(@NotNull Color color) {
        this.color = color;
    }

    public @NotNull Color color() {
        return color;
    }

    @Override
    public @NotNull Paint paint() {
        return color;
    }
}
