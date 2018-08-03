package com.hzcf.edge.common.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author JacyCheng
 * 
 */
@Table(name="api_users")
@Entity
public class UserEntity implements Serializable{
	private static final long serialVersionUID = -8657785860287768665L;
	
	@Id
    @GeneratedValue(strategy= GenerationType.AUTO)
	private Integer id;
	private String userId;
	private String userName;
	private String userPwd;
	private String userSalt;
	private String apiCode;
	private String apiKey;
	private String companyCode;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	private boolean isTest;
	private boolean isValid;

	public boolean isTest() {
		return isTest;
	}

	public void setTest(boolean test) {
		isTest = test;
	}

	public String getApiCode() {
		return apiCode;
	}

	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean valid) {
		isValid = valid;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPwd() {
		return userPwd;
	}

	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}

	public String getUserSalt() {
		return userSalt;
	}

	public void setUserSalt(String userSalt) {
		this.userSalt = userSalt;
	}

	@Override
	public Object clone() {
		UserEntity u = null;
		try{
			u = (UserEntity)super.clone();
		}catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return u;
	}

	public  static void copy(final UserEntity userEntityOut,final UserEntity userEntityIn)
	{
		userEntityOut.setUserId(userEntityIn.getUserId());
		userEntityOut.setTest(userEntityIn.isTest());
		userEntityOut.setUserName(userEntityIn.getUserName());
		userEntityOut.setApiCode(userEntityIn.getApiCode());
		userEntityOut.setApiKey(userEntityIn.getApiKey());
		userEntityOut.setCompanyCode(userEntityIn.getCompanyCode());
	}
}
