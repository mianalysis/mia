package io.github.mianalysis.mia.lostandfound.inputoutput;

import java.util.HashMap;

import io.github.mianalysis.mia.module.inputoutput.MetadataExtractor;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class MetadataExtractorLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new MetadataExtractor(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Keyword list", "");
        parameterNames.put("Keyword source", "");
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("Cell Voyager filename", MetadataExtractor.FilenameExtractors.CV1000_FILENAME_EXTRACTOR);
        values.put("Yokogawa filename", MetadataExtractor.FilenameExtractors.CV1000_FILENAME_EXTRACTOR);
        parameterValues = new HashMap<>();
        parameterValues.put(MetadataExtractor.FILENAME_EXTRACTOR, values);

        values = new HashMap<>();
        values.put("Cell Voyager foldername",
                MetadataExtractor.FoldernameExtractors.CV1000_FOLDERNAME_EXTRACTOR);
        parameterValues.put(MetadataExtractor.FILENAME_EXTRACTOR, values);

        return parameterValues;
    
    }
}
