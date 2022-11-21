package com.alexhong.petchill.product.exception;

import com.alexhong.common.exception.BizCodeEnume;
import com.alexhong.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/***
 * handle all exception
 */
@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.alexhong.petchill.product.controller")
@RestControllerAdvice(basePackages = "com.alexhong.petchill.product.controller")
public class PetchillExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
      log.error("Data validation issue{}, exception type{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item->{
            errorMap.put(item.getField(), item.getDefaultMessage());
        });

        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("Error: ", throwable);
        return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(), BizCodeEnume.UNKNOWN_EXCEPTION.getMsg());
    }
}
