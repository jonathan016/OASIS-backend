package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.request_mapper.AssetsRequestMapper;
import com.oasis.response_mapper.AssetsResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.ActiveComponentManager;
import com.oasis.service.implementation.AssetsServiceImpl;
import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.web_model.request.assets.DeleteAssetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping(value = APIMappingValue.API_ASSET)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetsController {

    @Autowired
    private AssetsResponseMapper assetsResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;
    @Autowired
    private AssetsServiceImpl assetsServiceImpl;
    @Autowired
    private ActiveComponentManager activeComponentManager;
    @Autowired
    private AssetsRequestMapper assetsRequestMapper;

    @GetMapping(value = APIMappingValue.API_LIST, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAvailableAssetsList(
            @RequestParam(value = "query", required = false)
                    String query,
            @RequestParam(value = "page")
            final int page,
            @RequestParam(value = "sort")
            final String sort
    ) {

        List< AssetModel > availableAssets;
        int totalRecords;

        try {
            if (query != null && query.isEmpty()) {
                query = "defaultQuery";
            }
            availableAssets = new ArrayList<>(assetsServiceImpl.getAvailableAssetsList(query, page, sort));

            totalRecords = assetsServiceImpl.getAvailableAssetsCount(query, sort);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        //TODO include parameter role
        return new ResponseEntity<>(
                assetsResponseMapper.produceViewFoundAssetSuccessResult(HttpStatus.OK.value(), availableAssets,
                                                                        activeComponentManager.getAssetsListActiveComponents(
                                                                                ""), page, totalRecords
                ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DATA_ASSET, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAssetDetailData(
            @PathVariable(value = "identifier")
            final String sku
    ) {

        AssetModel asset;

        try {
            asset = assetsServiceImpl.getAssetDetailData(sku);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        List< String > images = assetsServiceImpl.getAssetDetailImages(asset.getSku(), asset.getImageDirectory());

        //TODO include parameter role
        return new ResponseEntity<>(assetsResponseMapper.produceViewAssetDetailSuccessResult(HttpStatus.OK.value(),
                                                                                             activeComponentManager.getAssetDetailActiveComponents(
                                                                                                     ""), asset, images
        ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_IMAGE_ASSET,
                produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE },
                consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAssetImage(
            @PathVariable(value = "identifier")
            final String sku,
            @PathVariable(value = "image")
            final String image,
            @RequestParam(value = "extension")
            final String extension
    ) {

        byte[] photo;

        photo = assetsServiceImpl.getAssetImage(sku, image, extension);

        return new ResponseEntity<>(photo, HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_PDF_ASSET, produces = MediaType.APPLICATION_PDF_VALUE,
                consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAssetDetailInPdf(
            @PathVariable(value = "identifier")
            final String sku
    ) {

        byte[] document;

        try {
            document = assetsServiceImpl.getAssetDetailInPdf(sku, AssetsController.class.getClassLoader());
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE, produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity saveAsset(
            @RequestParam(value = "photos")
            final List< MultipartFile > photos,
            @RequestParam(value = "data")
            final String rawAssetData
    ) {

        try {
            String username = assetsRequestMapper.getAdminUsernameFromRawData(rawAssetData);
            boolean isAddOperation = assetsRequestMapper.checkAddOperationFromRawData(rawAssetData);

            assetsServiceImpl.saveAsset(photos, username,
                                        assetsRequestMapper.getAssetModelFromRawData(rawAssetData, isAddOperation),
                                        isAddOperation
            );
        } catch (DuplicateDataException duplicateDataException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.CONFLICT.value(),
                                                                                 duplicateDataException.getErrorCode(),
                                                                                 duplicateDataException.getErrorMessage()
            ), HttpStatus.CONFLICT);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 unauthorizedOperationException.getErrorCode(),
                                                                                 unauthorizedOperationException.getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(HttpStatus.CREATED.value()),
                                    HttpStatus.CREATED
        );
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE, produces = MediaType.APPLICATION_JSON_VALUE,
                   consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteAssets(
            @RequestBody
            final DeleteAssetRequest request
    ) {
        //TODO Handle concurrency

        try {
            assetsServiceImpl.deleteAssets(request.getSkus(), request.getUsername());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 unauthorizedOperationException.getErrorCode(),
                                                                                 unauthorizedOperationException.getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(assetsResponseMapper.produceAssetSaveSuccessResult(HttpStatus.OK.value()),
                                    HttpStatus.OK
        );
    }

    @RequestMapping(value = APIMappingValue.API_MISDIRECT, method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.HEAD,
            RequestMethod.OPTIONS,
            RequestMethod.PATCH,
            RequestMethod.TRACE
    })
    public ResponseEntity returnIncorrectMappingCalls() {

        return new ResponseEntity<>(
                failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                                                         "Incorrect mapping/method"
                ), HttpStatus.BAD_REQUEST);
    }

}
