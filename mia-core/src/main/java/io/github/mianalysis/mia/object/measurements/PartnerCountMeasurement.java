package io.github.mianalysis.mia.object.measurements;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.ObjI;

public class PartnerCountMeasurement extends Measurement {
    private ObjI obj;
    private String partnerName;

    public PartnerCountMeasurement(String name, ObjI obj, String partnerName) {
        super(name);
        this.obj = obj;
        this.partnerName = partnerName;
    }
    
    public double getValue() {
        ObjsI partners = obj.getPartners(partnerName);

        if (partners == null)
            return 0;

        return partners.size();

    }
}
