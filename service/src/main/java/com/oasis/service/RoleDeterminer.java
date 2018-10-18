package com.oasis.service;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;

import java.util.List;

import static com.oasis.service.ServiceConstant.*;

public class RoleDeterminer {
    public String determineRole(EmployeeRepository employeeRepository,
                                SupervisionRepository supervisionRepository,
                                String employeeId) {
        EmployeeModel employee = employeeRepository.findBy_id(employeeId);

        if (employee.getSupervisingCount() == 0) {
            return ROLE_EMPLOYEE;
        } else {
            List<SupervisionModel> supervisions = supervisionRepository.findAllBySupervisorId(employeeId);
            for (SupervisionModel supervision : supervisions) {
                EmployeeModel supervisedEmployee = employeeRepository.findBy_id(supervision.getEmployeeId());
                if (supervisedEmployee.getSupervisingCount() > 0) {
                    return ROLE_ADMINISTRATOR;
                }
            }
        }

        return ROLE_SUPERIOR;
    }
}
