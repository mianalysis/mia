package wbif.sjx.MIA.Object.References;

import java.util.TreeSet;

import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

public class PartnerRefCollection extends TreeSet<PartnerRef> implements RefCollection<PartnerRef> {
    /**
     *
     */
    private static final long serialVersionUID = 5754666524686556727L;

    @Override
    public TreeSet<PartnerRef> values() {
        return this;
    }

    public PartnerRef getOrPut(String object1Name, String object2Name) {
        // Check if this reference is present
        for (PartnerRef ref : this) {
            if (ref.getObject1Name().equals(object1Name) && ref.getObject2Name().equals(object2Name)
                    || ref.getObject1Name().equals(object2Name) && ref.getObject2Name().equals(object1Name)) {
                return ref;
            }
        }

        // If no reference was found, create a new one
        PartnerRef ref = new PartnerRef(object1Name, object2Name);
        add(ref);

        // Return new reference
        return ref;

    }
    
    public TreeSet<String> getPartnerNames(String objectName) {
        TreeSet<String> partners = new TreeSet<>();

        // Iterating over all references.  If one of the names matches the input, adding its partner to the set
        for (PartnerRef ref : this) {
            if (ref.getObject1Name().equals(objectName)) {
                partners.add(ref.getObject2Name());
            } else if (ref.getObject2Name().equals(objectName)) {
                partners.add(ref.getObject1Name());
            }
        }

        return partners;

    }

    public String[] getPartnerNamesArray(String objectName) {
        TreeSet<String> partnerNames = getPartnerNames(objectName);

        return partnerNames.toArray(new String[0]);

    }
}
