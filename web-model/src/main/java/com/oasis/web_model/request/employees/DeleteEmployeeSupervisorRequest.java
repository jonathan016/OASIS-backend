package com.oasis.web_model.request.employees;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteEmployeeSupervisorRequest {

    private String oldSupervisorUsername;
    private String newSupervisorUsername;

}
