package com.faa.controller.limit;

import com.faa.controller.exception.RequestLimitException;
import com.faa.utils.CommonUtil;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 *  单位时间内 url 访问次数限制实现类
 */
@Aspect
@Component
public class RequestLimitContract {
    Logger logger = Logger.getLogger(RequestLimitContract.class);

    private Map<String, Integer> redisTemplate = new HashMap<String,Integer>();

    @Before("within(@org.springframework.stereotype.Controller *) && @annotation(limit)")
    public void requestLimit(final JoinPoint joinPoint, RequestLimit limit) throws RequestLimitException {
        try {
            Object[] args = joinPoint.getArgs();
            HttpServletRequest request = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof HttpServletRequest) {
                    request = (HttpServletRequest) args[i];
                    break;
                }
            }

            // 被监控的方法必须有 HttpServletRequest 参数
            if (request != null) {
                String ip = CommonUtil.getRemoteHost(request);
                String url = request.getRequestURL().toString();
                String key = "req_limit_".concat(url).concat(ip);
                if (redisTemplate.get(key) == null || redisTemplate.get(key) == 0) {
                    redisTemplate.put(key, 1);
                } else {
                    redisTemplate.put(key, redisTemplate.get(key) + 1);
                }

                int count = redisTemplate.get(key);
                if (count > 0) {
                    Timer timer = new Timer();
                    // 每次访问都创建一个新的计时器
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            redisTemplate.remove(key);
                        }
                    };
                    timer.schedule(task, limit.time());
                }

                if (count > limit.count()) {
                    logger.warn("用户IP[" + ip + "]访问地址[" + url + "]的频率超出设定的限制[" + limit.count() + "]");
                    System.out.println("用户IP[" + ip + "]访问地址[" + url + "]的频率超出设定的限制[" + limit.count() + "]");
                    throw new RequestLimitException();
                }

            } else {
//                System.out.println("request is null");
            }

        } catch (RequestLimitException rle){
            throw rle;
        } catch (Exception e) {
//            logger.error("发生异常: ", e);
        }
    }
}
