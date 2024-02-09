package org.metagene.genestrip.service.role;

import org.metagene.genestrip.service.ServiceException;

public class MissingRightException extends ServiceException {
	private static final long serialVersionUID = 1L;

	public MissingRightException(String message) {
		super(message);
	}
}
