// package io.github.mianalysis.mia.module.objects.convert;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.ParameterGroup;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
// import io.github.mianalysis.mia.object.parameters.text.IntegerP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;

// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class ClassesToObjects extends Module {

//     public static final String INPUT_SEPARATOR = "Image input";

//     public static final String INPUT_IMAGE = "Input image";

//     public static final String OUTPUT_SEPARATOR = "Objects output";

//     public static final String ADD_OBJECTS = "Add output objects";

//     public static final String OUTPUT_OBJECTS = "Output objects";

//     public static final String CHANNEL = "Channel (>=1)";

//     public ClassesToObjects(Modules modules) {
//         super("Classes to objects", modules);
//     }

//     @Override
//     public Category getCategory() {
//         return Categories.OBJECTS_CONVERT;
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
//         // // Getting parameters
//         // String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         // ParameterGroup outputCollection = parameters.getParameter(ADD_OBJECTS);

//         // // Getting input image
//         // Image inputImage = workspace.getImage(inputImageName);
//         // ImagePlus inputIpl = inputImage.getImagePlus();

//         // // Getting input objects
//         // LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_OBJECTS, workspace);
//         // HashMap<Integer, Objs> outputObjects = new HashMap<>();

//         // // Initialising output objects
//         // for (Parameters collection : outputCollection.getCollections(true).values()) {
//         //     int channel = collection.getValue(CHANNEL, workspace);

//         //     Objs currOutputObjects = new Objs((String) collection.getValue(OUTPUT_OBJECTS, workspace), inputIpl);
//         //     outputObjects.put(channel, currOutputObjects);
//         // }

//         // for (Objs currOutputObjects : outputObjects.values()) {
//         //     workspace.addObjects(currOutputObjects);

//         //     if (showOutput)
//         //         currOutputObjects.convertToImageIDColours().show();

//         // }

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, null));

//         Parameters collection = new Parameters();
//         collection.add(new OutputObjectsP(OUTPUT_OBJECTS, null));
//         collection.add(new IntegerP(CHANNEL, this, 1));
//         parameters.add(new ParameterGroup(ADD_OBJECTS, this, collection, 1, "Add another object set."));

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
