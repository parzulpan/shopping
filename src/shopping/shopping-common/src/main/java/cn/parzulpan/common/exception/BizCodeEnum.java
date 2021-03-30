package cn.parzulpan.common.exception;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-03
 * @project shopping
 * @package cn.parzulpan.common.exception
 * @desc
 * 错误码和错误信息定义类
 *   5.1 错误码定义规则为 5 位数字
 *   5.2 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 *   5.3 维护错误码后需要维护错误描述，将它们定义为枚举形式
 *   错误码列表：
 *   10 通用
 *      001 参数格式校验
 *   11 商品
 *   12 订单
 *   13 购物车
 *   14 物流
 *
 *   为了定义这些错误状态码，可以单独定义一个常量类，用来存储这些错误状态码
 */

public enum BizCodeEnum {
    // 系统未知异常
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    // 参数格式校验失败
    VALID_EXCEPTION(10001, "参数格式校验失败");

    private final int code;
    private final String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
