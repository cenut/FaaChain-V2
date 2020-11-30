package com.faa.mapper;

import com.faa.entity.TTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TTransaction, Integer>, JpaSpecificationExecutor<TTransaction> {

    List<TTransaction> findAllByIdBlockAndStatusOrderByDateCreatedAsc(int idBlock, int status);

    @Query(value="select count(*) from transaction where enable=1", nativeQuery=true)
    Object transactionCount();

    @Query(value="select * from transaction where enable=1 and status=9 order by date_created desc limit 10", nativeQuery=true)
    List<TTransaction> findLast10Transaction();

    @Query(value="select * from transaction where hash=?1 and status=9 and enable=1 limit 1", nativeQuery=true)
    TTransaction findTransactionByHash(String hash);

    @Query(value="select * from transaction where hash=?1 and enable=1 limit 1", nativeQuery=true)
    TTransaction findAllStatusTransactionByHash(String hash);

    @Query(value="select * from transaction where block_no=?1 and status=9 and enable=1 order by date_created desc limit 50", nativeQuery=true)
    List<TTransaction> find50TransactionByBlockNo(int blockNo);

    @Query(value="select * from transaction where (from_address=?1 or to_address=?1) and status=9 and enable=1 order by date_created desc limit 50", nativeQuery=true)
    List<TTransaction> findLast20TransactionByAddress(String address);

    @Query(value="select * from transaction where (block_no>=?1 and block_no<=?2) and enable=1 order by date_created desc", nativeQuery=true)
    List<TTransaction> findAllTransactionByBlockFromTo(int fromBlock, int toBlock);

}
