package com.faa.controller.exception;

import com.faa.utils.CommonUtil;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常捕获
 */
@ControllerAdvice
@ResponseBody
public class GlobalDefaultExceptionHandler {
    Logger logger = Logger.getLogger(GlobalDefaultExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public String defaultErrorHandler(HttpServletRequest httpServletRequest, Exception e)  {
        logger.error("全局异常 请求URL[" + httpServletRequest.getRequestURL() + "] 请求IP[" + CommonUtil.getRemoteHost(httpServletRequest) + "]", e);

        // 返回json字符串到请求页面
        return "{'err': 99}";
    }
}
