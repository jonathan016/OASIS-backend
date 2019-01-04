package com.oasis.service.implementation.assets;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UnauthorizedOperationException;
import com.oasis.repository.AssetRepository;
import com.oasis.service.api.assets.AssetDeleteServiceApi;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;
import static com.oasis.model.constant.service_constant.StatusConstant.STATUS_ACCEPTED;
import static com.oasis.model.constant.service_constant.StatusConstant.STATUS_DELIVERED;

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
            DataNotFoundException,
            UnauthorizedOperationException {

        if (skus.isEmpty() || skus.contains(null)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            List< AssetModel > selectedAssets = new ArrayList<>();

            for (final String sku : skus) {
                final AssetModel asset = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);
                final boolean assetRequested = requestUtilServiceApi.existsRequestModelsBySkuAndStatusIn(
                        sku,
                        Arrays.asList(STATUS_ACCEPTED, STATUS_DELIVERED)
                );

                if (asset == null) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else if (assetRequested) {
                    throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
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
