package com.oasis.response_mapper;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.service.ServiceConstant;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.Paging;
import com.oasis.web_model.response.PagingResponse;
import com.oasis.web_model.response.success.dashboard.DashboardStatusResponse;
import com.oasis.web_model.response.success.dashboard.RequestMyListResponse;
import com.oasis.web_model.response.success.dashboard.RequestOthersListResponse;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DashboardResponseMapper {

    public NoPagingResponse< DashboardStatusResponse > produceDashboardStatusSuccessResult(
            final int httpStatusCode, final long requestedRequestsCount, final long acceptedRequestsCount,
            final long availableAssetsCount
    ) {

        NoPagingResponse< DashboardStatusResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(null);

        successResponse.setValue(
                new DashboardStatusResponse(requestedRequestsCount, acceptedRequestsCount, availableAssetsCount));

        return successResponse;
    }


    public PagingResponse< RequestOthersListResponse > produceViewOthersFoundRequestSuccessResult(
            final int httpStatusCode, final List< RequestModel > requests, final List< EmployeeModel > employees,
            final List< AssetModel > assets, final Map< String, Boolean > components, final int pageNumber,
            final long totalRecords
    ) {

        PagingResponse< RequestOthersListResponse > successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(components);

        MapperFactory requestDataFactory = new DefaultMapperFactory.Builder().build();
        requestDataFactory.classMap(RequestModel.class, RequestOthersListResponse.RequestListObject.Request.class)
                          .field("requestNote", "note").field("_id", "id").byDefault().register();
        MapperFactory employeeDataFactory = new DefaultMapperFactory.Builder().build();
        employeeDataFactory.classMap(EmployeeModel.class, RequestOthersListResponse.RequestListObject.Employee.class)
                           .byDefault().register();
        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, RequestOthersListResponse.RequestListObject.Asset.class)
                        .field("stock", "quantity").byDefault().register();
        List< RequestOthersListResponse.RequestListObject > mappedRequests = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            mappedRequests.add(new RequestOthersListResponse.RequestListObject(
                    requestDataFactory.getMapperFacade(RequestModel.class,
                                                       RequestOthersListResponse.RequestListObject.Request.class
                    ).map(requests.get(i)), employeeDataFactory.getMapperFacade(EmployeeModel.class,
                                                                                RequestOthersListResponse.RequestListObject.Employee.class
            ).map(employees.get(i)), assetDataFactory.getMapperFacade(
                    AssetModel.class, RequestOthersListResponse.RequestListObject.Asset.class).map(assets.get(i))));

            if (!mappedRequests.get(i).getRequest().getStatus().equals(ServiceConstant.STATUS_REQUESTED)) {
                mappedRequests.get(i).getRequest().setNote(
                        (requests.get(i).getTransactionNote() == null || requests.get(i).getTransactionNote().isEmpty())
                        ? "No transaction note" : requests.get(i).getTransactionNote());
            }
        }
        successResponse.setValue(new RequestOthersListResponse(mappedRequests));

        successResponse.setPaging(new Paging(pageNumber, requests.size(), (int) Math
                .ceil((double) totalRecords / ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE), totalRecords));

        return successResponse;
    }

    public PagingResponse< RequestMyListResponse > produceViewMyFoundRequestSuccessResult(
            final int httpStatusCode, final List< RequestModel > requests, final List< EmployeeModel > employees,
            final List< EmployeeModel > modifiers, final List< AssetModel > assets,
            final Map< String, Boolean > components, final int pageNumber, final long totalRecords
    ) {

        PagingResponse< RequestMyListResponse > successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(components);

        MapperFactory requestDataFactory = new DefaultMapperFactory.Builder().build();
        requestDataFactory.classMap(RequestModel.class, RequestMyListResponse.RequestListObject.Request.class)
                          .field("requestNote", "note").field("_id", "id").byDefault().register();
        MapperFactory employeeDataFactory = new DefaultMapperFactory.Builder().build();
        employeeDataFactory.classMap(EmployeeModel.class, RequestMyListResponse.RequestListObject.Employee.class)
                           .byDefault().register();
        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, RequestMyListResponse.RequestListObject.Asset.class)
                        .field("stock", "quantity").byDefault().register();
        List< RequestMyListResponse.RequestListObject > mappedRequests = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            mappedRequests.add(new RequestMyListResponse.RequestListObject(requestDataFactory
                                                                                   .getMapperFacade(RequestModel.class,
                                                                                                    RequestMyListResponse.RequestListObject.Request.class
                                                                                   ).map(requests.get(i)),
                                                                           employeeDataFactory
                                                                                   .getMapperFacade(EmployeeModel.class,
                                                                                                    RequestMyListResponse.RequestListObject.Employee.class
                                                                                   ).map(employees.get(i)),
                                                                           employeeDataFactory
                                                                                   .getMapperFacade(EmployeeModel.class,
                                                                                                    RequestMyListResponse.RequestListObject.Employee.class
                                                                                   ).map(modifiers.get(i)),
                                                                           assetDataFactory
                                                                                   .getMapperFacade(AssetModel.class,
                                                                                                    RequestMyListResponse.RequestListObject.Asset.class
                                                                                   ).map(assets.get(i))
            ));

            if (!mappedRequests.get(i).getRequest().getStatus().equals(ServiceConstant.STATUS_REQUESTED)) {
                mappedRequests.get(i).getRequest().setNote(
                        (requests.get(i).getTransactionNote() == null || requests.get(i).getTransactionNote().isEmpty())
                        ? "No transaction note" : requests.get(i).getTransactionNote());
            }
        }
        successResponse.setValue(new RequestMyListResponse(mappedRequests));

        successResponse.setPaging(new Paging(pageNumber, requests.size(), (int) Math
                .ceil((double) totalRecords / ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE), totalRecords));

        return successResponse;
    }

}
