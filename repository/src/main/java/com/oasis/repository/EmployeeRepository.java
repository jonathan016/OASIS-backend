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

    int countAllByDeletedIsFalseAndUsernameIsNot(String username);

    List< EmployeeModel > findAllByDeletedIsFalseAndUsernameIsNotNullOrderByUsernameAsc();

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameIsNotOrderByNameAsc(
            String username, Pageable pageable
    );

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameIsNotOrderByNameDesc(
            String username, Pageable pageable
    );

    EmployeeModel findByDeletedIsFalseAndUsername(String username);

    long countAllByUsernameStartsWith(String username);

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameAsc(
            String username, String name, Pageable pageable
    );

    Page< EmployeeModel > findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameDesc(
            String username, String name, Pageable pageable
    );

    List< EmployeeModel > findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameAsc(
            String username, String name
    );

    List< EmployeeModel > findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameDesc(
            String username, String name
    );

    boolean existsByDeletedIsFalseAndNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(
            String name, Date dob, String phone, String jobTitle, String division, String location
    );

    boolean existsEmployeeModelByDeletedIsFalseAndUsername(String username);

}
