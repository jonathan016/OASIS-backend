package com.oasis.service.api.requests;

import com.oasis.model.entity.RequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RequestUtilServiceApi {

    boolean existsRequestModelsBySku(final String sku);

    List< RequestModel > findAllByUsernameAndStatus(final String username, final String status);

    void save(final RequestModel request);

    long countAllByUsernameEqualsAndStatusEquals(final String username, final String status);

    List< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(final String username);

    List< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(final String username, final String status);

    Page< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(
            final String username, final Pageable pageable
    );

}
