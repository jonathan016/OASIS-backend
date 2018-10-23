package com.oasis.responsemapper;

import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.Paging;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssetsResponseMapper {

    public PagingResponse<AssetListResponse>
    produceFindAssetSuccessResult(List<AssetListResponse.Asset> mappedAssets,
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
    produceFindAssetFailedResult(String errorCode, String errorMessage) {
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
}
