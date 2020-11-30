package com.faa.service;

import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;
import com.faa.entity.TTransaction;
import com.faa.entity.TUnionNode;
import com.faa.mapper.TransactionRepository;
import com.faa.mapper.UnionNodeRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Service
public class UnionNodeService {

    @Resource
    private UnionNodeRepository unionNodeRepository;

    // 新增一条
    public TUnionNode addNewNode(String ip, String port, String name, String info,  String address, String hash){
        TUnionNode un = new TUnionNode();

        un.setNodeIp(ip);
        un.setNodePort(port);
        un.setNodeName(name);
        un.setNodeInfo(info);
        un.setNodeAddr(address);
        un.setRewardBlock("0");
        un.setRewardFee("0");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        un.setDateCreated(ts);
        un.setStatus(0);

        return unionNodeRepository.saveAndFlush(un);
    }

    // 统计node总数
    public int nodeCount(){
        Object obj = unionNodeRepository.nodeCount();
        if(null == obj){
            return 0;
        }else{
            BigInteger countB = (BigInteger)obj;
            return countB.intValue();
        }
    }

    // 获取联盟注册的激活节点列表
    public List<InetSocketAddress> listAllUnionNode(){
        List<InetSocketAddress> isaList = new ArrayList<>();
        List<TUnionNode> unionNodes = unionNodeRepository.findAllActive();
        for (TUnionNode un : unionNodes) {
            InetSocketAddress isa = new InetSocketAddress(un.getNodeIp(), Integer.parseInt(un.getNodePort()));
            isaList.add(isa);
        }

        return isaList;
    }

}