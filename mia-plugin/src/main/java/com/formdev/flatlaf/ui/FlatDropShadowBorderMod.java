/*
 * Copyright 2020 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.formdev.flatlaf.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.formdev.flatlaf.ui.FlatStylingSupport.Styleable;
import com.formdev.flatlaf.ui.FlatStylingSupport.StyleableBorder;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.UIScale;

/**
 * Paints a drop shadow border around the component.
 * Supports 1-sided, 2-side, 3-sided or 4-sided drop shadows.
 * <p>
 * The shadow insets allow specifying drop shadow thickness for each side.
 * A zero or negative value hides the drop shadow on that side.
 * A negative value can be used to indent the drop shadow on corners.
 * E.g. -4 on left indents drop shadow at top-left and bottom-left corners by 4
 * pixels.
 *
 * @author Karl Tauber
 */
public class FlatDropShadowBorderMod
        extends FlatEmptyBorder
        implements StyleableBorder {
    @Styleable
    protected Color shadowColor;
    @Styleable
    protected Insets shadowInsets;
    @Styleable
    protected float shadowOpacity;

    private int shadowSize;
    private Image pointShadowImage;
    private Image arcShadowImage;
    private Color lastShadowColor;
    private float lastShadowOpacity;
    private int lastShadowSize;
    private double lastSystemScaleFactor;
    private float lastUserScaleFactor;
    private int arc;

    public FlatDropShadowBorderMod() {
        this(null);
    }

    public FlatDropShadowBorderMod(Color shadowColor) {
        this(shadowColor, 4, 0.5f);
    }

    public FlatDropShadowBorderMod(Color shadowColor, int shadowSize, float shadowOpacity) {
        this(shadowColor, new Insets(-shadowSize, -shadowSize, shadowSize, shadowSize), shadowOpacity, 0);
    }

    public FlatDropShadowBorderMod(Color shadowColor, int shadowSize, float shadowOpacity, int arc) {
        this(shadowColor, new Insets(-shadowSize, -shadowSize, shadowSize, shadowSize), shadowOpacity, arc);
    }

    public FlatDropShadowBorderMod(Color shadowColor, Insets shadowInsets, float shadowOpacity, int arc) {
        super(nonNegativeInsets(shadowInsets));

        this.shadowColor = shadowColor;
        this.shadowInsets = shadowInsets;
        this.shadowOpacity = shadowOpacity;
        this.arc = arc;

        shadowSize = maxInset(shadowInsets);

    }

    private static Insets nonNegativeInsets(Insets shadowInsets) {
        return new Insets(Math.max(shadowInsets.top, 0), Math.max(shadowInsets.left, 0),
                Math.max(shadowInsets.bottom, 0), Math.max(shadowInsets.right, 0));
    }

    private int maxInset(Insets shadowInsets) {
        return Math.max(
                Math.max(shadowInsets.left, shadowInsets.right),
                Math.max(shadowInsets.top, shadowInsets.bottom));
    }

    /** @since 2 */
    @Override
    public Object applyStyleProperty(String key, Object value) {
        Object oldValue = FlatStylingSupport.applyToAnnotatedObject(this, key, value);
        if (key.equals("shadowInsets")) {
            applyStyleProperty(nonNegativeInsets(shadowInsets));
            shadowSize = maxInset(shadowInsets);
        }
        return oldValue;
    }

    /** @since 2 */
    @Override
    public Map<String, Class<?>> getStyleableInfos() {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    /** @since 2.5 */
    @Override
    public Object getStyleableValue(String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (shadowSize <= 0)
            return;

        HiDPIUtils.paintAtScale1x((Graphics2D) g, x, y, width, height, this::paintImpl);
    }

    private void paintImpl(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        Color shadowColor = (this.shadowColor != null) ? this.shadowColor : g.getColor();
        int shadowSize = scale(this.shadowSize, scaleFactor);

        // create and cache shadow image
        float userScaleFactor = UIScale.getUserScaleFactor();
        if (pointShadowImage == null ||
                !shadowColor.equals(lastShadowColor) ||
                lastShadowOpacity != shadowOpacity ||
                lastShadowSize != shadowSize ||
                lastSystemScaleFactor != scaleFactor ||
                lastUserScaleFactor != userScaleFactor) {
            pointShadowImage = createShadowImage(shadowColor, shadowSize, shadowOpacity,
                    (float) (scaleFactor * userScaleFactor), 0);
            arcShadowImage = createShadowImage(shadowColor, shadowSize, shadowOpacity,
                    (float) (scaleFactor * userScaleFactor), arc);
            lastShadowColor = shadowColor;
            lastShadowOpacity = shadowOpacity;
            lastShadowSize = shadowSize;
            lastSystemScaleFactor = scaleFactor;
            lastUserScaleFactor = userScaleFactor;
        }

        /*
         * debug
         * int m = shadowImage.getWidth( null );
         * Color oldColor = g.getColor();
         * g.setColor( Color.lightGray );
         * g.drawRect( x - m - 1, y - m - 1, m + 1, m + 1 );
         * g.setColor( Color.white );
         * g.fillRect( x - m, y - m, m, m );
         * g.drawImage( shadowImage, x - m, y - m, null );
         * g.setColor( oldColor );
         * debug
         */

        int left = Math.max(0,scale(shadowInsets.left, scaleFactor));
        int right = Math.max(0,scale(shadowInsets.right, scaleFactor));
        int top = Math.max(0,scale(shadowInsets.top, scaleFactor));
        int bottom = Math.max(0,scale(shadowInsets.bottom, scaleFactor));

        // shadow outer coordinates
        int x1o = x - Math.min(left, 0);
        int y1o = y - Math.min(top, 0);
        int x2o = x + width + Math.min(right, 0);
        int y2o = y + height + Math.min(bottom, 0);

        // shadow inner coordinates
        int x1i = x1o + shadowSize;
        int y1i = y1o + shadowSize;
        int x2i = x2o - shadowSize;
        int y2i = y2o - shadowSize;

        int pointWh = (shadowSize * 2) - 1;
        int pointCenter = shadowSize - 1;
        int arcWh = ((shadowSize + arc) * 2) - 1;
        int arcCenter = shadowSize + arc - 1;

        // left-top edge
        if (left > 0 && top > 0) {
            g.drawImage(arcShadowImage, x1o, y1o, x1i+arc, y1i+arc,
                    0, 0, arcCenter, arcCenter, null);
        } else if (left > 0 || top == 0) {
            g.drawImage(pointShadowImage, x1o, y1o+arc, x1i, y1i+arc,
                    0, 0, pointCenter, pointCenter, null);
        } else if (left == 0 || top > 0) {
            g.drawImage(pointShadowImage, x1o+arc, y1o, x1i+arc, y1i,
                    0, 0, pointCenter, pointCenter, null);
        }

        // top shadow
        if (top > 0) {
            g.drawImage(pointShadowImage, x1i + arc, y1o, x2i - arc, y1i,
                    pointCenter, 0, pointCenter + 1, pointCenter, null);
        }

        // right-top edge
        if (right > 0 && top > 0) {
            g.drawImage(arcShadowImage, x2i-arc, y1o, x2o, y1i+arc,
                    arcCenter, 0, arcWh, arcCenter, null);
        } else if (right > 0 && top == 0) {
            g.drawImage(pointShadowImage, x2i, y1o+arc, x2o, y1i+arc,
                    pointCenter, 0, pointWh, pointCenter, null);
        } else if (right == 0 && top > 0) {
            g.drawImage(pointShadowImage, x2i-arc, y1o, x2o-arc, y1i,
                    pointCenter, 0, pointWh, pointCenter, null);
        }

        // left shadow
        if (left > 0) {
            g.drawImage(pointShadowImage, x1o, y1i + arc, x1i, y2i - arc,
                    0, pointCenter, pointCenter, pointCenter + 1, null);
        }

        // right shadow
        if (right > 0) {
            g.drawImage(pointShadowImage, x2i, y1i + arc, x2o, y2i - arc,
                    pointCenter, pointCenter, pointWh, pointCenter + 1, null);
        }

        // left-bottom edge
        if (left > 0 && bottom > 0) {
            g.drawImage(arcShadowImage, x1o, y2i - arc, x1i + arc, y2o,
                    0, arcCenter, arcCenter, arcWh, null);
        } else if (left > 0 && bottom == 0) {
            g.drawImage(pointShadowImage, x1o, y2i-arc, x1i, y2o-arc,
                    0, pointCenter, pointCenter, pointWh, null);
        } else if (left == 0 && bottom > 0) {
            g.drawImage(pointShadowImage, x1o+arc, y2i, x1i+arc, y2o,
                    0, pointCenter, pointCenter, pointWh, null);
        }

        // bottom shadow
        if (bottom > 0) {
            g.drawImage(pointShadowImage, x1i + arc, y2i, x2i - arc, y2o,
                    pointCenter, pointCenter, pointCenter + 1, pointWh, null);
        }

        // right-bottom edge
        if (right > 0 && bottom > 0) {
            g.drawImage(arcShadowImage, x2i - arc, y2i - arc, x2o, y2o,
                    arcCenter, arcCenter, arcWh, arcWh, null);
        } else if (right > 0 && bottom == 0) {
            g.drawImage(pointShadowImage, x2i, y2i-arc, x2o, y2o-arc,
                    pointCenter, pointCenter, pointWh, pointWh, null);
        } else if (right == 0 && bottom > 0) {
            g.drawImage(pointShadowImage, x2i-arc, y2i, x2o-arc, y2o,
                    pointCenter, pointCenter, pointWh, pointWh, null);
        }
    }

    private int scale(int value, double scaleFactor) {
        return (int) Math.ceil(UIScale.scale(value) * scaleFactor);
    }

    private static BufferedImage createShadowImage(Color shadowColor, int shadowSize,
            float shadowOpacity, float scaleFactor, int arc) {
        shadowSize = shadowSize + arc;
        int shadowRGB = shadowColor.getRGB() & 0xffffff;
        int shadowAlpha = (int) (255 * shadowOpacity);
        Color startColor = new Color(shadowRGB | ((shadowAlpha & 0xff) << 24), true);
        Color midColor = new Color(shadowRGB | (((shadowAlpha / 2) & 0xff) << 24), true);
        Color endColor = new Color(shadowRGB, true);

        /*
         * debug
         * startColor = Color.red;
         * midColor = Color.green;
         * endColor = Color.blue;
         * debug
         */

        int wh = (shadowSize * 2) - 1;
        int center = shadowSize - 1;

        RadialGradientPaint p;
        if (arc == 0) {
            p = new RadialGradientPaint(center, center,
                    shadowSize - (0.75f * scaleFactor),
                    new float[] { 0, 0.35f, 1 },
                    new Color[] { startColor, midColor, endColor });
        } else {
            float arcEdge = (float) arc / (float) shadowSize;
            float gradientMid = arcEdge + (1 - arcEdge) * 0.35f;

            p = new RadialGradientPaint(center, center,
                    shadowSize - (0.75f * scaleFactor),
                    new float[] { 0, arcEdge, arcEdge + 0.0000001f, gradientMid, 1 },
                    new Color[] { endColor, endColor, startColor, midColor, endColor });
        }

        BufferedImage image = new BufferedImage(wh, wh, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        try {
            g.setPaint(p);
            g.fillRect(0, 0, wh, wh);
        } finally {
            g.dispose();
        }

        return image;
    }
}