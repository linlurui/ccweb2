package ccait.ccweb.repo;

import ccait.ccweb.entites.DefaultEntity;
import entity.query.Queryable;
import entity.query.core.DBTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

@Scope("prototype")
@org.springframework.stereotype.Repository
public class CCRepository {

    private static final Logger log = LoggerFactory.getLogger( CCRepository.class );

    @Autowired
    private ApplicationContext applicationContext;

    private Connection connection;
    private DBTransaction dbTransaction;
    private boolean hasSession = false;
    private Queryable queryable;

    public CCRepository create() {
        return new CCRepository();
    }

    public synchronized void openSession() {
        try {
            if(hasSession && !connection.isClosed() && !connection.getAutoCommit()) {
                return;
            }
            releaseSession();
            hasSession = true;
            queryable = new DefaultEntity();
            dbTransaction = queryable.dataSource().beginTransaction();
            connection = queryable.getConnection();
        } catch (SQLException | ClassNotFoundException throwables) {
            log.error("Database Transaction Error=====>>> ", throwables);
        }
    }

    public <T extends Queryable> T get(T obj) {
        if(!hasSession) {
            return obj;
        }

        if(obj instanceof Queryable) {
            Queryable<T> queryable = (Queryable<T>) obj;
            queryable.setConnection(connection);
            queryable.setTransaction(dbTransaction);
        }
        return obj;
    }

    public <T> T get(Class<T> clazz) {
        if(!hasSession) {
            return  (T) applicationContext.getBean(clazz);
        }

        Queryable<T> queryable = (Queryable<T>) applicationContext.getBean(clazz);
        queryable.setConnection(connection);
        queryable.setTransaction(dbTransaction);

        return (T) queryable;
    }

    public <T extends Queryable> Connection getConnection(Class<T> clazz) {
        if(connection != null) {
            return connection;
        }

        return ((Queryable<T>) get(clazz)).getConnection();
    }

    public <T extends Queryable> DBTransaction getDBTransaction(Class<T> clazz) {
        if(dbTransaction != null) {
            return dbTransaction;
        }

        return ((Queryable<T>) get(clazz)).getTransaction();
    }

    public void rollback() {
        hasSession = false;
        if(queryable != null) {
            queryable.dataSource().rollback();
        }

        releaseSession();
    }

    public void commit() {
        hasSession = false;
        if(queryable != null) {
            queryable.dataSource().commit();
        }

        releaseSession();
    }

    private synchronized void releaseSession() {
        dbTransaction = null;
        connection = null;
        queryable = null;
        hasSession = false;
    }
}
