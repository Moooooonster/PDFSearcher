package com.PDFSearcher.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.itextpdf.text.factories.GreekAlphabetFactory.getString;

/**
 * @author up
 * @date 2018/6/6
 */

public class creatIndex {
    public IndexReader getIndexReaderByFSD(String path) {
        FSDirectory fsDirectory = null;
        try {
            fsDirectory = FSDirectory.open(Paths.get(path));
            return DirectoryReader.open(fsDirectory);
        } catch (IOException e) {
            System.out.println("初始化索引目录失败！（IndexReaderUtil.java）" + e.toString());
            e.printStackTrace();
        }
        return null;
    }
    public void createIndex(String filePath, String indexPath) throws IOException {
        // 创建一个简单的分词器,可以对数据进行分词
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriter indexWriter = null;
        if (null == indexWriter) {
            String path = indexPath + "/" + createIndexDirectory();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            File index = new File(path);
            try {
                if (!index.exists()) {// 如果目录不存在
                    index.mkdirs();// 创建文件夹
                }
                FSDirectory dir = FSDirectory.open(Paths.get(path));
                LogDocMergePolicy mergePolicy = new LogDocMergePolicy();
                mergePolicy.setMinMergeDocs(1000);
                iwc.setMaxBufferedDocs(20000);
                iwc.setMergePolicy(mergePolicy);
                indexWriter = new IndexWriter(dir, iwc);
            } catch (Exception e) {
                System.out.println("创建文件错误" + e);
                e.printStackTrace();
            }
        }
        // 获取所有需要建立索引的文件
        File[] files = new File(filePath).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()){ //判断如果是不是文件，则跳过继续其他文件循环
                continue;
            }
            // 文件是第几个
            System.out.println("这是第" + i + "个文件----------------");
            // 文件的完整路径
            System.out.println("完整路径：" + files[i].toString());
            // 获取文件名称
            String fileName = files[i].getName();
            // 获取文件后缀名，将其作为文件类型
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1,
                    fileName.length()).toLowerCase();
            // 文件名称
            System.out.println("文件名称：" + fileName);
            // 文件类型
            System.out.println("文件类型：" + fileType);
            InputStream in = new FileInputStream(files[i]);
            if (fileType != null && !fileType.equals("")) {
                if (fileType.equals("pdf")) {
                    // 获取pdf文档
                    PDFParser parser = new PDFParser((RandomAccessRead) in);
                    parser.parse();
                    PDDocument pdDocument = parser.getPDDocument();
                    System.out.println("page==" + pdDocument.getNumberOfPages());
                    int numberOfPages = pdDocument.getNumberOfPages();
                    if (numberOfPages > 0) {
                        for (int j = 1; j < numberOfPages; j++) {
                            Document doc = new Document();
                            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                            //设置是否排序
                            stripper.setSortByPosition(true);
                            //设置起始页
                            stripper.setStartPage(j);
                            //设置结束页
                            stripper.setEndPage(j);
                            System.out.println("content==" + stripper.getText(pdDocument));
                            // 创建Field对象，并放入doc对象中
                            doc.add(new TextField("contents", stripper.getText(pdDocument),
                                    Field.Store.YES));
                            doc.add(new TextField("page", getString(j),
                                    Field.Store.YES));
                            doc.add(new TextField("filepath", files[i].getAbsolutePath(),
                                    Field.Store.YES));
                            // 创建文件名的域，并放入doc对象中
                            doc.add(new StringField("filename", files[i].getName(), Field.Store.YES));
                            // 写入IndexWriter
                            indexWriter.addDocument(doc);
                            // 换行
                            System.out.println();
                        }
                        indexWriter.commit();
                    }
                    // 关闭文档
                    pdDocument.close();
                    System.out.println("注意：已为文件“" + fileName + "”创建了索引");
                } else {
                    System.out.println();
                    continue;
                }
            }
        }
        // 查看IndexWriter里面有多少个索引
        System.out.println("numDocs=" + indexWriter.numDocs());
        // 关闭索引
        indexWriter.close();
    }

    public static String createIndexDirectory() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }


}
