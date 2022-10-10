mainWin = getTitle();
	
getDimensions(width, height, channels, slices, frames);

for (c=1;c<=channels;c++) {
	for (z=1;z<=slices;z++) {
		for (t=1;t<=frames;t++) {
			Stack.setPosition(c, z, t);
			selectWindow(mainWin);
			run("Morphological Filters", "operation=Gradient element=Disk radius=3 stack");
			currWin = getTitle();
			run("Copy");
			selectWindow(mainWin);
			run("Paste");
			close(currWin);
		}
	}
}