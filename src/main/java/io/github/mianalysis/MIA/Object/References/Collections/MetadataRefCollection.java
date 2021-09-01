package io.github.mianalysis.MIA.Object.References.Collections;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mianalysis.MIA.Object.References.MetadataRef;

public class MetadataRefCollection extends TreeMap<String,MetadataRef> implements RefCollection<MetadataRef> {
    /**
     *
     */
    private static final long serialVersionUID = 952279509862406698L;

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
            for (String name : keySet()) {
                if (name.equals(metadataName)) {
                    found = true;
                    break;
                }
            }

            // If the current reference wasn't found, return false
            if (!found)
                return false;

        }

        return true;

    }
    
    public String getMetadataValues() {
        StringBuilder sb = new StringBuilder();

        sb.append("The following metadata values are available to use for generation of a filename string.  "
                + "Each metadata reference should include the \"M{\" and \"}\":\r\n\r\n");

        int count = 0;
        for (MetadataRef ref : values()) {
            sb.append("M{");
            sb.append(ref.getName());
            sb.append("}");
            if (++count < size())
                sb.append(", ");
        }

        sb.append(
                "\r\n\r\nWildcard character \"*\" is also available to match variable content (first matching instance will be loaded).\r\n");

        return sb.toString();

    }
}