package ch.epfl.biop.wrappers.elastix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.epfl.biop.java.utilities.Converter;
import ch.epfl.biop.java.utilities.image.ElastixMultiFile;

public class Mod_RegisterHelper extends RegisterHelper {
    private int nThreads = 1;

    public void setNThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    public int getNThreads() {
        return this.nThreads;
    }

    @Override
    public void align(ElastixTask align) throws Exception {
        ElastixTaskSettings settings = new ElastixTaskSettings();
        settings.nThreads(nThreads);
        
        if (!alignTaskSet) {
            if (checkParametersForAlignement()) {

                ElastixMultiFile emfMoving = ((ElastixMultiFile) movingImage.to(ElastixMultiFile.class));
                for (File f : emfMoving.files) {
                    settings.movingImage(() -> f.getAbsolutePath());
                }
                ElastixMultiFile emfFixed = ((ElastixMultiFile) fixedImage.to(ElastixMultiFile.class));
                for (File f : emfFixed.files) {
                    settings.fixedImage(() -> f.getAbsolutePath());
                }
                settings.outFolder(outputDir);

                if (verbose)
                    settings.verbose();

                if (registerInfo != null)
                    settings.taskInfo = registerInfo;

                for (Supplier<String> s : this.transformFilesSupplier) {
                    settings.addTransform(s);
                }

                if (this.initialTransformFilePath != null) {
                    settings.addInitialTransform(initialTransformFilePath);
                }
                alignTaskSet = true;
            } else {
                align = null;
                alignTaskSet = false;
            }
        }
        if (alignTaskSet) {
            assert align != null;
            align.run(settings);
        }
    }

    @Converter
    public Mod_RegisterHelper loadFromZip(RHZipFile rzf) {
        // Initializes register helper
        Mod_RegisterHelper rh = new Mod_RegisterHelper();
        rh.setDefaultOutputDir();
        String pathOutputDir = rh.outputDir.get();

        Map<Integer, File> registerFiles = new HashMap<>();
        Map<Integer, File> transformFiles = new HashMap<>();
        
        try {
            // Unzips files into temp directory
            String fileZip = rzf.f.getAbsolutePath();
            byte[] buffer = new byte[1024];
            ZipInputStream zis;
            FileInputStream fis = new FileInputStream(fileZip);
            zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry != null){
                String fileName = zipEntry.getName();
                File newFile;
                String regexTrParam  = "(TransformParameters\\.)(\\d+)(\\.txt)";
                String regexReParam  = "(RegisterParameters\\.)(\\d+)(\\.txt)";
                if (fileName.startsWith("TransformParameters")) {
                    String number  = fileName.replaceAll(regexTrParam, "$2");
                    //System.out.println("number="+number);
                	newFile = new File(pathOutputDir+File.separator+fileName);
                    newFile.deleteOnExit();
                	transformFiles.put(Integer.valueOf(number), newFile);
                    //System.out.println(newFile.getAbsolutePath());
                } else {
                    assert fileName.startsWith("RegisterParameters");
                    String number  = fileName.replaceAll(regexReParam, "$2");
                    newFile = File.createTempFile("rpa", ".txt");
                    newFile.deleteOnExit();
                	registerFiles.put(Integer.valueOf(number), newFile);
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Registration parameters can be put in the register helper element
        int i=0;
        while (registerFiles.containsKey(i)) {
        	rh.addTransformFromFilePath(registerFiles.get(i).getAbsolutePath());
        	i=i+1;
        }
        		
        // Transform downloaded files should be changed get the right path for transformation chaining
        i=1; // File 0 has no reference to previous file -> doesn't need to be changed
    	try {
    		String regexTr = "(\\(InitialTransformParametersFileName\\s\")(.+)(\"\\))";
	        while (transformFiles.containsKey(i)) {
	        	String putData;
	        	File f = transformFiles.get(i);
	        	
	        	// input the file content to the StringBuffer "input"
	            BufferedReader file = new BufferedReader(new FileReader(f));
	            String line;
	            StringBuilder inputBuffer = new StringBuilder();
                //System.out.println(transformFiles.get(i-1).getAbsolutePath().replaceAll("\\\\","\\\\\\\\"));
	            while ((line = file.readLine()) != null) {

	            	putData=line.replaceAll(regexTr, "$1"+transformFiles.get(i-1).getAbsolutePath().replaceAll("\\\\","\\\\\\\\")+"$3");
	                inputBuffer.append(putData);//line);
	                inputBuffer.append('\n');
	            }
	            String inputStr = inputBuffer.toString();
	            file.close();
	            FileOutputStream fileOut = new FileOutputStream(f);
	            fileOut.write(inputStr.getBytes());
	            fileOut.close();
				i=i+1;
				//System.out.println(f.getAbsolutePath());
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return rh;
    }
}
