package com.vivi.utils;

import com.vivi.vo.ItemVO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangwei
 * 2020/7/9 22:48
 *
 * JD搜索页面解析器
 */
public class JDSearchHtmlParser {

    private static final String BASE_URL = "https://search.jd.com/Search?keyword=";

    public static List<ItemVO> parse(String keyword){
        // 指定编码集，防止中文乱码
        Document document = null;
        try {
            document = Jsoup.parse(new URL(BASE_URL + keyword + "&enc=utf-8"), 30000);
        } catch (IOException e) {
            System.out.println("---JDSearchHtmlParser.parse()失败---");
            e.printStackTrace();
        }
        // 每个li标签保存一本书的信息，li标签下是一个div，class是 gl-i-wrap
        Elements elements = document.getElementsByClass("gl-i-wrap");
        // 从每个div中提取出书籍信息
        List<ItemVO> itemVOList = elements.stream().map(element -> {
            ItemVO itemVO = new ItemVO();
            // 图片地址
            itemVO.setImgUrl("http:" + element.getElementsByTag("img").eq(0).attr("src"));
            // 价格
            itemVO.setPrice(element.select(".p-price > strong > i").text());
            // 标题
            itemVO.setTitle(element.getElementsByClass("p-name").eq(0).text());
            // 出版社
            itemVO.setPublisher(element.getElementsByClass("p-shop").eq(0).text());
            return itemVO;
        }).collect(Collectors.toList());
        // bookVOList.forEach(e -> System.out.println(e));
        return itemVOList;
    }

    public static void main(String[] args) {
        parse("java");
    }
}
