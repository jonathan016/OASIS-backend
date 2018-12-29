package com.oasis.service.implementation.requests;

import com.oasis.exception.BadRequestException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.repository.RequestRepository;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.api.requests.RequestOthersListServiceApi;
import com.oasis.service.api.requests.RequestListServiceApi;
import com.oasis.tool.helper.ImageHelper;
import com.oasis.tool.util.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestListServiceImpl
        implements RequestListServiceApi {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AssetUtilServiceApi assetUtilServiceApi;
    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Autowired
    private RequestOthersListServiceApi requestOthersListServiceApi;

    @Autowired
    private ImageHelper imageHelper;



    @Override
    public long getRequestsCount(
            final String type, final String username, final String query, final String status, final int page,
            final String sort
    )
            throws
            BadRequestException {

        final boolean emptyQueryGiven = ( query != null && query.isEmpty() );
        final boolean emptyStatusGiven = ( status != null && status.isEmpty() );

        if (emptyQueryGiven || emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (type.equals("Username")) {
                final boolean viewAllRequestsRegardlessOfStatus = ( status == null );
                final boolean viewAllRequests = ( query == null );

                if (viewAllRequestsRegardlessOfStatus) {
                    if (viewAllRequests) {
                        return requestRepository.countAllByUsernameEquals(username);
                    } else {
                        long requestCount = 0;

                        List< AssetModel > assets = assetUtilServiceApi
                                .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);

                        for (final AssetModel asset : assets) {
                            requestCount += requestRepository
                                    .countAllByUsernameEqualsAndSkuContainsIgnoreCase(username, asset.getSku());
                        }

                        return requestCount;
                    }
                } else {
                    if (viewAllRequests) {
                        return requestRepository.countAllByUsernameEqualsAndStatusEquals(username, status);
                    } else {
                        long requestCount = 0;

                        List< AssetModel > assets = assetUtilServiceApi
                                .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);

                        for (final AssetModel asset : assets) {
                            requestCount += requestRepository
                                    .countAllByUsernameEqualsAndSkuContainsIgnoreCaseAndUsernameEqualsAndStatusEquals(
                                            username, asset.getSku(), username, status);
                        }

                        return requestCount;
                    }
                }
            } else {
                if (type.equals("Others")) {
                    return requestOthersListServiceApi.getOthersRequestList(username, query, status, sort).size();
                }
            }

            return -1;
        }
    }

    @Override
    public List< EmployeeModel > getEmployeesDataFromRequest(
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

    @Override
    public List< AssetModel > getAssetDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< AssetModel > assets = new ArrayList<>();

        for (final RequestModel request : requests) {
            assets.add(assetUtilServiceApi.findByDeletedIsFalseAndSkuEquals(request.getSku()));
            assets.get(assets.size() - 1).setStock(request.getQuantity());
        }

        return assets;
    }

    @Override
    public String getEmployeeDetailPhoto(
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

    @Override
    public String validateSortInformationGiven(String sort)
            throws
            BadRequestException {

        final boolean useDefaultSort = ( sort == null );

        if (useDefaultSort) {
            sort = "D-updatedDate";
        } else {
            final boolean properSortFormatGiven = sort.matches(Regex.REGEX_REQUEST_SORT);

            if (!properSortFormatGiven) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            }
        }
        return sort;
    }

}
