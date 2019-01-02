package com.oasis.repository;

import com.oasis.model.entity.EmployeeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EmployeeRepository
        extends MongoRepository< EmployeeModel, String > {

    long countAllByDeletedIsFalseAndUsernameIsNot(String username);

    long countAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCase(
            String username, String name
    );

    List< EmployeeModel > findAllByDeletedIsFalseAndUsernameIsNotNullAndDivisionEqualsOrDivisionEqualsOrderByUsernameAsc(
            String division1, String division2
    );

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameIsNotOrderByNameAsc(
            String username, Pageable pageable
    );

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameIsNotOrderByNameDesc(
            String username, Pageable pageable
    );

    EmployeeModel findByDeletedIsFalseAndUsernameEquals(String username);

    long countAllByUsernameStartsWith(String username);

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameAsc(
            String username, String name, Pageable pageable
    );

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameDesc(
            String username, String name, Pageable pageable
    );

    boolean existsByDeletedIsFalseAndNameEqualsAndDobEqualsAndPhoneEqualsAndJobTitleEqualsAndDivisionEqualsAndLocationEquals(
            String name, Date dob, String phone, String jobTitle, String division, String location
    );

    boolean existsEmployeeModelByDeletedIsFalseAndUsernameEquals(String username);

    boolean existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(String username);

}
