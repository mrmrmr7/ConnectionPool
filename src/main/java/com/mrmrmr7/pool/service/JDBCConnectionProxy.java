package com.mrmrmr7.pool.service;

import org.mockito.Mockito;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.*;

public class JDBCConnectionProxy implements InvocationHandler {
    private Connection connection;

    JDBCConnectionProxy(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {
        if (method.getName().equals("close")) {
            BasicConnectionPool.releaseConnection(Mockito.any());
        }
        return method.invoke(connection, objects);
    }
}
