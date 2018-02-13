clear

pname = 'C:\Users\sc13967\Local Documents\Java Projects\ModularImageAnalysis\src\test\resources\images\RelateObjects\';
fname = 'ProxCubes2_3D_8bit.tif';

for i = 1:12
    im(:,:,i) = imread([pname,fname],i);
end

idx = find(im ~=0);
[x,y,z] = ind2sub(size(im),idx);
ID = im(idx);
ct = zeros(numel(x),1);

coords = [ID,x,y,ct,z,ct];

csvwrite('ProxCubes2.csv',coords);