clear

pnameIn = 'C:\Users\sc13967\Documents\Java_Projects\ModularImageAnalysis\src\test\resources\images\';
fnameIn = 'BinarySphere3D_8bit.tif';

pnameOut = 'C:\Users\sc13967\Documents\Java_Projects\ModularImageAnalysis\src\test\resources\coordinates\';
fnameOut = 'ExpectedSphere3D.csv';

for i = 1:numel(imfinfo([pnameIn,fnameIn]))
   im(:,:,i) = imread([pnameIn,fnameIn],i);    
end

[x,y,z] = ind2sub(size(im),find(im==0));
coords = [];
for i=1:numel(x)
   newCoord = [im(x(i),y(i),z(i)),im(x(i),y(i),z(i)),y(i)-1,x(i)-1,0,z(i)-1,0];
   coords = [coords;newCoord];
    
end

csvwrite([pnameOut,fnameOut],coords);