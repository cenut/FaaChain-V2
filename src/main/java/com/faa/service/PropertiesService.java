package com.faa.service;

import com.faa.entity.Properties;
import com.faa.mapper.PropertiesRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PropertiesService {

    @Resource
    private PropertiesRepository propertiesRepository;

    public Properties getPropertiesById(int id){
        return propertiesRepository.findOne(id);
    }

    public Properties getMainProForUpdate(){
        return propertiesRepository.findByIdForUpdate(1);
    }

    public Properties saveProperties(Properties properties){
        return propertiesRepository.saveAndFlush(properties);
    }
}