package com.oasis.service.implementation.assets;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.service.api.assets.AssetListServiceApi;
import com.oasis.model.constant.service_constant.PageSizeConstant;
import com.oasis.model.constant.service_constant.ServiceConstant;
import com.oasis.service.tool.util.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetListServiceImpl
        implements AssetListServiceApi {

    @Autowired
    private AssetRepository assetRepository;



    @Override
    @Cacheable(value = "availableAssetsList",
               unless = "#result.size() == 0")
    public List< AssetModel > getAvailableAssetsList(
            final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyQueryGiven = ( ( query != null ) && query.isEmpty() );

        if (emptyQueryGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else if (sort != null && !sort.matches(Regex.REGEX_ASSET_SORT)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (sort == null) {
                sort = "A-name";
            }

            final Set< AssetModel > availableAssets;

            final long availableAssetsCount = getAvailableAssetsCount(query, sort);
            final long availablePages = (long) Math.ceil(
                    (double) availableAssetsCount / PageSizeConstant.ASSETS_LIST_PAGE_SIZE);

            final boolean noQueryGiven = ( query == null );
            final boolean noAvailableAsset = ( availableAssetsCount == 0 );
            final boolean pageIndexOutOfBounds = ( ( page < 1 ) || ( page > availablePages ) );

            if (noAvailableAsset || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                if (noQueryGiven) {
                    availableAssets = new LinkedHashSet<>(getSortedAvailableAssets(page, sort));
                } else {
                    availableAssets = new LinkedHashSet<>(getSortedAvailableAssetsFromQuery(page, query, sort));
                }

                return new ArrayList<>(availableAssets);
            }
        }
    }

    @Override
    public long getAvailableAssetsCount(
            final String query, final String sort
    ) {

        final boolean noQueryGiven = ( query == null );

        if (noQueryGiven) {
            return assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);
        } else {
            return assetRepository
                    .countAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCase(
                            ServiceConstant.ZERO, query, ServiceConstant.ZERO, query);
        }
    }

    private Set< AssetModel > getSortedAvailableAssets(
            final int page, final String sort
    ) {

        Set< AssetModel > sortedAvailableAssets = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, PageSizeConstant.ASSETS_LIST_PAGE_SIZE);

        if (sort.substring(2).equals("SKU")) {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuAsc(
                        ServiceConstant.ZERO, pageable).getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuDesc(
                        ServiceConstant.ZERO, pageable).getContent());
            }
        } else {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderByNameAsc(
                        ServiceConstant.ZERO, pageable).getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderByNameDesc(
                        ServiceConstant.ZERO, pageable).getContent());
            }
        }

        return sortedAvailableAssets;
    }

    private Set< AssetModel > getSortedAvailableAssetsFromQuery(
            final int page, final String query, final String sort
    ) {

        Set< AssetModel > sortedAvailableAssets = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, PageSizeConstant.ASSETS_LIST_PAGE_SIZE);

        if (sort.substring(2).equals("SKU")) {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderBySkuAsc(
                                                             ServiceConstant.ZERO, query, ServiceConstant.ZERO, query,
                                                             pageable
                                                     )
                                                     .getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderBySkuDesc(
                                                             ServiceConstant.ZERO, query, ServiceConstant.ZERO, query,
                                                             pageable
                                                     )
                                                     .getContent());
            }
        } else {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderByNameAsc(
                                                             ServiceConstant.ZERO, query, ServiceConstant.ZERO, query,
                                                             pageable
                                                     )
                                                     .getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderByNameDesc(
                                                             ServiceConstant.ZERO, query, ServiceConstant.ZERO, query,
                                                             pageable
                                                     )
                                                     .getContent());
            }
        }

        return sortedAvailableAssets;
    }

}
