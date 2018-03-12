package com.wiitrans.base.order;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import com.wiitrans.base.translator.Translator;

public class OrderOperate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);	
		System.out.println(df.format(246.184));
		System.out.println(df.format(246.185));
		System.out.println(df.format(18452.124));
		System.out.println(df.format(2.125));
		System.out.println(df.format(343.125));
		System.out.println(df.format(674.125));
		System.out.println(df.format(455.125));
		System.out.println(df.format(65.125));
		System.out.println(df.format(7343.125));
		System.out.println(df.format(854.194));
		System.out.println(df.format(1845.185));
		System.out.println(df.format(1852.125));
	}

	public ArrayList<String> ChoiceTrans(ArrayList<Translator> waiters,
			ArrayList<String> list) {
		ArrayList<String> newlist = new ArrayList<String>();
		Random ran = new Random();

		String temp;

		for (Translator trans : waiters) {
			if (!list.contains(trans._sid) && !newlist.contains(trans._sid)) {
				newlist.add(trans._sid);
			}
		}
		if (newlist.size() >= 4) {
			while (newlist.size() > 4) {
				newlist.remove(ran.nextInt(newlist.size()));
			}
		} else if (newlist.size() + list.size() <= 4) {
			newlist.addAll(list);
		} else {
			while (newlist.size() < 4) {
				temp = list.get(ran.nextInt(list.size()));
				if (!newlist.contains(temp)) {
					newlist.add(temp);
				}
			}
		}

		return newlist;
	}
}
