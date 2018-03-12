package com.hzcf.variable.listener;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.hzcf.variable.engine.AlgorithmPool;
import com.hzcf.variable.engine.DerivedAlgorithms;
import com.hzcf.variable.loader.DynamicClassLoader;
import com.hzcf.variable.misc.Constant;

/**
 * Create by hanlin on 2017年11月21日 上下文创建完成后执行的事件监听器，在此处读取数据库，并缓存起来。
 * TODO:启动日志不需要存入mongo
 **/
@Component("BeanDefineConfigue")
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private AlgorithmPool algorithmPool;

	@Value("${init.failed.exit}")
	private boolean initFailedExit;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		boolean success = true;
		// 加载类加载器
		ClassLoader classLoader = this.getClass().getClassLoader();
		DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(classLoader);
		// 查询未删除已发布的数据
		String sql = "select g.query_iface,v.* from derived_var v,derived_var_group g where v.var_group_id = g.var_group_id and  v.data_status = 1  and g.data_status = 1 and v.state = 3";
		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql);
		// 使用hashtable的值非空特性，保证class必定有值。
		logger.info(String.format("初始化[%s]个算法", queryForList.size()));
		Map<String, Class<?>> classNameMap = new Hashtable<String, Class<?>>();
		for (Map<String, Object> var : queryForList) {
			String service = var.get("query_iface").toString();
			String varName = var.get("var_ret_name").toString();//返回name系统内的唯一标识
			String className = var.get("clazz_name").toString();
			int varType = Integer.parseInt(var.get("var_type").toString());//衍生变量类型，1为直接变量，2为衍生变量。
			try {
				// 同一个class，在同一个classLoader只存在一个。
				if (!classNameMap.containsKey(className)) {
					if(varType == 1){
						//如果为直接变量，则添加默认的直接变量处理类。
						classNameMap.put(className, Class.forName(Constant.DIRECT_VARIABLE_ALGORITHMS));
					}else{
						byte[] classFile = (byte[]) var.get("class_file");
						//衍生变量需要读取文件动态加载class
						Class<?> generateClass = dynamicClassLoader.generateClass(className, classFile);
						classNameMap.put(className, generateClass);
					}
				}
				DerivedAlgorithms algorithms = (DerivedAlgorithms) classNameMap.get(className).newInstance();
				algorithms.setVarName(varName);
				algorithms.setVar(var);
				algorithmPool.putAlgorithm(service, varName, algorithms);
				algorithmPool.putDerivedVariable(varName, var);
				logger.info(String.format("初始化接口:[%s],变量名:[%s],算法:[%s]", service, varName, algorithms.getClass().getName()));
			} catch (ClassNotFoundException notFound) {
				success = false;
				logger.error("启动失败,无法找到类:" + className, notFound);
			} catch (ClassCastException classCast) {
				success = false;
				logger.error("启动失败,类未实现DerivedAlgorithms接口", classCast);
			} catch (NullPointerException nullPointerException) {
				success = false;
				logger.error("启动失败,实例化算法为空", nullPointerException);
			} catch (Exception e) {
				success = false;
				logger.error("启动失败," + e.getCause(), e.fillInStackTrace());
			}
		}
		// application.properties配置如果发生异常退出
		if (!success && initFailedExit) {
			System.exit(0);
		}
	}
}
