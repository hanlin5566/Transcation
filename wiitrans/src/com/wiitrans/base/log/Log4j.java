/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.log;

import java.io.FileInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

// TODO: log4j
public class Log4j {

	// private static PrintStream _ps = null;

	public static org.apache.logging.log4j.Logger _log4jLogger;

	public static void log(Object msg) {
		if (msg != null) {
			_log4jLogger.info(msg);
		}
	}

	// 致命
	public static void fatal(Object msg) {
		if (msg != null) {
			_log4jLogger.fatal(msg);
		}
	}

	// 警告
	public static void warn(Object msg) {
		if (msg != null) {
			_log4jLogger.warn(msg);
		}
	}

	// 错误
	public static void error(Object msg) {
		if (msg != null) {
			_log4jLogger.error(msg);
		}
	}

	// 调试
	public static void debug(Object msg) {
		if (msg != null) {
			_log4jLogger.debug(msg);
		}
	}

	// 信息
	public static void info(Object msg) {
		if (msg != null) {
			_log4jLogger.info(msg);
		}
	}

	public static void error(String msg, Exception ex) {
		_log4jLogger.error(msg, ex);
		// _log4jLogger.error(ex.getMessage());
	}

	public static void error(Exception ex) {
		_log4jLogger.error(ex.getCause(), ex);
		// _log4jLogger.error(ex.getMessage());
	}

	public static void initOuter(int log, String configure) {
		if (_log4jLogger == null) {
			ConfigurationSource source;
			try {
				// source = new ConfigurationSource(new FileInputStream(
				// "/opt/wiitrans/conf/log4j2-config.xml"));

				source = new ConfigurationSource(new FileInputStream(configure));
				Configurator.initialize(null, source);
				if (log == 2) {
					_log4jLogger = LogManager.getLogger("tmsvr");
				} else if (log > 0) {
					_log4jLogger = LogManager.getLogger("manager");
				} else {
					_log4jLogger = LogManager.getLogger("storm");
				}
			} catch (Exception e) {
			}
		}

	}
}
