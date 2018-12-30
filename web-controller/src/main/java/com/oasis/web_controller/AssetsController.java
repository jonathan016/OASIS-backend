package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.request_mapper.AssetsRequestMapper;
import com.oasis.response_mapper.AssetsResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.api.assets.AssetDeleteServiceApi;
import com.oasis.service.api.assets.AssetDetailServiceApi;
import com.oasis.service.api.assets.AssetListServiceApi;
import com.oasis.service.api.assets.AssetSaveServiceApi;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.tool.helper.ActiveComponentManager;
import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.web_model.request.assets.DeleteAssetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping(value = APIMappingValue.API_ASSET)
@CrossOrigin(origins = APIMappingValue.CROSS_ORIGIN_LINK)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetsController {

    @Autowired
    private AssetsRequestMapper assetsRequestMapper;
    @Autowired
    private AssetsResponseMapper assetsResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @Autowired
    private AssetDeleteServiceApi assetDeleteServiceApi;
    @Autowired
    private AssetDetailServiceApi assetDetailServiceApi;
    @Autowired
    private AssetListServiceApi assetListServiceApi;
    @Autowired
    private AssetSaveServiceApi assetSaveServiceApi;
    @Autowired
    private AssetUtilServiceApi assetUtilServiceApi;

    @Autowired
    private ActiveComponentManager activeComponentManager;



    @GetMapping(value = APIMappingValue.API_LIST,
                produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAvailableAssetsList(
            @RequestParam(value = "query",
                          required = false)
            final String query,
            @RequestParam(value = "page")
            final int page,
            @RequestParam(value = "sort",
                          required = false)
            final String sort,
            @AuthenticationPrincipal
            final User user
    ) {

        final List< AssetModel > availableAssets;
        final long totalRecords;

        try {
            availableAssets = assetListServiceApi.getAvailableAssetsList(query, page, sort);
            totalRecords = assetListServiceApi.getAvailableAssetsCount(query, sort);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.BAD_REQUEST.value(),
                    badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage(),
                    activeComponentManager
                            .getAssetsListActiveComponents(
                                    user.getUsername(),
                                    new ArrayList<>(
                                            user.getAuthorities())
                                            .get(0)
                                            .getAuthority()
                            )
            ), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage(),
                    activeComponentManager
                            .getAssetsListActiveComponents(
                                    user.getUsername(),
                                    new ArrayList<>(
                                            user.getAuthorities())
                                            .get(0)
                                            .getAuthority()
                            )
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(assetsResponseMapper
                                            .produceViewFoundAssetSuccessResult(HttpStatus.OK.value(), availableAssets,
                                                                                activeComponentManager
                                                                                        .getAssetsListActiveComponents(
                                                                                                user.getUsername(),
                                                                                                new ArrayList<>(
                                                                                                        user.getAuthorities())
                                                                                                        .get(0)
                                                                                                        .getAuthority()
                                                                                        ), page, totalRecords
                                            ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DATA_ASSET,
                produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAssetDetailData(
            @PathVariable(value = "identifier")
            final String sku,
            @AuthenticationPrincipal
            final User user
    ) {

        final AssetModel asset;

        try {
            asset = assetDetailServiceApi.getAssetDetailData(sku);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage(), activeComponentManager
                            .getAssetDetailActiveComponents(new ArrayList<>(user.getAuthorities()).get(0)
                                                                                                  .getAuthority())
            ), HttpStatus.NOT_FOUND);
        }

        final List< String > imageURLs = assetDetailServiceApi
                .getAssetDetailImages(asset.getSku(), asset.getImageDirectory());

        return new ResponseEntity<>(
                assetsResponseMapper.produceViewAssetDetailSuccessResult(HttpStatus.OK.value(), activeComponentManager
                                                                                 .getAssetDetailActiveComponents(new ArrayList<>(user.getAuthorities()).get(0).getAuthority()),
                                                                         asset, imageURLs
                ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_PDF_ASSET,
                produces = APPLICATION_PDF_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAssetDetailInPdf(
            @PathVariable(value = "identifier")
            final String sku
    ) {

        final byte[] assetPdf = assetDetailServiceApi.getAssetDetailInPdf(sku);

        return new ResponseEntity<>(assetPdf, HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_IMAGE_ASSET,
                produces = { IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE },
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getAssetImage(
            @PathVariable(value = "identifier")
            final String sku,
            @PathVariable(value = "image")
            final String imageName,
            @RequestParam(value = "extension")
            final String extension
    ) {

        final byte[] image = assetUtilServiceApi.getAssetImage(sku, imageName, extension);

        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE,
                 produces = APPLICATION_JSON_VALUE,
                 consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity saveAsset(
            @RequestParam(value = "images")
            final List< MultipartFile > images,
            @RequestParam(value = "data")
            final String rawAssetData,
            @AuthenticationPrincipal
            final User user
    ) {

        try {
            final String username = user.getUsername();
            final boolean addAssetOperation = assetsRequestMapper.isAddAssetOperationFromRawData(rawAssetData);
            final AssetModel asset = assetsRequestMapper.getAssetModelFromRawData(rawAssetData, addAssetOperation);

            assetSaveServiceApi.saveAsset(images, username, asset, addAssetOperation);
        } catch (DuplicateDataException duplicateDataException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.CONFLICT.value(), duplicateDataException.getErrorCode(),
                    duplicateDataException.getErrorMessage(), null
            ), HttpStatus.CONFLICT);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(),
                    unauthorizedOperationException.getErrorMessage(), null
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage(), null
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage(), null
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                assetsResponseMapper.produceAssetSaveSuccessResult(HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE,
                   produces = APPLICATION_JSON_VALUE,
                   consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity deleteAssets(
            @RequestBody
            final DeleteAssetRequest request,
            @AuthenticationPrincipal
            final User user
    ) {

        try {
            assetDeleteServiceApi.deleteAssets(request.getSkus(), user.getUsername());
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage(), null
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage(), null
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                assetsResponseMapper.produceAssetSaveSuccessResult(HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @RequestMapping(value = APIMappingValue.API_MISDIRECT,
                    method = {
                            RequestMethod.GET,
                            RequestMethod.POST,
                            RequestMethod.PUT,
                            RequestMethod.DELETE,
                            RequestMethod.HEAD,
                            RequestMethod.OPTIONS,
                            RequestMethod.PATCH,
                            RequestMethod.TRACE
                    })
    public ResponseEntity returnIncorrectMappingCalls(
            final MissingServletRequestParameterException exception
    ) {

        String message;

        if (exception.getParameterName() != null) {
            message = exception.getMessage();
        } else {
            message = "Incorrect mapping/method!";
        }

        return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                             HttpStatus.BAD_REQUEST.name(), message,
                                                                             null
        ), HttpStatus.BAD_REQUEST);
    }

}
