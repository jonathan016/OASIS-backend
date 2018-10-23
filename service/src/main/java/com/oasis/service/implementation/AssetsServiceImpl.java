package com.oasis.service.implementation;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.AssetsServiceApi;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.ASSET_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.EMPTY_SEARCH_QUERY;

public class AssetsServiceImpl implements AssetsServiceApi {

    @Autowired
    private AssetRepository assetRepository;

    @Override
    public List<AssetListResponse.Asset> getAssetsBySearchQuery(String searchQuery, Integer pageNumber, String sortInfo) throws BadRequestException, DataNotFoundException {
        if (searchQuery.isEmpty()) {
            throw new BadRequestException(EMPTY_SEARCH_QUERY.getErrorCode(), EMPTY_SEARCH_QUERY.getErrorMessage());
        }

        if (assetRepository.findAllBySkuContainsOrNameContains(searchQuery, searchQuery).size() == 0) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }
        if ((int) Math.ceil(
                (float) assetRepository.findAllBySkuContainsOrNameContains(searchQuery, searchQuery).size()
                        / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) < pageNumber) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }

        List<AssetModel> assetsFound = fillData(searchQuery, sortInfo);

        return mapAssetsFound(assetsFound);
    }

    @Override
    public List<AssetListResponse.Asset> getAvailableAsset(int pageNumber, String sortInfo) {
        return null;
    }

    public List<AssetModel> fillData(String searchQuery, String sortInfo) {
        List<AssetModel> assetsFound = new ArrayList<>();

        if (sortInfo.substring(1).equals("assetId")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                assetsFound.addAll(
                        assetRepository.findAllBySkuContainsOrNameContainsOrderBySkuAsc(
                                searchQuery,
                                searchQuery
                        )
                );
            } else if (sortInfo.substring(0, 1).equals("D")) {
                assetsFound.addAll(
                        assetRepository.findAllBySkuContainsOrNameContainsOrderBySkuDesc(
                                searchQuery,
                                searchQuery
                        )
                );
            }
        } else if (sortInfo.substring(1).equals("assetName")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                assetsFound.addAll(
                        assetRepository.findAllBySkuContainsOrNameContainsOrderByNameAsc(
                                searchQuery,
                                searchQuery
                        )
                );
            } else if (sortInfo.substring(0, 1).equals("D")) {
                assetsFound.addAll(
                        assetRepository.findAllBySkuContainsOrNameContainsOrderByNameDesc(
                                searchQuery,
                                searchQuery
                        )
                );
            }
        }

        return assetsFound;
    }

    public List<AssetListResponse.Asset> mapAssetsFound(List<AssetModel> assetsFound) {
        List<AssetListResponse.Asset> mappedAssets = new ArrayList<>();

        for (AssetModel assetFound : assetsFound) {
            AssetListResponse.Asset asset = new AssetListResponse.Asset(
                    assetFound.getSku(),
                    assetFound.getName(),
                    assetFound.getBrand(),
                    assetFound.getType(),
                    assetFound.getLocation(),
                    assetFound.getStock()
            );

            mappedAssets.add(asset);
        }

        return mappedAssets;
    }
}
