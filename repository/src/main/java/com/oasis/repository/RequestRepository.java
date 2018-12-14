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

    List< RequestModel > findAllByUsernameAndStatus(
            String username, String status
    );

    List< RequestModel > findAllBySku(String sku);

    List< RequestModel > findAllByUsernameAndStatusContainsOrderByStatusAsc(
            String username, String status
    );

    List< RequestModel > findAllByUsernameAndStatusContainsOrderByStatusDesc(
            String username, String status
    );

    List< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateAsc(
            String username, String status
    );

    List< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(
            String username, String status
    );

    Page< RequestModel > findAllByUsernameAndStatusContainsOrderByUpdatedDateAsc(
            String username, String status, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameAndStatusContainsOrderByUpdatedDateDesc(
            String username, String status, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateAsc(
            String username, String status, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(
            String username, String status, Pageable pageable
    );

    long countAllByUsernameAndStatus(
            String username, String status
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusAsc(
            String username, String status, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
            String username1, String status, String username2, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
            String username, String status, String sku
    );

    List< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
            String username1, String status, String username2, String sku
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusAsc(
            String username, String status, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
            String username1, String status, String username2, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
            String username, String status, String sku, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
            String username1, String status, String username2, String sku, Pageable pageable
    );

    long countAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCase(
            String username, String status, String sku
    );

    List< RequestModel > findAllByUsernameOrderByStatusAsc(
            String username
    );

    List< RequestModel > findAllByUsernameOrderByStatusDesc(
            String username
    );

    List< RequestModel > findAllByUsernameOrderByUpdatedDateAsc(
            String username
    );

    List< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(
            String username
    );

    Page< RequestModel > findAllByUsernameOrderByStatusAsc(
            String username, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameOrderByStatusDesc(
            String username, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameOrderByUpdatedDateAsc(
            String username, Pageable pageable
    );

    Page< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(
            String username, Pageable pageable
    );

    long countAllByUsername(
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

    RequestModel findBy_id(String _id);

}
