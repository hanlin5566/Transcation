package com.hzcf.variable.loader;

/**
 * Create by hanlin on 2017年11月22日
 **/
public class DynamicClassLoader extends ClassLoader {
    public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * 根据传入的字节码文件，与类名加载class。
     * @param classFullName 类全路径（包）
     * @return
     * @throws ClassNotFoundException
     */
    public Class<?> generateClass(String classFullName,byte[] raw)throws ClassNotFoundException {
        // definClass方法参数说明：name：类包的全路径如com.hzcf.client.BaseClient
        //                         b：读取的class文件的byte数组
        //                         off：从byte数组中读取的索引
        //                         len：从byte数组中读取的长度
        // 注：假如此类中有引入别的class类，如com.hzcf.client.BaseClient，循环执行findClass方法
        Class<?> clazz = defineClass(classFullName, raw, 0, raw.length);
        // 连接class
        resolveClass(clazz);
        return clazz;
    }
}
