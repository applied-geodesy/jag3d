package org.applied_geodesy.jag3d.sql;

public class PointTypeMismatchException extends Exception {
	private static final long serialVersionUID = 801280202342575901L;

	public PointTypeMismatchException() { 
		super();
	}
	
	public PointTypeMismatchException(String message) { 
		super(message); 
	}
	
	public PointTypeMismatchException(String message, Throwable cause) { 
		super(message, cause);
	}
	
	public PointTypeMismatchException(Throwable cause) { 
		super(cause); 
	}
}