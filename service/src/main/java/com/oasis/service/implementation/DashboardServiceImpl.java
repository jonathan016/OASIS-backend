package com.oasis.service.implementation;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.RoleDeterminer;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.DashboardServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardServiceApi {
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private SupervisionRepository supervisionRepository;

    @Override
    public int getAvailableAssetsCount() {
        return assetRepository.findAllByStockGreaterThan(ServiceConstant.ZERO).size();
    }

    @Override
    public List<RequestModel> getMyPendingHandoverRequests(String employeeId) {
        return requestRepository.findAllByEmployeeIdAndStatus(employeeId, ServiceConstant.PENDING_HANDOVER);
    }

    @Override
    public List<RequestModel> getMyRequestedRequests(String employeeId) {
        return requestRepository.findAllByEmployeeIdAndStatus(employeeId, ServiceConstant.REQUESTED);
    }

    @Override
    public List<RequestModel> getMyAssignedRequestedRequests(String supervisorId) {
        List<String> supervisedEmployeeIdList = getSupervisedEmployeeIdList(supervisorId);

        return getRequestsList(ServiceConstant.REQUESTED, supervisedEmployeeIdList);
    }

    @Override
    public List<RequestModel> getMyAssignedPendingHandoverRequests(String supervisorId) {
        List<String> supervisedEmployeeIdList = getSupervisedEmployeeIdList(supervisorId);

        return getRequestsList(ServiceConstant.PENDING_HANDOVER, supervisedEmployeeIdList);
    }

    @Override
    public AssetModel getAssetData(String assetId) {
        return assetRepository.findBy_id(assetId);
    }

    @Override
    public EmployeeModel getEmployeeData(String employeeId) {
        return employeeRepository.findBy_id(employeeId);
    }

    @Override
    public SupervisionModel getEmployeeSupervisorData(String employeeId) {
        return supervisionRepository.findByEmployeeId(employeeId);
    }

    @Override
    public String determineUserRole(String employeeId) {
        RoleDeterminer roleDeterminer = new RoleDeterminer();
        return roleDeterminer.determineRole(employeeRepository, supervisionRepository, employeeId);
    }

    private List<String> getSupervisedEmployeeIdList(String supervisorId) {
        List<SupervisionModel> supervisions = supervisionRepository.findAllBySupervisorId(supervisorId);

        List<String> supervisedEmployeeIdList = new ArrayList<>();
        for (SupervisionModel supervision : supervisions) {
            supervisedEmployeeIdList.add(supervision.getEmployeeId());
        }

        return supervisedEmployeeIdList;
    }

    private List<RequestModel> getRequestsList(String requestStatus, List<String> supervisedEmployeeIdList) {
        List<RequestModel> assignedRequests = new ArrayList<>();
        for (String supervisedEmployeeId : supervisedEmployeeIdList) {
            EmployeeModel employee = employeeRepository.findBy_id(supervisedEmployeeId);
            if (employee.getSupervisingCount() > 0) {
                if (requestStatus.equals(ServiceConstant.PENDING_HANDOVER)) {
                    assignedRequests.addAll(getMyAssignedPendingHandoverRequests(supervisedEmployeeId));
                } else {
                    assignedRequests.addAll(getMyAssignedRequestedRequests(supervisedEmployeeId));
                }
            } else {
                assignedRequests.addAll(
                        requestRepository.findAllByEmployeeIdAndStatus(
                                supervisedEmployeeId,
                                requestStatus
                        )
                );
            }
        }

        return assignedRequests;
    }
}
