package com.hzcf.edge.common.aspect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.edge.common.utils.StringUtils;

/**
 * Create by hanlin on 2018年7月12日
 **/
@Component
public class EdgeFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(EdgeFilter.class);
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		ResponseWrapper wrapperResponse = new ResponseWrapper((HttpServletResponse) response);// 转换成代理类
		// 这里只拦截返回，直接让请求过去，如果在请求前有处理，可以在这里处理
		chain.doFilter(request, wrapperResponse);
		byte[] content = wrapperResponse.getContent();// 获取返回值
		// 判断是否有值
		if (content.length > 0) {
			String str = new String(content, "UTF-8");
			logger.debug("responsebody:{}",str);
			String ciphertext = null;
			try {
				JSONObject parse = JSONObject.parseObject(str);
				JSONObject ret = new JSONObject();
				int state = parse.getInteger("state");
				ret.put("state", state);
				if(state == 1) {
					//失败
					String errorReturn = parse.getString("errorReturn");
					ret.put("errorReturn", errorReturn);
				}else {
					String results = parse.getString("results");
					//成功
					ret.put("results", results);
				}
				ciphertext = ret.toJSONString();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("modify response error:{}",e);
			}finally {
				if(!StringUtils.isNotNull(ciphertext)){
					ciphertext = str;
				}
			}
			// 把返回值输出到客户端
			ServletOutputStream out = response.getOutputStream();
			out.write(ciphertext.getBytes());
			out.flush();
		}

	}

	@Override
	public void destroy() {
	}
}

class ResponseWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream buffer;

	private ServletOutputStream out;

	public ResponseWrapper(HttpServletResponse httpServletResponse) {
		super(httpServletResponse);
		buffer = new ByteArrayOutputStream();
		out = new WrapperOutputStream(buffer);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (out != null) {
			out.flush();
		}
	}

	public byte[] getContent() throws IOException {
		flushBuffer();
		return buffer.toByteArray();
	}

	class WrapperOutputStream extends ServletOutputStream {
		private ByteArrayOutputStream bos;

		public WrapperOutputStream(ByteArrayOutputStream bos) {
			this.bos = bos;
		}

		@Override
		public void write(int b) throws IOException {
			bos.write(b);
		}

		@Override
		public boolean isReady() {

			// TODO Auto-generated method stub
			return false;

		}

		@Override
		public void setWriteListener(WriteListener arg0) {

			// TODO Auto-generated method stub

		}
	}

}
