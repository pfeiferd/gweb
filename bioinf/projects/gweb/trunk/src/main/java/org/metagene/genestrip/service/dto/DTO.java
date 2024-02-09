package org.metagene.genestrip.service.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
public abstract class DTO implements Serializable, Cloneable {
	public static long INVALID_ID = -1;
	
	private long id;
	
	public DTO() {
		id = INVALID_ID;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
