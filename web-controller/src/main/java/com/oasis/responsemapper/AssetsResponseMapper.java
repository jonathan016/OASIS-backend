package com.oasis.responsemapper;

import com.oasis.model.entity.AssetModel;
import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.*;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.assets.AssetDetailResponse;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AssetsResponseMapper {

    public PagingResponse<AssetListResponse>
    produceViewFoundAssetSuccessResult(final int httpStatusCode, final List<AssetModel> assets,
                                       final Map<String, Boolean> components, final int pageNumber) {
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
        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, AssetListResponse.Asset.class)
                        .field("location", "location")
                        .field("stock", "quantity")
                        .exclude("expendable")
                        .byDefault()
                        .register();
        List<AssetListResponse.Asset> mappedAssets = new ArrayList<>();
        for (AssetModel asset : assets) {
            mappedAssets.add(
                    assetDataFactory
                            .getMapperFacade(AssetModel.class, AssetListResponse.Asset.class).map(asset)
            );
        }
        successResponse.setValue(
                new AssetListResponse(
                        mappedAssets
                )
        );
        successResponse.setComponents(components);
        successResponse.setPaging(
                new Paging(
                        pageNumber,
                        ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE,
                        assets.size()
                )
        );

        return successResponse;
    }

    public NoPagingResponse<FailedResponse>
    produceAssetsFailedResult(final int httpStatusCode, final String errorCode, final String errorMessage) {
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
    produceAssetSaveSuccessResult(final int httpStatusCode) {
        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

    public NoPagingResponse<AssetDetailResponse>
    produceViewAssetDetailSuccessResult(final int httpStatusCode, final Map<String, Boolean> components,
                                        final AssetModel asset, final String[] images){
        NoPagingResponse<AssetDetailResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(components);

        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, AssetDetailResponse.class);
        AssetDetailResponse mappedAsset = assetDataFactory.getMapperFacade(AssetModel.class,
                                                                            AssetDetailResponse.class).map(asset);
        mappedAsset.setImages(images);
        successResponse.setValue(mappedAsset);

        return successResponse;
    }
}
