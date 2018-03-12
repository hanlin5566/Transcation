package com.wiitrans.base.misc;

import com.wiitrans.base.bundle.IBundle;

public class TestBundleClassLoader extends Thread {
	
	public static IBundle _bundle = null;
	
	public int Start()
	{
		BundleClassLoader<IBundle> loader = new BundleClassLoader<IBundle>();
		
		_bundle = loader.Load("/root/workspace4work/AnalysisService/bin", 
				"com.wiitrans.analysis.bundle.Bundle");
		
		_bundle.Start();
		
		return 0;
	}
	
	public void run()
	{
		try
		{
			Thread.sleep(5000);
			while(true)
			{
				_bundle.Request("fileid=t.txt&sessionid=dfsdf-dfasd-fdda-3343-xfdfd");
				Thread.sleep(1000);
			}
			
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		TestBundleClassLoader test = new TestBundleClassLoader();
		test.start();
		test.Start();
		
		Thread.sleep(1000000);
	}
}
