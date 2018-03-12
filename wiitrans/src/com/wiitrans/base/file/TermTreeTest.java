package com.wiitrans.base.file;

import java.util.ArrayList;

import com.mysql.fabric.xmlrpc.base.Array;
import com.wiitrans.base.db.model.DictTermBean;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.lang.MakeWordTree;
import com.wiitrans.base.file.lang.TextTerm;

public class TermTreeTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ArrayList<DictTermBean> beanlist = new ArrayList<DictTermBean>();

		DictTermBean bean = new DictTermBean();
		bean.term_id = 1;
		bean.pair_id = 6;
		bean.industry_id = 1;
		bean.term = "aaa";

		beanlist.add(bean);

		// bean = new DictTermBean();
		// bean.term_id = 2;
		// bean.pair_id = 6;
		// bean.industry_id = 1;
		// bean.term = "bbb";
		//
		// beanlist.add(bean);
		//
		// bean = new DictTermBean();
		// bean.term_id = 3;
		// bean.pair_id = 6;
		// bean.industry_id = 1;
		// bean.term = "app bug";
		//
		// beanlist.add(bean);
		//
		// bean = new DictTermBean();
		// bean.term_id = 4;
		// bean.pair_id = 6;
		// bean.industry_id = 1;
		// bean.term = "ccc";
		//
		// beanlist.add(bean);
		//
		// bean = new DictTermBean();
		// bean.term_id = 5;
		// bean.pair_id = 6;
		// bean.industry_id = 1;
		// bean.term = "app abc";
		//
		// beanlist.add(bean);
		//
		bean = new DictTermBean();
		bean.term_id = 6;
		bean.pair_id = 6;
		bean.industry_id = 1;
		bean.term = "aaa bbb";

		beanlist.add(bean);

		bean = new DictTermBean();
		bean.term_id = 6;
		bean.pair_id = 6;
		bean.industry_id = 1;
		bean.term = "aaa bbb";

		beanlist.add(bean);

		Language lang = new English();
		MakeWordTree tree = new MakeWordTree(lang);
		tree.Make(beanlist);
		ArrayList<TextTerm> list = tree.AnalyseGreedyText("aaa tt bbb ", false);
		for (TextTerm textTerm : list) {
			System.out.print(" [term_id] ");
			System.out.print(textTerm.term_id);
			System.out.print(" [term] ");
			System.out.print(textTerm.term);
			System.out.print(" [index] ");
			System.out.print(textTerm.index);
			System.out.println();
		}
		System.out.println();
	}
}
