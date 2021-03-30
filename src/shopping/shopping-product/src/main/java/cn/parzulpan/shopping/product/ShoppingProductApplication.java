package cn.parzulpan.shopping.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1. 整合 MyBatis-Plus
 *   1.1 导入依赖
 *   1.2 配置
 *     1.2.1 配置数据源
 *       1.2.1.1 导入数据库驱动
 *       1.2.1.2 在配置文件 配置数据源相关信息
 *     1.2.2 配置 MyBatis-Plus
 *       1.2.2.1 使用 @MapperScan 扫描 DAO
 *       1.2.1.2 在配置文件 配置映射文件地址
 *
 * 2. 逻辑删除
 *   2.1 配置全局的逻辑删除规则（可省略）
 *   2.2 配置逻辑删除的组件 Bean（3.1.1以上的高版本可省略）
 *   2.3 给实体类字段加上 @TableLogic
 *
 * 3. 数据校验 JSR303
 *   3.1 给 Bean 添加检验注解：javax.validation.constraints，并自定义自己的 message 提示
 *   3.2 开启校验功能 @Valid，效果：校验错误以后会有默认的响应
 *   3.3 给校验的 Bean 后紧跟一个 BindingResult，就可以获取到校验的结果
 *   3.4 分组校验
 *     3.4.1 @NotBlank(message = "品牌名不能为空", groups = {AddGroup.class, UpdateGroup.class})
 *     给校验注解标注什么情况下需要校验
 *     3.4.2 业务方法参数上使用 @Validated({AddGroup.class}) 注解
 *     3.4.3 默认没有指定分组的校验注解@NotBlack，在分组校验情况 @Validated({AddGroup.class}) 下不生效
 *   3.5 自定义校验
 *     3.5.1 编写一个自定义的校验注解
 *     3.5.2 编写一个自定义的校验器
 *     3.5.3 关联自定义的校验器和自定义的校验注解 @Constraint(validatedBy = { ListValueConstraintValidator.class })
 *           可以指定多个校验器
 *
 * 4. 统一的异常处理
 * @ControllerAdvice
 *
 * 5. 错误状态码
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
 *
 * @author parzulpan
 */

@EnableDiscoveryClient
@MapperScan("cn.parzulpan.shopping.product.dao")
@SpringBootApplication
public class ShoppingProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingProductApplication.class, args);
    }

}
