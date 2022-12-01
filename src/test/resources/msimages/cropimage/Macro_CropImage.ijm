outputPath = "C:/Users/steph/Documents/Programming/Java Projects/mia/src/test/resources/msimages/cropimage/";
x = 12;
y = 15;
w = 23;
h = 6;

name = getTitle();
bits = split(name, "_.");
dim = bits[1]

if (indexOf(name, "B8") == -1) {
	return;
}

//if (indexOf(name, "D4ZT") == -1) {
//	return;
//}

makeRectangle(x,y,w,h);
run("Crop");
saveAs("ZIP", outputPath + "CropImage_"+ dim +"_B8_X" + x + "_Y" + y +"_W" + w + "_H" + h + ".zip");
close();
