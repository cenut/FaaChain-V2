package com.faa.chain.net.filter;

import io.netty.handler.ipfilter.IpFilterRuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class FaaIpFilter {

    private static final Logger logger = LoggerFactory.getLogger(FaaIpFilter.class);

    private final CopyOnWriteArrayList<SingleIpFilterRule> rules;

    public FaaIpFilter(List<InetSocketAddress> addresses) throws UnknownHostException {
        this.rules = new CopyOnWriteArrayList<>();
        for (InetSocketAddress addr : addresses) {
            rules.add(new SingleIpFilterRule(
                    addr.getAddress().getHostAddress().toString(),
                    IpFilterRuleType.valueOf("ACCEPT")));
        }
    }

    public boolean isAcceptable(InetSocketAddress address) {
        return rules.stream().filter(rule -> rule != null && rule.matches(address)).findFirst().flatMap(rule -> {
            if (rule.ruleType() == IpFilterRuleType.ACCEPT) {
                return Optional.of(true);
            } else {
                return Optional.of(false);
            }
        }).orElse(false);
    }
}
