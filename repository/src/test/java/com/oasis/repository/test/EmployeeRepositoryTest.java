package com.oasis.repository.test;

import com.oasis.repository.EmployeeRepository;
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
@EnableMongoRepositories(basePackageClasses = EmployeeRepository.class)
public class EmployeeRepositoryTest {

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
    public void countAllByDeletedIsFalseAndUsernameIsNot() {

    }

    @Test
    public void countAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCase() {

    }

    @Test
    public void findAllByDeletedIsFalseAndUsernameIsNotNullAndDivisionEqualsOrDivisionEqualsOrderByUsernameAsc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndUsernameIsNotOrderByNameAsc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndUsernameIsNotOrderByNameDesc() {

    }

    @Test
    public void findByDeletedIsFalseAndUsernameEquals() {

    }

    @Test
    public void countAllByUsernameStartsWith() {

    }

    @Test
    public void findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameAsc() {

    }

    @Test
    public void findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameDesc() {

    }

    @Test
    public void existsByDeletedIsFalseAndNameEqualsAndDobEqualsAndPhoneEqualsAndJobTitleEqualsAndDivisionEqualsAndLocationEquals() {

    }

    @Test
    public void existsEmployeeModelByDeletedIsFalseAndUsernameEquals() {

    }

    @Test
    public void existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull() {

    }

}