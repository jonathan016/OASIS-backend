package com.oasis.repository;

import com.oasis.model.entity.EmployeeModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EmployeeRepository extends MongoRepository<EmployeeModel, String> {

    EmployeeModel findByUsername(String username);

    List<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCase(String username, String name);

    List<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByUsernameAsc(String username,
                                                                                                String name);

    List<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByUsernameDesc(String username,
                                                                                                String name);

    List<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(String username,
                                                                                                String name);

    List<EmployeeModel> findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(String username,
                                                                                                String name);

    EmployeeModel findFirstByDivisionOrderByUsernameDesc(String division);

    EmployeeModel findFirstByUsernameContainsOrderByUsernameDesc(String username);

    EmployeeModel findFirstByUsernameContainsAndDivisionOrderByUsernameDesc(String username, String division);

    boolean existsByNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(String name, Date dob, String phone,
                                                                        String jobTitle, String division,
                                                                        String location);

    List<EmployeeModel> findAllByUsernameContains(String username);

    void deleteByUsername(String username);

}
