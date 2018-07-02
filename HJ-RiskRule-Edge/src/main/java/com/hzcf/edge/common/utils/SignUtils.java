package com.hzcf.edge.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by liqinwen on 2017/10/10.
 */
public class SignUtils {

    public static final Logger logger = LoggerFactory.getLogger(SignUtils.class);

    public SignUtils() {
    }

    public static Map<String, String> paraFilter(Map<String, String> sArray) {
        Map<String, String> result = new HashMap();
        if(sArray != null && sArray.size() > 0) {
            Iterator var2 = sArray.keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                String value = (String)sArray.get(key);
                if(value != null && !value.equals("") && !"null".equals(value) && !key.equalsIgnoreCase("sign") && !key.equalsIgnoreCase("sign_type")) {
                    result.put(key, value);
                }
            }

            return result;
        } else {
            return result;
        }
    }

    public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList(params.keySet());
        Collections.sort(keys);
        String prestr = "";

        for(int i = 0; i < keys.size(); ++i) {
            String key = (String)keys.get(i);
            String value = (String)params.get(key);
            if(value != null && !value.equals("") && !"null".equals(value) && !key.equalsIgnoreCase("sign") && !key.equalsIgnoreCase("sign_type")) {
                if(i == keys.size() - 1) {
                    prestr = prestr + key + "=" + value;
                } else {
                    prestr = prestr + key + "=" + value + "&";
                }
            }
        }

        return prestr;
    }

    public static Map<String, String> paraObjectFilter(Map<String, Object> sArray) {
        Map<String, String> result = new HashMap();
        if(sArray != null && sArray.size() > 0) {
            Iterator var2 = sArray.keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                String value = String.valueOf(sArray.get(key));
                if(value != null && !value.equals("") && !"null".equals(value) && !key.equalsIgnoreCase("sign") && !key.equalsIgnoreCase("sign_type")) {
                    result.put(key, value);
                }
            }

            return result;
        } else {
            return result;
        }
    }

    public static String createLinkStringForObject(Map<String, Object> params) {
        List<String> keys = new ArrayList(params.keySet());
        Collections.sort(keys);
        String prestr = "";

        for(int i = 0; i < keys.size(); ++i) {
            String key = (String)keys.get(i);
            String value = String.valueOf(params.get(key));
            if(value != null && !value.equals("") && !"null".equals(value) && !key.equalsIgnoreCase("sign") && !key.equalsIgnoreCase("sign_type")) {
                if(i == keys.size() - 1) {
                    prestr = prestr + key + "=" + value;
                } else {
                    prestr = prestr + key + "=" + value + "&";
                }
            }
        }

        return prestr;
    }
}
