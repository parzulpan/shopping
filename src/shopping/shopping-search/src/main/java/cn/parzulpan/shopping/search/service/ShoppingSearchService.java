package cn.parzulpan.shopping.search.service;

import cn.parzulpan.shopping.search.vo.SearchParam;
import cn.parzulpan.shopping.search.vo.SearchResult;
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
     * @param searchParam 检索的所有参数
     * @return 返回的结果
     */
    SearchResult search(SearchParam searchParam);
}
