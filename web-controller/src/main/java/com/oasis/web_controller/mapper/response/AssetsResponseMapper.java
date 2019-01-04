package com.oasis.web_controller.mapper.response;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.constant.service_constant.PageSizeConstant;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.Paging;
import com.oasis.web_model.response.PagingResponse;
import com.oasis.web_model.response.success.assets.AssetDetailResponse;
import com.oasis.web_model.response.success.assets.AssetListResponse;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AssetsResponseMapper {

    public PagingResponse< AssetListResponse > produceViewFoundAssetSuccessResult(
            final int httpStatusCode, final List< AssetModel > assets, final Map< String, Boolean > components,
            final int pageNumber, final long totalRecords
    ) {

        PagingResponse< AssetListResponse > successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(components);

        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, AssetListResponse.Asset.class).field("stock", "quantity")
                        .exclude("expendable").byDefault().register();
        List< AssetListResponse.Asset > mappedAssets = new ArrayList<>();
        for (AssetModel asset : assets) {
            mappedAssets
                    .add(assetDataFactory.getMapperFacade(AssetModel.class, AssetListResponse.Asset.class).map(asset));
        }
        successResponse.setValue(new AssetListResponse(mappedAssets));

        final int totalPage = (int) Math.ceil((double) totalRecords / PageSizeConstant.ASSETS_LIST_PAGE_SIZE);
        successResponse.setPaging(new Paging(pageNumber, assets.size(), totalPage, totalRecords));

        return successResponse;
    }

    public BaseResponse produceAssetSaveSuccessResult(final int httpStatusCode) {

        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

    public NoPagingResponse< AssetDetailResponse > produceViewAssetDetailSuccessResult(
            final int httpStatusCode, final Map< String, Boolean > components, final AssetModel asset,
            final List< String > imageURLs
    ) {

        NoPagingResponse< AssetDetailResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(components);

        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, AssetDetailResponse.class).byDefault().exclude("expendable")
                        .register();
        AssetDetailResponse mappedAsset = assetDataFactory.getMapperFacade(AssetModel.class, AssetDetailResponse.class)
                                                          .map(asset);
        mappedAsset.setExpendable(( asset.isExpendable() ) ? "Yes" : "No");
        mappedAsset.setImages(imageURLs);

        successResponse.setValue(mappedAsset);

        return successResponse;
    }

}
