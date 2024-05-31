/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2010 - 2024 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package io.github.mianalysis.mia.module.objects.relate.trackmate.tracking;

import static fiji.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeAttribute;
import static fiji.plugin.trackmate.util.TMUtils.checkParameter;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.gui.components.tracker.OverlapTrackerSettingsPanel;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import io.github.mianalysis.mia.object.Objs;

@Plugin(type = SpotTrackerFactory.class)
public class OverlapTracker3DFactory implements SpotTrackerFactory {

    final static String BASE_ERROR_MESSAGE = "[IoUTracker] ";

    /**
     * The key to the parameter that stores the minimal IoU below which links
     * are not created. Values are strictly positive {@link Double}s.
     */
    public static final String KEY_MIN_IOU = "MIN_IOU";

    public static final String KEY_MIA_OBJECTS = "MIA_OBJECTS";

    public static final Double DEFAULT_MIN_IOU = Double.valueOf(0.3);

    public static final String TRACKER_KEY = "OVERLAP_TRACKER";

    public static final String TRACKER_NAME = "Overlap tracker";

    public static final String TRACKER_INFO_TEXT = "";

    private String errorMessage;

    @Override
    public String getKey() {
        return TRACKER_KEY;
    }

    @Override
    public String getName() {
        return TRACKER_NAME;
    }

    @Override
    public String getInfoText() {
        return TRACKER_INFO_TEXT;
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public SpotTracker create(final SpotCollection spots, final Map<String, Object> settings) {
        final double minIoU = (Double) settings.get(KEY_MIN_IOU);
        final Objs objs = (Objs) settings.get(KEY_MIA_OBJECTS);
        return new OverlapTracker3D(spots, objs, minIoU);
    }

    @Override
    public ConfigurationPanel getTrackerConfigurationPanel(final Model model) {
        return new OverlapTrackerSettingsPanel();
    }

    @Override
    public boolean marshall(final Map<String, Object> settings, final Element element) {
        boolean ok = true;
        final StringBuilder str = new StringBuilder();

        ok = ok & writeAttribute(settings, element, KEY_MIN_IOU, Double.class, str);
        return ok;
    }

    @Override
    public boolean unmarshall(final Element element, final Map<String, Object> settings) {
        settings.clear();
        final StringBuilder errorHolder = new StringBuilder();
        boolean ok = true;

        ok = ok & readDoubleAttribute(element, settings, KEY_MIN_IOU, errorHolder);
        return ok;
    }

    @Override
    public String toString(final Map<String, Object> settings) {
        if (!checkSettingsValidity(settings))
            return errorMessage;

        final double minIoU = (Double) settings.get(KEY_MIN_IOU);

        final StringBuilder str = new StringBuilder();

        str.append(String.format("  - min. IoU: %.2f\n", minIoU));
        return str.toString();
    }

    @Override
    public Map<String, Object> getDefaultSettings() {
        final Map<String, Object> settings = new HashMap<>();
        settings.put(KEY_MIN_IOU, DEFAULT_MIN_IOU);
        return settings;
    }

    @Override
    public boolean checkSettingsValidity(final Map<String, Object> settings) {
        if (null == settings) {
            errorMessage = BASE_ERROR_MESSAGE + "Settings map is null.\n";
            return false;
        }

        boolean ok = true;
        final StringBuilder str = new StringBuilder();
        ok = ok & checkParameter(settings, KEY_MIN_IOU, Double.class, str);
        if (!ok) {
            errorMessage = str.toString();
            return false;

        }

        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public OverlapTracker3DFactory copy() {
        return new OverlapTracker3DFactory();
    }
}