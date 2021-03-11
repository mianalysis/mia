package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

/**
 * Created by sc13967 on 21/02/2018.
 */
public class Objects2D extends ExpectedObjects {
    public Objects2D(VolumeType volumeType) {
        super(volumeType, 64, 76, 1, 1, 0.02, UNITS.SECOND);
    }

    public enum Measures {
        PCC, ASM_1PX, CONTRAST_1PX, CORRELATION_1PX, ENTROPY_1PX, ASM_3PX, CONTRAST_3PX, CORRELATION_3PX, ENTROPY_3PX
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Objects2D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        HashMap<Integer,HashMap<String,Double>> expectedValues = new HashMap<>();

        HashMap<String,Double> obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.4);
        obj.put(Measures.ASM_1PX.name(),0.005232379);
        obj.put(Measures.CONTRAST_1PX.name(),230.3947368);
        obj.put(Measures.CORRELATION_1PX.name(),0.373613729);
        obj.put(Measures.ENTROPY_1PX.name(),7.650829597);
        obj.put(Measures.ASM_3PX.name(),0.006469363);
        obj.put(Measures.CONTRAST_3PX.name(),348.5121951);
        obj.put(Measures.CORRELATION_3PX.name(),0.051144508);
        obj.put(Measures.ENTROPY_3PX.name(),7.296576395);
        expectedValues.put(3,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),Double.NaN); // Can't calculate from a single value object
        obj.put(Measures.ASM_1PX.name(),0d);
        obj.put(Measures.CONTRAST_1PX.name(),0d);
        obj.put(Measures.CORRELATION_1PX.name(),Double.NaN);
        obj.put(Measures.ENTROPY_1PX.name(),0d);
        obj.put(Measures.ASM_3PX.name(),0d);
        obj.put(Measures.CONTRAST_3PX.name(),0d);
        obj.put(Measures.CORRELATION_3PX.name(),Double.NaN);
        obj.put(Measures.ENTROPY_3PX.name(),0d);
        expectedValues.put(4,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.43);
        obj.put(Measures.ASM_1PX.name(),0.00305427);
        obj.put(Measures.CONTRAST_1PX.name(),207.4351852);
        obj.put(Measures.CORRELATION_1PX.name(),0.684526481);
        obj.put(Measures.ENTROPY_1PX.name(),8.480514065);
        obj.put(Measures.ASM_3PX.name(),0.003271605);
        obj.put(Measures.CONTRAST_3PX.name(),325d);
        obj.put(Measures.CORRELATION_3PX.name(),0.448661021);
        obj.put(Measures.ENTROPY_3PX.name(),8.32518643);
        expectedValues.put(7,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.04);
        obj.put(Measures.ASM_1PX.name(),0.011880165);
        obj.put(Measures.CONTRAST_1PX.name(),317.3409091);
        obj.put(Measures.CORRELATION_1PX.name(),0.512108697);
        obj.put(Measures.ENTROPY_1PX.name(),6.413977073);
        obj.put(Measures.ASM_3PX.name(),0.013888889);
        obj.put(Measures.CONTRAST_3PX.name(),353d);
        obj.put(Measures.CORRELATION_3PX.name(),0.433626741);
        obj.put(Measures.ENTROPY_3PX.name(),6.169925001);
        expectedValues.put(8,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),Double.NaN);
        obj.put(Measures.ASM_1PX.name(),0.083333333);
        obj.put(Measures.CONTRAST_1PX.name(),354d);
        obj.put(Measures.CORRELATION_1PX.name(),-0.486354094);
        obj.put(Measures.ENTROPY_1PX.name(),3.584962501);
        obj.put(Measures.ASM_3PX.name(),0d);
        obj.put(Measures.CONTRAST_3PX.name(),0d);
        obj.put(Measures.CORRELATION_3PX.name(),Double.NaN);
        obj.put(Measures.ENTROPY_3PX.name(),0d);
        expectedValues.put(9,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),-0.25);
        obj.put(Measures.ASM_1PX.name(),0.004736);
        obj.put(Measures.CONTRAST_1PX.name(),215.688);
        obj.put(Measures.CORRELATION_1PX.name(),0.628963877);
        obj.put(Measures.ENTROPY_1PX.name(),7.781784285);
        obj.put(Measures.ASM_3PX.name(),0.005278537);
        obj.put(Measures.CONTRAST_3PX.name(),355.3495146);
        obj.put(Measures.CORRELATION_3PX.name(),0.319105339);
        obj.put(Measures.ENTROPY_3PX.name(),7.599121886);
        expectedValues.put(13,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.55);
        obj.put(Measures.ASM_1PX.name(),0d);
        obj.put(Measures.CONTRAST_1PX.name(),0.005232379);
        obj.put(Measures.CONTRAST_1PX.name(),0d);
        obj.put(Measures.CORRELATION_1PX.name(),Double.NaN);
        obj.put(Measures.ENTROPY_1PX.name(),0d);
        obj.put(Measures.ASM_3PX.name(),0d);
        obj.put(Measures.CONTRAST_3PX.name(),0d);
        obj.put(Measures.CORRELATION_3PX.name(),Double.NaN);
        obj.put(Measures.ENTROPY_3PX.name(),0d);
        expectedValues.put(20,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),Double.NaN);
        obj.put(Measures.ASM_1PX.name(),0d);
        obj.put(Measures.CONTRAST_1PX.name(),0d);
        obj.put(Measures.CORRELATION_1PX.name(),Double.NaN);
        obj.put(Measures.ENTROPY_1PX.name(),0d);
        obj.put(Measures.ASM_3PX.name(),0d);
        obj.put(Measures.CONTRAST_3PX.name(),0d);
        obj.put(Measures.CORRELATION_3PX.name(),Double.NaN);
        obj.put(Measures.ENTROPY_3PX.name(),0d);
        expectedValues.put(23,obj);

        return expectedValues;

    }
}
