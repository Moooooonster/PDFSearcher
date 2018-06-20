package com.PDFSearcher.utils;

/**
 * @author up
 * @date 2018/6/18
 */

import com.PDFSearcher.model.entity.Pdf;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.wltea.analyzer.lucene.IKAnalyzer;


import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;

public class CreatIndex {
    public static final String INDEXPATH = "F:\\PDFSearcher\\pdf\\pdfIndex";
    public static void main(String args[]) {

        File pdfdir = new File("F:\\PDFSearcher\\pdf\\pdfFile");


        try
        {
            BooleanQuery.setMaxClauseCount(10000);
            Directory dir = FSDirectory.open(Paths.get(INDEXPATH)); // 使用了nio，存储索引的路径

            Analyzer analyzer = new MyIkAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer); // 新的IndexWriter配置类
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // 创建模式打开
            //iwc.setRAMBufferSizeMB(256.0); // 设置内存缓存的大小，提高索引效率，不过如果修改过大的值，需要修改JVM的内存值
            IndexWriter writer = new IndexWriter(dir, iwc); // 创建IndexWriter


            // 方式一：
            /**
             InputStream input = null;
             input = new FileInputStream( pdfFile );
             //加载 pdf 文档
             PDFParser parser = new PDFParser(new RandomAccessBuffer(input));
             parser.parse();
             document = parser.getPDDocument();
             **/

            // 方式二：
            HashMap<String,Pdf> contentSet= new HashMap<String,Pdf>();

            String[] filename = pdfdir.list();
            for(int i=0 ;i<filename.length;i++){
                File pdfFile1 = new File("F:\\PDFSearcher\\pdf\\pdfFile\\"+filename[i]);
                PDDocument document = null;



                document=PDDocument.load(pdfFile1);
                System.out.println("第"+i);
                // 获取页码
                int pages = document.getNumberOfPages();

                // 读文本内容
                PDFTextStripper stripper=new PDFTextStripper();

                // 设置按顺序输出
                stripper.setEndPage(pages);
                stripper.setSortByPosition(true);
                stripper.setStartPage(1);

                Pdf pdfFile = new Pdf();

                String content = stripper.getText(document);
                pdfFile.setFileName(filename[i]);
                pdfFile.setContent(content);
                System.out.println(content);
                contentSet.put(filename[i].split("_")[0],pdfFile);
                document.close();
            }


            //excel表读取，读取关键词
            Sheet sheet;
            Workbook book;
            //提取 题名，作者，关键词，摘要
            Cell cell1,cell2,cell3,cell4;
            //
            book= Workbook.getWorkbook(new File("F:\\PDFSearcher\\pdf\\BookExcel.xls"));

            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....)
            sheet=book.getSheet(0);
            //获取左上角的单元格
            cell1=sheet.getCell(0,0);
            System.out.println("标题："+cell1.getContents());

            int i = 1;
            int line = sheet.getRows()-1;

            while(line > 0)
            {
                //获取每一行的单元格
                cell1=sheet.getCell(1,i);//（列，行）
                cell2=sheet.getCell(2,i);
                cell3=sheet.getCell(3,i);
                cell4=sheet.getCell(5,i);
                String keywords = cell3.getContents();

                String author   = cell2.getContents();
                if (author==null||"".equals(keywords)){
                    author = "无";
                }


                //如果读取的数据为不为空
                if(keywords!=null&&!"".equals(keywords)){
                    System.out.println(i);
                    System.out.println(cell1.getContents()+"\t"+cell2.getContents()+"\t"+cell3.getContents()+"\t"+cell4.getContents());

                    String name = cell1.getContents();
                    Pdf  pdfFile = contentSet.get(cell1.getContents());
                    String pdfName = pdfFile.getFileName();
                    String mainContent = pdfFile.getContent();
                    String abstact = cell4.getContents();

                    Document doc = new Document();
                    TextField tfKeywords = new TextField("keywords",keywords,Field.Store.YES);
                    TextField tfName = new TextField("name",name,Field.Store.YES);
                    TextField tfMainContent = new TextField("mainContent",mainContent,Field.Store.YES);
                    TextField tfAbstact = new TextField("abstact",abstact,Field.Store.YES);
                    TextField tfPdfName = new TextField("pdfName",pdfName,Field.Store.YES);
                    TextField tfAuthor = new TextField("author",author,Field.Store.YES);
                    tfName.setBoost(100);
                    tfMainContent.setBoost(50);
                    tfAbstact.setBoost(100);
                    tfAuthor.setBoost(100);

                    doc.add(tfKeywords);
                    doc.add(tfName); // 做analyze
                    doc.add(tfAbstact); // 都做analyze
                    doc.add(tfAuthor);
                    doc.add(tfPdfName);
                    doc.add(tfMainContent);
                    writer.addDocument(doc);
                    System.out.println("ok");

                }
                i++;
                line --;
            }
            book.close();
            writer.close();

        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
