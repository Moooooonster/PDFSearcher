package com.PDFSearcher;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;

/**
 * @author up
 * @date 2018/6/7
 */
@ComponentScan("com.PDFSearcher")
@SpringBootApplication
public class Application {
    public static void main(String[] args){
        ConfigurableApplicationContext run = run (Application.class,args);
    }
}
