package com.hzcf.edge.common.utils;


import net.sf.json.JSONObject;
import org.apache.poi.util.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServletUtil {
	/** 
	   * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址, 
	   * 
	   * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？ 
	   * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。 
	   * 
	   * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 
	   * 192.168.1.100 
	   * 
	   * 用户真实IP为： 192.168.1.110 
	   * 
	   * @param request 
	   * @return 
	   */ 
	  public static String getIpAddress(HttpServletRequest request) { 
	    String ip = request.getHeader("x-forwarded-for"); 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("Proxy-Client-IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("WL-Proxy-Client-IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("HTTP_CLIENT_IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getRemoteAddr(); 
	    } 
	    return ip; 
	  }


    /**
     * 参数解析
     * @param request
     * @return
     */
    public static Map getParameterMap(final HttpServletRequest request) {
		// 参数Map
		Map properties = request.getParameterMap();
		Map returnMap = new HashMap();
		try{
			ServletInputStream inputStream =  request.getInputStream();
			if(inputStream!=null){
				byte[] bytes = IOUtils.toByteArray(inputStream);
				String params = new String(bytes, "UTF-8");
				JSONObject jsonObject =new JSONObject();
				if(StringUtils.isNotNull(params))
				{
					 jsonObject = JSONObject.fromObject(params);
				}else{
					//兼容测试需求
					 Iterator it = properties.keySet().iterator();
					 String key = String.valueOf(it.next());
					 jsonObject = JSONObject.fromObject(key);
				}
				Iterator it = jsonObject.keys();
				// 遍历父级节点
				while (it.hasNext())
				{
					String key = String.valueOf(it.next());
					Object value =  jsonObject.get(key);
					returnMap.put(key, value);
				}
				try{
					JSONObject data = jsonObject.getJSONObject("data");
					if(data.size()>0)
					{
						it = data.keys();
						//遍历data节点
						while (it.hasNext())
						{
							String key = String.valueOf(it.next());
							Object value = data.get(key);
							returnMap.put(key, value);
						}
					}
				} catch (Exception e)
				{
					String data=jsonObject.getString("data");
					returnMap.put("data",data);
				}
			}
		}catch(Exception e){
			return null;
		}
		return returnMap;
	}


}
