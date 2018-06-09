package com.PDFSearcher.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
    public String result(Model model){
        return "searchResult";
    }


}
