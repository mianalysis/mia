package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 21/02/2018.
 */
public class Objects2D extends ExpectedObjects {
    public enum Measures {
        PCC,ASM,CONTRAST,CORRELATION,ENTROPY
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Objects2D.csv");
    }

    @Override
    public boolean is2D() {
        return true;
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        HashMap<Integer,HashMap<String,Double>> expectedValues = new HashMap<>();

        HashMap<String,Double> obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.4);
        obj.put(Measures.ASM.name(),0.005232379);
        obj.put(Measures.CONTRAST.name(),230.3947368);
        obj.put(Measures.CORRELATION.name(),0.373613729);
        obj.put(Measures.ENTROPY.name(),7.650829597);
        expectedValues.put(3,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),Double.NaN); // Can't calculate from a single value object
        obj.put(Measures.ASM.name(),0d);
        obj.put(Measures.CONTRAST.name(),0d);
        obj.put(Measures.CORRELATION.name(),Double.NaN);
        obj.put(Measures.ENTROPY.name(),0d);
        expectedValues.put(4,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.43);
        obj.put(Measures.ASM.name(),0.00305427);
        obj.put(Measures.CONTRAST.name(),207.4351852);
        obj.put(Measures.CORRELATION.name(),0.684526481);
        obj.put(Measures.ENTROPY.name(),8.480514065);
        expectedValues.put(7,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.04);
        obj.put(Measures.ASM.name(),0.011880165);
        obj.put(Measures.CONTRAST.name(),317.3409091);
        obj.put(Measures.CORRELATION.name(),0.512108697);
        obj.put(Measures.ENTROPY.name(),6.413977073);
        expectedValues.put(8,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),Double.NaN);
        obj.put(Measures.ASM.name(),0.083333333);
        obj.put(Measures.CONTRAST.name(),354d);
        obj.put(Measures.CORRELATION.name(),-0.486354094);
        obj.put(Measures.ENTROPY.name(),3.584962501);
        expectedValues.put(9,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),-0.25);
        obj.put(Measures.ASM.name(),0.004736);
        obj.put(Measures.CONTRAST.name(),215.688);
        obj.put(Measures.CORRELATION.name(),0.628963877);
        obj.put(Measures.ENTROPY.name(),7.781784285);
        expectedValues.put(13,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),0.55);
        obj.put(Measures.ASM.name(),0d);
        obj.put(Measures.CONTRAST.name(),0.005232379);
        obj.put(Measures.CONTRAST.name(),0d);
        obj.put(Measures.CORRELATION.name(),Double.NaN);
        obj.put(Measures.ENTROPY.name(),0d);
        expectedValues.put(20,obj);

        obj = new HashMap<>();
        obj.put(Measures.PCC.name(),Double.NaN);
        obj.put(Measures.ASM.name(),0d);
        obj.put(Measures.CONTRAST.name(),0d);
        obj.put(Measures.CORRELATION.name(),Double.NaN);
        obj.put(Measures.ENTROPY.name(),0d);
        expectedValues.put(23,obj);

        return expectedValues;
    }
}
