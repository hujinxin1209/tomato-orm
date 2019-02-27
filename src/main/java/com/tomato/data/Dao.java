package com.tomato.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.spi.DirStateFactory.Result;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomato.helper.BeanHelper;
import com.tomato.helper.ConvertHelper;

public abstract class Dao<T> {
	private static DataSource dataSource = new SimpleDataSource();
	
	private static final Logger log = LoggerFactory.getLogger(Dao.class);
	
	protected Class<?> entityClass;
	
	public Dao() {
		ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        entityClass = (Class<?>) type.getActualTypeArguments()[0];
	}
	
	protected void save(Object entity) {
		String sql = buildSql(entity, SQLType.Insert);
		Number id = execute(sql);
		try {
			BeanHelper.setProperty(entity, "id", id);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("主键注入失败：" + e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	protected void delete(Object entity) {
		String sql = buildSql(entity, SQLType.Delete);
		execute(sql);
	}
	
	protected void update(Object entity) {
		String sql = buildSql(entity, SQLType.Update);
		execute(sql); 
	}
	
	protected List<Object> query(String sql, Object... args){
		List<Object> list = executeQuery(sql, args);
		return list;
	}
	
	private String buildSql(Object entity, SQLType type) {
		StringBuilder sql = new StringBuilder();
		String tableName = entity.getClass().getSimpleName().toLowerCase();
		Map<String, Object> rowMap = ConvertHelper.bean2Map(entity);
		Set<String> columns = rowMap.keySet();
		switch(type) {
		case Insert:
			sql.append("insert into " + tableName + "(");
			for(String column : columns) {
				sql.append(column + ",");
			}
			sql.deleteCharAt(sql.lastIndexOf(","));
			sql.append(") values(");
			for(String col : columns) {
				Object value = rowMap.get(col);
				if(value instanceof String) {
					value = "'" + value + "'";
				}
				sql.append(value + ",");
			}
			sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
            break;
		case Update:
			Object id = rowMap.remove("id");
			if(id == null) {
				throw new RuntimeException("更新操作时,实体的主键(Id)不能为空！");
			}
			sql.append("update " + tableName + " set ");
			for (String column : columns) {
                sql.append(column + " = ");
                Object value = rowMap.get(column);
                if (value instanceof String)
                    value = "'" + value + "'";
                sql.append(value + ",");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(" where id = " + id);
            break;
		case Delete:
            sql.append("delete from " + tableName + " where id = " + rowMap.get("id"));
            break;
        default:
            break;
		}
		return sql.toString();
	}
	
	private List<Object> executeQuery(String sql, Object...args ){
		Connection connection = null;
		List<Object> list = null;
		try {
			connection = dataSource.getConnection();
			PreparedStatement statement = getStatement(sql, connection, args);
			ResultSet result = statement.executeQuery();
			list = parseResult(result);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}
	
	private List<Object> parseResult(ResultSet result){
		List<Object> list = new ArrayList<>();
		Field[] fields = entityClass.getDeclaredFields();
		try {
			while(result.next()) {
				Object bean = entityClass.newInstance();
				for(Field field : fields) {
					String name = field.getName();
					Object value = result.getObject(name);
					BeanHelper.setProperty(bean, name, value);
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	private Number execute(String sql, Object...args) {
		Connection connection = null;
		Number primaryKey = null;
		try {
			connection = dataSource.getConnection();
			log.debug("获得连接！");
			PreparedStatement statement = getStatement(sql, connection, args);
			statement.executeUpdate();
			ResultSet result = statement.getGeneratedKeys();
			if(result.next())
				primaryKey = result.getLong(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return primaryKey;
	}
	
	private Number execute(String sql) {
		return execute(sql, new Object[] {});
	}
	
	private PreparedStatement getStatement(String sql, 
			Connection connection, Object...args) throws SQLException{
		PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int i = 1;
		for(Object object : args) {
			statement.setObject(i, object);
			++i;
		}
		return statement;
	}
}
