/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.misc;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Stack;

import com.wiitrans.base.log.Log4j;

public class BundleClassLoader<T> {

	@SuppressWarnings("unchecked")
	public T Load(String classPath, String className) {
		T obj = null;
		try {
			File pathRoot = new File(classPath);

			// 记录加载.class文件的数量
			int classCount = 0;

			if (pathRoot.exists() && pathRoot.isDirectory()) {
				// 获取路径长度
				int pathRootLen = pathRoot.getAbsolutePath().length() + 1;

				Stack<File> stack = new Stack<>();
				stack.push(pathRoot);

				// 遍历类路径
				while (stack.isEmpty() == false) {
					File path = stack.pop();
					File[] classFiles = path.listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isDirectory() || pathname.getName().endsWith(".class");
						}
					});
					for (File subFile : classFiles) {
						if (subFile.isDirectory()) {
							stack.push(subFile);
						} else {
							if (classCount++ == 0) {
								Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
								boolean accessible = method.isAccessible();
								try {
									if (accessible == false) {
										method.setAccessible(true);
									}
									// 设置类加载器
									URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
									// 将当前类路径加入到类加载器中
									method.invoke(classLoader, pathRoot.toURI().toURL());
								} finally {
									method.setAccessible(accessible);
								}
							}
							// 文件名称
							String classFileName = subFile.getAbsolutePath();
							classFileName = classFileName.substring(pathRootLen, classFileName.length() - 6);
							classFileName = classFileName.replace(File.separatorChar, '.');

							if (0 == classFileName.compareTo(className)) {

								// 加载Class类
								obj = (T) Class.forName(classFileName).newInstance();
								Log4j.log("Load bundle class name is : " + classFileName);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			Log4j.error(e);
		}

		return obj;
	}
}
