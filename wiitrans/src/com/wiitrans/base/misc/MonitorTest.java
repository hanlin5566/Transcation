package com.wiitrans.base.misc;

import java.util.ArrayList;

import com.wiitrans.base.misc.Monitor.EVENT;


public class MonitorTest {

	public static void main(String[] args) {
		
		Monitor m = new Monitor();
		m.SetLocalPath("/root/Desktop/Test");
		m.Start();
		
		ArrayList<String> li = new ArrayList<String>();
		m.Wait(EVENT.MODIFY, li);
		
		for(int index = 0; index < li.size(); ++index)
		{
			System.out.println(li.get(index));
		}

		m.Stop();
	}

}
