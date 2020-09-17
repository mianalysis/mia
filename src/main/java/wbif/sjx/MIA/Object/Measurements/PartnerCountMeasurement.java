package wbif.sjx.MIA.Object.Measurements;

import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;

public class PartnerCountMeasurement extends Measurement {
    private Obj obj;
    private String partnerName;

    public PartnerCountMeasurement(String name, Obj obj, String partnerName) {
        super(name);
        this.obj = obj;
        this.partnerName = partnerName;
    }
    
    public double getValue() {
        ObjCollection partners = obj.getPartners(partnerName);

        if (partners == null)
            return 0;

        return partners.size();

    }
}
