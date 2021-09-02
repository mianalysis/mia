package io.github.mianalysis.mia.Object.Units;

import ome.units.UNITS;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

public class TemporalUnit {
    private static Unit<Time> selectedUnit = UNITS.SECOND;

    public interface AvailableUnits {
        String NANOSECOND = "Nanoseconds";
        String MILLISECOND = "Milliseconds";
        String SECOND = "Seconds";
        String MINUTE = "Minutes";
        String HOUR = "Hours";
        String DAY = "Days";

        String[] ALL = new String[] { NANOSECOND, MILLISECOND, SECOND, MINUTE, HOUR, DAY };

    }

    public static String replace(String string) {
        return string.replace("${TCAL}", selectedUnit.getSymbol());
    }

    public static Unit<Time> getOMEUnit() {
        return selectedUnit;
    }

    public static void setUnit(String units) {
        switch (units) {
            case AvailableUnits.NANOSECOND:
                selectedUnit = UNITS.NANOSECOND;
                break;
            case AvailableUnits.MILLISECOND:
                selectedUnit = UNITS.MILLISECOND;
                break;
            case AvailableUnits.SECOND:
                selectedUnit = UNITS.SECOND;
                break;
            case AvailableUnits.MINUTE:
                selectedUnit = UNITS.MINUTE;
                break;
            case AvailableUnits.HOUR:
                selectedUnit = UNITS.HOUR;
                break;
            case AvailableUnits.DAY:
                selectedUnit = UNITS.DAY;
                break;
        }
    }
}
