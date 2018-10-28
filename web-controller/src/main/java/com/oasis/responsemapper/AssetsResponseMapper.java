package com.oasis.responsemapper;

import com.oasis.model.entity.AssetModel;
import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.*;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.assets.AssetDetailResponse;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssetsResponseMapper {

    public PagingResponse<AssetListResponse>
    produceViewFoundAssetSuccessResult(List<AssetListResponse.Asset> mappedAssets,
                                       Integer pageNumber) {
        PagingResponse<AssetListResponse> successResponse = new PagingResponse<>();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new AssetListResponse(
                        mappedAssets
                )
        );
        successResponse.setPaging(
                new Paging(
                        pageNumber,
                        ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE,
                        mappedAssets.size()
                )
        );

        return successResponse;
    }

    public PagingResponse<FailedResponse>
    produceViewFoundAssetFailedResult(String errorCode, String errorMessage) {
        PagingResponse<FailedResponse> failedResponse = new PagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new FailedResponse(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }

    public BaseResponse
    produceAssetSaveSuccessResult() {
        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

    public NoPagingResponse<FailedResponse>
    produceAssetSaveFailedResult(String errorCode, String errorMessage) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new FailedResponse(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }

    public NoPagingResponse<AssetDetailResponse>
    produceViewAssetDetailSuccessResult(AssetModel asset){
        NoPagingResponse<AssetDetailResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new AssetDetailResponse(
                        asset.getSku(),
                        asset.getName(),
                        asset.getBrand(),
                        asset.getType(),
                        asset.getLocation(),
                        asset.getStock()
                )
        );

        return successResponse;
    }

    public NoPagingResponse<FailedResponse>
    produceViewAssetDetailFailedResult(String errorCode, String errorMessage) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new FailedResponse(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }
}
