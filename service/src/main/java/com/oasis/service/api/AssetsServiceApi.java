package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.webmodel.response.success.assets.AssetListResponse;

import java.util.List;

public interface AssetsServiceApi {

    List<AssetListResponse.Asset> getAssetsBySearchQuery(String searchQuery, Integer pageNumber, String sortInfo) throws BadRequestException, DataNotFoundException;

    List<AssetListResponse.Asset> getAvailableAsset(int pageNumber, String sortInfo) throws DataNotFoundException;

    List<AssetModel> fillData(String searchQuery, String sortInfo);

    List<AssetListResponse.Asset> mapAssetsFound(List<AssetModel> assetsFound);

    void sortData(List<AssetModel> assetsAvailable, String sortInfo, int stockLimit);
}
