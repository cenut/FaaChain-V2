package com.faa.mapper;

import com.faa.entity.TUnionNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnionNodeRepository extends JpaRepository<TUnionNode, Integer>, JpaSpecificationExecutor<TUnionNode> {

    @Query(value="select count(*) from unionnode where status=1", nativeQuery=true)
    Object nodeCount();

    @Query(value="select * from unionnode where status=1", nativeQuery=true)
    List<TUnionNode> findAllActive();

}
