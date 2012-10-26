/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.util;

/**
 * 表示nul的对象
 * @author sihai
 *
 */
public class Null {
	
	private static Null instance = new Null();
	
	/**
	 * 
	 */
	private Null(){}
	
	/**
	 * 
	 * @return
	 */
	public static Null getInstance() {
		return instance;
	}
}
