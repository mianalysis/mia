package io.github.mianalysis.mia.object.units;

import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

public class SpatialUnit {
    private static Unit<Length> selectedUnit = UNITS.MICROMETRE;

    public interface AvailableUnits {
        String METRE = "Metres";
        String CENTIMETRE = "Centimetres";
        String MILLIMETRE = "Millimetres";
        String MICROMETRE = "Micrometres";
        String NANOMETRE = "Nanometres";
        String ANGSTROM = "Angstroms";

        String[] ALL = new String[] { METRE, CENTIMETRE, MILLIMETRE, MICROMETRE, NANOMETRE, ANGSTROM };

    }

    public static String replace(String string) {
        return string.replace("${SCAL}", selectedUnit.getSymbol());
    }

    public static Unit<Length> getOMEUnit() {
        return selectedUnit;
    }

    public static Unit<Length> getOMEUnit(String unit) {
        switch (unit) {
            default:
                return null;
                
            case "um":
            case "μm":
            case "µm":
            case "micron":
            case "microns":
                return UNITS.MICROMETER;
                
            case "mm":
            case "millimeter":
            case "millimeters":
            case "millimetre":
            case "millimetres":
                return UNITS.MILLIMETER;
                
            case "cm":
            case "centimeter":
            case "centimeters":
            case "centimetre":
            case "centimetres":
                return UNITS.CENTIMETER;
                
            case "nm":
            case "nanometer":
            case "nanometers":
            case "nanometre":
            case "nanometres":
                return UNITS.NANOMETER;
                
            case "A":
            case "Å":
            case "ang":
            case "angstrom":
            case "angstroms":
                return UNITS.ANGSTROM;
                
            case "m":
            case "meter":
            case "meters":
            case "metre":
            case "metres":
                return UNITS.METER;
            }
    }

    public static void setUnit(String units) {
        switch (units) {
            case AvailableUnits.METRE:
                selectedUnit = UNITS.METRE;
                break;
            case AvailableUnits.CENTIMETRE:
                selectedUnit = UNITS.CENTIMETRE;
                break;
            case AvailableUnits.MILLIMETRE:
                selectedUnit = UNITS.MILLIMETER;
                break;
            case AvailableUnits.MICROMETRE:
                selectedUnit = UNITS.MICROMETER;
                break;
            case AvailableUnits.NANOMETRE:
                selectedUnit = UNITS.NANOMETRE;
                break;
            case AvailableUnits.ANGSTROM:
                selectedUnit = UNITS.ANGSTROM;
                break;
        }
    }
}