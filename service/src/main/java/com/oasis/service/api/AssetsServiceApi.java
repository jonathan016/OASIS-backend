package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.webmodel.request.AddAssetRequest;
import com.oasis.webmodel.request.UpdateAssetRequest;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface AssetsServiceApi {

    List<AssetListResponse.Asset> getAssetsBySearchQuery(
            final String searchQuery, final int pageNumber, final String sortInfo)
            throws BadRequestException, DataNotFoundException;

    List<AssetListResponse.Asset> getAvailableAsset(final int pageNumber, final String sortInfo)
            throws DataNotFoundException;

    List<AssetModel> fillData(final String searchQuery, final String sortInfo);

    List<AssetListResponse.Asset> mapAssetsFound(final Set<AssetModel> assetsFound);

    Set<AssetModel> sortData(final String sortInfo, final long stockLimit);

    void insertToDatabase(final MultipartFile[] photos, final String request)
            throws DuplicateDataException, UnauthorizedOperationException, DataNotFoundException;

    String generateAssetSkuCode(
            final String assetBrand, final String assetType, final String assetName);

    void updateAsset(final UpdateAssetRequest.Asset request, final String employeeNik)
            throws UnauthorizedOperationException, DataNotFoundException;

    void deleteAssets(final List<String> assetSkus, final String employeeNik)
            throws UnauthorizedOperationException, BadRequestException, DataNotFoundException;

    AssetModel getAssetData(final String assetSku) throws DataNotFoundException;

    void savePhotos(final MultipartFile[] photos, final String assetSku);

    byte[] getAssetPhoto(final String assetSku, final String assetPhotoName, final String extension, final ClassLoader loader) throws DataNotFoundException;
}
