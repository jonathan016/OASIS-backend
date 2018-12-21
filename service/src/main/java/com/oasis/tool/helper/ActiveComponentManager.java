package com.oasis.tool.helper;

import com.oasis.exception.DataNotFoundException;
import com.oasis.tool.constant.RoleConstant;
import com.oasis.tool.constant.ServiceConstant;
import com.oasis.tool.constant.StatusConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class ActiveComponentManager {

    private Logger logger = LoggerFactory.getLogger(ActiveComponentManager.class);
    @Autowired
    private RoleDeterminer roleDeterminer;

    public Map< String, Boolean > getAssetsListActiveComponents(String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("addBtn", true);
        activeComponents.put("editBtn", true);
        activeComponents.put("deleteBtn", true);
        activeComponents.put("requestBtn", true);

        return activeComponents;
    }

    public Map< String, Boolean > getAssetDetailActiveComponents(String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("editBtn", true);
        activeComponents.put("deleteBtn", true);
        activeComponents.put("printBtn", true);

        return activeComponents;
    }

    public Map< String, Boolean > getEmployeesListActiveComponents(final String username) {

        String role;

        try {
            role = roleDeterminer.determineRole(username);
        } catch (DataNotFoundException dataNotFoundException) {
            logger.error(
                    "Failed to get active components for requests list data as no employee with given username exists");
            role = null;
        }

        Map< String, Boolean > activeComponents = new HashMap<>();

        if (role != null) {
            activeComponents.put("addBtn", false);
            activeComponents.put("viewBtn", true);
            activeComponents.put("editBtn", false);
            activeComponents.put("deleteBtn", false);

            if (role.equals(RoleConstant.ROLE_ADMINISTRATOR)) {
                activeComponents.put("addBtn", true);
                activeComponents.put("editBtn", true);
                activeComponents.put("deleteBtn", true);
            }
        }

        return activeComponents;
    }

    public Map< String, Boolean > getEmployeeDetailActiveComponents(String role) {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("editBtn", true);
        activeComponents.put("deleteBtn", true);

        return activeComponents;
    }

    public Map< String, Boolean > getRequestsListDataActiveComponents(
            final String tab, final String username, String status
    ) {

        String role;

        try {
            role = roleDeterminer.determineRole(username);
        } catch (DataNotFoundException dataNotFoundException) {
            logger.error(
                    "Failed to get active components for requests list data as no employee with given username exists");
            role = null;
        }

        Map< String, Boolean > activeComponents = new HashMap<>();

        if (role != null) {
            activeComponents.put("cancelBtn", false);
            activeComponents.put("acceptBtn", false);
            activeComponents.put("rejectBtn", false);
            activeComponents.put("deliverBtn", false);
            activeComponents.put("returnBtn", false);

            if (status != null) {
                if (tab.equals(ServiceConstant.TAB_OTHERS)) {
                    if (role.equals(RoleConstant.ROLE_ADMINISTRATOR)) {
                        switch (status) {
                            case StatusConstant.STATUS_REQUESTED:
                                activeComponents.put("acceptBtn", true);
                                activeComponents.put("rejectBtn", true);
                                break;
                            case StatusConstant.STATUS_ACCEPTED:
                                activeComponents.put("deliverBtn", true);
                                break;
                            case StatusConstant.STATUS_DELIVERED:
                                activeComponents.put("returnBtn", true);
                                break;
                        }
                    } else if (role.equals(RoleConstant.ROLE_SUPERIOR)) {
                        if (status.equals(StatusConstant.STATUS_REQUESTED)) {
                            activeComponents.put("acceptBtn", true);
                            activeComponents.put("rejectBtn", true);
                        }
                    }
                } else {
                    if (status.equals(StatusConstant.STATUS_REQUESTED)) {
                        activeComponents.put("cancelBtn", true);
                    }
                }
            }
        }

        return activeComponents;
    }

}
