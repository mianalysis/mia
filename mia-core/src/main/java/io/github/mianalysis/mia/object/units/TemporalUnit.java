package io.github.mianalysis.mia.object.units;

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

    public static Unit<Time> getOMEUnit(String unit) {
        switch (unit) {
            default:
                return null;

            case "ns":
            case "nanosecond":
            case "nanoseconds":
                return UNITS.NANOSECOND;

            case "ms":
            case "millisecond":
            case "milliseconds":
                return UNITS.MILLISECOND;

            case "m":
            case "min":
            case "mins":
            case "minute":
            case "minutes":
                return UNITS.MINUTE;

            case "h":
            case "hour":
            case "hours":
            return UNITS.HOUR;

            case "d":
            case "day":
            case "days":
                return UNITS.DAY;

            case "s":
            case "sec":
            case "secs":
            case "second":
            case "seconds":
                return UNITS.SECOND;

        }
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
