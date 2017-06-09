package wbif.sjx.ModularImageAnalysis.Extractor;

import wbif.sjx.ModularImageAnalysis.Object.HCMetadata;

/**
 * Created by steph on 30/04/2017.
 */
public interface Extractor {
    String getName();
    String getPattern();
    boolean extract(HCMetadata result, String str);

}
