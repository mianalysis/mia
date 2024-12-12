// package io.github.mianalysis.mia.module.images.measure;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ij.ImagePlus;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.WorkspaceI;
// import io.github.mianalysis.mia.object.image.ImageI;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import net.sourceforge.tess4j.Tesseract;
// import net.sourceforge.tess4j.TesseractException;

// /**
//  * Created by sc13967 on 12/05/2017.
//  */
// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class ReadTextFromImage extends Module {
//     public static final String INPUT_SEPARATOR = "Image input";
//     public static final String INPUT_IMAGE = "Input image";

//     public ReadTextFromImage(Modules modules) {
//         super("Read text from image", modules);
//     }

//     @Override
//     public Category getCategory() {
//         return Categories.IMAGES_MEASURE;
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     @Override
//     public Status process(WorkspaceI workspace) {
//         // Getting input image
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

//         ImageI inputImage = workspace.getImages().get(inputImageName);
//         ImagePlus inputImagePlus = inputImage.getImagePlus();

//         Tesseract tesseract = new Tesseract();
//         tesseract.setDatapath("/Users/sc13967/Desktop/tessdata");
//         try {
//             String text = tesseract.doOCR(inputImagePlus.getBufferedImage());
//             MIA.log.writeDebug(text);
//         } catch (TesseractException e) {
//             MIA.log.writeError(e);
//         }
//         return Status.PASS;

//     }

//     @Override
//     public void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));

//         addParameterDescriptions();

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

//     void addParameterDescriptions() {
//         // parameters.get(INPUT_IMAGE).setDescription(
//         //         "Image to measure intensity statistics for.  The resulting measurements will be associated with this image for use in subsequent modules.");

//     }
// }
