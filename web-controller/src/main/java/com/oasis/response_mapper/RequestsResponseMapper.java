package com.oasis.response_mapper;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.service.ServiceConstant;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.Paging;
import com.oasis.web_model.response.PagingResponse;
import com.oasis.web_model.response.success.requests.RequestListResponse;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RequestsResponseMapper {

    public PagingResponse< RequestListResponse > produceViewFoundAssetSuccessResult(
            final int httpStatusCode, final List< RequestModel > requests, final List< EmployeeModel > employees,
            final List< AssetModel > assets, final Map< String, Boolean > components, final int pageNumber,
            final long totalRecords
    ) {

        PagingResponse< RequestListResponse > successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(components);

        MapperFactory requestDataFactory = new DefaultMapperFactory.Builder().build();
        requestDataFactory.classMap(RequestModel.class, RequestListResponse.RequestListObject.Request.class)
                          .field("_id", "id")
                          .byDefault()
                          .register();
        MapperFactory employeeDataFactory = new DefaultMapperFactory.Builder().build();
        employeeDataFactory.classMap(EmployeeModel.class, RequestListResponse.RequestListObject.Employee.class)
                           .byDefault()
                           .register();
        MapperFactory assetDataFactory = new DefaultMapperFactory.Builder().build();
        assetDataFactory.classMap(AssetModel.class, RequestListResponse.RequestListObject.Asset.class)
                        .field("stock", "quantity")
                        .byDefault()
                        .register();
        List< RequestListResponse.RequestListObject > mappedRequests = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            mappedRequests.add(new RequestListResponse.RequestListObject(
                    requestDataFactory.getMapperFacade(RequestModel.class,
                                                       RequestListResponse.RequestListObject.Request.class
                    )
                                      .map(requests.get(i)), employeeDataFactory.getMapperFacade(EmployeeModel.class,
                                                                                                 RequestListResponse.RequestListObject.Employee.class
            )
                                                                                .map(employees.get(i)),
                    assetDataFactory.getMapperFacade(AssetModel.class,
                                                     RequestListResponse.RequestListObject.Asset.class
                    )
                                    .map(assets.get(i))
            ));
        }
        successResponse.setValue(new RequestListResponse(mappedRequests));

        successResponse.setPaging(new Paging(pageNumber, ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE,
                                             (int) Math.ceil((double) totalRecords /
                                                             ServiceConstant.REQUESTS_FIND_REQUEST_PAGE_SIZE),
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
