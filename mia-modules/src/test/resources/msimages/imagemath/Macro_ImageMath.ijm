outputPath = "C:/Users/steph/Documents/Programming/Java Projects/mia/src/test/resources/msimages/imagemath/";
name = getTitle();
bits = split(name, "_.");
dim = bits[1]

if (indexOf(name, "B8") == -1) {
	return;
}

if (indexOf(name, "D3Z") == -1) {
	return;
}

run("32-bit");

run("Duplicate...", "duplicate");
run("Add...", "value=3.2 stack");
saveAs("ZIP", outputPath + "ImageMath_"+ dim +"_B8_OADD_V3.2_C32T.zip");
close();

run("Duplicate...", "duplicate");
run("Subtract...", "value=3.2 stack");
saveAs("ZIP", outputPath + "ImageMath_"+ dim +"_B8_OSUBTRACT_V3.2_C32T.zip");
close();

run("Duplicate...", "duplicate");
run("Divide...", "value=3.2 stack");
saveAs("ZIP", outputPath + "ImageMath_"+ dim +"_B8_ODIVIDE_V3.2_C32T.zip");
close();

run("Duplicate...", "duplicate");
run("Multiply...", "value=3.2 stack");
saveAs("ZIP", outputPath + "ImageMath_"+ dim +"_B8_OMULTIPLY_V3.2_C32T.zip");
close();

run("Duplicate...", "duplicate");
run("Square");
saveAs("ZIP", outputPath + "ImageMath_"+ dim +"_B8_OSQUARE_C32T.zip");
close();

run("Duplicate...", "duplicate");
run("Square Root");
saveAs("ZIP", outputPath + "ImageMath_"+ dim +"_B8_OSQRT_C32T.zip");
close();

run("Duplicate...", "duplicate");
// Absolute will do nothing for an 8-bit image
saveAs("ZIP", outputPath + "ImageMath_"+ dim +"_B8_OABSOLUTE_C32T.zip");
close();