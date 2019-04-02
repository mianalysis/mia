package wbif.sjx.MIA.Object;

import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

public class Units {
    private static Unit<Length> selectedUnit = UNITS.MICROMETRE;

    public interface SpatialUnits {
        String METRE = "METRE";
        String CENTIMETRE = "CENTIMETRE";
        String MILLIMETRE = "MILLIMETRE";
        String MICROMETRE = "MICROMETRE";
        String NANOMETRE = "NANOMETRE";
        String ANGSTROM = "ANGSTROM";

        String[] ALL = new String[]{METRE, CENTIMETRE, MILLIMETRE, MICROMETRE, NANOMETRE, ANGSTROM};

    }

    public static String replace(String string) {
        return string.replace("${CAL}",selectedUnit.getSymbol());
    }

    public static Unit<Length> getOMEUnits() {
        return selectedUnit;
    }

    public static void setUnits(String units) {
        switch (units) {
            case SpatialUnits.METRE:
                selectedUnit = UNITS.METRE;
                break;
            case SpatialUnits.CENTIMETRE:
                selectedUnit = UNITS.CENTIMETRE;
                break;
            case SpatialUnits.MILLIMETRE:
                selectedUnit = UNITS.MILLIMETER;
                break;
            case SpatialUnits.MICROMETRE:
                selectedUnit = UNITS.MICROMETER;
                break;
            case SpatialUnits.NANOMETRE:
                selectedUnit = UNITS.NANOMETRE;
                break;
            case SpatialUnits.ANGSTROM:
                selectedUnit = UNITS.ANGSTROM;
                break;

        }
    }
}
