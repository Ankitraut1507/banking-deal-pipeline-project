package com.bank.pipeline.repository;

import com.bank.pipeline.model.Deal;
import com.bank.pipeline.model.DealStage;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
public interface DealRepository extends MongoRepository<Deal, String> {

    Page<Deal> findAll( Pageable pageable);

    Page<Deal> findByOwnerId(String ownerId, Pageable pageable);

    Page<Deal> findByStage(DealStage stage, Pageable pageable);

    Page<Deal> findByOwnerIdAndStage(
            String ownerId,
            DealStage stage,
            Pageable pageable
    );

    Page<Deal> findBySector(String sector, Pageable pageable);

    Page<Deal> findByStageAndSector(
            DealStage stage,
            String sector,
            Pageable pageable
    );

    List<Deal> findByStage(DealStage stage);
}

