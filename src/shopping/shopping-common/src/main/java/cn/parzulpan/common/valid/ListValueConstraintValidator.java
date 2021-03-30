package cn.parzulpan.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-03
 * @project shopping
 * @package cn.parzulpan.common.valid
 * @desc 自定义校验器，ConstraintValidator<ListValue, Integer> <注解,校验值类型>
 */

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
    /** 存储所有可能的值
     */
    private Set<Integer> set = new HashSet<>();

    /**
     * 可以获取注解上的内容并进行处理
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        for(int value : values) {
            set.add(value);
        }
    }

    /**
     * 验证是否校验成功
     * @param integer 需要校验的值
     * @param constraintValidatorContext 上下文环境
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }
}
