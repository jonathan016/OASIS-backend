package com.oasis.webmodel.response;

import lombok.Data;

@Data
public class BaseResponse<T> {
    private String code;
    private String status;
    private T value;
    private Paging paging;

    @Data
    public class Paging {
        private long pageSize;
        private long pageNumber;
        private long totalRecords;
    }
}
