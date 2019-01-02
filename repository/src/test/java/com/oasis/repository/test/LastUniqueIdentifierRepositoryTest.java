package com.oasis.repository.test;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.oasis.model.entity.LastUniqueIdentifierModel;
import com.oasis.repository.LastUniqueIdentifierRepository;
import com.oasis.repository.test.configuration.MvcTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.*;

@WebAppConfiguration
@ActiveProfiles({ "mvc-test", "db-test", "security-test" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcTestConfiguration.class)
@EnableMongoRepositories(basePackageClasses = LastUniqueIdentifierRepository.class)
public class LastUniqueIdentifierRepositoryTest {

    final String[] skus = new String[]{ "SKU-00001-00001-00001", "SKU-00001-00001-99999", "SKU-00001-99999-00001",
                                        "SKU-00001-99999-99999", "SKU-99999-00001-00001", "SKU-99999-00001-99999",
                                        "SKU-99999-99999-00001", "SKU-99999-99999-99999" };
    final String[] existingBrands = new String[]{ "Brand 1", "Brand 2", "Brand 3", "Brand 4", "Brand 5", "Brand 6",
                                                  "Brand 7", "Brand 8" };
    final String[] nonExistingBrands = new String[]{ "Brand 11", "Brand 12", "Brand 13", "Brand 14", "Brand 15",
                                                     "Brand 16", "Brand 17", "Brand 18" };
    final String[] types = new String[]{ "Type 1", "Type 2", "Type 3" };

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("oasis-test");
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ApplicationContext applicationContext;

    @Inject
    private LastUniqueIdentifierRepository lastUniqueIdentifierRepository;



    @Before
    public void setUp() {

        mongoTemplate.createCollection("last_unique_identifiers");
    }

    @Test
    public void findByBrandEquals_BrandExists_ReturnsCorrectLastIdentifier() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (final String brand : existingBrands) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEquals(brand);

            assertNotNull(lastUniqueIdentifierModel);
        }
    }

    @Test
    public void findByBrandEquals_BrandDoesNotExist_ReturnsNull() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (final String brand : nonExistingBrands) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEquals(brand);

            assertNull(lastUniqueIdentifierModel);
        }
    }

    @Test
    public void findByBrandEquals_NoDataInDatabase_ReturnsNull() {

        for (final String brand : existingBrands) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEquals(brand);

            assertNull(lastUniqueIdentifierModel);
        }

        for (final String brand : nonExistingBrands) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEquals(brand);

            assertNull(lastUniqueIdentifierModel);
        }
    }

    @Test
    public void existsLastUniqueIdentifierModelByBrandEquals_BrandExists_ReturnsTrue() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (final String brand : existingBrands) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEquals(brand);

            assertTrue(lastUniqueIdentifierExists);
        }
    }

    @Test
    public void existsLastUniqueIdentifierModelByBrandEquals_BrandDoesNotExist_ReturnsFalse() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (final String brand : nonExistingBrands) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEquals(brand);

            assertFalse(lastUniqueIdentifierExists);
        }
    }

    @Test
    public void existsLastUniqueIdentifierModelByBrandEquals_NoDataInDatabase_ReturnsFalse() {

        for (final String brand : existingBrands) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEquals(brand);

            assertFalse(lastUniqueIdentifierExists);
        }

        for (final String brand : nonExistingBrands) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEquals(brand);

            assertFalse(lastUniqueIdentifierExists);
        }
    }

    @Test
    public void existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals_BrandAndTypeExists_ReturnsTrue() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (int i = 0; i < existingBrands.length; i++) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals(existingBrands[ i ], types[ i / 4 ]);

            assertTrue(lastUniqueIdentifierExists);
        }
    }

    @Test
    public void existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals_BrandExistsTypeDoesNotExist_ReturnsFalse() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (int i = 0; i < existingBrands.length; i++) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals(existingBrands[ i ], types[ 2 ]);

            assertFalse(lastUniqueIdentifierExists);
        }
    }

    @Test
    public void existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals_NoDataInDatabase_ReturnsFalse() {

        for (final String existingBrand : existingBrands) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals(existingBrand, types[ 2 ]);

            assertFalse(lastUniqueIdentifierExists);
        }

        for (final String nonExistingBrand : nonExistingBrands) {
            final boolean lastUniqueIdentifierExists = lastUniqueIdentifierRepository
                    .existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals(nonExistingBrand, types[ 2 ]);

            assertFalse(lastUniqueIdentifierExists);
        }
    }

    @Test
    public void findByBrandEqualsAndTypeEquals_BrandAndTypeExists_ReturnsCorrectLastIdentifier() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (int i = 0; i < existingBrands.length; i++) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEqualsAndTypeEquals(existingBrands[ i ], types[ i / 4 ]);

            assertNotNull(lastUniqueIdentifierModel);
        }
    }

    @Test
    public void findByBrandEqualsAndTypeEquals_BrandExistsAndTypeDoesNotExist_ReturnsNull() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (String existingBrand : existingBrands) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEqualsAndTypeEquals(existingBrand, types[ 2 ]);

            assertNull(lastUniqueIdentifierModel);
        }
    }

    @Test
    public void findByBrandEqualsAndTypeEquals_BrandDoesNotExistAndTypeExists_ReturnsNull() {

        fillDatabaseWithLastUniqueIdentifiers();

        for (int i = 0; i < nonExistingBrands.length; i++) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEqualsAndTypeEquals(nonExistingBrands[ i ], types[ i / 4 ]);

            assertNull(lastUniqueIdentifierModel);
        }
    }

    @Test
    public void findByBrandEqualsAndTypeEquals_NoDataInDatabase_ReturnsNull() {

        for (int i = 0; i < existingBrands.length; i++) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEqualsAndTypeEquals(existingBrands[ i ], types[ i / 4 ]);

            assertNull(lastUniqueIdentifierModel);
        }

        for (int i = 0; i < nonExistingBrands.length; i++) {
            final LastUniqueIdentifierModel lastUniqueIdentifierModel = lastUniqueIdentifierRepository
                    .findByBrandEqualsAndTypeEquals(nonExistingBrands[ i ], types[ i / 4 ]);

            assertNull(lastUniqueIdentifierModel);
        }
    }

    @Test
    public void findFirstBySkuContainsOrderBySkuDesc_LastUniqueIdentifierWithSkuExists_ReturnsCorrectLastIdentifier() {

        fillDatabaseWithLastUniqueIdentifiers();

        LastUniqueIdentifierModel firstLastUniqueIdentifier =
                lastUniqueIdentifierRepository.findFirstBySkuContainsOrderBySkuDesc("SKU-00001");

        assertNotNull(firstLastUniqueIdentifier);
        assertEquals(firstLastUniqueIdentifier.getSku(), skus[ 3 ]);

        LastUniqueIdentifierModel secondLastUniqueIdentifier =
                lastUniqueIdentifierRepository.findFirstBySkuContainsOrderBySkuDesc("SKU-99999");

        assertNotNull(secondLastUniqueIdentifier);
        assertEquals(secondLastUniqueIdentifier.getSku(), skus[ 7 ]);
    }

    @Test
    public void findFirstBySkuContainsOrderBySkuDesc_LastUniqueIdentifierWithSkuDoesNotExist_ReturnsNull() {

        fillDatabaseWithLastUniqueIdentifiers();

        LastUniqueIdentifierModel lastUniqueIdentifier =
                lastUniqueIdentifierRepository.findFirstBySkuContainsOrderBySkuDesc("SKU-00002");

        assertNull(lastUniqueIdentifier);
    }

    @After
    public void tearDown()
            throws
            Exception {

        mongoTemplate.dropCollection("last_unique_identifiers");
        mongoTemplate.getDb().drop();
    }

    private List< LastUniqueIdentifierModel > generateLastUniqueIdentifiers() {

        List< LastUniqueIdentifierModel > lastUniqueIdentifiers = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            LastUniqueIdentifierModel lastUniqueIdentifier = new LastUniqueIdentifierModel();

            lastUniqueIdentifier.setSku(skus[ i ]);
            lastUniqueIdentifier.setBrand(existingBrands[ i ]);
            lastUniqueIdentifier.setType(types[ i / 4 ]);
            lastUniqueIdentifier.setCreatedBy("admin");
            lastUniqueIdentifier.setCreatedDate(new Date());
            lastUniqueIdentifier.setUpdatedBy("admin");
            lastUniqueIdentifier.setUpdatedDate(new Date());

            lastUniqueIdentifiers.add(lastUniqueIdentifier);
        }

        return lastUniqueIdentifiers;
    }

    private void fillDatabaseWithLastUniqueIdentifiers() {

        for (final LastUniqueIdentifierModel lastUniqueIdentifier : generateLastUniqueIdentifiers()) {
            mongoTemplate.insert(lastUniqueIdentifier, "last_unique_identifiers");
        }
    }

}