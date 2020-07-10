package com.vivi.controller;

import com.vivi.service.ItemSearchService;
import com.vivi.utils.JDSearchHtmlParser;
import com.vivi.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author wangwei
 * 2020/7/10 7:00
 */
@Controller
@RequestMapping("/search")
@CrossOrigin(allowedHeaders = "*")
public class ItemSearchController {

    @Autowired
    private ItemSearchService service;

    @GetMapping("/{keyword}/{pageNum}/{pageSize}")
    @ResponseBody
    public List<Map<String, Object>> list(@PathVariable("keyword") String keyword,
                                          @PathVariable("pageNum") Integer pageNum,
                                          @PathVariable("pageSize")Integer pageSize) {
        // 解析网页，得到商品信息列表
        List<ItemVO> itemVOList = JDSearchHtmlParser.parse(keyword);
        if (itemVOList.size() == 0) return null;
        // 创建与搜索词一致的索引
        service.createItemIndex(keyword);
        // 批量插入到ES
        service.bulkSave(keyword, itemVOList);
        // 从ES查出来并指定title字段高亮
        List<Map<String, Object>> list = service.highLightSearch(keyword, "title", keyword, pageNum, pageSize);
        return list;
    }

}
