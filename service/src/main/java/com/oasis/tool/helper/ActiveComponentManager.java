package com.oasis.tool.helper;

import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.tool.constant.RoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@SuppressWarnings({ "SpringJavaAutowiredFieldsWarningInspection", "Duplicates" })
public class ActiveComponentManager {

    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;



    public Map< String, Boolean > getDashboardActiveComponents(final String username, final String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        switch (role) {
            case RoleConstant.ROLE_EMPLOYEE:
                activeComponents.put("btnNewRequestChangeTab", false);
                activeComponents.put("sectionNewRequestOthers", false);
                break;
            case RoleConstant.ROLE_SUPERIOR:
                activeComponents.put("btnNewRequestChangeTab", true);
                activeComponents.put("sectionNewRequestOthers", true);
                break;
            case RoleConstant.ROLE_ADMINISTRATOR:
                activeComponents.put("btnNewRequestChangeTab", true);
                activeComponents.put("sectionNewRequestOthers", true);
                break;
        }

        if (employeeUtilServiceApi.isEmployeeTopAdministrator(username)) {
            activeComponents.put("btnNewRequestChangeTab", false);
            activeComponents.put("sectionNewRequestMy", false);
        } else {
            activeComponents.put("sectionNewRequestMy", true);
        }

        return activeComponents;
    }

    public Map< String, Boolean > getAssetsListActiveComponents(final String username, final String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        if (role.equals(RoleConstant.ROLE_ADMINISTRATOR)) {
            activeComponents.put("btnAssetAddNew", true);
            activeComponents.put("btnAssetEdit", true);
            activeComponents.put("btnAssetDeleteBulk", true);
        } else {
            activeComponents.put("btnAssetAddNew", false);
            activeComponents.put("btnAssetEdit", false);
            activeComponents.put("btnAssetDeleteBulk", false);
        }

        if (employeeUtilServiceApi.isEmployeeTopAdministrator(username)) {
            activeComponents.put("btnAssetRequest", false);
        } else {
            activeComponents.put("btnAssetRequest", true);
        }

        return activeComponents;
    }

    public Map< String, Boolean > getAssetDetailActiveComponents(final String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        if (role.equals(RoleConstant.ROLE_ADMINISTRATOR)) {
            activeComponents.put("btnAssetDetailEditDelete", true);
        } else {
            activeComponents.put("btnAssetDetailEditDelete", false);
        }

        return activeComponents;
    }

    public Map< String, Boolean > getEmployeesListActiveComponents(final String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("sectionEmployeeUpper", true);
        activeComponents.put("btnEmployeeAdd", false);
        activeComponents.put("btnEmployeeEdit", false);
        activeComponents.put("btnEmployeeDelete", false);

        if (role.equals(RoleConstant.ROLE_ADMINISTRATOR)) {
            activeComponents.put("sectionEmployeeUpper", false);
            activeComponents.put("btnEmployeeAdd", true);
            activeComponents.put("btnEmployeeEdit", true);
            activeComponents.put("btnEmployeeDelete", true);
        }

        return activeComponents;
    }

    public Map< String, Boolean > getEmployeeDetailActiveComponents(final String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        if (role.equals(RoleConstant.ROLE_ADMINISTRATOR)) {
            activeComponents.put("btnEmployeeDetailEditDelete", true);
        } else {
            activeComponents.put("btnEmployeeDetailEditDelete", false);
        }

        return activeComponents;
    }

    public Map< String, Boolean > getSideBarActiveComponents(final String username, final String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        if (employeeUtilServiceApi.isEmployeeTopAdministrator(username)) {
            activeComponents.put("sidebarRequestMy", false);
        } else {
            activeComponents.put("sidebarRequestMy", true);
        }

        if (role.equals(RoleConstant.ROLE_EMPLOYEE)) {
            activeComponents.put("sidebarRequestOther", false);
        } else {
            activeComponents.put("sidebarRequestOther", true);
        }

        return activeComponents;
    }

}
