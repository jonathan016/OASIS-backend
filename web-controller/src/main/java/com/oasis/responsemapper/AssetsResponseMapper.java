package com.oasis.responsemapper;

import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.*;
import com.oasis.webmodel.response.failed.FailedResponse;
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
    produceAssetInsertionSuccessResult() {
        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

    public NoPagingResponse<FailedResponse>
    produceAssetInsertionFailedResult(String errorCode, String errorMessage) {
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
