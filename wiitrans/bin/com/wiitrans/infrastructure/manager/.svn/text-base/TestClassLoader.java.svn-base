package com.wiitrans.infrastructure.manager;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Stack;

import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.log.Log4j;

public class TestClassLoader extends Thread {

	public IBundle b = null;

	public void run() {
		try {
			Thread.sleep(5000);
			while (true) {
				b.Request("fileid=t.txt&sessionid=dfsdf-dfasd-fdda-3343-xfdfd");
				Thread.sleep(1000);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		TestClassLoader l = new TestClassLoader();

		// TODO Auto-generated method stub
		try {
			File clazzPath = new File(
					"/root/workspace4work/AnalysisService/bin");

			// 记录加载.class文件的数量
			int clazzCount = 0;

			if (clazzPath.exists() && clazzPath.isDirectory()) {
				// 获取路径长度
				int clazzPathLen = clazzPath.getAbsolutePath().length() + 1;

				Stack<File> stack = new Stack<>();
				stack.push(clazzPath);

				// 遍历类路径
				while (stack.isEmpty() == false) {
					File path = stack.pop();
					File[] classFiles = path.listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isDirectory()
									|| pathname.getName().endsWith(".class");
						}
					});
					for (File subFile : classFiles) {
						if (subFile.isDirectory()) {
							stack.push(subFile);
						} else {
							if (clazzCount++ == 0) {
								Method method = URLClassLoader.class
										.getDeclaredMethod("addURL", URL.class);
								boolean accessible = method.isAccessible();
								try {
									if (accessible == false) {
										method.setAccessible(true);
									}
									// 设置类加载器
									URLClassLoader classLoader = (URLClassLoader) ClassLoader
											.getSystemClassLoader();
									// 将当前类路径加入到类加载器中
									method.invoke(classLoader, clazzPath
											.toURI().toURL());
								} finally {
									method.setAccessible(accessible);
								}
							}
							// 文件名称
							String className = subFile.getAbsolutePath();
							className = className.substring(clazzPathLen,
									className.length() - 6);
							className = className.replace(File.separatorChar,
									'.');
							// 加载Class类
							Class test = Class.forName(className);
							Log4j.log("Read class name is : " + className);

							if (0 == className
									.compareTo("com.wiitrans.analysis.bundle.Bundle")) {
								l.b = (IBundle) test.newInstance();
							}
						}
					}
				}
			}

			l.start();
			l.b.Start();

			sleep(1000000);

		} catch (Exception e) {
			Log4j.error(e);
		}
	}

}
