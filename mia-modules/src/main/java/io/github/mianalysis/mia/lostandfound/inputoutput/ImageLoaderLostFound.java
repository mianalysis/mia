package io.github.mianalysis.mia.lostandfound.inputoutput;

import java.util.HashMap;

import io.github.mianalysis.mia.module.inputoutput.ImageLoader;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class ImageLoaderLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ImageLoader(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = new HashMap<>();

        values = new HashMap<>();
        values.put("Image sequence", ImageLoader.ImportModes.IMAGE_SEQUENCE_ZEROS);
        parameterValues.put(ImageLoader.IMPORT_MODE, values);

        values = new HashMap<>();
        values.put("BioFormats", ImageLoader.Readers.BIOFORMATS);
        parameterValues.put(ImageLoader.READER, values);

        return parameterValues;

    }
    
}
