package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.responsemapper.AssetsResponseMapper;
import com.oasis.service.implementation.AssetsServiceImpl;
import com.oasis.webmodel.request.AddAssetRequest;
import com.oasis.webmodel.request.DeleteAssetRequest;
import com.oasis.webmodel.request.UpdateAssetRequest;
import com.oasis.webmodel.response.BaseResponse;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@CrossOrigin(origins = "http://localhost")
@RestController
public class AssetsController {

    @Autowired
    private AssetsResponseMapper assetsResponseMapper;
    @Autowired
    private AssetsServiceImpl assetsServiceImpl;

    @GetMapping(value = APIMappingValue.API_FIND_ASSET,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public PagingResponse<?> callFindAssetsService(@RequestParam String searchQuery,
                                                   @RequestParam int pageNumber,
                                                   @RequestParam String sortInfo) {
        List<AssetListResponse.Asset> assetsFound;

        try {
            assetsFound = new ArrayList<>(assetsServiceImpl.getAssetsBySearchQuery(searchQuery, pageNumber, sortInfo));
        } catch (BadRequestException | DataNotFoundException e) {
            return assetsResponseMapper.produceViewFoundAssetFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceViewFoundAssetSuccessResult(assetsFound, pageNumber);
    }

    @GetMapping(value = APIMappingValue.API_ASSET_LIST,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public PagingResponse<?> callGetAssetsListService(@RequestParam int pageNumber,
                                                      @RequestParam String sortInfo) {
        List<AssetListResponse.Asset> assetsFound;

        try {
            assetsFound = new ArrayList<>(assetsServiceImpl.getAvailableAsset(pageNumber, sortInfo));
        } catch (DataNotFoundException e) {
            return assetsResponseMapper.produceViewFoundAssetFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceViewFoundAssetSuccessResult(assetsFound, pageNumber);
    }

    @GetMapping(value = APIMappingValue.API_ASSET_DETAIL,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public NoPagingResponse<?> callGetAssetDetailService(@PathVariable String assetSku) {
        AssetModel asset;

        try {
            asset = assetsServiceImpl.getAssetData(assetSku);
        } catch (DataNotFoundException e) {
            return assetsResponseMapper.produceViewAssetDetailFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceViewAssetDetailSuccessResult(asset);
    }


    @PostMapping(value = APIMappingValue.API_SAVE_ASSET,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public BaseResponse callInsertToDatabaseService(@RequestBody AddAssetRequest request) {

        try {
            assetsServiceImpl.insertToDatabase(request.getAsset(), request.getEmployeeNik());
        } catch (DuplicateDataException | UnauthorizedOperationException | DataNotFoundException e) {
            return assetsResponseMapper.produceAssetSaveFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceAssetSaveSuccessResult();
    }

    @PutMapping(value = APIMappingValue.API_SAVE_ASSET,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public BaseResponse callUpdateAssetService(@RequestBody UpdateAssetRequest request) {
        //TODO Handle concurrency
        //TODO Handle existing requests with the asset

        try {
            assetsServiceImpl.updateAsset(request.getAsset(), request.getEmployeeNik());
        } catch (UnauthorizedOperationException | DataNotFoundException e) {
            return assetsResponseMapper.produceAssetSaveFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceAssetSaveSuccessResult();
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE_ASSET,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public BaseResponse callDeleteAssetsService(@RequestBody DeleteAssetRequest request) {
        //TODO Handle concurrency

        try {
            assetsServiceImpl.deleteAssets(request.getSelectedAssets(), request.getEmployeeNik());
        } catch (UnauthorizedOperationException | DataNotFoundException | BadRequestException e) {
            return assetsResponseMapper.produceAssetSaveFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return assetsResponseMapper.produceAssetSaveSuccessResult();
    }
}
