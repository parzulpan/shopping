package cn.parzulpan.shopping.product.web;

import cn.parzulpan.shopping.product.entity.CategoryEntity;
import cn.parzulpan.shopping.product.service.CategoryService;
import cn.parzulpan.shopping.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.web
 * @desc
 */

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        model.addAttribute("categorys", categoryEntities);

        // 视图解析器进行拼串
        // classpath:/templates(前缀) + 返回值 + .html(后缀)
        return "index";
    }

    /**
     * index/catalog.json
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     * JMeter 压测 简单服务
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
