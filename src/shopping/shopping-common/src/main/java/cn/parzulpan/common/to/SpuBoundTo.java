package cn.parzulpan.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.common.to
 * @desc
 */

@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
