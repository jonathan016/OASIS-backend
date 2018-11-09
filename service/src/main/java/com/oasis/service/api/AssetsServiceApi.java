package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.webmodel.request.UpdateAssetRequest;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface AssetsServiceApi {

    /*-------------Assets List Methods-------------*/
    List<AssetListResponse.Asset> getAvailableAssets(
            final int pageNumber,
            final String sortInfo
    )
            throws DataNotFoundException;

    Set<AssetModel> getSortedAvailableAssets(
            final String sortInfo,
            final long stockLimit
    );

    List<AssetListResponse.Asset> getAvailableAssetsBySearchQuery(
            final String searchQuery,
            final int pageNumber,
            final String sortInfo
    )
            throws BadRequestException,
                   DataNotFoundException;

    Set<AssetModel> getSortedAvailableAssetsFromSearchQuery(
            final String searchQuery,
            final String sortInfo
    );

    List<AssetListResponse.Asset> mapAvailableAssets(
            final Set<AssetModel> assetsFound
    );

    AssetModel getAssetDetail(
            final String sku
    )
            throws DataNotFoundException;

    byte[] getAssetImage(
            final String sku,
            final String photoName,
            final String extension,
            final ClassLoader classLoader
    )
            throws DataNotFoundException;

    /*-------------Add Asset Methods-------------*/
    void addAsset(
            final MultipartFile[] assetPhotos,
            final String rawAssetData
    )
            throws DuplicateDataException,
                   UnauthorizedOperationException,
                   DataNotFoundException;

    String generateAssetSkuCode(
            final String brand,
            final String type,
            final String name
    );

    void savePhotos(
            final MultipartFile[] photos,
            final String sku
    );

    /*-------------Update Asset Method-------------*/
    void updateAsset(
            final MultipartFile[] assetPhotos,
            final String rawAssetData
    )
            throws UnauthorizedOperationException,
                   DataNotFoundException;

    /*-------------Delete Asset(s) Method-------------*/
    void deleteAssets(
            final List<String> skus,
            final String adminNik
    )
            throws UnauthorizedOperationException,
                   BadRequestException,
                   DataNotFoundException;
}