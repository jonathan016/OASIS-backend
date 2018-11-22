package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.responsemapper.AssetsResponseMapper;
import com.oasis.service.implementation.AssetsServiceImpl;
import com.oasis.webmodel.request.assets.DeleteAssetRequest;
import com.oasis.webmodel.response.success.assets.AssetDetailResponse;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost")
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetsController {

    @Autowired
    private AssetsResponseMapper assetsResponseMapper;
    @Autowired
    private AssetsServiceImpl assetsServiceImpl;

    @GetMapping(value = APIMappingValue.API_FIND_ASSET,
                produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callGetAvailableAssetsBySearchQueryService(
            @RequestParam final String searchQuery,
            @RequestParam final int pageNumber,
            @RequestParam final String sortInfo
    ) {

        List<AssetListResponse.Asset> availableAssets;

        try {
            availableAssets = new ArrayList<>(
                    assetsServiceImpl.getAvailableAssetsBySearchQuery(searchQuery, pageNumber, sortInfo));
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.BAD_REQUEST.value(),
                    badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceViewFoundAssetSuccessResult(
                HttpStatus.OK.value(),
                availableAssets,
                assetsServiceImpl.getAssetsListActiveComponents(),
                pageNumber
        ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_LIST_ASSET,
                produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callGetAvailableAssetsService(
            @RequestParam final int pageNumber,
            @RequestParam final String sortInfo
    ) {

        List<AssetListResponse.Asset> assetsFound;

        try {
            assetsFound = new ArrayList<>(assetsServiceImpl.getAvailableAssets(pageNumber, sortInfo));
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceViewFoundAssetSuccessResult(
                HttpStatus.OK.value(),
                assetsFound,
                assetsServiceImpl.getAssetsListActiveComponents(),
                pageNumber
        ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DETAIL_ASSET,
                produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callGetAssetDetailService(
            @PathVariable final String sku
    ) {

        AssetDetailResponse asset;

        try {
            asset = assetsServiceImpl.getAssetDetail(sku);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceViewAssetDetailSuccessResult(
                HttpStatus.OK.value(),
                assetsServiceImpl.getAssetDetailActiveComponents(),
                asset
        ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_IMAGE_ASSET,
                produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE},
                consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callGetAssetImageService(
            @PathVariable final String sku,
            @PathVariable final String imageName,
            @PathVariable final String extension
    ) {

        byte[] photo;

        try {
            photo = assetsServiceImpl.getAssetImage(
                    sku,
                    imageName,
                    extension,
                    AssetsController.class.getClassLoader()
            );
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(photo, HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_PDF_ASSET,
                produces = MediaType.APPLICATION_PDF_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callGetAssetDetailInPdfService(
            @PathVariable final String sku
    ) {

        byte[] document;
        try {
            document = assetsServiceImpl.getAssetDetailInPdf(sku, AssetsController.class.getClassLoader());
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE_ASSET,
                 produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity callAddAssetService(
            @RequestParam("assetPhotos") final MultipartFile[] assetPhotos,
            @RequestParam("assetData") final String assetData
    ) {

        try {
            assetsServiceImpl.addAsset(assetPhotos, assetData);
        } catch (DuplicateDataException duplicateDataException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.CONFLICT.value(),
                    duplicateDataException.getErrorCode(),
                    duplicateDataException.getErrorMessage()
            ), HttpStatus.CONFLICT);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.UNAUTHORIZED.value(),
                    unauthorizedOperationException.getErrorCode(),
                    unauthorizedOperationException.getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(
                HttpStatus.CREATED.value()
        ), HttpStatus.CREATED);
    }

    @PutMapping(value = APIMappingValue.API_SAVE_ASSET,
                produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity callUpdateAssetService(
            @RequestParam("assetPhotos") final MultipartFile[] assetPhotos,
            @RequestParam("assetData") final String assetData
    ) {
        //TODO Handle concurrency

        try {
            assetsServiceImpl.updateAsset(assetPhotos, assetData);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.UNAUTHORIZED.value(),
                    unauthorizedOperationException.getErrorCode(),
                    unauthorizedOperationException.getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(
                HttpStatus.OK.value()
        ), HttpStatus.OK);
//        return null;
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE_ASSET,
                   produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity callDeleteAssetsService(
            @RequestBody final DeleteAssetRequest request
    ) {
        //TODO Handle concurrency

        try {
            assetsServiceImpl.deleteAssets(request.getAssetSkus(), request.getNik());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.UNAUTHORIZED.value(),
                    unauthorizedOperationException.getErrorCode(),
                    unauthorizedOperationException.getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(assetsResponseMapper.produceAssetsFailedResult(
                    HttpStatus.BAD_REQUEST.value(),
                    badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(
                HttpStatus.OK.value()
        ), HttpStatus.OK);
    }
}
