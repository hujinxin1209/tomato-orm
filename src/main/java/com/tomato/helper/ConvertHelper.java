package com.tomato.helper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.map.LinkedMap;

public class ConvertHelper {
	private ConvertHelper() {
	}

	public static Object map2Bean(Map<String, Object> map, Class<?> beanClass) {
		if (map == null) {
			return null;
		}
		Object bean = null;
		try {
			bean = beanClass.newInstance();
			BeanUtils.populate(bean, map);
		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return bean;
	}

	public static Map<String, Object> bean2Map(Object entity) {
		Map<String, Object> rowMap = new LinkedHashMap<>();
		Class<?> entityClass = entity.getClass();
		Field[] fields = entityClass.getDeclaredFields();
		for (Field field : fields) {
			try {
				PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
				Object value = propertyDescriptor.getReadMethod().invoke(entity, new Object[] {});
				if (value != null) {
					String columnName;
					Column annotation = field.getAnnotation(Column.class);
					if (annotation != null && annotation.name() != null) {
						columnName = annotation.name();
					} else {
						columnName = field.getName();
					}
					rowMap.put(columnName, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rowMap;
	}
}
