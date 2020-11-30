package com.faa.controller;

import com.faa.entity.TBalance;
import com.faa.entity.TBlock;
import com.faa.entity.TTransaction;
import com.faa.mapper.BalanceRepository;
import com.faa.mapper.BlockRepository;
import com.faa.mapper.TransactionRepository;
import com.faa.service.BlockService;
import com.faa.service.TransactionService;
import com.faa.utils.CommonUtil;
import org.apache.log4j.Logger;
import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;


@Controller
public class ScanController {
    Logger logger = Logger.getLogger(ScanController.class);

    @Resource
    private BlockRepository blockRepository;
    @Resource
    private BlockService blockService;
    @Resource
    private TransactionRepository transactionRepository;
    @Resource
    private TransactionService transactionService;
    @Resource
    private BalanceRepository balanceRepository;

    // 区块浏览器首页
    @RequestMapping(value="/", method=RequestMethod.GET)
    public String scanIndex(HttpSession httpSession){

        int blockCount = blockRepository.findLastBlock().getBlockNo() + 1;
        int transactionCount = transactionService.transactionCount();
        double faaCount = blockService.sumAllFaa().doubleValue();
        int addressCount = blockService.addressCount();
        List<TBlock> blockList = blockService.findLast10Block();
        List<TTransaction> transactionList = transactionService.findLast10Transaction();

        httpSession.setAttribute("blockCount", blockCount);
        httpSession.setAttribute("transactionCount", transactionCount);
        httpSession.setAttribute("faaCount", faaCount);
        httpSession.setAttribute("addressCount", addressCount);
        httpSession.setAttribute("blockList", blockList);
        httpSession.setAttribute("transactionList", transactionList);
        httpSession.setAttribute("faaPrice", CommonUtil.FAA_PRICE);
        httpSession.setAttribute("faaRose", CommonUtil.FAA_ROSE);
        httpSession.setAttribute("feeNow", CommonUtil.FEE_NOW);
        httpSession.setAttribute("avgBlockS", CommonUtil.AVG_BLOCK_S);

        return "home";
    }

    // 交易详情页
    @RequestMapping(value="/transDetail", method=RequestMethod.GET)
    public String transactionDetail(HttpSession httpSession, String hash){

        TTransaction transaction = transactionRepository.findTransactionByHash(hash);
        if(null == transaction){
            return "nothing";
        }

        BigDecimal valueHuman = CoinsBaseUnits.toHumanUnit(transaction.getValue(), FaaMain.get());
        BigDecimal feeHuman = CoinsBaseUnits.toHumanUnit(transaction.getFee(), FaaMain.get());
        transaction.setValueHuman(valueHuman.doubleValue());
        transaction.setFeeHuman(feeHuman.doubleValue());
        int confirm = blockRepository.findLastBlock().getBlockNo() - transaction.getBlockNo() + 1;

        httpSession.setAttribute("transaction", transaction);
        httpSession.setAttribute("confirm", confirm);

        return "transaction";
    }

    // 地址详情页
    @RequestMapping(value="/addressDetail", method=RequestMethod.GET)
    public String addressDetail(HttpSession httpSession, String address){

        List<TTransaction> transList = transactionService.findLast20TransactionByAddress(address);
        TBalance balance = balanceRepository.findOneByAddress(address);
        if(null == balance){
            return "nothing";
        }

        httpSession.setAttribute("transList", transList);
        httpSession.setAttribute("balance", balance);
        httpSession.setAttribute("faaPrice", CommonUtil.FAA_PRICE);

        return "address";
    }

    // 区块详情页
    @RequestMapping(value="/blockDetail", method=RequestMethod.GET)
    public String blockDetail(HttpSession httpSession, int blockNo){

        List<TTransaction> transList = transactionService.findTransactionByBlockNo(blockNo);
        TBlock block = blockRepository.findBlockByBlockNo(blockNo);
        if(null == block){
            return "nothing";
        }

        BigDecimal rewardHuman = CoinsBaseUnits.toHumanUnit(block.getReward(), FaaMain.get());
        BigDecimal feeHuman = CoinsBaseUnits.toHumanUnit(block.getFee(), FaaMain.get());
        block.setRewardHuman(rewardHuman.doubleValue());
        block.setFeeHuman(feeHuman.doubleValue());
        httpSession.setAttribute("transs", transList);
        httpSession.setAttribute("block", block);
        httpSession.setAttribute("faaPrice", CommonUtil.FAA_PRICE);

        return "block";
    }

    // 搜索
    @RequestMapping(value="/searchDetail", method=RequestMethod.GET)
    public String searchDetail(HttpSession httpSession, String keyword){

        if(null == keyword || "".equals(keyword.trim())){
            return "home";
        }

        // 是数字 搜索区块
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if(pattern.matcher(keyword).matches()){
            return this.blockDetail(httpSession, Integer.parseInt(keyword));
        }

        // 钱包地址前缀 搜索钱包地址
        if(keyword.toLowerCase().startsWith("fw")){
            return this.addressDetail(httpSession, keyword);
        }

        // 交易哈希前缀 搜索交易
        if(keyword.toLowerCase().startsWith("fx")){
            return this.transactionDetail(httpSession, keyword);
        }

        return "nothing";
    }
}