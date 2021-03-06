package com.oasis.web_model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {

    private long pageNumber;
    private long pageSize;
    private long totalPage;
    private long totalRecords;

}