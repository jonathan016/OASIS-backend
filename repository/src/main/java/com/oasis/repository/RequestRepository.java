package com.oasis.repository;

import com.oasis.model.entity.RequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends MongoRepository<RequestModel, String> {

    List<RequestModel> findAllByUsernameAndStatus(String username, String status);

    List<RequestModel> findAllBySku(String assetSku);

    Page<RequestModel> findAllByUsernameOrderByStatusAsc(String username, Pageable pageable);

    Page<RequestModel> findAllByUsernameOrderByStatusDesc(String username, Pageable pageable);

    Page<RequestModel> findAllByUsernameOrderByUpdatedDateAsc(String username, Pageable pageable);

    Page<RequestModel> findAllByUsernameOrderByUpdatedDateDesc(String username, Pageable pageable);

    long countAllByUsername(String username);

    Page<RequestModel> findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusAsc(String username,
                                                                                                     String status,
                                                                                                     String sku, Pageable pageable);

    Page<RequestModel> findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusDesc(String username,
                                                                                                      String status,
                                                                                                      String sku, Pageable pageable);

    Page<RequestModel> findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByUpdatedDateAsc(String username,
                                                                                                          String status,
                                                                                                          String sku, Pageable pageable);

    Page<RequestModel> findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByUpdatedDateDesc(String username,
                                                                                                           String status,
                                                                                                           String sku, Pageable pageable);

    long countAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCase(String username, String status, String sku);

    Page<RequestModel> findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
            String username1,
            String status,
            String username2,
            String sku,
            Pageable pageable
    );

    RequestModel findBy_id(String _id);

}
