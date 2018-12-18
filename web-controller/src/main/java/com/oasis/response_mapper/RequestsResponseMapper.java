package com.oasis.response_mapper;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.service.ServiceConstant;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.Paging;
import com.oasis.web_model.response.PagingResponse;
import com.oasis.web_model.response.success.requests.AssetRequestDetailsResponse;
import com.oasis.web_model.response.success.requests.RequestMyListResponse;
import com.oasis.web_model.response.success.requests.RequestOthersListResponse;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RequestsResponseMapper {

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

            mappedRequests.get(i).getRequest().setUpdatedDate(
                    new SimpleDateFormat("EEE, dd MMM ''yy HH:mm").format(requests.get(i).getUpdatedDate()));
        }
        successResponse.setValue(new RequestOthersListResponse(mappedRequests));

        successResponse.setPaging(new Paging(pageNumber, ServiceConstant.REQUESTS_LIST_PAGE_SIZE, (int) Math
                .ceil((double) totalRecords / ServiceConstant.REQUESTS_LIST_PAGE_SIZE), totalRecords));

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

            mappedRequests.get(i).getRequest().setUpdatedDate(
                    new SimpleDateFormat("EEE, dd MMM ''yy HH:mm").format(requests.get(i).getUpdatedDate()));
        }
        successResponse.setValue(new RequestMyListResponse(mappedRequests));

        successResponse.setPaging(new Paging(pageNumber, ServiceConstant.REQUESTS_LIST_PAGE_SIZE, (int) Math
                .ceil((double) totalRecords / ServiceConstant.REQUESTS_LIST_PAGE_SIZE), totalRecords));

        return successResponse;
    }

    public PagingResponse< AssetRequestDetailsResponse > produceViewAssetRequestDetailsSuccessResult(
            final int httpStatusCode, final List< AssetModel > requestedAssets,
            final List< List< String > > requestedAssetsImages, final int pageNumber, final long totalRecords
    ) {

        PagingResponse< AssetRequestDetailsResponse > successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, AssetRequestDetailsResponse.AssetRequestDetailsObject.class)
                        .field("stock", "quantity").byDefault().register();
        List< AssetRequestDetailsResponse.AssetRequestDetailsObject > requestedAssetsListObjects = new ArrayList<>();
        for (int i = 0; i < requestedAssets.size(); i++) {
            requestedAssetsListObjects.add(assetDataFactory.getMapperFacade(AssetModel.class,
                                                                            AssetRequestDetailsResponse.AssetRequestDetailsObject.class
            ).map(requestedAssets.get(i)));

            requestedAssetsListObjects.get(requestedAssetsListObjects.size() - 1)
                                      .setImages(requestedAssetsImages.get(i));
        }
        successResponse.setValue(new AssetRequestDetailsResponse(requestedAssetsListObjects));

        successResponse.setPaging(new Paging(pageNumber, ServiceConstant.ASSET_REQUEST_DETAILS_LIST_PAGE_SIZE,
                                             (int) Math.ceil((double) totalRecords /
                                                             ServiceConstant.ASSET_REQUEST_DETAILS_LIST_PAGE_SIZE),
                                             totalRecords
        ));

        return successResponse;
    }

    public BaseResponse produceRequestSaveSuccessResult(final int httpStatusCode) {

        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

}
