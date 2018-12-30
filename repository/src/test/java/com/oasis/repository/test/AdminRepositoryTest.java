package com.oasis.repository.test;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.oasis.model.entity.AdminModel;
import com.oasis.repository.AdminRepository;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@WebAppConfiguration
@ActiveProfiles({"mvc-test", "db-test", "security-test"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcTestConfiguration.class)
@EnableMongoRepositories(basePackageClasses = AdminRepository.class)
public class AdminRepositoryTest {

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("oasis-test");
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private ApplicationContext applicationContext;



    @Inject
    private AdminRepository adminRepository;

    @Before
    public void init() {

        mongoTemplate.insert(generateAdmins(), "admins");
    }

    @Test
    public void findByDeletedIsFalseAndUsernameEquals_AdminExists_AdminNotNull() {

        AdminModel admin = adminRepository.findByDeletedIsFalseAndUsernameEquals("jonathan");

        assertNotNull(admin);
    }

    @Test
    public void findByDeletedIsFalseAndUsernameEquals_AdminDoesNotExist_AdminNull() {

        AdminModel admin = adminRepository.findByDeletedIsFalseAndUsernameEquals("a.wan");

        assertNull(admin);
    }

    @Test
    public void findByDeletedIsFalseAndUsernameEquals_AdminIsDeleted_AdminNull() {

        AdminModel admin = adminRepository.findByDeletedIsFalseAndUsernameEquals("d.william");

        assertNull(admin);
    }

    @Test
    public void findByDeletedIsTrueAndUsernameEquals_AdminExists_AdminNotNull() {

        AdminModel admin = adminRepository.findByDeletedIsTrueAndUsernameEquals("d.william");

        assertNotNull(admin);
    }

    @Test
    public void findByDeletedIsTrueAndUsernameEquals_AdminDoesNotExist_AdminNull() {

        AdminModel admin = adminRepository.findByDeletedIsTrueAndUsernameEquals("a.wan");

        assertNull(admin);
    }

    @Test
    public void findByDeletedIsTrueAndUsernameEquals_AdminIsNotDeleted_AdminNull() {

        AdminModel admin = adminRepository.findByDeletedIsTrueAndUsernameEquals("s.tan");

        assertNull(admin);
    }

    @Test
    public void existsAdminModelByDeletedIsFalseAndUsernameEquals_AdminExists_True() {

        boolean adminExist = adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals("admin");

        assertTrue(adminExist);
    }

    @Test
    public void existsAdminModelByDeletedIsFalseAndUsernameEquals_AdminDoesNotExist_False() {

        boolean adminExist = adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals("a.wan");

        assertFalse(adminExist);
    }

    @Test
    public void existsAdminModelByDeletedIsFalseAndUsernameEquals_AdminIsDeleted_False() {

        boolean adminExist = adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals("s.dewanto");

        assertFalse(adminExist);
    }

    @Test
    public void existsAdminModelByDeletedIsTrueAndUsernameEquals_AdminExists_True() {

        boolean adminExist = adminRepository.existsAdminModelByDeletedIsTrueAndUsernameEquals("s.dewanto");

        assertTrue(adminExist);
    }

    @Test
    public void existsAdminModelByDeletedIsTrueAndUsernameEquals_AdminDoesNotExist_False() {

        boolean adminExist = adminRepository.existsAdminModelByDeletedIsTrueAndUsernameEquals("a.wan");

        assertFalse(adminExist);
    }

    @Test
    public void existsAdminModelByDeletedIsTrueAndUsernameEquals_AdminIsNotDeleted_False() {

        boolean adminExist = adminRepository.existsAdminModelByDeletedIsTrueAndUsernameEquals("jonathan");

        assertFalse(adminExist);
    }

    @After
    public void destroy() {

        mongoTemplate.getDb().drop();
    }

    private List< AdminModel > generateAdmins() {

        List< AdminModel > admins = new ArrayList<>();

        String[] names = new String[]{"admin", "jonathan", "s.tan", "d.william", "s.dewanto"};
        String[] password = new String[]{"admin", "jonathan", "stellitan", "davidwilliam", "stephendewanto"};
        boolean[] deleted = new boolean[]{false,false,false,true,true};

        for(int i = 0; i < names.length; i++) {
            AdminModel admin = new AdminModel();

            admin.setUsername(names[i]);
            admin.setPassword(encoder.encode(password[i]));
            admin.setDeleted(deleted[i]);
            admin.setCreatedDate(new Date());
            admin.setUpdatedDate(new Date());

            if (i == 0) {
                admin.setCreatedBy("");
                admin.setUpdatedBy("");
            } else {
                admin.setCreatedBy(names[0]);
                admin.setUpdatedBy(names[0]);
            }

            admins.add(admin);
        }

        return admins;
    }
}
