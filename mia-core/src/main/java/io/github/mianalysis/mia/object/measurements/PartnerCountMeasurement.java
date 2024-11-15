package io.github.mianalysis.mia.object.measurements;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;

public class PartnerCountMeasurement extends Measurement {
    private Obj obj;
    private String partnerName;

    public PartnerCountMeasurement(String name, Obj obj, String partnerName) {
        super(name);
        this.obj = obj;
        this.partnerName = partnerName;
    }
    
    public double getValue() {
        Objs partners = obj.getPartners(partnerName);

        if (partners == null)
            return 0;

        return partners.size();

    }
}
