package cn.parzulpan.shopping.search.service;

import cn.parzulpan.shopping.search.vo.SearchParam;
import org.springframework.stereotype.Service;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.service
 * @desc
 */
public interface ShoppingSearchService {
    /**
     * 根据条件检索
     * @param searchParam 检索得所有参数
     * @return 返回得结果
     */
    Object search(SearchParam searchParam);
}
