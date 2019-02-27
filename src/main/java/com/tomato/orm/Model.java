package com.tomato.orm;

import java.util.List;

import com.tomato.data.ActiveRecord;
import com.tomato.data.Dao;
import com.tomato.data.Query;

public class Model<T> extends Dao<T> implements ActiveRecord<T>{

	@Override
	public void save() {
		save(this);
	}

	@Override
	public void delete() {
		delete(this);
	}

	@Override
	public void update() {
		update(this);	
	}

	@Override
	public Query<T> createQuery() {
		return new QueryImpl();
	}
	
	private final String tableName = entityClass.getSimpleName().toLowerCase();
	
	private class QueryImpl implements Query<T>{

		@SuppressWarnings("unchecked")
		@Override
		public List<T> list() {
			String sql = "select * from " + tableName;
			return (List<T>)query(sql, new Object[]{});
		}

		@Override
		public List<T> page(int page, int size) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T findBy(String name, Object value) {
			String sql = "select * from "+ tableName +" where "+name+" = "+"?";
			return (T) query(sql, value).get(0);
		}
		
	}
	
}
