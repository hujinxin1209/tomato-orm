package com.tomato.data;

import java.util.List;

public interface Query<T> {
	List<T> list();
	List<T> page(int page, int size);
	T findBy(String name, Object value);
}
