package com.oasis.repository;

import com.oasis.model.entity.RequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository
        extends MongoRepository< RequestModel, String > {

    List< RequestModel > findAllBySkuEqualsAndStatusEquals(
            String sku, String status
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEquals(
            String username, String status
    );

    boolean existsRequestModelsBySkuEquals(String sku);

    List< RequestModel > findAllByUsernameEqualsAndStatusContainsOrderByStatusAsc(
            String username, String status
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusContainsOrderByStatusDesc(
            String username, String status
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateAsc(
            String username, String status
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc(
            String username, String status
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateAsc(
            String username, String status, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc(
            String username, String status, Pageable pageable
    );

    long countAllByUsernameEqualsAndStatusEquals(
            String username, String status
    );

    List< RequestModel > findAllByUsernameEqualsOrderByStatusAsc(
            String username
    );

    List< RequestModel > findAllByUsernameEqualsOrderByStatusDesc(
            String username
    );

    List< RequestModel > findAllByUsernameEqualsOrderByUpdatedDateAsc(
            String username
    );

    List< RequestModel > findAllByUsernameEqualsOrderByUpdatedDateDesc(
            String username
    );

    Page< RequestModel > findAllByUsernameEqualsOrderByStatusAsc(
            String username, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsOrderByStatusDesc(
            String username, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsOrderByUpdatedDateAsc(
            String username, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsOrderByUpdatedDateDesc(
            String username, Pageable pageable
    );

    long countAllByUsernameEquals(
            String username
    );

    List< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
            String username, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
            String username, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
            String username, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
            String username, String sku
    );

    Page< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
            String username, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
            String username, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
            String username, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
            String username, String sku, Pageable pageable
    );

    long countAllByUsernameEqualsAndSkuContainsIgnoreCase(
            String username, String sku
    );

    long countAllByUsernameEqualsAndSkuContainsIgnoreCaseAndUsernameEqualsAndStatusEquals(
            String username1, String sku, String username2, String status
    );

    RequestModel findBy_id(String _id);

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
            String username, String status, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
            String username, String status, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
            String username, String status, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
            String username, String status, String sku, Pageable pageable
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
            String username, String status, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
            String username, String status, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
            String username, String status, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
            String username, String status, String sku
    );

}
