package com.wiitrans.frag.grouping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.wiitrans.base.log.Log4j;

import backtype.storm.generated.GlobalStreamId;
import backtype.storm.grouping.CustomStreamGrouping;
import backtype.storm.task.WorkerTopologyContext;

// 分组：根据文件ID进行，如果该文件ID已经部署到Bolt，忽略该emit
public class FragmentationGrouping implements CustomStreamGrouping {

	public class TaskInfo
	{
		public Integer _task = null;
		public int _count = -1;
	}
    
    // Global routine.
    private HashMap<String, Integer> _globalMap = null;
    private ArrayList<TaskInfo> _arrTaskInfo = null;
    
    private Integer GetTask(String fileId)
    {
    	Integer task = null;
    	
    	if(_globalMap.containsKey(fileId))
    	{
    		task = _globalMap.get(fileId);
    	}
    	
    	return task;
    }
    
    private Integer GetNewTask(String fileId)
    {
    	TaskInfo newTaskInfo = null;
    	
    	int count = Integer.MAX_VALUE;
    	for(TaskInfo taskInfo : _arrTaskInfo)
    	{
    		if(taskInfo._count < count)
    		{
    			newTaskInfo = taskInfo;
    			count = taskInfo._count;
    		}
    	}
    	
    	if(newTaskInfo != null)
    	{
    		_globalMap.put(fileId, newTaskInfo._task);
    		newTaskInfo._count++;
    	}
    	
    	return newTaskInfo._task;
    }
    
	@Override
	public List<Integer> chooseTasks(int taskId, List<Object> values)
	{
		// TODO:
		// 1.fileId is get from values.
		// 2.Delete the map value, if the command is end the task.
		String fileId = (String)values.get(0);
		Integer task = GetTask(fileId);
		if(task == null)
		{
			// Assign the new task.
			task = GetNewTask(fileId);
			
			Log4j.debug(String.format("New task id is [%d].", task.intValue()));
		}
		
		return Arrays.asList(task);
	}

	@Override
	public void prepare(WorkerTopologyContext context, GlobalStreamId streamId,	List<Integer> tasks)
	{
		if(_arrTaskInfo == null)
		{
			_arrTaskInfo = new ArrayList<TaskInfo>();
		}
		if(_globalMap == null)
		{
			_globalMap = new HashMap<String, Integer>();
		}
		
        for(Integer id : tasks)
        {
        	TaskInfo taskInfo = new TaskInfo();
        	taskInfo._task = id;
        	taskInfo._count = 0;
        	//taskInfo._lastTime = System.currentTimeMillis();
        	_arrTaskInfo.add(taskInfo);
        }
	}
}
