package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.AssetsServiceApi;
import com.oasis.webmodel.request.AddAssetRequest;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
public class AssetsServiceImpl implements AssetsServiceApi {

    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private RoleDeterminer roleDeterminer;

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
    public List<AssetListResponse.Asset> getAvailableAsset(int pageNumber, String sortInfo) throws DataNotFoundException {
        if (assetRepository.findAllByStockGreaterThan(ServiceConstant.ZERO).size() == 0) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }
        if ((int) Math.ceil(
                (float) assetRepository.findAllByStockGreaterThan(ServiceConstant.ZERO).size()
                        / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) < pageNumber) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }

        List<AssetModel> assetsAvailable = new ArrayList<>();

        sortData(assetsAvailable, sortInfo, ServiceConstant.ZERO);

        return mapAssetsFound(assetsAvailable);
    }

    @Override
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

    @Override
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

    @Override
    public void sortData(List<AssetModel> assetsAvailable, String sortInfo, long stockLimit) {
        if (sortInfo.substring(1).equals("assetId")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                assetsAvailable.addAll(assetRepository.findAllByStockGreaterThanOrderBySkuAsc(stockLimit));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                assetsAvailable.addAll(assetRepository.findAllByStockGreaterThanOrderBySkuDesc(stockLimit));
            }
        } else if (sortInfo.substring(1).equals("assetName")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                assetsAvailable.addAll(assetRepository.findAllByStockGreaterThanOrderByNameAsc(stockLimit));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                assetsAvailable.addAll(assetRepository.findAllByStockGreaterThanOrderByNameDesc(stockLimit));
            }
        }
    }

    @Override
    public void insertToDatabase(AddAssetRequest.Asset assetRequest, String employeeNik) throws DuplicateDataException, UnauthorizedOperationException, DataNotFoundException {

        try {
            if (!roleDeterminer.determineRole(employeeNik).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
                throw new UnauthorizedOperationException(ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorCode(),
                        ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorMessage());
            }
        } catch (DataNotFoundException e) {
            throw new DataNotFoundException(e.getErrorCode(), e.getErrorMessage());
        }

        if (assetRepository.findByNameAndBrandAndType(assetRequest.getAssetName(), assetRequest.getAssetBrand(), assetRequest.getAssetType()) != null) {
            throw new DuplicateDataException(SAME_ASSET_EXISTS.getErrorCode(), SAME_ASSET_EXISTS.getErrorMessage());
        } else {
            AssetModel asset = new AssetModel();

            asset.setSku(generateAssetSku(assetRequest.getAssetBrand(), assetRequest.getAssetType()));
            asset.setName(assetRequest.getAssetName());
            asset.setLocation(assetRequest.getAssetLocation());
            asset.setPrice(assetRequest.getAssetPrice());
            asset.setStock(assetRequest.getAssetQty());
            asset.setBrand(assetRequest.getAssetBrand());
            asset.setType(assetRequest.getAssetType());
            asset.setCreatedBy(employeeNik);
            asset.setUpdatedBy(employeeNik);
            asset.setCreatedDate(new Date());
            asset.setUpdatedDate(new Date());

            assetRepository.save(asset);
        }
    }

    @Override
    public String generateAssetSku(String assetBrand, String assetType) {
        StringBuilder sku = new StringBuilder();

        sku.append(ServiceConstant.SKU_PREFIX);

        AssetModel asset = assetRepository.findFirstByBrandAndTypeOrderBySkuDesc(assetBrand, assetType);

        if (asset == null) {
            sku.append(generateSkuCode(String.valueOf(sku), null));
            sku.append(generateSkuCode(String.valueOf(sku), null));
            sku.append(generateSkuCode(String.valueOf(sku), null));
        } else {
            sku.append(asset.getSku(), 3, 11);
            sku.append(generateSkuCode(asset.getSku(), "ProductId"));
        }

        return String.valueOf(sku);
    }

    @Override
    public String generateSkuCode(String sku, String target) {
        StringBuilder skuCode = new StringBuilder();
        String lastCode = null;

        if (target != null) {
            if (target.equals("ProductId")) {
                String lastProductIdCode = sku.substring(12, 15);
                lastCode = lastProductIdCode;
            }
        }

        if (lastCode == null)
            return String.format("-%03d", 1);
        int latestCode = Integer.valueOf(lastCode);
        latestCode++;
        skuCode.append(String.format("-%03d", latestCode));

        return String.valueOf(skuCode);
    }
}