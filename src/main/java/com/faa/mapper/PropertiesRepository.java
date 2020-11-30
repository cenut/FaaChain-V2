package com.faa.mapper;

import com.faa.entity.Properties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PropertiesRepository extends JpaRepository<Properties, Integer>, JpaSpecificationExecutor<Properties> {

    @Query(value="select * from properties where id=?1 for update", nativeQuery=true)
    Properties findByIdForUpdate(int id);
}
