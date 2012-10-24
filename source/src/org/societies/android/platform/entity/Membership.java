package org.societies.android.platform.entity;

import java.util.Date;

public class Membership {

	private String id;
	private String globalId;
	private String globalIdMember;
	private String glibalIdCommunity;
	private String type;
	private Date creationDate;
	private Date lastModifiedDate;
	private boolean dirty;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getGlobalId() {
		return globalId;
	}
	
	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
	
	public String getGlobalIdMember() {
		return globalIdMember;
	}
	
	public void setGlobalIdMember(String globalIdMember) {
		this.globalIdMember = globalIdMember;
	}
	
	public String getGlibalIdCommunity() {
		return glibalIdCommunity;
	}
	
	public void setGlibalIdCommunity(String glibalIdCommunity) {
		this.glibalIdCommunity = glibalIdCommunity;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
