package ch.epfl.biop.wrappers.transformix;

import java.io.File;

import ch.epfl.biop.java.utilities.roi.types.TransformixInputRoisFile;
import ch.epfl.biop.java.utilities.roi.types.TransformixOutputRoisFile;

public class Mod_TransformHelper extends TransformHelper {
    private int nThreads = 1;

    public void setNThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    public int getNThreads() {
        return this.nThreads;
    }

    public void transform(TransformixTask task) {
        TransformixTaskSettings transformSettings = new TransformixTaskSettings().transform(this.transformFile)
                .outFolder(this.outputDir);
            transformSettings.nThreads(nThreads);

        if (!transformTaskSet) {
            if (checkParametersForTransformation()) {
                transformSettings = new TransformixTaskSettings().transform(this.transformFile)
                        .outFolder(this.outputDir); // THIS SHOULD BE HERE!!!

                if (verbose) transformSettings.verbose();

                if (transformInfo!=null) transformSettings.taskInfo = transformInfo;

                if (transformType==IMAGE_TRANSFORM) {
                    transformSettings.image(this::imageToTransformPathSupplier);
                }

                if (transformType==ROIS_TRANSFORM) {
                    transformSettings.pts(this::roisToTransformPathSupplier);
                }
                
                //transform = new TransformixTask(transformSettings);//transformBuilder.build();
                //task.setSettings(transformSettings);
                transformTaskSet = true;
            } else {
                task = null;
                transformTaskSet = false;
            }
        }
        if (transformTaskSet) {
            try {
                task.run(transformSettings);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (transformType==IMAGE_TRANSFORM) {
            	imageTransformed.clear();
            	imageTransformed.set(new File(this.outputDir.get()+File.separator+"result.tif"));
            }
            if (transformType==ROIS_TRANSFORM) {
            	TransformixOutputRoisFile erf =  new TransformixOutputRoisFile(new File(this.outputDir.get()+File.separator+"outputpoints.txt"),
                        (TransformixInputRoisFile) roisToTransform.to(TransformixInputRoisFile.class));// roisToTransform.to(TransformixInputRoisFile.class);
            	roisTransformed.clear();            	
            	roisTransformed.set(erf);
            	//System.out.println("Output rois set!");
            	//roisTransformed.set(roisToTransform.to(ArrayList.class));
            	//roisTransformed.elastixFileFormatToArray(erf);
            	//new File(this.outputDir.get()+File.separator+"outputpoints.txt"));
            }
        }
    }
}
