package com.oasis.service.implementation;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.exception.helper.BaseError;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.repository.*;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.RequestsServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestsServiceImpl implements RequestsServiceApi {

    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;
    @Autowired
    private AdminRepository adminRepository;

    /*-------------Requests List Methods-------------*/
    @Override
    public List<RequestModel> getRequestsList(
            final String username,
            final String query,
            final int page,
            final String sort
    ) throws BadRequestException, DataNotFoundException {

        if (query != null && query.equals("defaultQuery")) {
            throw new BadRequestException(EMPTY_SEARCH_QUERY);
        }

        long foundDataSize = requestRepository.countAllByUsername(username);

        if (page < 1 || foundDataSize == 0 ||
            (int) Math.ceil((double) getRequestsCount(username, query)
                            / ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE) < page) {
            throw new DataNotFoundException(ASSET_NOT_FOUND);
        }

        if (query == null) {

            switch (sort.substring(0, 1)) {
                case ServiceConstant.ASCENDING:
                    if (sort.substring(2).equals("status")) {
                        return requestRepository.findAllByUsernameOrderByStatusAsc(username, PageRequest.of(
                                page - 1,
                                ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                        )).getContent();
                    } else if (sort.substring(2).equals("updatedDate")) {
                        return requestRepository.findAllByUsernameOrderByUpdatedDateAsc(username, PageRequest.of(
                                page - 1,
                                ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                        )).getContent();

                    }
                    break;
                case ServiceConstant.DESCENDING:
                    if (sort.substring(2).equals("status")) {
                        return requestRepository.findAllByUsernameOrderByStatusDesc(username, PageRequest.of(
                                page - 1,
                                ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                        )).getContent();
                    } else if (sort.substring(2).equals("updatedDate")) {
                        return requestRepository.findAllByUsernameOrderByUpdatedDateDesc(username, PageRequest.of(
                                page - 1,
                                ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                        )).getContent();

                    }
                    break;
            }
        } else {
            switch (sort.substring(0, 1)) {
                case ServiceConstant.ASCENDING:
                    if (sort.substring(2).equals("status")) {
                        return requestRepository.findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusAsc(
                                username,
                                query,
                                query,
                                PageRequest.of(
                                page - 1,
                                ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                        )).getContent();
                    } else if (sort.substring(2).equals("updatedDate")) {
                        return requestRepository.findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                username,
                                query,
                                query,
                                PageRequest.of(
                                        page - 1,
                                        ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                                )).getContent();

                    }
                    break;
                case ServiceConstant.DESCENDING:
                    if (sort.substring(2).equals("status")) {
                        return requestRepository.findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusDesc(
                                username,
                                query,
                                query,
                                PageRequest.of(
                                        page - 1,
                                        ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                                )).getContent();
                    } else if (sort.substring(2).equals("updatedDate")) {
                        return requestRepository.findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                username,
                                query,
                                username,
                                query,
                                PageRequest.of(
                                        page - 1,
                                        ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE
                                )).getContent();

                    }
                    break;
            }
        }

        return new ArrayList<>();
    }

    @Override
    public List<EmployeeModel> getEmployeeDataFromRequest(
            final List<RequestModel> requests
    ) {

        List<EmployeeModel> employees = new ArrayList<>();
        for (RequestModel request : requests) {
            employees.add(employeeRepository.findByUsername(request.getUsername()));
            employees.get(employees.size() - 1).setPhoto(
                    getEmployeeDetailPhoto(
                            employees.get(employees.size() - 1).getUsername(),
                            employees.get(employees.size() - 1).getPhoto()
                    )
            );
        }

        return employees;
    }

    @Override
    public List<AssetModel> getAssetDataFromRequest(
            final List<RequestModel> requests
    ) {

        List<AssetModel> assets = new ArrayList<>();
        for (RequestModel request : requests) {
            assets.add(assetRepository.findBySku(request.getSku()));
            assets.get(assets.size() - 1).setStock(
                    request.getQuantity()
            );
        }

        return assets;
    }

    @Override
    public long getRequestsCount(
            final String username,
            final String query
    ) throws BadRequestException {

        if (query != null && query.equals("defaultQuery")) {
            throw new BadRequestException(EMPTY_SEARCH_QUERY);
        }

        if (query == null) {
            return requestRepository.countAllByUsername(username);
        } else {
            return requestRepository.countAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCase(query, query, query);
        }

    }

    @Override
    public String getEmployeeDetailPhoto(
            final String username,
            final String photoDirectory
    ) {

        if (photoDirectory == null || photoDirectory.isEmpty()) {
            return "http://localhost:8085/oasis/api/employees/" + username +
                   "/image_not_found"
                           .concat("?extension=jpeg");
        } else {
            File photo = new File(photoDirectory);
            if(Files.exists(photo.toPath())) {
                StringBuilder extensionBuilder = new StringBuilder();
                extensionBuilder.append(photo.getName());
                extensionBuilder.reverse();
                extensionBuilder.replace(
                        0,
                        extensionBuilder.length(),
                        extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf("."))
                );
                extensionBuilder.reverse();

                return "http://localhost:8085/oasis/api/employees/" + username +
                       "/" + username
                               .concat("?extension=")
                               .concat(String.valueOf(extensionBuilder));
            } else {
                return "http://localhost:8085/oasis/api/employees/" + username +
                       "/image_not_found"
                               .concat("?extension=jpeg");
            }
        }
    }

    /*-------------Save Request Methods-------------*/
    @Override
    public void saveRequests(
            final String username,
            final List<RequestModel> requests
    ) throws DataNotFoundException, BadRequestException {

        if (!employeeRepository.existsEmployeeModelByUsername(username)) {
            throw new DataNotFoundException(USER_NOT_FOUND);
        }

        if (requests.isEmpty()) {
            throw new BadRequestException(NO_ASSET_SELECTED);
        }

        for (RequestModel request : requests) {
            if (!assetRepository.existsAssetModelBySku(request.getSku())) {
                throw new DataNotFoundException(ASSET_NOT_FOUND);
            }

            if (assetRepository.findBySku(request.getSku()).getStock() - request.getQuantity() < 0) {
                throw new BadRequestException(ASSET_NOT_FOUND);
            }
        }

        if (requests.size() > 1 && requests.stream().anyMatch(requestModel -> requestModel.get_id() != null)) {
            throw new BadRequestException(ASSET_NOT_FOUND);
        }

        for (RequestModel request : requests) {
            RequestModel savedRequest;
            if (request.get_id() == null) {
                savedRequest = request;

                savedRequest.setStatus(ServiceConstant.REQUESTED);
                savedRequest.setTransactionNote(null);
                savedRequest.setCreatedBy(username);
                savedRequest.setCreatedDate(new Date());
            } else {
                savedRequest = requestRepository.findBy_id(request.get_id());

                if (savedRequest == null || request.getStatus() == null) {
                    throw new DataNotFoundException(new BaseError(
                            "1","1"
                    ));
                } else {
                    if (!employeeRepository.existsEmployeeModelByUsername(request.getUsername())) {
                        throw new DataNotFoundException(USER_NOT_FOUND);
                    }

                    boolean usernameIsAdmin = adminRepository.existsAdminModelByUsername(username);
                    boolean supervisorIsValid = supervisionRepository.existsSupervisionModelBySupervisorUsernameAndEmployeeUsername(
                            username,
                            savedRequest.getUsername()
                    );
                    boolean usernameIsAdminOrSupervisor = usernameIsAdmin || supervisorIsValid;

                    boolean requestStatusIsRequested = savedRequest.getStatus().equals(ServiceConstant.REQUESTED);
                    boolean requestStatusIsAccepted = savedRequest.getStatus().equals(ServiceConstant.ACCEPTED);
                    boolean requestStatusIsDelivered = savedRequest.getStatus().equals(ServiceConstant.DELIVERED);

                    boolean newRequestStatusIsCancelled = request.getStatus().equals(ServiceConstant.CANCELLED);
                    boolean newRequestStatusIsAccepted = request.getStatus().equals(ServiceConstant.ACCEPTED);
                    boolean newRequestStatusIsRejected = request.getStatus().equals(ServiceConstant.REJECTED);
                    boolean newRequestStatusIsDelivered = request.getStatus().equals(ServiceConstant.DELIVERED);
                    boolean newRequestStatusIsReturned = request.getStatus().equals(ServiceConstant.RETURNED);

                    boolean expendableAsset = assetRepository.findBySku(savedRequest.getSku()).isExpendable();

                    boolean usernameIsRequester = request.getUsername().equals(savedRequest.getUsername());

                    boolean requestedToCancelled = requestStatusIsRequested && newRequestStatusIsCancelled;
                    boolean requestedToAccepted = requestStatusIsRequested && newRequestStatusIsAccepted;
                    boolean requestedToRejected = requestStatusIsRequested && newRequestStatusIsRejected;
                    boolean acceptedToDelivered = requestStatusIsAccepted && newRequestStatusIsDelivered;
                    boolean deliveredToReturned = requestStatusIsDelivered && newRequestStatusIsReturned;

                    /*
                    Cancel hanya diri sendiri
                        Request username dan username ga sama		DONE
                        Banyak yang dilempar buat cancel    		DONE
                        Status bukan cancel				            DONE
                    Accept/reject hanya supervisor/admin
                        Supervisor bukan supervisor dari username	DONE
                        Non supervisor/admin accept/reject		    DONE
                        Status bukan accept/reject			        DONE
                        Banyak yang dilempar buat accept/reject		DONE
                        Self accept/reject				            DONE
                    Deliver hanya admin
                        Non admin deliver				            DONE
                        Status bukan deliver				        DONE
                        Banyak yang dilempar buat deliver		    DONE
                        Self deliver					            DONE
                    Return hanya admin
                        Non admin return				            DONE
                        Status bukan return				            DONE
                        Banyak yang dilempar buat return		    DONE
                        Self return					                DONE
                     */

                    if (!usernameIsAdminOrSupervisor && !usernameIsRequester) {
                        throw new BadRequestException(
                                new BaseError(
                                        "2", "2"
                                )
                        );
                    }

                    if (usernameIsRequester && !usernameIsAdminOrSupervisor && !requestedToCancelled) {
                        throw new BadRequestException(
                                new BaseError(
                                        "3", "3"
                                )
                        );
                    }

                    if (usernameIsRequester && usernameIsAdmin && !requestedToCancelled) {
                        throw new BadRequestException(
                                new BaseError(
                                        "4", "4"
                                )
                        );
                    }

                    if (requestedToCancelled && !usernameIsRequester) {
                        throw new BadRequestException(
                                new BaseError(
                                        "5", "5"
                                )
                        );
                    }

                    if (!usernameIsAdmin && acceptedToDelivered) {
                        throw new BadRequestException(
                                new BaseError(
                                        "6", "6"
                                )
                        );
                    }

                    if (!usernameIsAdminOrSupervisor && (requestedToAccepted || requestedToRejected)) {
                        throw new BadRequestException(
                                new BaseError(
                                        "7", "7"
                                )
                        );
                    }

                    if (usernameIsRequester && !requestedToCancelled) {
                        throw new BadRequestException(
                                new BaseError(
                                        "8", "8"
                                )
                        );
                    }

                    if (!usernameIsAdminOrSupervisor && (newRequestStatusIsDelivered || newRequestStatusIsReturned)) {
                        throw new BadRequestException(
                                new BaseError(
                                        "9", "9"
                                )
                        );
                    }

                    if (!requestedToCancelled && !requestedToAccepted && !requestedToRejected && !acceptedToDelivered && !deliveredToReturned) {
                        throw new BadRequestException(
                                new BaseError(
                                        "10", "10"
                                )
                        );
                    }

                    boolean allowedToCancelRequest = usernameIsRequester && requestedToCancelled;

                    boolean allowedToAcceptOrRejectRequest = usernameIsAdminOrSupervisor && requestStatusIsRequested &&
                                                             (newRequestStatusIsAccepted || newRequestStatusIsRejected);

                    boolean allowedToDeliverAsset = usernameIsAdmin && acceptedToDelivered;

                    boolean assetReturned = (expendableAsset && allowedToDeliverAsset) ||
                                            (usernameIsAdmin && deliveredToReturned);

                    if (allowedToCancelRequest) {
                        savedRequest.setStatus(ServiceConstant.CANCELLED);
                    }

                    if (allowedToAcceptOrRejectRequest) {
                        savedRequest.setStatus(request.getStatus());

                        if (requestedToAccepted) {
                            if (assetRepository.findBySku(savedRequest.getSku()).getStock() - savedRequest.getQuantity() < 0) {
                                throw new BadRequestException(ASSET_NOT_FOUND);
                            }

                            // TODO Handle concurrency!
                            AssetModel asset = assetRepository.findBySku(savedRequest.getSku());
                            asset.setStock(
                                    assetRepository.findBySku(savedRequest.getSku()).getStock()
                                    -
                                    savedRequest.getQuantity()
                            );

                            assetRepository.save(asset);
                        }

                        if (requestedToRejected) {
                            savedRequest.setTransactionNote(request.getTransactionNote());
                        }
                    }

                    if (allowedToDeliverAsset) {
                        savedRequest.setStatus(ServiceConstant.DELIVERED);
                    }

                    if (assetReturned) {
                        // TODO Handle concurrency!
                        AssetModel asset = assetRepository.findBySku(savedRequest.getSku());
                        asset.setStock(
                                assetRepository.findBySku(savedRequest.getSku()).getStock()
                                +
                                savedRequest.getQuantity()
                        );

                        assetRepository.save(asset);

                        savedRequest.setStatus(ServiceConstant.RETURNED);
                    }
                }
            }
            savedRequest.setUpdatedBy(username);
            savedRequest.setUpdatedDate(new Date());

            requestRepository.save(savedRequest);
        }

    }

}
