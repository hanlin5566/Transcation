package com.hzcf.edge.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.edge.common.enums.ResponseEnums;
import com.hzcf.edge.common.exception.CustomException;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("restriction")
public class AESUtil {
    /**
     * 加密方法
     * @param data  要加密的数据
     * @param key 加密key
     * @return 加密的结果
     * @throws Exception
     */
    public static String getEncryptString(String data, String key) {
        try {

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");//"算法/模式/补码方式"
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes("utf-8");
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(key.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return new Base64().encodeToString(encrypted);

        } catch (Exception e) {
            throw new CustomException(ResponseEnums.SYSTEM_ERROR,e.getMessage());
        }
    }

    /**
     * 解密方法
     * @param data 要解密的数据
     * @param key  解密key
     * @return 解密的结果
     * @throws Exception
     */
    public static String getDecryptString(String data, String key) {
        try {
            byte[] encrypted1 = new Base64().decode(data);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(key.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original,"utf-8");
            return originalString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getDecryptMap(String str, String key) {
        String params = null;
        JSONObject data = new JSONObject();
        try {
            params = getDecryptString(str,key).trim();
            data = JSONObject.parseObject(params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }


    public static Key getKey(String strKey) {
        try {
            if(strKey == null) {
                strKey = "";
            }
            KeyGenerator _generator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
            secureRandom.setSeed(strKey.getBytes());
            _generator.init(128, secureRandom);
            return _generator.generateKey();
        } catch (Exception var3) {
            throw new RuntimeException(" 初始化密钥出现异常 ");
        }
    }

    public static String encrypt(String content,String key) {
        try {
            SecureRandom sr = new SecureRandom();
            Key secureKey = getKey(key);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(1, secureKey, sr);
            byte[] bt = cipher.doFinal(content.getBytes());
            String strS = (new BASE64Encoder()).encode(bt);
            strS = replaceBlank(strS);
            return strS;
        } catch (Exception var6) {
            var6.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String content,String key) {
        try {
            content = replaceBlank(content);
            SecureRandom sr = new SecureRandom();
            Cipher cipher = Cipher.getInstance("AES");
            Key secureKey = getKey(key);
            cipher.init(2, secureKey, sr);
            byte[] res = (new BASE64Decoder()).decodeBuffer(content);
            res = cipher.doFinal(res);
            return new String(res);
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    private static String replaceBlank(String str) {
        String dest = "";
        if(str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
            dest = dest.replaceAll("\"", "");
        }

        return dest;
    }
    /**
     * 测试
     */
    public static void main(String args[]) throws Exception {
        String test = "TH2XNH7dbRmhF7e5f/Hc778vCYh/9Vex33e/PyTg0cJG6MoPBkqrwJUxJ6L9YLs6tEqjPwgZrlRZQMGaTmLXk8VX5oROiyRx0b3QdNju/wAqqIHC1mPP+wG6hiDdhYBD/7qxrLdHqedG71hWnvPirL5qXT0arx1574ygYl/umm8pmZ8ZtfMUSPzpFKiqHP0/qRyA1Iew8+UXzBfRln+6d1+5g/rp+Mt7+7yRKdAjJ5+WjvbUaaQWJEYdUc2t1dsec9aGiJq0JFEWw9D0efkYpuCb8FIIE1p8CptgiKjwbmBsuBQ/zTWBaWYHBYImbQ+gh2qRSqOeLFYM84uC4RK6mT34617LTZ4ql920qXZPYawMUCrSY1YBpT3PaSsCP4bKCmCDXGEu/5lEF87yrv26L7pUlm8GbZvHoRZ5mYt5pfUve83Hmkmqb4l/k/ihRQfqLTVfVBOYSeYNKlhmnsutkC0bl5Twg1QxazLC1Au2FLcuDNtGL92zoLAb1gQ8aBr2TqilwtADFAmniAAunviqLz2b75JOKIxSuq3n/5zzWzBPXSb8DPjVEbGOb1b7tXffwyW44pxy4IBk1A33dkmFobJt+6fV2bRojySdiagKmWa/Ftvp09loyibJGMCMxX9Wn2VTC+0220X9+DYtAhbmFnj1dQGmr3bHw9Uz1cueYtI3LAIei0RPkwLo7AJa+oZTVr9yptPeEObo15Q5NlDJKtCuWbxlQTNGB5swHuQm3ba/78Bf49s733F9rHyMsV5vKaJHHBgGH2Cu9n+nH3QaEqso0VYt3Qv/yk7tzAeW9wYjhpddDox703a5Uv1hnpvu//ySgYKWneo2YHu5Fwxc4JT4F6WhXXgtMp8GrOs7CuUA0QGYUcxandWr+4YjWW9lMrkb7urjmHRRFXs0OpM4FRAOWoYcMgBtVdWAXXNfxNrCajyuPq0AlxBdkGBFxQxkjP22VtClljA1Fdl8Vwisa3MMdyFzriTf1rPEBeftlCDZhCepgTc+bgieIHuwFlEmW0aAxHeR39WGfY3RWyLA4clCQBVWo8GMI2f49CDt06fRaFr+d+CFIg6HPRc+9uyzMwhUGns2RyGMcuMSdXKKUSJs1GN72ScXMVRD4HEZupqFPuIifyIszK+MityzLqGz0duXtuXeTEcZC9fFovlLqfgQmymVYzfKozmxxbbPEpcBNF+t+fe+FP7c7GmreI8H00JMQZ4Q7xqdySAyVuB5McHBuw9fXEtU1kjSNMLrNhi8oxmuKorLxdNsecd4FCOPnn08LnXCtQ1mao9LY54DLAbAEbE83sYfbz444LQJRkD1RtI/PpPPFLtiL45qUh2+QSiiHlIlHXxcd810LN0W2o/fW2z9n9o1mhT78+L+HXb1nR4qbUEGGeCltxJuDPApuo7tSv4/XwOlsdN2ce0KeAuNWUCQxRvp1RtYItnPnwdSb3uGenlfPKXIuXAus++kit+BoxBHqpJaNyuMfqYLoEnsQ8qqaq2d8T13NUCW8Q9uXChi+sk/QXlkRTyCdBnEJ2hLbXoydyoK6WHbLKHKo9gQpb5vci/DdMvc0dGZavp13+Y8pcwm2eBqNWsERtm98WAMS27twuAIqgr8UKqI9g7XwP+DZktZVHh4qAc7GpCM2mZ8NostyYSyzMw6y9F4KKkzsDJK0w9eyz3U9zxSotFAQ3Nzi0HyhWKnpopKQ82V7Vdy9GogPNMOGLfkCCNviLSETaz+wHS2HgnYBMGfbcV4BiLlE+U+yM0KF63DTil/XTMW6XjZQ5TmaIbTZ4smPQvboUwX5v1CT4Q8ciNqTTPmxUU6ykrdqAq+Nn5uEKEJNJRCaYR1Nlmd15bBTAgVkILbqGqEmPHJOgTd+mSBmqryTGeIkP6JhvpuEcRtMFDl0FflKBMPlvnDkoJYEDw4fY04B34YkbasuBFkyO29ELAR7aPdNR4u6CAyNyqxNTd4wzFKuaN/2Qtr6JbY3zbCnLuAIyyKvN/4ep3Hi2mtjAWNIN5glhoUwAE/nzV4i3M4Wiji4h7dHN1BoY6rLuGe/cgFgKAwiOrLFhj0qWd9KibK2V8GM0OAhe22DklQx4A9nf6E2qC7K0pWwmRhiG653XjZJu283PEvxD2aVZUDcli3pPTSDrjFOUT2QXhtNMeDBNSiPQZEtqEggMsxTUk0hY94s+/RDyNfEFYuqG50+V/gh8cnb2suSn0WoKdk544FoaFZw6t8EMHcRgJYdt1gmdLPKpaDEgG7q01twVBPHU74mJANfOJbq0Y/txdoI28zySuKZxG1XlHK3zQA7pfwf6kSVDKoR9pQ135bgRuCfkFu/1fpdZoG1w6WFWAvkSkthU8dkVzBWtJK5sb3dpdWZw01VyTqhuBqW3ALqCVuNI0HlHdonUHJq4a31lY4lQmyXiHbST/vtxcuiQTOls02Dz+utCntX/TndrD841Nq+Ne3sAxYG3ieChEXIQqUSbA=";
        String key = "HUnX9E5qV5gLhlDB";
        Map data = getDecryptMap(test, key);

    }
}
