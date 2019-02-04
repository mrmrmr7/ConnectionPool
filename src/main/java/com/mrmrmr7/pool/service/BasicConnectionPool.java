package com.mrmrmr7.pool.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hsqldb.jdbc.JDBCConnection;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BasicConnectionPool implements ConnectionPool {
    private final static Logger logger = LogManager
            .getLogger(BasicConnectionPool.class);
    private final String jdbcUrl;
    private final String user;
    private final String password;
    private final int POOL_CAPACITY;
    private final Lock lock = new ReentrantLock();
    private final static List<Connection> inUseConnection = new ArrayList<>();
    private final static List<Connection> notUsedConnection = new ArrayList<>();

    public BasicConnectionPool(String driverClass,
                               String jdbcUrl,
                               String user,
                               String password,
                               int poolCapacity) {

        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
        this.POOL_CAPACITY = poolCapacity;
        initDriver(driverClass);
    }

    @Override
    public Connection getConnection() throws InterruptedException {
        lock.lock();
        Connection connection;
        try {
            if (notUsedConnection.size() + inUseConnection.size() < POOL_CAPACITY) {
                connection = (Connection) Proxy.newProxyInstance(
                        JDBCConnection.class.getClassLoader(),
                        JDBCConnection.class.getInterfaces(),
                        new JDBCConnectionProxy(DriverManager.
                                getConnection(jdbcUrl, jdbcUrl, null)));
                notUsedConnection.add(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (notUsedConnection.isEmpty()) {
            Thread.sleep(10L);
        }

        connection = notUsedConnection.remove(0);
        inUseConnection.add(connection);

        lock.unlock();

        return connection;
    }

    private void initDriver(String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new IllegalStateException("Driver cannot be found", e);
        }
    }

    public synchronized static void releaseConnection(Mockito mockito){
        if (!inUseConnection.isEmpty()) {
            notUsedConnection.add(inUseConnection.remove(0));
        }
    }
}
