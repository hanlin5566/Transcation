package com.hzcf.edge.common.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpServletUtils {
    public static boolean jsAjax(HttpServletRequest req){
        //判断是否为ajax请求，默认不是
        boolean isAjaxRequest = false;
        if(!StringUtils.isNotNull(req.getHeader("x-requested-with")) && req.getHeader("x-requested-with").equals("XMLHttpRequest")){
            isAjaxRequest = true;
        }
        return isAjaxRequest;
    }


    /**
     * 获取访问路径
     * @param request
     * @return
     */
    public static String getAppPath(HttpServletRequest request)
    {
        return request.getRequestURL().toString().replace("http://","")
                .replace(request.getRemoteAddr(),"")
                .replace(":","").replace(String.valueOf(request.getServerPort()),"");
    }
}
