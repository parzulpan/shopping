package cn.parzulpan.shopping.product.exception;

import cn.parzulpan.common.exception.BizCodeEnum;
import cn.parzulpan.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-03
 * @project shopping
 * @package cn.parzulpan.shopping.product.exception
 * @desc 统一异常处理
 */

@Slf4j
@RestControllerAdvice(basePackages = "cn.parzulpan.shopping.product.controller")
public class ShoppingExceptionControllerAdvice {

    /**
     * 数据校验的异常
     * @param exception Exception
     * @return R
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException exception) {
        Map<String, String> map = new HashMap<>(0);
        BindingResult bindingResult = exception.getBindingResult();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            String message = fieldError.getDefaultMessage();
            String field = fieldError.getField();
            map.put(field, message);
        });

        log.error("数据校验出现问题 {}，异常类型 {} ",exception.getMessage(),exception.getClass());
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    /**
     * 默认未知异常
     * @param throwable Exception
     * @return R
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("未知异常 {}， 异常类型 {} ", throwable.getMessage(), throwable.getClass());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }
}
