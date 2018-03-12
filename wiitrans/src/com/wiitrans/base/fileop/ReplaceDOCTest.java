package com.wiitrans.base.fileop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;


// HWPFDocument->Different Ranges->Paragraph->getText/replaceText
public class ReplaceDOCTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File file = new File("/root/Desktop/test.doc");

		try {
			FileInputStream fis = new FileInputStream(file);
			HWPFDocument doc = new HWPFDocument(fis);
			
			String target = "TT----------------00000000000000000000000000000000000000000000000000000000000000000000000000---------------------------------------------TT\r\n";
			
			int paragraphCount = doc.getRange().numParagraphs();
			for(int index = 0; index < paragraphCount; ++index)
			{
				Paragraph para = doc.getRange().getParagraph(index);
				if(para != null)
				{
					String paraText = para.text();
					System.out.println("+++++++++++++++++++++++++++++");
					System.out.println(paraText);
					para.replaceText(paraText, paraText+"TTTT", 0);
					//para.replaceText(paraText, target+paraText, 0);
					//para.replaceText(target, false);
					
					System.out.println(para.text());
					//System.out.println("+++++++++++++++++++++++++++++");
					
					int runCount = para.numCharacterRuns();
					for(int runIndex = 0; runIndex < runCount; ++runIndex)
					{
						//CharacterRun run = para.getCharacterRun(runIndex);
						//System.out.println("---------------------------");
						//System.out.println(run.text());
						//System.out.println("---------------------------");
						
						//run.replaceText("苏凯", "VB");
					}
				}
			}

			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			FileOutputStream out = null;
			out = new FileOutputStream("/root/Desktop/test-target.doc", true);
			doc.write(ostream);
			out.write(ostream.toByteArray());
			out.close();
			ostream.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
