package com.tomato.data;

public interface ActiveRecord<T> {
	void save();
	void delete();
	void update();
	Query<T> createQuery();
}
