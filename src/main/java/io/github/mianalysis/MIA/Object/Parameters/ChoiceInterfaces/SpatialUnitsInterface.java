package io.github.mianalysis.MIA.Object.Parameters.ChoiceInterfaces;

import io.github.mianalysis.MIA.Module.Core.InputControl;

public interface SpatialUnitsInterface {
    String CALIBRATED = "Calibrated";
    String PIXELS = "Pixel/slice";

    String[] ALL = new String[] { CALIBRATED, PIXELS };

    public static String getDescription() {
        return "Controls whether spatial values are assumed to be specified in calibrated units (as defined by the \""
        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
        + "\") or pixel units.";
    }
}