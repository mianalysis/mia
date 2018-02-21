clear

pnameIn = 'C:\Users\sc13967\Documents\Java_Projects\ModularImageAnalysis\src\test\resources\images\';
fnameIn = 'LabelledObjects2D_8bit.tif';

pnameOut = 'C:\Users\sc13967\Documents\Java_Projects\ModularImageAnalysis\src\test\resources\coordinates\';
fnameOut = 'ExpectedObjects2D.csv';

for i = 1:numel(imfinfo([pnameIn,fnameIn]))
   im(:,:,i) = imread([pnameIn,fnameIn],i);    
end

[x,y,z] = ind2sub(size(im),find(im~=0));
coords = [];
for i=1:numel(x)
   newCoord = [im(x(i),y(i),z(i)),im(x(i),y(i),z(i)),x(i),y(i),0,z(i),0];
   coords = [coords;newCoord];
    
end

csvwrite([pnameOut,fnameOut],coords);