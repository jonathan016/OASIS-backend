package com.oasis.responsemapper;

import com.oasis.model.entity.AssetModel;
import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.*;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.assets.AssetDetailResponse;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssetsResponseMapper {

    public PagingResponse<AssetListResponse>
    produceViewFoundAssetSuccessResult(int httpStatusCode, List<AssetListResponse.Asset> mappedAssets,
                                       Integer pageNumber) {
        PagingResponse<AssetListResponse> successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
//        if(mappedAssets.size() - ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE * pageNumber > 0){
//            successResponse.setValue(
//                    new AssetListResponse(
//                            mappedAssets.subList(ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE * pageNumber - ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE, ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE * pageNumber)
//                    )
//            );
//        } else {
//            successResponse.setValue(
//                    new AssetListResponse(
//                            mappedAssets.subList(ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE * pageNumber - ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE, ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE * pageNumber - ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE + mappedAssets.size() % ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE)
//                    )
//            );
//        }
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

    public NoPagingResponse<FailedResponse>
    produceAssetsFailedResult(int httpStatusCode, String errorCode, String errorMessage) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(httpStatusCode);
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
    produceAssetSaveSuccessResult(int httpStatusCode) {
        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

    public NoPagingResponse<AssetDetailResponse>
    produceViewAssetDetailSuccessResult(int httpStatusCode, AssetModel asset){
        NoPagingResponse<AssetDetailResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new AssetDetailResponse(
                        asset.getSku(),
                        asset.getName(),
                        asset.getBrand(),
                        asset.getType(),
                        asset.getLocation(),
                        asset.getStock(),
                        asset.getPrice(),
                        asset.getImageDirectory()
                )
        );

        return successResponse;
    }
}
