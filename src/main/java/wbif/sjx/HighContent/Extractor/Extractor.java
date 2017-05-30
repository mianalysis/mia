package wbif.sjx.HighContent.Extractor;

import wbif.sjx.HighContent.Object.HCMetadata;

/**
 * Created by steph on 30/04/2017.
 */
public interface Extractor {
    String getName();
    String getPattern();
    boolean extract(HCMetadata result, String str);

}
