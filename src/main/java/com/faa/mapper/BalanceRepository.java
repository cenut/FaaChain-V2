package com.faa.mapper;

import com.faa.entity.TBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BalanceRepository extends JpaRepository<TBalance, Integer>, JpaSpecificationExecutor<TBalance> {

    @Query(value="select * from balance where address=?1 and enable=1", nativeQuery=true)
    TBalance findOneByAddress(String address);

    @Query(value="select sum(balancef) from balance where enable=1", nativeQuery=true)
    Object sumAllFaaBalance();

    @Query(value="select count(*) from balance where enable=1", nativeQuery=true)
    Object addressCount();
}
