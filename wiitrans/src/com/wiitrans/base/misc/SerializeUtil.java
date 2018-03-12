package com.wiitrans.base.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.wiitrans.base.log.Log4j;

public class SerializeUtil {
    public static byte[] serialize(Object obj) {
	ObjectOutputStream oos = null;
	ByteArrayOutputStream baos = null;
	try {
	    baos = new ByteArrayOutputStream();
	    oos = new ObjectOutputStream(baos);
	    oos.writeObject(obj);
	    byte[] bytes = baos.toByteArray();
	    return bytes;
	} catch (Exception e) {
	    Log4j.error(e);
	}finally{
	    try {
		baos.flush();
		baos.close();
		oos.close();
	    } catch (IOException e) {
		 Log4j.error(e);
	    }
	}
	return null;
    }
    
    public static Object unserialize(byte [] bytes){
	ByteArrayInputStream bis = null;
	ObjectInputStream ois = null;
	try {
	    bis = new ByteArrayInputStream(bytes);
	    ois = new ObjectInputStream(bis);
	    Object obj = ois.readObject();
	    return obj;
	} catch (Exception e) {
	    Log4j.error(e);
	}finally{
	    try {
		bis.close();
		ois.close();
	    } catch (IOException e) {
		 Log4j.error(e);
	    }
	}
	return null;
    }
}
