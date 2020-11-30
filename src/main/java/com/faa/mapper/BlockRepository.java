package com.faa.mapper;

import com.faa.entity.TBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BlockRepository extends JpaRepository<TBlock, Integer>, JpaSpecificationExecutor<TBlock> {

    @Query(value="select * from block where block_no=?1 and enable=1 limit 1", nativeQuery=true)
    TBlock findBlockByBlockNo(int blockNo);

    @Query(value="select * from block where hash=?1 and enable=1 limit 1", nativeQuery=true)
    TBlock findBlockByHash(String hash);

    @Query(value="select * from block where enable=1 order by block_no desc limit 1", nativeQuery=true)
    TBlock findLastBlock();

    @Query(value="select * from block where enable=1 order by block_no desc limit 10", nativeQuery=true)
    List<TBlock> findLast10Block();

    @Query(value="select id from block where enable=1 order by block_no desc limit 1", nativeQuery=true)
    int findLastBlockNumber();

    @Query(value="select count(e)>0 from block e where id=?1", nativeQuery=true)
    Boolean hasBlock(long number);

    @Query(value="select count(e)>0 from transaction e where hash=?1", nativeQuery=true)
    Boolean hasTransaction(String hash);
}
