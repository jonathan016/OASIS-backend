package com.oasis.service.implementation.dashboard;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.BaseEntity;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.api.dashboard.DashboardRequestUpdateServiceApi;
import com.oasis.service.api.dashboard.DashboardUtilServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import com.oasis.tool.constant.PageSizeConstant;
import com.oasis.tool.constant.ServiceConstant;
import com.oasis.tool.constant.StatusConstant;
import com.oasis.tool.helper.ImageHelper;
import com.oasis.tool.util.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class DashboardRequestUpdateServiceImpl
        implements DashboardRequestUpdateServiceApi {

    @Autowired
    private AssetUtilServiceApi assetUtilServiceApi;
    @Autowired
    private DashboardUtilServiceApi dashboardUtilServiceApi;
    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Autowired
    private RequestUtilServiceApi requestUtilServiceApi;

    @Autowired
    private ImageHelper imageHelper;



    @Override
    public Map< String, List< ? > > getRequestUpdateSectionData(
            final String username, final String tab, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        if (!username.matches(Regex.REGEX_USERNAME)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (!tab.equals(ServiceConstant.TAB_OTHERS) && !tab.equals(ServiceConstant.TAB_MY)) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            } else {
                if (tab.equals(ServiceConstant.TAB_OTHERS)) {
                    return getOthersRequestListData(username, page);
                } else {
                    return getMyRequestsListData(username, page);
                }
            }
        }
    }

    @Override
    public List< RequestModel > getOthersRequestList(
            final String username, final String status
    )
            throws
            BadRequestException {

        final boolean emptyStatusGiven = ( status != null && status.isEmpty() );

        if (emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            List< SupervisionModel > supervisions = employeeUtilServiceApi
                    .findAllByDeletedIsFalseAndSupervisorUsername(username);

            List< String > supervisedEmployeesUsernames = new ArrayList<>();

            for (final SupervisionModel supervision : supervisions) {
                supervisedEmployeesUsernames.add(supervision.getEmployeeUsername());
            }

            List< RequestModel > requests = new ArrayList<>();

            for (final String supervisedEmployeeUsername : supervisedEmployeesUsernames) {
                boolean administratorWithUsernameExists = employeeUtilServiceApi
                        .existsAdminModelByDeletedIsFalseAndUsernameEquals(supervisedEmployeeUsername);
                boolean supervisorIsValid = employeeUtilServiceApi
                        .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(supervisedEmployeeUsername);
                boolean usernameIsAdminOrSupervisor = ( administratorWithUsernameExists || supervisorIsValid );

                if (usernameIsAdminOrSupervisor) {
                    requests.addAll(getOthersRequestList(supervisedEmployeeUsername, status));
                }

                final boolean viewAllRequestsRegardlessOfStatus = ( status == null );

                if (viewAllRequestsRegardlessOfStatus) {
                    requests.addAll(
                            requestUtilServiceApi.findAllByUsernameOrderByUpdatedDateDesc(supervisedEmployeeUsername));
                } else {
                    requests.addAll(requestUtilServiceApi.findAllByUsernameAndStatusOrderByUpdatedDateDesc(
                            supervisedEmployeeUsername, status));
                }
            }

            requests.sort(Comparator.comparing(BaseEntity::getUpdatedDate).reversed());

            return requests;
        }
    }

    private Map< String, List< ? > > getMyRequestsListData(
            final String username, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > myRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getUsernameRequestsList(username, page);
        final List< EmployeeModel > employees = getEmployeesDataFromRequest(requests);
        final List< EmployeeModel > modifiers = getRequestModifiersDataFromRequest(requests);
        final List< AssetModel > assets = getAssetDataFromRequest(requests);

        myRequestsListData.put("requests", requests);
        myRequestsListData.put("employees", employees);
        myRequestsListData.put("modifiers", modifiers);
        myRequestsListData.put("assets", assets);

        return myRequestsListData;
    }

    private Map< String, List< ? > > getOthersRequestListData(
            final String username, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > othersRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getOthersRequestListPaged(username, page);
        final List< EmployeeModel > employees = getEmployeesDataFromRequest(requests);
        final List< AssetModel > assets = getAssetDataFromRequest(requests);

        othersRequestsListData.put("requests", requests);
        othersRequestsListData.put("employees", employees);
        othersRequestsListData.put("assets", assets);

        return othersRequestsListData;
    }

    @SuppressWarnings({ "ConstantConditions", "UnnecessaryLocalVariable" })
    private List< RequestModel > getUsernameRequestsList(
            final String username, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final long requestsCount = requestUtilServiceApi.countAllByUsernameEqualsAndStatusEquals(
                username, StatusConstant.STATUS_REQUESTED);
        final boolean noRequests = ( requestsCount == 0 );
        final long totalPages = (long) Math.ceil(
                (double) dashboardUtilServiceApi
                        .getRequestsCount("Username", username, StatusConstant.STATUS_REQUESTED) /
                PageSizeConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
        final boolean pageIndexOutOfBounds = ( ( page < 1 ) || ( page > totalPages ) );

        if (noRequests || pageIndexOutOfBounds) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            final int zeroBasedIndexPage = page - 1;
            final Pageable pageable = PageRequest
                    .of(zeroBasedIndexPage, PageSizeConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);

            List< RequestModel > requests = new ArrayList<>(
                    requestUtilServiceApi.findAllByUsernameOrderByUpdatedDateDesc(username, pageable).getContent());

            return requests;
        }
    }

    private List< RequestModel > getOthersRequestListPaged(
            final String username, final int page
    )
            throws
            DataNotFoundException,
            BadRequestException {

        final List< RequestModel > requests = getOthersRequestList(username, StatusConstant.STATUS_REQUESTED);
        final long totalPages = (long) Math
                .ceil((double) requests.size() / PageSizeConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
        final boolean noRequests = requests.isEmpty();
        final boolean pageIndexOutOfBounds = ( ( page < 1 ) || ( page > totalPages ) );

        if (noRequests || pageIndexOutOfBounds) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        PagedListHolder< RequestModel > pagedListHolder = new PagedListHolder<>(new ArrayList<>(requests));
        pagedListHolder.setPage(page - 1);
        pagedListHolder.setPageSize(PageSizeConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);

        return new ArrayList<>(pagedListHolder.getPageList());
    }

    private List< EmployeeModel > getEmployeesDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< EmployeeModel > employees = new ArrayList<>();

        for (final RequestModel request : requests) {
            employees.add(employeeUtilServiceApi.findByDeletedIsFalseAndUsername(request.getUsername()));
            employees.get(employees.size() - 1).setPhoto(
                    getEmployeeDetailPhoto(
                            employees.get(employees.size() - 1).getUsername(),
                            employees.get(employees.size() - 1).getPhoto()
                    ));
        }

        return employees;
    }

    private List< EmployeeModel > getRequestModifiersDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< EmployeeModel > requestModifiers = new ArrayList<>();

        for (final RequestModel request : requests) {
            final String modifierUsername = request.getUpdatedBy();

            requestModifiers.add(employeeUtilServiceApi.findByDeletedIsFalseAndUsername(modifierUsername));
            requestModifiers.get(requestModifiers.size() - 1).setPhoto(
                    getEmployeeDetailPhoto(
                            requestModifiers.get(requestModifiers.size() - 1).getUsername(),
                            requestModifiers.get(requestModifiers.size() - 1).getPhoto()
                    ));
        }

        return requestModifiers;
    }

    private List< AssetModel > getAssetDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< AssetModel > assets = new ArrayList<>();

        for (final RequestModel request : requests) {
            assets.add(assetUtilServiceApi.findByDeletedIsFalseAndSkuEquals(request.getSku()));
            assets.get(assets.size() - 1).setStock(request.getQuantity());
        }

        return assets;
    }

    private String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    ) {

        final boolean validImageLocation = ( photoLocation != null && photoLocation.isEmpty() );

        if (validImageLocation) {
            final File photo = new File(photoLocation);

            if (photo.exists() && Files.exists(photo.toPath())) {
                return "http://localhost:8085/oasis/api/employees/" + username + "/" +
                       username.concat("?extension=").concat(imageHelper.getExtensionFromFileName(photo.getName()));
            }
        }

        return "http://localhost:8085/oasis/api/employees/" + username + "/photo_not_found".concat("?extension=jpg");
    }

}
