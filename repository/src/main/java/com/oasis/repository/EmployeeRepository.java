package com.oasis.repository;

import com.oasis.model.entity.EmployeeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EmployeeRepository extends MongoRepository<EmployeeModel, String> {

    int countAllByUsernameIsNot(String username);

    List<EmployeeModel> findAllByUsernameIsNotNullOrderByUsernameAsc();

    Page<EmployeeModel> findAllByUsernameIsNotOrderByNameAsc(String username, Pageable pageable);

    Page<EmployeeModel> findAllByUsernameIsNotOrderByNameDesc(String username, Pageable pageable);

    EmployeeModel findByUsername(String username);

    Page<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
            String username,
            String name,
            Pageable pageable
    );

    Page<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
            String username,
            String name,
            Pageable pageable
    );

    List<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
            String username,
            String name
    );

    List<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
            String username,
            String name
    );

    boolean existsByNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(String name, Date dob, String phone,
                                                                        String jobTitle, String division,
                                                                        String location);

    List<EmployeeModel> findAllByNameEquals(String name);

    void deleteByUsername(String username);

    boolean existsEmployeeModelByUsername(String username);

}
