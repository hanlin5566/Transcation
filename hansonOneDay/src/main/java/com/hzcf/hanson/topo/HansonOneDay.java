package com.hzcf.hanson.topo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzcf.workflow.topo.TopoBuilder;
import com.hzcf.workflow.topo.TopoExecutor;

/**
 * Create by hanlin on 2017年12月26日
 **/
public class HansonOneDay{
	public static void main(String[] args) {
//		//绘制流程--初始节点睡觉，动作可以使用装饰模式。
//		LoggerConsolePriterAction action = new LoggerConsolePriterAction(new ConsolePrinterAction());
//		Hanson hanson = new Hanson();
//		//睡觉-->连线-->睡觉
//		Sleep sleep = new Sleep(action,"A");
//		GetUp getUp = new GetUp(action,"B");
//		TopoBuilder topo = new TopoBuilder();
//		topo.addTransition(sleep,new SequenceFlow("A-B") , getUp);
//		topo.addTransition(getUp,new SequenceFlow("B-A") , sleep);
//		TopoExecutor executor = new TopoExecutor(topo);
//		executor.start("wake");
		String nodeStr = "["
				+ "{\"category\":\"Start\",\"text\":\"开始\",\"type\":\"START\",\"key\":-1,\"loc\":\"-271 -293\"},"
				+ "{\"text\":\"读取身份证信息\",\"type\":\"BETWEEN\",\"handlerClass\":\"com.hzcf.hanson.action.IDCardInfoAction\",\"key\":-2,\"loc\":\"-265 -181\"},"
				+ "{\"text\":\"年龄\",\"figure\":\"Diamond\",\"type\":\"CONDITION\",\"key\":-3,\"loc\":\"-270 -58\"},"
				+ "{\"category\":\"End\",\"text\":\"结束\",\"type\":\"END\",\"key\":-4,\"loc\":\"-269 126\"},"
				+ "{\"text\":\"成年\",\"type\":\"BETWEEN\",\"key\":-5,\"loc\":\"-396 23\"},"
				+ "{\"text\":\"未成年\",\"type\":\"BETWEEN\",\"key\":-6,\"loc\":\"-169 26\"}"
				+ "]";
		String flowsStr = "["
				+ "{\"from\":-1,\"to\":-2,\"fromPort\":\"B\",\"toPort\":\"T\",\"points\":[-271,-271.1918773207554,-271,-261.1918773207554,-271,-234.2459386603777,-265,-234.2459386603777,-265,-207.3,-265,-197.3]},"
				+ "{\"from\":-3,\"to\":-5,\"fromPort\":\"L\",\"toPort\":\"T\",\"visible\":true,\"points\":[-315.8199768066406,-57.99999999999999,-325.8199768066406,-57.99999999999999,-396,-57.99999999999999,-396,-30.649999999999995,-396,-3.299999999999997,-396,6.700000000000003],\"text\":\"age>=18\"},"
				+ "{\"from\":-3,\"to\":-6,\"fromPort\":\"R\",\"toPort\":\"T\",\"visible\":true,\"points\":[-224.18002319335938,-57.99999999999999,-214.18002319335938,-57.99999999999999,-169,-57.99999999999999,-169,-29.15,-169,-0.3000000000000007,-169,9.7],\"text\":\"age<18\"},"
				+ "{\"from\":-2,\"to\":-3,\"fromPort\":\"B\",\"toPort\":\"T\",\"points\":[-265,-164.70000000000002,-265,-154.70000000000002,-265,-127.4,-270,-127.4,-270,-100.1,-270,-90.1]},"
				+ "{\"from\":-5,\"to\":-4,\"fromPort\":\"B\",\"toPort\":\"L\",\"points\":[-396,39.3,-396,49.3,-396,126,-348.40406133962233,126,-300.80812267924466,126,-290.80812267924466,126]},"
				+ "{\"from\":-6,\"to\":-4,\"fromPort\":\"B\",\"toPort\":\"R\",\"points\":[-169,42.3,-169,52.3,-169,126,-203.09593866037767,126,-237.19187732075537,126,-247.19187732075537,126]}]";
		JSONArray nodes = JSONObject.parseArray(nodeStr);
		JSONArray flows = JSONObject.parseArray(flowsStr);
		TopoBuilder topo = new TopoBuilder(nodes,flows);
		TopoExecutor executor = new TopoExecutor(topo);
		executor.run();
	}
}
