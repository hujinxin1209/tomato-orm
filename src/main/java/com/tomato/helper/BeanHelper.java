package com.tomato.helper;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

public class BeanHelper {
	private BeanHelper() {}
	
	public static void setProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
		BeanUtils.setProperty(bean, name, value);
	}
}
