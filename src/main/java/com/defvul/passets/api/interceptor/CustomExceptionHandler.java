package com.defvul.passets.api.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 说明:
 * 时间: 2020/2/11 16:59
 *
 * @author wimas
 */
@ControllerAdvice
public class CustomExceptionHandler {


    @ExceptionHandler(value = SecretErrorException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorMessage handleSecretErrorException(SecretErrorException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return handleErrorInfo(request, status, "无权访问");
    }

    private ErrorMessage handleErrorInfo(HttpServletRequest request, HttpStatus status, Object message) {
        ErrorMessage result = handleErrorInfo(request, status);
        result.setMessage(message);
        return result;
    }

    private ErrorMessage handleErrorInfo(HttpServletRequest request, HttpStatus status) {
        ErrorMessage message = new ErrorMessage();
        message.setPath(request.getRequestURI());
        message.setTimestamp(new Date());
        message.setStatus(status.value());
        message.setError(status.getReasonPhrase());
        return message;
    }

}
