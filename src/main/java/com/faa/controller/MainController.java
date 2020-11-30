package com.faa.controller;

import com.alibaba.fastjson.JSONArray;
import com.faa.entity.TBalance;
import com.faa.entity.TBlock;
import com.faa.entity.TTransaction;
import com.faa.mapper.BalanceRepository;
import com.faa.mapper.BlockRepository;
import com.faa.mapper.TransactionRepository;
import com.faa.service.TransactionService;
import com.faa.utils.CommonUtil;
import org.apache.log4j.Logger;
import com.faa.chain.node.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;


@Controller
public class MainController {
    Logger logger = Logger.getLogger(MainController.class);

    @Resource
    private TransactionService transactionService;
    @Resource
    private TransactionRepository transactionRepository;
    @Resource
    private BlockRepository blockRepository;
    @Resource
    private BalanceRepository balanceRepository;

    // FAA 主链离线签名交易
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value="/faaRawTransaction", method=RequestMethod.POST)
    @ResponseBody
    public String faaRawTransaction(String signedTransactionData) throws Exception {

        Transaction t = new Transaction(signedTransactionData);
        int rtn = t.validate();
        // 数据检测通过
        if(rtn == 0){
            // 读数据库判断 from 的余额是否 >= (value + fee)
            TBalance tbFrom = balanceRepository.findOneByAddress(t.getFrom());
            // 已存在数据库的from地址
            if(tbFrom != null) {
                BigInteger balance = new BigInteger(tbFrom.getBalance());
                BigInteger willOut = t.getValue().add(t.getFee());
                // 余额足够
                if (balance.compareTo(willOut) != -1) {
                    transactionService.addNewTransaction(t.getFrom(), t.getTo(), t.getValue(), t.getFee(), t.getHexTransaction(), t.getHash());
                    return "{\"err\":0,\"hash\":\"" + t.getHash() + "\"}";
                }else{
                    return "{\"err\":-1}";
                }
            }
            else{
                return "{\"err\":-1}";
            }
        }else{
            return "{\"err\":-1}";
        }
    }

    // 获取交易结果
    @RequestMapping(value="/faaGetTransactionReceipt", method=RequestMethod.GET)
    @ResponseBody
    public String faaGetTransactionReceipt(String hash) throws Exception {
        TTransaction transaction = transactionRepository.findAllStatusTransactionByHash(hash);
        int rtn = -2;
        String msg = "null";
        // 无此交易
        if(null == transaction){
            rtn = -2;
            msg = "null";
        }else {
            // 成功
            if(transaction.getStatus() == 9){
                rtn = 9;
                msg = "success";
            }
            // 失败
            if(transaction.getStatus() == -1){
                rtn = -1;
                msg = "failed";
            }
            // pending
            if(transaction.getStatus() == 0){
                rtn = 0;
                msg = "pending";
            }
        }

        return "{\"err\":0,\"status\":" + rtn + ",\"msg\":\"" + msg + "\"}";
    }

    // 获取单个交易详情
    @RequestMapping(value="/faaGetTransactionDetail", method=RequestMethod.GET)
    @ResponseBody
    public String faaGetTransactionDetail(String hash) throws Exception {
        TTransaction transaction = transactionRepository.findAllStatusTransactionByHash(hash);
        // 无此交易
        if(null == transaction){
            int rtn = -2;
            return "{\"status\":" + rtn + "}";
        }else {
            Object jsonObj = JSONArray.toJSON(transaction);
            return jsonObj.toString();
        }
    }

    // 获取某地址FAA余额
    @RequestMapping(value="/faaGetBalance", method=RequestMethod.GET)
    @ResponseBody
    public String faaGetBalance(String address) throws Exception {
        TBalance balance = balanceRepository.findOneByAddress(address);
        if(null == balance){
            return "{\"err\":0,\"balance\":0,\"balance_human\":0}";
        }

        return "{\"err\":0,\"balance\":" + balance.getBalance() + ",\"balance_human\":" + balance.getBalancef() + "}";
    }

    // 获取当前推荐手续费
    @RequestMapping(value="/faaGetFeeRecommend", method=RequestMethod.GET)
    @ResponseBody
    public String faaGetFeeRecommend() throws Exception {
        return "{\"err\":0,\"fee\":" + CommonUtil.FEE_NOW + "}";
    }

    // 获取当前最新的区块号
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value="/faaLastBlockNumber", method=RequestMethod.GET)
    @ResponseBody
    public String faaLastBlockNumber() throws Exception {
        TBlock lastBlock = blockRepository.findLastBlock();
        if(null == lastBlock){
            return "{\"err\":0,\"number\":-1}";
        }else{
            return "{\"err\":0,\"number\":" + lastBlock.getBlockNo() + "}";
        }
    }

    // 获取FAA区块交易列表
    @RequestMapping(value="/faaGetLogs", method=RequestMethod.GET)
    @ResponseBody
    public String faaGetLogs(int fromBlock, int toBlock) throws Exception {

        // 不允许一次查询100个以上的区块
        if(fromBlock < 0 || toBlock < 0 || (toBlock - fromBlock) >= 100){
            return "{\"err\":1}";
        }

        List<TTransaction> tranList = transactionRepository.findAllTransactionByBlockFromTo(fromBlock, toBlock);

        Object jsonObj = JSONArray.toJSON(tranList);

        return "{\"err\":0,\"data\":" + jsonObj.toString() + "}";
    }

    // 获取某地址的交易列表
    @RequestMapping(value="/faaGetAddressTransactions", method=RequestMethod.GET)
    @ResponseBody
    public String faaGetAddressTransactions(String address){

        List<TTransaction> transList = transactionService.findLast20TransactionByAddress(address);

        Object jsonObj = JSONArray.toJSON(transList);

        return "{\"err\":0,\"data\":" + jsonObj.toString() + "}";
    }

    // 获取交易所中 faa 的当前价格
    @RequestMapping(value="/faaGetPrice", method=RequestMethod.GET)
    @ResponseBody
    public String faaGetPrice(){
        return "{\"err\":0,\"price\":" + CommonUtil.FAA_PRICE + "}";
    }
}