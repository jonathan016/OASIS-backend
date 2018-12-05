package com.oasis.service.implementation;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.RequestsServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.ASSET_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.EMPTY_SEARCH_QUERY;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestsServiceImpl implements RequestsServiceApi {

    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AssetRepository assetRepository;

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

}
