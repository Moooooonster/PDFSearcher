package com.PDFSearcher.web.controller;

import com.PDFSearcher.model.entity.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

import static com.PDFSearcher.utils.search.searchData;

/**
 * @author up
 * @date 2018/6/7
 */
@Controller
public class mainController {

    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String index(){
        return "main";
    }

    @RequestMapping(value = "/search",method = RequestMethod.GET)
    public String result(Model model, @RequestParam("keywords") String keywords,@RequestParam(value = "page",required=false) String page){
        //搜索时间
        int tPage = 1;
        if (page==null||page==""){
        }else{
            tPage = Integer.parseInt(page);
        }

        //搜索结果列表
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        results = searchData(keywords,tPage);

        int resultNum = results.get(0).getResultNum();

        String time = null;
        model.addAttribute("searchTime",results.get(0).getTime());
        model.addAttribute("results",results);

        model.addAttribute("resultNum",resultNum);
        //搜索页面的title
        model.addAttribute("title",keywords+"_百事可喜");
        //搜索词
        model.addAttribute("keywords",keywords);
        //页数
        if (page!=null&&!"".equals(page)){
            model.addAttribute("pageNum",page);
        }


        return "searchResult";
    }
    @RequestMapping(value = "/pdf/{pdfName}",method = RequestMethod.GET)
    public String pdfPage(Model model,@PathVariable String pdfName){
        pdfName += ".pdf";
        model.addAttribute("pdfName",pdfName);
        return "showPdf";
    }
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String test(){
        return "test";
    }


}
