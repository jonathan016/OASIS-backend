package com.oasis.repository;

import com.oasis.model.entity.EmployeeModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends MongoRepository<EmployeeModel, String> {

    EmployeeModel findByUsername(String username);

    EmployeeModel findByNik(String nik);

    List<EmployeeModel> findAllByNikContainsOrFullnameContains(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsOrFullnameContainsOrderByNikAsc(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsOrFullnameContainsOrderByNikDesc(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsOrFullnameContainsOrderByFullnameAsc(String nik, String fullname);

    List<EmployeeModel> findAllByNikContainsOrFullnameContainsOrderByFullnameDesc(String nik, String fullname);
}
