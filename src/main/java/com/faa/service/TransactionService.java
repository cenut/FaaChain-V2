package com.faa.service;

import com.faa.entity.TTransaction;
import com.faa.mapper.TransactionRepository;
import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;


@Service
public class TransactionService {

    @Resource
    private TransactionRepository transactionRepository;

    // 新增一条待处理的交易
    public TTransaction addNewTransaction(String from, String to, BigInteger value, BigInteger fee, String hexdata, String hash){
        TTransaction tt = new TTransaction();

        tt.setIdBlock(-1);  // -1 代表还未归属某个区块
        tt.setFromAddress(from);
        tt.setToAddress(to);
        tt.setValue(value.toString());
        tt.setFee(fee.toString());
        tt.setHexdata(hexdata);
        tt.setHash(hash);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        tt.setDateCreated(ts);
        tt.setStatus(0);
        tt.setEnable(true);

        return transactionRepository.saveAndFlush(tt);
    }

    // 统计交易总数
    public int transactionCount(){
        Object obj = transactionRepository.transactionCount();
        if(null == obj){
            return 0;
        }else{
            BigInteger countB = (BigInteger)obj;
            return countB.intValue();
        }
    }

    // 查询最新的10条交易并换算单位
    public List<TTransaction> findLast10Transaction(){
        List<TTransaction> transactionList = transactionRepository.findLast10Transaction();
        for (TTransaction t : transactionList) {
            BigDecimal valueHuman = CoinsBaseUnits.toHumanUnit(t.getValue(), FaaMain.get());
            BigDecimal feeHuman = CoinsBaseUnits.toHumanUnit(t.getFee(), FaaMain.get());
            t.setValueHuman(valueHuman.doubleValue());
            t.setFeeHuman(feeHuman.doubleValue());
        }
        return transactionList;
    }

    // 查询某地址最新的50条交易并换算单位
    public List<TTransaction> findLast20TransactionByAddress(String address){
        List<TTransaction> transactionList = transactionRepository.findLast20TransactionByAddress(address);
        for (TTransaction t : transactionList) {
            BigDecimal valueHuman = CoinsBaseUnits.toHumanUnit(t.getValue(), FaaMain.get());
            BigDecimal feeHuman = CoinsBaseUnits.toHumanUnit(t.getFee(), FaaMain.get());
            t.setValueHuman(valueHuman.doubleValue());
            t.setFeeHuman(feeHuman.doubleValue());
        }
        return transactionList;
    }

    // 查询某区块最新的50条交易并换算单位
    public List<TTransaction> findTransactionByBlockNo(int blockNo){
        List<TTransaction> transactionList = transactionRepository.find50TransactionByBlockNo(blockNo);
        for (TTransaction t : transactionList) {
            BigDecimal valueHuman = CoinsBaseUnits.toHumanUnit(t.getValue(), FaaMain.get());
            BigDecimal feeHuman = CoinsBaseUnits.toHumanUnit(t.getFee(), FaaMain.get());
            t.setValueHuman(valueHuman.doubleValue());
            t.setFeeHuman(feeHuman.doubleValue());
        }
        return transactionList;
    }

}