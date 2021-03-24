package ccait.ccweb.repo;

import ccait.ccweb.entites.DefaultEntity;
import entity.query.Queryable;
import entity.query.core.DBTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

@org.springframework.stereotype.Repository
public class CCRepository {

    private static final Logger log = LoggerFactory.getLogger( CCRepository.class );

    @Autowired
    private ApplicationContext applicationContext;

    private volatile Hashtable<Class, Queryable> sessionMap = new Hashtable<>();
    private Connection connection;
    private DBTransaction dbTransaction;
    private boolean hasSession = false;

    public CCRepository create() {
        return new CCRepository();
    }

    public void openSession() {
        openSession(DefaultEntity.class);
    }

    public <T> void openSession(Class<T> clazz) {
        try {
            releaseSession();
            Queryable<T> queryable = (Queryable<T>) get(clazz);
            dbTransaction = queryable.dataSource().beginTransaction();
            connection = queryable.getConnection();
            updateSession(clazz, queryable);
            hasSession = true;
            if(!DefaultEntity.class.equals(clazz)) {
                sessionMap.get(DefaultEntity.class).setConnection(connection);
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            log.error("Database Transaction Error=====>>> ", throwables);
        }
    }

    public <T> T get(T obj) {
        if(!hasSession) {
            return obj;
        }

        if(obj instanceof Queryable) {
            updateSession(obj.getClass(), (Queryable<T>) obj);
        }
        return obj;
    }

    public <T> T get(Class<T> clazz) {
        if(!hasSession) {
            return  (T) applicationContext.getBean(clazz);
        }

        if(sessionMap!=null && sessionMap.containsKey(clazz)) {
            return (T) sessionMap.get(clazz);
        }

        Queryable<T> queryable = (Queryable<T>) applicationContext.getBean(clazz);

        updateSession(clazz, queryable);

        return (T) queryable;
    }

    public <T> Connection getConnection(Class<T> clazz) {
        if(connection != null) {
            return connection;
        }

        return ((Queryable<T>) get(clazz)).getConnection();
    }

    public <T> DBTransaction getDBTransaction(Class<T> clazz) {
        if(dbTransaction != null) {
            return dbTransaction;
        }

        return ((Queryable<T>) get(clazz)).getTransaction();
    }

    public void rollback() {
        hasSession = false;
        if(sessionMap.size() > 0 && sessionMap.containsKey(DefaultEntity.class)) {
            sessionMap.get(DefaultEntity.class).dataSource().rollback();
        }

        releaseSession();
    }

    public void commit() {
        hasSession = false;
        if(sessionMap.size() > 0 && sessionMap.containsKey(DefaultEntity.class)) {
            sessionMap.get(DefaultEntity.class).dataSource().commit();
        }

        releaseSession();
    }

    private synchronized <T> void updateSession(Class<?> clazz, Queryable<T> queryable) {
        if(connection == null) {
            connection = queryable.getConnection();
            dbTransaction = queryable.getTransaction();
        }
        queryable.setConnection(connection);
        sessionMap.put(clazz, queryable);
    }

    private synchronized void releaseSession() {
        sessionMap.clear();
        dbTransaction = null;
        connection = null;
        hasSession = false;
    }
}
