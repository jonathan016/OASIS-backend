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

    Page<RequestModel> findAllByUsernameAndStatusContainsOrderByUpdatedDateAsc(String username, String status,
                                                                               Pageable pageable);

    Page<RequestModel> findAllByUsernameAndStatusContainsOrderByUpdatedDateDesc(String username, String status,
                                                                                Pageable pageable);

    Page<RequestModel> findAllByUsernameAndStatusOrderByUpdatedDateAsc(String username, String status,
                                                                       Pageable pageable);

    Page<RequestModel> findAllByUsernameAndStatusOrderByUpdatedDateDesc(String username, String status,
                                                                        Pageable pageable);

    long countAllByUsernameAndStatus(String username, String status);

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
