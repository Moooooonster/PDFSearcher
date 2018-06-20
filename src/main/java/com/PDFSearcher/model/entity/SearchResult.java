package com.PDFSearcher.model.entity;

/**
 * @author up
 * @date 2018/6/10
 */
import lombok.*;

@Data
public class SearchResult {
    //pdf名字
    String name;
    //简介
    String briIntro;
    //标签
    String label;
    //该期pdf文章作者
    String author;
    //内容的highlight
    String highlightMainContent;
    //杂志所在位置
    String pdfLink;
    //搜索花的时间
    String time;
    //搜索结果页数
    int pageNnum;
    //搜索结果数量
    int resultNum;

}
