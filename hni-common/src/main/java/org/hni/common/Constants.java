package org.hni.common;

public interface Constants {
	static final Long SUPER_USER = 1L;
	static final Long ADMIN = 2L;
	static final Long VOLUNTEER = 3L;
	static final Long USER = 5L;  
	static final Long CLIENT = 7L;
	
	// domains
	static final String ORGANIZATION = "organizations";
	static final String PROVIDER = "providers";
	static final String ORDER = "orders";
	static final String MENU = "menus";
	
	// basic permissions
	static final String CREATE = "create";
	static final String READ = "read";
	static final String UPDATE = "update";
	static final String DELETE = "delete";
	
	static final String USERID = "userId";
	static final String PERMISSIONS = "permissions";

}
