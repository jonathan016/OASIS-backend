package com.oasis.service.test.implementation.assets.save;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.DuplicateDataException;
import com.oasis.model.exception.UnauthorizedOperationException;
import com.oasis.repository.AssetRepository;
import com.oasis.service.implementation.assets.AssetSaveServiceImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetSaveServiceImplExceptionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private AssetSaveServiceImpl assetSaveService;
    @Mock
    private AssetRepository assetRepository;

    @Before
    public void setUp() {

        when(assetRepository
                     .existsAssetModelByDeletedIsFalseAndNameEqualsAndBrandEqualsAndTypeEquals("name", "brand", "type"))
                .thenReturn(true);
        when(assetRepository.findByDeletedIsFalseAndSkuEquals("SKU-00001-00001-00001")).thenReturn(null);

        final AssetModel asset = generateAssetData("SKU-00001-00001-00002", "brand", "type", "name", "location", 1000,
                                                   2
        );

        when(assetRepository.findByDeletedIsFalseAndSkuEquals("SKU-00001-00001-00002")).thenReturn(asset);
    }

    @Test
    public void saveAsset_NullAssetGiven_ThrowsBadRequestException()
            throws
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException,
            UnauthorizedOperationException {

        thrown.expect(BadRequestException.class);
        assetSaveService.saveAsset(new ArrayList<>(), "admin", null, true);
    }

    @Test
    public void saveAsset_ExistingAssetInDatabase_ThrowsDuplicateDataException()
            throws
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException,
            UnauthorizedOperationException {

        thrown.expect(DuplicateDataException.class);

        final AssetModel asset = generateAssetData(null, "brand", "type", "name", "location", 1000, 2);

        assetSaveService.saveAsset(new ArrayList<>(), "admin", asset, true);
    }

    @Test
    public void saveAsset_NoAssetWithSkuInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException,
            UnauthorizedOperationException {

        thrown.expect(DataNotFoundException.class);

        final AssetModel asset = generateAssetData("SKU-00001-00001-00001", "brand", "type", "name", "location", 1000,
                                                   2
        );

        assetSaveService.saveAsset(new ArrayList<>(), "admin", asset, false);
    }

    @Test
    public void saveAsset_BrandChanged_ThrowsUnauthorizedOperationException()
            throws
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException,
            UnauthorizedOperationException {

        thrown.expect(UnauthorizedOperationException.class);

        final AssetModel asset = generateAssetData("SKU-00001-00001-00002", "brand1", "type", "name", "location", 10000,
                                                   10
        );

        assetSaveService.saveAsset(new ArrayList<>(), "admin", asset, false);
    }

    @Test
    public void saveAsset_TypeChanged_ThrowsUnauthorizedOperationException()
            throws
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException,
            UnauthorizedOperationException {

        thrown.expect(UnauthorizedOperationException.class);

        final AssetModel asset = generateAssetData("SKU-00001-00001-00002", "brand", "type1", "name", "location",
                                                   10000, 10
        );

        assetSaveService.saveAsset(new ArrayList<>(), "admin", asset, false);
    }

    @Test
    public void saveAsset_NameChanged_ThrowsUnauthorizedOperationException()
            throws
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException,
            UnauthorizedOperationException {

        thrown.expect(UnauthorizedOperationException.class);

        final AssetModel asset = generateAssetData("SKU-00001-00001-00002", "brand", "type", "name1", "location", 10000,
                                                   10
        );

        assetSaveService.saveAsset(new ArrayList<>(), "admin", asset, false);
    }

    private AssetModel generateAssetData(
            final String sku, final String brand, final String type, final String name,
            final String location, final double price, final long stock
    ) {

        AssetModel asset = new AssetModel();

        asset.setSku(sku);
        asset.setBrand(brand);
        asset.setType(type);
        asset.setName(name);
        asset.setLocation(location);
        asset.setPrice(price);
        asset.setStock(stock);

        return asset;
    }

}
