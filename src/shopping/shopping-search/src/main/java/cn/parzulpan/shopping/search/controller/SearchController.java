package cn.parzulpan.shopping.search.controller;

import cn.parzulpan.shopping.search.service.ShoppingSearchService;
import cn.parzulpan.shopping.search.vo.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.controller
 * @desc
 */

@Controller
public class SearchController {

    @Autowired
    ShoppingSearchService shoppingSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam) {
        Object result = shoppingSearchService.search(searchParam);

        return "list";
    }
}
