package com.wiitrans.base.db;

import java.util.List;
import java.util.Map;

import com.wiitrans.base.db.model.RoomBean;
import com.wiitrans.base.db.model.RoomBeanMapper;
import com.wiitrans.base.db.model.RoomUserBean;
import com.wiitrans.base.db.model.RoomUserBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class RoomDAO extends CommonDAO {

	private RoomBeanMapper _roommapper = null;
	private RoomUserBeanMapper _roomUsermapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_roommapper = _session.getMapper(RoomBeanMapper.class);
				_roomUsermapper = _session.getMapper(RoomUserBeanMapper.class);
				if (_roommapper != null && _roomUsermapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}
	
	public void insertRoomBean(RoomBean room) {
		 _roommapper.insert(room);
	}
	
	public RoomBean selectRoom(Map<String,Object> param) {
		 return _roommapper.selectRoom(param);
	}
	
	public RoomBean selectRoomByRoom(RoomBean room) {
		 return _roommapper.selectRoomByRoom(room);
	}
	
	public void insertRoomUserBean(RoomUserBean roomUser) {
		_roomUsermapper.insert(roomUser);
	}
	
	public void updateConnectTime(RoomUserBean roomUser) {
		_roomUsermapper.updateConnectTime(roomUser);
	}
	
	public void updateNodeConnectTime(RoomUserBean roomUser) {
		_roomUsermapper.updateNodeConnectTime(roomUser);
	}
	
	public RoomUserBean selectRoomUser(RoomUserBean roomUser){
		return _roomUsermapper.selectRoomUser(roomUser);
	}
	
	public List<RoomUserBean> selectRoomUserList(RoomUserBean roomUser){
		return _roomUsermapper.selectRoomUserList(roomUser);
	}
}