package com.tomato.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDataSource implements DataSource{
	private static final Logger log = LoggerFactory.getLogger(SimpleDataSource.class);
	
	private List<Connection> pool = Collections.synchronizedList(new LinkedList<Connection>());
	
	private static String DEFAULT_PROPERTIES_FILE_NAME = "application.properties";
	
	private static int MIN_POOL_SIZE; // 最小连接数
	private static int MAX_POOL_SIZE; // 最大连接数
	
	private static String USER;
	private static String PASSWORD;
	private static String URL;
	private static String DRIVER_CLASS;
	
	static {
		Properties properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE_NAME));
			DRIVER_CLASS = properties.getProperty("jdbc.driver", "com.mysql.jdbc.Driver");
			URL = properties.getProperty("jdbc.url", "");
			USER = properties.getProperty("jdbc.user", "root");
			PASSWORD = properties.getProperty("jdbc.password", "123456");
			MIN_POOL_SIZE =Integer.valueOf(properties.getProperty("pool.minsize", "5"));
			MAX_POOL_SIZE = Integer.valueOf(properties.getProperty("pool.maxsize", "15"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SimpleDataSource() {
		initPool();
		log.debug("初始化DataSource: " + this);
	}
	
	private void initPool() {
		try {
			Class.forName(DRIVER_CLASS);
			addConnection(MIN_POOL_SIZE);
		} catch(ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private void addConnection(int quantity) throws SQLException{
		while(quantity > 0) {
			Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
			pool.add(connection);
			--quantity;
		}
	}
	
	private synchronized void checkCapacity() {
		if(pool.size() < 1) {
			try {
				addConnection(MIN_POOL_SIZE);
			} catch (SQLException e) {
				throw new RuntimeException("扩容失败！", e);
			}
			
		}
	}
	
	public Connection getConnection() throws SQLException {
		checkCapacity();
		final Connection connection = pool.remove(0);
		// 使用动态代理，修改connection.close方式，使其调用时放入连接池中而不是关闭
		Connection proxy = (Connection)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				connection.getClass().getInterfaces(), new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if("close".equals(method.getName())) {
							if(pool.size() < MAX_POOL_SIZE) {
								pool.add(connection);
								log.debug("DataSource:" + connection + "返回连接池");
							} else {
								connection.close();
								log.debug("DataSource:" + connection + "关闭");
							}
							return null;
						}
						return method.invoke(connection, args);
					}
				});
		log.debug("DataSource: " + "获得Connection:" + proxy);
		return proxy;
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return null;
	}

	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		
	}

	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
