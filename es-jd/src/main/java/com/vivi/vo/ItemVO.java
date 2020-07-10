package com.vivi.vo;

import lombok.Data;

/**
 * @author wangwei
 * 2020/7/9 22:47
 */
@Data
public class ItemVO {

    // 标题
    private String title;
    // 图片地址
    private String imgUrl;
    // 价格
    private String price;
    // 出版社
    private String publisher;
}
