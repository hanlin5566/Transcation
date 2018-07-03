package com.hzcf.edge.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hzcf.edge.common.entity.UserEntity;

public interface AuthDAO extends JpaRepository<UserEntity, Long>{
	public UserEntity findByUserIdAndIsValid(String userId,boolean isvalid);
}
