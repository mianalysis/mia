clear

pnameIn = 'C:\Users\steph\Documents\Java Projects\ModularImageAnalysis\src\test\resources\images\';
fnameIn = 'LabelledObjects4D_8bit.tif';

pnameOut = 'C:\Users\steph\Documents\Java Projects\ModularImageAnalysis\src\test\resources\coordinates\';
fnameOut = 'ExpectedObjects4D.csv';

nZ = 12;
nT = 4;

for j=1:nT
    for i = 1:nZ
        (j-1)*nZ+i
        im(:,:,i,j) = imread([pnameIn,fnameIn],(j-1)*nZ+i);
    end
end

[x,y,z,t] = ind2sub(size(im),find(im~=0));
coords = zeros(numel(x),7);
for i=1:numel(x)
   newCoord = [im(x(i),y(i),z(i),t(i)),im(x(i),y(i),z(i)),y(i)-1,x(i)-1,0,z(i)-1,t(i)-1];
   coords(i,:) = newCoord;
    
end

csvwrite([pnameOut,fnameOut],coords);