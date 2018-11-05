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

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCase(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByNikAsc(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByNikDesc(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByFullnameAsc(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByFullnameDesc(String nik, String fullname);

    EmployeeModel save(EmployeeModel employee);

    EmployeeModel findFirstByDivisionOrderByNikDesc(String division);

    EmployeeModel findFirstByNikContainsAndDivisionOrderByNikDesc(String nik, String division);

    List<EmployeeModel> findAllByFullnameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(String fullname, Date dob, String phone, String jobTitle, String division, String location);

    List<EmployeeModel> findAllByUsernameContains(String username);

    void deleteByNik(String employeeNik);
}
