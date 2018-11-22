package com.oasis.repository;

import com.oasis.model.entity.EmployeeModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EmployeeRepository extends MongoRepository<EmployeeModel, String> {

    EmployeeModel findByUsername(String username);

    EmployeeModel findByNik(String nik);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCase(String nik, String name);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNikAsc(String nik, String name);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNikDesc(String nik, String name);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(String nik, String name);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(String nik, String name);

    @SuppressWarnings("unchecked")
    EmployeeModel save(EmployeeModel employee);

    EmployeeModel findFirstByDivisionOrderByNikDesc(String division);

    EmployeeModel findFirstByNikContainsOrderByNikDesc(String nik);

    EmployeeModel findFirstByNikContainsAndDivisionOrderByNikDesc(String nik, String division);

    boolean existsByNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(String name, Date dob, String phone,
                                                                        String jobTitle, String division,
                                                                        String location);

    List<EmployeeModel> findAllByUsernameContains(String username);

    void deleteByNik(String employeeNik);

}
