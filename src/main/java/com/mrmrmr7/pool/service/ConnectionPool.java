package com.mrmrmr7.pool.service;

import java.sql.Connection;

public interface ConnectionPool {
    Connection getConnection() throws InterruptedException;
}
