package org.metagene.genestrip.service.dto;

public class DB extends DTO {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String dbFilePrefix;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDbFilePrefix() {
		return dbFilePrefix;
	}

	public void setDbFilePrefix(String dbFilePrefix) {
		this.dbFilePrefix = dbFilePrefix;
	}
}
