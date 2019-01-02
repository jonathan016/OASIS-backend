package com.oasis.repository.test;

import com.oasis.repository.RequestRepository;
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
@EnableMongoRepositories(basePackageClasses = RequestRepository.class)
public class RequestRepositoryTest {

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
    public void findAllBySkuEqualsAndStatusEquals() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEquals() {

    }

    @Test
    public void existsRequestModelsBySkuEquals() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusContainsOrderByStatusAsc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusContainsOrderByStatusDesc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateAsc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateAsc1() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc1() {

    }

    @Test
    public void countAllByUsernameEqualsAndStatusEquals() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByStatusAsc() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByStatusDesc() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByUpdatedDateAsc() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByUpdatedDateDesc() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByStatusAsc1() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByStatusDesc1() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByUpdatedDateAsc1() {

    }

    @Test
    public void findAllByUsernameEqualsOrderByUpdatedDateDesc1() {

    }

    @Test
    public void countAllByUsernameEquals() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc1() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc1() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc1() {

    }

    @Test
    public void findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc1() {

    }

    @Test
    public void countAllByUsernameEqualsAndSkuContainsIgnoreCase() {

    }

    @Test
    public void countAllByUsernameEqualsAndSkuContainsIgnoreCaseAndUsernameEqualsAndStatusEquals() {

    }

    @Test
    public void findBy_id() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc1() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc1() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc1() {

    }

    @Test
    public void findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc1() {

    }

}