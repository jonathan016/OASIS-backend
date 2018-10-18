package com.oasis.webmodel.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Paging {
    private long pageNumber;
    private long pageSize;
    private long totalRecords;
}