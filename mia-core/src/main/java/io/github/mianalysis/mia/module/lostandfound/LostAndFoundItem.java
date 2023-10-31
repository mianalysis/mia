package io.github.mianalysis.mia.module.lostandfound;

import java.util.HashMap;

import org.scijava.plugin.SciJavaPlugin;

public abstract class LostAndFoundItem implements SciJavaPlugin {
    public abstract String getModuleName();

    public abstract String[] getPreviousModuleNames();

    public abstract HashMap<String, String> getPreviousParameterNames();

    public abstract HashMap<String, HashMap<String, String>> getPreviousParameterValues();

}
