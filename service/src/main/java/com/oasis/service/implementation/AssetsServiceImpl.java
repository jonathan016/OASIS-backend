package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.AssetsServiceApi;
import com.oasis.webmodel.request.AddAssetRequest;
import com.oasis.webmodel.request.UpdateAssetRequest;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
public class AssetsServiceImpl implements AssetsServiceApi {

    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private RoleDeterminer roleDeterminer;

    @Override
    public List<AssetListResponse.Asset> getAssetsBySearchQuery(String searchQuery, Integer pageNumber, String sortInfo) throws BadRequestException, DataNotFoundException {
        if (searchQuery.isEmpty()) {
            throw new BadRequestException(EMPTY_SEARCH_QUERY.getErrorCode(), EMPTY_SEARCH_QUERY.getErrorMessage());
        }

        String[] queries = searchQuery.split(" ");
        Set<AssetModel> assetsFound = new HashSet<>();

        if(queries.length == 0){
            if (assetRepository.findAllBySkuContainsOrNameContains(searchQuery, searchQuery).size() == 0) {
                throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
            }

            if ((int) Math.ceil(
                    (float) assetRepository.findAllBySkuContainsOrNameContains(searchQuery, searchQuery).size()
                            / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) < pageNumber) {
                throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
            }

            assetsFound.addAll(fillData(searchQuery, sortInfo));
        } else {
            for(String query : queries){
                if (assetRepository.findAllBySkuContainsOrNameContains(query, query).size() == 0) {
                    throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
                }

                if ((int) Math.ceil(
                        (float) assetRepository.findAllBySkuContainsOrNameContains(query, query).size()
                                / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) < pageNumber) {
                    throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
                }

                assetsFound.addAll(fillData(query, sortInfo));
            }
        }

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

        Set<AssetModel> assetsAvailable = new HashSet<>();

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
    public List<AssetListResponse.Asset> mapAssetsFound(Set<AssetModel> assetsFound) {
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
    public void sortData(Set<AssetModel> assetsAvailable, String sortInfo, long stockLimit) {
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

            asset.setSku(generateAssetSkuCode(assetRequest.getAssetBrand(), assetRequest.getAssetType(), assetRequest.getAssetName()));
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
    public String generateAssetSkuCode(String assetBrand, String assetType, String assetName) {
        StringBuilder sku = new StringBuilder();

        sku.append(ServiceConstant.SKU_PREFIX);

        AssetModel assetWithBrand = assetRepository.findFirstByBrandOrderBySkuDesc(assetBrand);

        if (assetWithBrand != null) {
            AssetModel assetWithType = assetRepository.findFirstByBrandAndTypeOrderBySkuDesc(assetBrand, assetType);
            String lastBrandSku = assetRepository.findFirstBySkuContainsOrderBySkuDesc(String.valueOf(sku)).getSku();
            int lastBrandCode = Integer.valueOf(lastBrandSku.substring(4, 7));

            sku.append(String.format("-%03d", lastBrandCode));

            int lastTypeCode = Integer.valueOf(lastBrandSku.substring(8, 11));

            if (assetWithType != null) {
                sku.append(String.format("-%03d", lastTypeCode));

                int lastProductIdCode = Integer.valueOf(lastBrandSku.substring(12, 15));
                sku.append(String.format("-%03d", lastProductIdCode + 1));
            } else {
                sku.append(String.format("-%03d", lastTypeCode + 1));

                sku.append(String.format("-%03d", 1));
            }
        } else {
            String lastBrandSku = assetRepository.findFirstBySkuContainsOrderBySkuDesc(String.valueOf(sku)).getSku();
            int lastBrandCode = Integer.valueOf(lastBrandSku.substring(4, 7));

            sku.append(String.format("-%03d", lastBrandCode + 1));
            sku.append(String.format("-%03d", 1));
            sku.append(String.format("-%03d", 1));
        }

        return String.valueOf(sku);
    }

    @Override
    public void updateAsset(UpdateAssetRequest.Asset assetRequest, String employeeNik) throws UnauthorizedOperationException, DataNotFoundException {

        try {
            if (!roleDeterminer.determineRole(employeeNik).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
                throw new UnauthorizedOperationException(ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorCode(),
                        ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorMessage());
            }
        } catch (DataNotFoundException e) {
            throw new DataNotFoundException(e.getErrorCode(), e.getErrorMessage());
        }

        AssetModel asset = assetRepository.findBySku(assetRequest.getAssetSku());

        if (asset == null)
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());

        asset.setName(assetRequest.getAssetName());
        asset.setLocation(assetRequest.getAssetLocation());
        asset.setPrice(assetRequest.getAssetPrice());
        asset.setStock(assetRequest.getAssetQty());
        asset.setBrand(assetRequest.getAssetBrand());
        asset.setType(assetRequest.getAssetType());
        asset.setUpdatedBy(employeeNik);
        asset.setUpdatedDate(new Date());

        assetRepository.save(asset);
    }

    @Override
    public void deleteAssets(List<String> assetSkus, String employeeNik) throws UnauthorizedOperationException, BadRequestException, DataNotFoundException {
        try {
            if (!roleDeterminer.determineRole(employeeNik).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
                throw new UnauthorizedOperationException(ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorCode(),
                        ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorMessage());
            }
        } catch (DataNotFoundException e) {
            throw new DataNotFoundException(e.getErrorCode(), e.getErrorMessage());
        }

        if (assetSkus.isEmpty())
            throw new BadRequestException(NO_ASSET_SELECTED.getErrorCode(), NO_ASSET_SELECTED.getErrorMessage());

        List<AssetModel> selectedAssets = new ArrayList<>();
        for (String sku : assetSkus) {
            if (assetRepository.findBySku(sku) == null)
                throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
            if (requestRepository.findAllByAssetSku(sku) != null)
                throw new BadRequestException(SELECTED_ASSET_STILL_REQUESTED.getErrorCode(), SELECTED_ASSET_STILL_REQUESTED.getErrorMessage());
            selectedAssets.add(assetRepository.findBySku(sku));
        }

        for (AssetModel selectedAsset : selectedAssets) {
            assetRepository.delete(selectedAsset);
        }
    }

    @Override
    public AssetModel getAssetData(String assetSku) throws DataNotFoundException {
        AssetModel asset = assetRepository.findBySku(assetSku);

        if(asset == null)
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());

        return asset;
    }
}