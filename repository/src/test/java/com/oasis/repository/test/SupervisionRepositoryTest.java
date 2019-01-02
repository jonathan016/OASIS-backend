package com.oasis.repository.test;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.SupervisionRepository;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({ "mvc-test", "db-test" })
@ContextConfiguration(classes = MvcTestConfiguration.class)
@EnableMongoRepositories(basePackageClasses = SupervisionRepository.class)
public class SupervisionRepositoryTest {

    final String[] undeletedEmployeeUsernames = new String[]{ "d.grayson", "t.drake", "c.marks", "c.kent1" };
    final String[] undeletedSupervisorUsernames = new String[]{ "b.wayne", "b.wayne", "d.prince", "c.kent" };
    final String[] deletedEmployeeUsernames = new String[]{ "j.todd", "d.troy", "j.henry" };
    final String[] deletedSupervisorUsernames = new String[]{ "h.dent", "b.manta", "l.luthor" };

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("oasis-test");
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ApplicationContext applicationContext;

    @Inject
    private SupervisionRepository supervisionRepository;



    @Before
    public void setUp()
            throws
            Exception {

        mongoTemplate.createCollection("supervisions");
    }

    @Test
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void findAllByDeletedIsFalseAndSupervisorUsernameEquals_SupervisionWithSupervisorUsernameExists_ReturnsListOfSupervisions() {

        fillDatabaseWithSupervisions();

        for (final String undeletedSupervisorUsername : undeletedSupervisorUsernames) {
            List< SupervisionModel > supervisionsBySupervisorUsername =
                    supervisionRepository.findAllByDeletedIsFalseAndSupervisorUsernameEquals(
                            undeletedSupervisorUsername);

            int count = 0;

            for (int i = 0; i < undeletedSupervisorUsernames.length; i++) {
                if (undeletedSupervisorUsername.equals(undeletedSupervisorUsernames[ i ])) {
                    count++;
                }
            }

            assertEquals(supervisionsBySupervisorUsername.size(), count);
        }
    }

    @Test
    public void findByDeletedIsFalseAndEmployeeUsernameEquals_SupervisionWithEmployeeUsernameExists_ReturnsSupervisionModel() {

        fillDatabaseWithSupervisions();

        for (final String undeletedEmployeeUsername : undeletedEmployeeUsernames) {
            final SupervisionModel supervision = supervisionRepository.findByDeletedIsFalseAndEmployeeUsernameEquals(
                    undeletedEmployeeUsername);

            assertNotNull(supervision);
        }
    }

    @Test
    public void existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals_SupervisionWithSupervisorUsernameExists_ReturnsTrue() {

        fillDatabaseWithSupervisions();

        for (final String undeletedSupervisorUsername : undeletedSupervisorUsernames) {
            final boolean supervisionExists =
                    supervisionRepository.existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(
                            undeletedSupervisorUsername);

            assertTrue(supervisionExists);
        }
    }

    @Test
    public void existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameEqualsAndEmployeeUsernameEquals_SupervisionWithSupervisorUsernameAndEmployeeUsernameExists_ReturnsTrue() {

        fillDatabaseWithSupervisions();

        for (int i = 0; i < undeletedSupervisorUsernames.length; i++) {
            final boolean supervisionExists =
                    supervisionRepository
                            .existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameEqualsAndEmployeeUsernameEquals(
                                    undeletedSupervisorUsernames[ i ], undeletedEmployeeUsernames[ i ]);

            assertTrue(supervisionExists);
        }
    }

    @After
    public void tearDown()
            throws
            Exception {

        mongoTemplate.dropCollection("supervisions");
        mongoTemplate.getDb().drop();
    }

    private List< SupervisionModel > generateSupervisionModels() {

        List< SupervisionModel > supervisions = new ArrayList<>();

        for (int i = 0; i < undeletedEmployeeUsernames.length; i++) {
            SupervisionModel supervision = new SupervisionModel();

            supervision.setEmployeeUsername(undeletedEmployeeUsernames[ i ]);
            supervision.setSupervisorUsername(undeletedSupervisorUsernames[ i ]);
            supervision.setDeleted(false);
            supervision.setCreatedBy("admin");
            supervision.setCreatedDate(new Date());
            supervision.setUpdatedBy("admin");
            supervision.setUpdatedDate(new Date());

            supervisions.add(supervision);
        }

        for (int i = 0; i < deletedEmployeeUsernames.length; i++) {
            SupervisionModel supervision = new SupervisionModel();

            supervision.setEmployeeUsername(deletedEmployeeUsernames[ i ]);
            supervision.setSupervisorUsername(deletedSupervisorUsernames[ i ]);
            supervision.setDeleted(true);
            supervision.setCreatedBy("admin");
            supervision.setCreatedDate(new Date());
            supervision.setUpdatedBy("admin");
            supervision.setUpdatedDate(new Date());

            supervisions.add(supervision);
        }

        return supervisions;
    }

    private void fillDatabaseWithSupervisions() {

        for (final SupervisionModel supervision : generateSupervisionModels()) {
            mongoTemplate.insert(supervision, "supervisions");
        }
    }

}