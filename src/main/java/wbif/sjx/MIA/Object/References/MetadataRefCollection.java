package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataRefCollection extends TreeMap<String,MetadataRef> implements RefCollection<MetadataRef>, Serializable {
    public String[] getMetadataNames() {
        return keySet().toArray(new String[0]);
    }

    public MetadataRef getOrPut(Object key) {
        putIfAbsent((String) key, new MetadataRef((String) key));
        return super.get(key);
    }

    public boolean add(MetadataRef metadataRef) {
        put(metadataRef.getName(), metadataRef);
        return true;
    }

    public boolean hasRef(String string) {
        Pattern pattern = Pattern.compile("\\$\\{([^${}]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String metadataName = matcher.group(1);

            boolean found = false;
            for (String name:keySet()) {
                if (name.equals(metadataName)) {
                    found = true;
                    break;
                }
            }

            // If the current reference wasn't found, return false
            if (!found) return false;

        }

        return true;

    }
}