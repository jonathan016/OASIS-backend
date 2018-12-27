package com.oasis.service.implementation.assets;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.service.api.assets.AssetDeleteServiceApi;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetDeleteServiceImpl
        implements AssetDeleteServiceApi {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private RequestUtilServiceApi requestUtilServiceApi;



    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @CacheEvict(value = { "availableAssetsList", "assetDetailData" },
                allEntries = true)
    public void deleteAssets(
            final List< String > skus, final String username
    )
            throws
            BadRequestException,
            DataNotFoundException {

        if (skus.isEmpty() || skus.contains(null)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            List< AssetModel > selectedAssets = new ArrayList<>();

            for (final String sku : skus) {
                final AssetModel asset = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);
                final boolean assetRequested = requestUtilServiceApi.existsRequestModelsBySku(sku);

                if (asset == null) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else if (assetRequested) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    selectedAssets.add(asset);
                }
            }

            for (AssetModel selectedAsset : selectedAssets) {
                selectedAsset.setDeleted(true);
            }

            assetRepository.saveAll(selectedAssets);
        }
    }

}
