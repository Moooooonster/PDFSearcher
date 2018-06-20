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
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author up
 * @date 2018/6/20
 */

public class Eva {
    public static final String INDEXPATH = "F:\\PDFSearcher\\pdf\\pdfIndex";
    public static ArrayList<SearchResult> senSenEva(String queryStr) {
        final int pageSize = 10;

        ArrayList<SearchResult> results = new ArrayList<SearchResult>();

        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        Directory dir = null;
        try {
            dir = FSDirectory.open(Paths.get(INDEXPATH));
            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            MyIkAnalyzer analyzer = new MyIkAnalyzer();
            //Analyzer analyzer = new StandardAnalyzer();
//                SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();

            String[] fields = {"name", "mainContent","author","abstact"}; // 要搜索的字段，一般搜索时都不会只搜索一个字段
            // 字段之间的与或非关系，MUST表示and，MUST_NOT表示not，SHOULD表示or，有几个fields就必须有几个clauses
            BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD};


            Query multiFieldQuery = MultiFieldQueryParser.parse(queryStr, fields, clauses, analyzer);

            long startTime = System.currentTimeMillis(); //记录索引开始时间
            TopDocs hits = searcher.search(multiFieldQuery, Integer.MAX_VALUE); // 查找操作


            long endTime = System.currentTimeMillis(); //记录索引结束时间
            String time = String.valueOf((endTime-startTime));
            System.out.println("匹配" + queryStr + "共耗时" + (time) + "毫秒");
            System.out.println("查询到" + hits.totalHits + "条记录");

            //此处加入的是搜索结果的高亮部分
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=#FF4040>","</font></b>"); //如果不指定参数的话，默认是加粗，即<b><b/>
            QueryScorer scorer = new QueryScorer(multiFieldQuery);//计算得分，会初始化一个查询结果最高的得分
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片段
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
            highlighter.setTextFragmenter(fragmenter); //设置一下要显示的片段

            int start = 0;
            int end = (int) hits.totalHits;

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
                    System.out.println(summary);

                    TokenStream tokenStreamAbstact = analyzer.tokenStream("abstact", new StringReader(abstact));
                    String summaryAbstact = highlighter.getBestFragment(tokenStreamAbstact, abstact);
                    if (summaryAbstact!=null&&!"".equals(summaryAbstact)){
                        abstact = summaryAbstact;
                    }
                    System.out.println(summaryAbstact);
                }
                //将搜索结果放入SearchResult类
                SearchResult sR = new SearchResult();

                sR.setName(name);
                sR.setAuthor(author);
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



    public static ArrayList<SearchResult> senKeywordsEva(String queryStr) {
        final int pageSize = 10;

        ArrayList<SearchResult> results = new ArrayList<SearchResult>();

        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        Directory dir = null;
        try {
            dir = FSDirectory.open(Paths.get(INDEXPATH));
            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            MyIkAnalyzer analyzer = new MyIkAnalyzer();
            //Analyzer analyzer = new StandardAnalyzer();
//                SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();

            String[] fields = {"keywords"}; // 要搜索的字段，一般搜索时都不会只搜索一个字段
            // 字段之间的与或非关系，MUST表示and，MUST_NOT表示not，SHOULD表示or，有几个fields就必须有几个clauses
            BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD};


            Query multiFieldQuery = MultiFieldQueryParser.parse(queryStr, fields, clauses, analyzer);

            long startTime = System.currentTimeMillis(); //记录索引开始时间
            TopDocs hits = searcher.search(multiFieldQuery, Integer.MAX_VALUE); // 查找操作


            long endTime = System.currentTimeMillis(); //记录索引结束时间
            String time = String.valueOf((endTime-startTime));
            System.out.println("匹配" + queryStr + "共耗时" + (time) + "毫秒");
            System.out.println("查询到" + hits.totalHits + "条记录");

            //此处加入的是搜索结果的高亮部分
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=#FF4040>","</font></b>"); //如果不指定参数的话，默认是加粗，即<b><b/>
            QueryScorer scorer = new QueryScorer(multiFieldQuery);//计算得分，会初始化一个查询结果最高的得分
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据这个得分计算出一个片段
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
            highlighter.setTextFragmenter(fragmenter); //设置一下要显示的片段

            int start = 0;
            int end = (int) hits.totalHits;

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



                if(name != null) {
                    TokenStream tokenStream = analyzer.tokenStream("name", new StringReader(name));
                    String summary = highlighter.getBestFragment(tokenStream, name);
                    System.out.println(summary);

                    TokenStream tokenStreamContent = analyzer.tokenStream("mainContent", new StringReader(mainContent));
                    String summaryContent = highlighter.getBestFragment(tokenStreamContent, mainContent);
                    System.out.println(summaryContent);
                }
                //将搜索结果放入SearchResult类
                SearchResult sR = new SearchResult();

                sR.setName(name);
                sR.setAuthor(author);
                sR.setBriIntro(abstact);
                sR.setPdfLink(pdfName);
                sR.setTime(time);
                sR.setLabel(keywords);
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

    public static void main(String[] args) {
        File txtFile = new File("F:\\PDFSearcher\\pdf\\句子.txt");
        //搜索句子作为主键，关键词作为值
        HashMap<String,String> sen = new HashMap<String,String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(txtFile));
            String tmp = "";
            int count=0;
            while((tmp=br.readLine())!=null){
                //第一行不是有效值
                if (count>=1){
                    //搜索句子
                    String senSen = tmp.split(",")[0];
                    //搜索句子的关键词
                    String senKeywords = tmp.split(",")[1];
                    System.out.println(senKeywords);
                    sen.put(senSen,senKeywords);
                }
                count++;
            }

            for (HashMap.Entry<String, String> entry:sen.entrySet()
                    ) {
                //搜索的句子
                String searchSen = entry.getKey();
                ArrayList<SearchResult> senSenRe = new ArrayList<SearchResult>();
                ArrayList<SearchResult> senKeywordsRe = new ArrayList<SearchResult>();
                //评价的系统答案
                senSenRe = senSenEva(entry.getValue());
                //参考答案
                senKeywordsRe = senKeywordsEva(entry.getKey());

                //1:创建excel文件
                File file=new File("F:\\PDFSearcher\\pdf\\评价\\"+searchSen+".xls");
                file.createNewFile();

                //2:创建工作簿
                WritableWorkbook workbook=Workbook.createWorkbook(file);
                //3:创建sheet,设置第二三四..个sheet，依次类推即可
                WritableSheet sheet=workbook.createSheet(searchSen, 0);
                //4：设置titles
                String[] titles={"所有相关文档","返回结果","返回结果中的相关文档","所有相关文档的数目","返回结果的数目","返回结果中的相关文档","正确率","召回率"};
                //5:单元格
                Label label=null;
                //6:给第一行设置列名

                for(int i=0;i<titles.length;i++){
                    //x,y,第一行的列名
                    label=new Label(i,0,titles[i]);
                    //7：添加单元格
                    sheet.addCell(label);
                }
                for (int i=1,k=0;i<senKeywordsRe.size();i++){

                    label=new Label(0,i,senKeywordsRe.get(k).getName());
                    sheet.addCell(label);
                    k++;
                }
                for (int i=1,k=0;i<senSenRe.size();i++){

                    label=new Label(1,i,senSenRe.get(k).getName());
                    sheet.addCell(label);
                    k++;
                }
                int c = 1;
                for (int i=1,k=0;i<senSenRe.size();i++){
                    for (int j=1,q=0;j<senKeywordsRe.size();j++){

                        if (senSenRe.get(k).getName().equals(senKeywordsRe.get(q).getName())){
                            label=new Label(2,c,senSenRe.get(k).getName());
                            sheet.addCell(label);
                            c++;
                        }
                        q++;
                    }
                    k++;
                }

                String PrecisonStr = String.valueOf((double)c/(double)senSenRe.size());
                String RecallStr = String.valueOf((double)c/(double)senKeywordsRe.size());
                label=new Label(3,1, String.valueOf(senKeywordsRe.size()));
                sheet.addCell(label);
                label=new Label(4,1, String.valueOf(senSenRe.size()));
                sheet.addCell(label);
                label=new Label(5,1,String.valueOf(c));
                sheet.addCell(label);
                label=new Label(6,1,PrecisonStr);
                sheet.addCell(label);
                label=new Label(7,1,RecallStr);
                sheet.addCell(label);

                //写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
                workbook.write();
                //最后一步，关闭工作簿
                workbook.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }
}
