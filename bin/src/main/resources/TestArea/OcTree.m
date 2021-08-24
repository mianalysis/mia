function [binDepths,binParents,binCorners,pointBins] = OcTree(points)

binDepths = [0]     % Initialize an array of bin depths with this single base-level bin
binParents = [0]    % This base level bin is not a child of other bins
binCorners = [min(points) max(points)] % It surrounds all points in XYZ space
pointBins(:) = 1    % Initially, all points are assigned to this first bin
divide(1)           % Begin dividing this first bin

    function divide(binNo)
        
        % If this bin meets any exit conditions, do not divide it any further.
        binPointCount = nnz(pointBins==binNo)
        binEdgeLengths = binCorners(binNo,1:3) - binCorners(binNo,4:6)
        binDepth = binDepths(binNo)
        exitConditionsMet = binPointCount<value || min(binEdgeLengths)<value || binDepth>value
        if exitConditionsMet
            return; % Exit recursive function
        end
        
        % Otherwise, split this bin into 8 new sub-bins with a new division point
        newDiv = (binCorners(binNo,1:3) + binCorners(binNo,4:6)) / 2
        for i = 1:8
            newBinNo = length(binDepths) + 1
            binDepths(newBinNo) = binDepths(binNo) + 1
            binParents(newBinNo) = binNo
            binCorners(newBinNo) = [one of the 8 pairs of the newDiv with minCorner or maxCorner]
            oldBinMask = pointBins==binNo
            % Calculate which points in pointBins==binNo now belong in newBinNo
            pointBins(newBinMask) = newBinNo
            % Recursively divide this newly created bin
            divide(newBinNo)
        end
    end
end