package com.hzcf.edge.common.utils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 懒加载省市资源文件为map
 */
public class CityResourceUtils {

    private static String cityIdNameFilePath = "city_id_name.txt";
    private static String provinceCityDistrictIdName = "province_city_district_id_name.txt";

    private CityResourceUtils() {
    }

    private static class SingletonCityIdNameMap {
        private static Map<String, String> lazyCityIdNameMap = loadMap(cityIdNameFilePath);
        private static Map<String, String> lazyProvinceCityDistrictIdNameMap = loadMap(provinceCityDistrictIdName);
    }

    public static Map<String, String> getCityIdNameMap() {
        return SingletonCityIdNameMap.lazyCityIdNameMap;
    }

    public static Map<String, String> getProvinceCityDistrictIdNameMap() {
        return SingletonCityIdNameMap.lazyProvinceCityDistrictIdNameMap;
    }

    private static Map<String, String> loadMap(String filePath) {
        Map<String, String> map = new HashMap<>();
        InputStream fileInputStream = CityResourceUtils.class.getClassLoader().getResourceAsStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] keyValue = line.split("\t");
                if (keyValue.length == 2) {
                    map.put(keyValue[0], keyValue[1]);
                }
            }
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(fileInputStream);
        }
        return map;
    }

}
