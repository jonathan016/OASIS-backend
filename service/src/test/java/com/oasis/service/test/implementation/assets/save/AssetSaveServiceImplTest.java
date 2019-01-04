package com.oasis.service.test.implementation.assets.save;

import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.LastUniqueIdentifierRepository;
import com.oasis.service.implementation.assets.AssetSaveServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class AssetSaveServiceImplTest {

    @InjectMocks
    private AssetSaveServiceImpl assetSaveService;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private LastUniqueIdentifierRepository lastUniqueIdentifierRepository;

    @Before
    public void setUp()
            throws
            Exception {

    }

    @Test
    public void saveAsset_AddAsset_SavedAssetSuccessfully() {

    }

    @After
    public void tearDown()
            throws
            Exception {

        verifyNoMoreInteractions(assetRepository);
        verifyNoMoreInteractions(lastUniqueIdentifierRepository);
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