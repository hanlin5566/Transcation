/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.service;

import io.netty.buffer.ByteBuf;

import com.wiitrans.base.interproc.BaseServer;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.interproc.IServer;
import com.wiitrans.base.misc.*;
import com.wiitrans.conf.Conf;
import com.wiitrans.manager.ClientManager;

public class Service implements IServer {
	
	private ClientManager _clientManager = null;

	public Service(ClientManager clientManager) 
	{
		_clientManager = clientManager;
	}

	public int Start()
	{
		int ret = Const.FAIL;
		
		ret = Run();
		
		return ret;
	}
	
	public int Stop()
	{
		int ret = Const.FAIL;
		
		
		return ret;
	}
	
	private int Run()
	{
		int ret = Const.FAIL;
		
		BaseServer svr = new BaseServer();
		svr.SetPort(Conf.SERVICE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(true);		
		
		return ret;
	}

	@Override
	public int NewClient(Client client)
	{
		return _clientManager.Request(client);
	}
	
	@Override
	public int SetContent(String clientId, ByteBuf content, boolean isSplit)
	{
		return _clientManager.SetContent(clientId, content, isSplit);
	}
}
