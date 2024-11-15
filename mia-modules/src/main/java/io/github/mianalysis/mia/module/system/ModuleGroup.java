// package io.github.mianalysis.mia.module.system;

// import java.io.File;
// import java.util.Arrays;
// import java.util.LinkedHashMap;

// import org.apache.commons.lang.SystemUtils;
// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ij.IJ;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.parameters.BooleanP;
// import io.github.mianalysis.mia.object.parameters.ChoiceP;
// import io.github.mianalysis.mia.object.parameters.ParameterGroup;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.RemovedImageP;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;

// /**
//  * Created by Stephen Cross on 12/11/2024.
//  */

// /**
//  * 
//  */
// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class ModuleGroup extends Module {

//     public static final String MODULE_GROUP_SEPARATOR = "Module group selection";
//     public static final String MODULE_GROUP = "Module group";

//     private static String moduleGroupPath = "";

//     public ModuleGroup(Modules modules) {
//         super("Module group", modules);
//     }

//     public static void setDefaultPath() {
//         if (MIA.isDebug())
//             if (SystemUtils.OS_NAME.equals("Mac OS X"))
//                 moduleGroupPath = "/Users/sc13967/Applications/Fiji.app/modulegroups/";
//             else
//                 // return "C:\\Users\\steph\\Programs\\Fiji.app\\models\\";
//                 moduleGroupPath = "C:\\Users\\sc13967\\Desktop\\Fiji.app\\modulegroups\\";
//         else
//             moduleGroupPath = IJ.getDirectory("imagej") + File.separator + "modulegroups" + File.separator;

//     }

//     public static String[] getAvailableModuleGroupNames(String moduleGroupPath) {
//         File moduleGroupFolder = new File(moduleGroupPath);
//         if (!moduleGroupFolder.exists()) {
//             MIA.log.writeWarning("Can't find module group path \""+moduleGroupPath+"\"");
//             return new String[0];
//         }

//         if (!moduleGroupFolder.isDirectory()) {
//             MIA.log.writeWarning("Module group path \""+moduleGroupPath+"\" isn't a directory");
//             return new String[0];
//         }

//         return (String[]) Arrays.stream(moduleGroupFolder.listFiles()).map(v -> v.getName()).toArray();

//     }

//     @Override
//     public Category getCategory() {
//         return Categories.SYSTEM;
//     }

//     @Override
//     public String getVersionNumber() {
//         return "1.0.0";
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     @Override
//     public Status process(Workspace workspace) {
//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         String[] moduleGroupNames = getAvailableModuleGroupNames(moduleGroupPath);
//         parameters.add(new SeparatorP(MODULE_GROUP_SEPARATOR, this));
//         parameters.add(new ChoiceP(MODULE_GROUP, this, moduleGroupNames[0], moduleGroupNames));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         return parameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }
