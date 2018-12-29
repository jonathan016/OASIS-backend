package com.oasis.service.implementation.requests;

import com.oasis.model.entity.RequestModel;
import com.oasis.repository.RequestRepository;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestUtilServiceImpl
        implements RequestUtilServiceApi {

    @Autowired
    private RequestRepository requestRepository;



    @Override
    public boolean existsRequestModelsBySku(final String sku) {

        return requestRepository.existsRequestModelsBySkuEquals(sku);
    }

    @Override
    public List< RequestModel > findAllByUsernameAndStatus(final String username, final String status) {

        return requestRepository.findAllByUsernameEqualsAndStatusEquals(username, status);
    }

    @Override
    public void save(final RequestModel request) {

        requestRepository.save(request);
    }

    @Override
    public long countAllByUsernameEqualsAndStatusEquals(final String username, final String status) {

        return requestRepository.countAllByUsernameEqualsAndStatusEquals(username, status);
    }

    @Override
    public List< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(final String username) {

        return requestRepository.findAllByUsernameEqualsOrderByUpdatedDateDesc(username);
    }

    @Override
    public List< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(
            final String username, final String status
    ) {

        return requestRepository.findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc(username, status);
    }

    @Override
    public Page< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(
            final String username, final Pageable pageable
    ) {

        return requestRepository.findAllByUsernameEqualsOrderByUpdatedDateDesc(username, pageable);
    }

}
