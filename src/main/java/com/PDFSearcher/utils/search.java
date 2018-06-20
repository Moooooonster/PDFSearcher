package com.PDFSearcher.utils;



import com.PDFSearcher.model.entity.SearchResult;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static com.PDFSearcher.utils.Eva.senKeywordsEva;


/**
 * Created by dell on 2015/8/11.
 */
public class search {

    public static final String INDEXPATH = "F:\\PDFSearcher\\pdf\\pdfIndex";

    public static ArrayList<SearchResult> searchData(String queryStr,int pageIndex) {
        final int pageSize = 10;

        ArrayList<SearchResult> results = new ArrayList<SearchResult>();

        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        Directory dir = null;
        try {
            dir = FSDirectory.open(Paths.get(INDEXPATH));
            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new MyIkAnalyzer();
            //Analyzer analyzer = new StandardAnalyzer();
//                SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();

            String[] fields = {"name", "mainContent","author","abstact"}; // 要搜索的字段，一般搜索时都不会只搜索一个字段
            // 字段之间的与或非关系，MUST表示and，MUST_NOT表示not，SHOULD表示or，有几个fields就必须有几个clauses
            BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD};


            Query multiFieldQuery = MultiFieldQueryParser.parse(queryStr, fields, clauses, analyzer);

            long startTime = System.currentTimeMillis(); //记录索引开始时间
            TopDocs hits = searcher.search(multiFieldQuery, Integer.MAX_VALUE); // 查找操作


            long endTime = System.currentTimeMillis(); //记录索引结束时间
            String time = String.valueOf((double) (endTime-startTime));
            System.out.println("匹配" + queryStr + "共耗时" + (time) + "毫秒");
            System.out.println("查询到" + hits.totalHits + "条记录");

            //此处加入的是搜索结果的高亮部分
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<font color=#FF4040>","</font>"); //如果不指定参数的话，默认是加粗，即<b><b/>
            QueryScorer scorer = new QueryScorer(multiFieldQuery);//计算得分，会初始化一个查询结果最高的得分
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片段
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
            highlighter.setTextFragmenter(fragmenter); //设置一下要显示的片段

            int start = (pageIndex-1)*pageSize;
            int end = pageIndex*pageSize;

            ScoreDoc[] shits = hits.scoreDocs;

            for(int i=start;i<end;i++) {

                Document doc = searcher.doc(shits[i].doc); // 根据文档打分得到文档的内容
                System.out.println(doc.get("name")); // 找到文件后，输出路径
                String name = doc.get("name");
                String author = doc.get("author");
                String mainContent = doc.get("mainContent");
                String abstact = doc.get("abstact");
                String pdfName = doc.get("pdfName");
                String keywords = doc.get("keywords");
                int resultNum = (int) hits.totalHits;



                if(name != null) {
                    TokenStream tokenStream = analyzer.tokenStream("name", new StringReader(name));
                    String summary = highlighter.getBestFragment(tokenStream, name);
                    if (summary!=null&&!"".equals(summary)){
                        name = summary;
                    }
                    System.out.println(summary);

                    TokenStream tokenStreamAbstact = analyzer.tokenStream("mainContent", new StringReader(mainContent));
                    String summarymainContent = highlighter.getBestFragment(tokenStreamAbstact, mainContent);
                    if (summarymainContent!=null&&!"".equals(summarymainContent)){
                        mainContent = summarymainContent;
                    }
                    System.out.println(summarymainContent);
                }
                //将搜索结果放入SearchResult类
                SearchResult sR = new SearchResult();

                sR.setName(name);
                sR.setAuthor(author);
                sR.setHighlightMainContent(mainContent);
                sR.setBriIntro(abstact);
                sR.setPdfLink(pdfName);
                sR.setTime(time);
                sR.setLabel(keywords);
                sR.setResultNum(resultNum);

                results.add(sR);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }finally {
            return results;
        }
    }


    public static void main(String[] args){
        senKeywordsEva("中国 内地 生物学家");
    }
}

