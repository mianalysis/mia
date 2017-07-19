package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.common.Object.HCMetadata;

import java.util.Comparator;

/**
 * Created by steph on 01/05/2017.
 */
public class StackComparator implements Comparator<HCMetadata> {
    private String field = HCMetadata.ZPOSITION;


    // PUBLIC METHODS

    @Override
    public int compare(HCMetadata result1, HCMetadata result2) {
        double z1 = Double.parseDouble(result1.getAsString(field));
        double z2 = Double.parseDouble(result2.getAsString(field));

        return z1 < z2 ? -1 : z1 == z2 ? 0 : 1;

    }


    // GETTERS AND SETTERS

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
