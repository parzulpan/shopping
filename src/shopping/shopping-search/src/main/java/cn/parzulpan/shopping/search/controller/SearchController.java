package cn.parzulpan.shopping.search.controller;

import cn.parzulpan.shopping.search.service.ShoppingSearchService;
import cn.parzulpan.shopping.search.vo.SearchParam;
import cn.parzulpan.shopping.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

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

    /**
     * 请求带来的参数是 SearchParam
     * 传给 ES 的参数是 SearchRequest
     * ES 返回结果是 SearchResponse
     * 把结果封装为 SearchResult
     */
    @GetMapping({"/list.html", "/"})
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {
        searchParam.setQueryString(request.getQueryString());
        SearchResult result = shoppingSearchService.search(searchParam);
        model.addAttribute("result", result);

        return "list";
    }
}
