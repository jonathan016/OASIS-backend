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
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity callFindAssetsService(@RequestParam String searchQuery,
                                                @RequestParam int pageNumber,
                                                @RequestParam String sortInfo) {
        List<AssetListResponse.Asset> assetsFound;

        try {
            assetsFound = new ArrayList<>(assetsServiceImpl.getAssetsBySearchQuery(searchQuery, pageNumber, sortInfo));
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceViewFoundAssetSuccessResult(HttpStatus.OK.value(), assetsFound, pageNumber), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_ASSET_LIST,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callGetAssetsListService(@RequestParam int pageNumber,
                                                   @RequestParam String sortInfo) {
        List<AssetListResponse.Asset> assetsFound;

        try {
            assetsFound = new ArrayList<>(assetsServiceImpl.getAvailableAsset(pageNumber, sortInfo));
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceViewFoundAssetSuccessResult(HttpStatus.OK.value(), assetsFound, pageNumber), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_ASSET_DETAIL,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callGetAssetDetailService(@PathVariable String assetSku) {
        AssetModel asset;

        try {
            asset = assetsServiceImpl.getAssetData(assetSku);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceViewAssetDetailSuccessResult(HttpStatus.OK.value(), asset), HttpStatus.OK);
    }


    @PostMapping(value = APIMappingValue.API_SAVE_ASSET,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callInsertToDatabaseService(@RequestBody AddAssetRequest request) {

        try {
            assetsServiceImpl.insertToDatabase(request.getAsset(), request.getEmployeeNik());
        } catch (DuplicateDataException duplicateDataException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.CONFLICT.value(), duplicateDataException.getErrorCode(), duplicateDataException.getErrorMessage()), HttpStatus.CONFLICT);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PutMapping(value = APIMappingValue.API_SAVE_ASSET,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callUpdateAssetService(@RequestBody UpdateAssetRequest request) {
        //TODO Handle concurrency
        //TODO Handle existing requests with the asset

        try {
            assetsServiceImpl.updateAsset(request.getAsset(), request.getEmployeeNik());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE_ASSET,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callDeleteAssetsService(@RequestBody DeleteAssetRequest request) {
        //TODO Handle concurrency

        try {
            assetsServiceImpl.deleteAssets(request.getSelectedAssets(), request.getEmployeeNik());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }
}
