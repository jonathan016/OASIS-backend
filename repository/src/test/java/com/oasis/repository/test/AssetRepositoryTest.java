package com.oasis.repository.test;

import com.oasis.repository.AssetRepository;
import com.oasis.repository.test.configuration.MvcTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({ "mvc-test", "db-test" })
@ContextConfiguration(classes = MvcTestConfiguration.class)
@EnableMongoRepositories(basePackageClasses = AssetRepository.class)
public class AssetRepositoryTest {

    @Before
    public void setUp()
            throws
            Exception {

    }

    @After
    public void tearDown()
            throws
            Exception {

    }

    @Test
    public void findByDeletedIsFalseAndSkuEquals() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanAndSkuIn() {

    }

    @Test
    public void countAllByDeletedIsFalseAndSkuIn() {

    }

    @Test
    public void countAllByDeletedIsFalseAndStockGreaterThan() {

    }

    @Test
    public void countAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCase() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuAsc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuDesc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanOrderByNameAsc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanOrderByNameDesc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderBySkuAsc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderBySkuDesc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderByNameAsc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderByNameDesc() {

    }

    @Test
    public void existsAssetModelByDeletedIsFalseAndNameEqualsAndBrandEqualsAndTypeEquals() {

    }

    @Test
    public void existsAssetModelByDeletedIsFalseAndSkuEquals() {

    }

    @Test
    public void findAllByDeletedIsFalseAndNameContainsIgnoreCase() {

    }

}