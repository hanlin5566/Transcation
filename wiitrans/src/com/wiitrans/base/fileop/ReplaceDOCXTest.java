package com.wiitrans.base.fileop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFFootnote;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

// XWPFDocument->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFHeader->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFFooter->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFFootnote->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFTable->XWPFTableRow->XWPFTableCell->XWPFParagraphs->XWPFRun->getText/setText
public class ReplaceDOCXTest {

	public static void main(String[] args) {

		InputStream is;
		try {
			is = new FileInputStream("/root/Desktop/test.docx");
			
			// TODO
			// TextBox

			XWPFDocument doc = new XWPFDocument(is);
			System.out.println("++++++++++++ doc.getParagraphs(); ++++++++++++");
			
			List<XWPFParagraph> paras = doc.getParagraphs();
			for (XWPFParagraph para : paras) {
				// 当前段落的属性
				// CTPPr pr = para.getCTP().getPPr();
				//System.out.println("---------------------------");
				//System.out.println(para.getText());
				
				List<XWPFRun> runList = para.getRuns();
				for(XWPFRun run : runList)
				{
					System.out.println("++++++++++++ doc.getParagraphs()--->getRuns(); ++++++++++++");
					System.out.println(run.getText(0));
				}
			}
			
			System.out.println("++++++++++++ doc.getHeaderList(); ++++++++++++");
			List<XWPFHeader> headerList = doc.getHeaderList();
			for(XWPFHeader headers : headerList)
			{
				List<XWPFParagraph> paraList = headers.getParagraphs();
				for(XWPFParagraph para : paraList)
				{
					//System.out.println("---------------------------");
					//System.out.println(para.getText());
					
					List<XWPFRun> runList = para.getRuns();
					for(XWPFRun run : runList)
					{
						System.out.println("++++++++++++ doc.getHeaderList()--->getRuns(); ++++++++++++");
						System.out.println(run.getText(0));
					}
				}
			}
			
			System.out.println("++++++++++++ doc.getFooterList(); ++++++++++++");
			List<XWPFFooter> footerList = doc.getFooterList();
			for(XWPFFooter footers : footerList)
			{
				List<XWPFParagraph> paraList = footers.getParagraphs();
				for(XWPFParagraph para : paraList)
				{
					//System.out.println("---------------------------");
					//System.out.println(para.getText());
					
					List<XWPFRun> runList = para.getRuns();
					for(XWPFRun run : runList)
					{
						System.out.println("++++++++++++ doc.getFooterList()--->getRuns(); ++++++++++++");
						System.out.println(run.getText(0));
					}
				}
			}
			
			System.out.println("++++++++++++ doc.getFootnotes(); ++++++++++++");
			List<XWPFFootnote> footnoteList = doc.getFootnotes();
			for(XWPFFootnote footnotes : footnoteList)
			{
				List<XWPFParagraph> paraList = footnotes.getParagraphs();
				for(XWPFParagraph para : paraList)
				{
					//System.out.println("---------------------------");
					//System.out.println(para.getText());
					
					List<XWPFRun> runList = para.getRuns();
					for(XWPFRun run : runList)
					{
						System.out.println("++++++++++++ doc.getFootnotes()--->getRuns(); ++++++++++++");
						System.out.println(run.getText(0));
						
						if((run.getText(0) != null) && (run.getText(0).compareTo("dddd") == 0))
						{
							run.setText("!@", 0);
						}
					}
				}
			}
			
			System.out.println("++++++++++++ doc.getTables(); ++++++++++++");
			// 获取文档中所有的表格
			List<XWPFTable> tables = doc.getTables();
			List<XWPFTableRow> rows;
			List<XWPFTableCell> cells;
			for (XWPFTable table : tables) {
				// 表格属性
				// CTTblPr pr = table.getCTTbl().getTblPr();
				// 获取表格对应的行
				rows = table.getRows();
				for (XWPFTableRow row : rows) {
					// 获取行对应的单元格
					cells = row.getTableCells();
					for (XWPFTableCell cell : cells) {
						//System.out.println("---------------------------");
						//System.out.println(cell.getText());
						
						List<XWPFParagraph> paraList = cell.getParagraphs();
						for(XWPFParagraph para : paraList)
						{
							List<XWPFRun> runList = para.getRuns();
							for(XWPFRun run : runList)
							{
								System.out.println("++++++++++++ doc.getFootnotes()--->getRuns(); ++++++++++++");
								System.out.println(run.getText(0));
							}
						}
					}
				}
			}
			is.close();
			
			FileOutputStream fos = new FileOutputStream("/root/Desktop/test-target.docx");  
            doc.write(fos);  
            fos.flush();  
            fos.close();
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
