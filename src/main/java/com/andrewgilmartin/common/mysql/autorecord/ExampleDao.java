package com.andrewgilmartin.common.mysql.autorecord;

import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.sql.Timestamp;
import java.util.List;
import javax.sql.DataSource;

/**
 * This is an example of extending the AutoRecordDao to include additional
 * finder and updater methods.
 */
public class ExampleDao extends AutoRecordDao<Example> {

    private static final CommonLogger logger = CommonLogger.getLogger(ExampleDao.class);

    public ExampleDao create(DataSource dataSource) {
        ExampleDao dao = new ExampleDao();
        dao.setDataSource(dataSource);
        dao.afterPropertiesSet();
        return dao;
    }

    public ExampleDao() throws AutoRecordException, IllegalStateException {
        super(Example.class);
    }

    /**
     * Returns the active instances with the given name
     */
    public List<Example> findByName(String name) throws AutoRecordException {
        return query("select id, name, description, active, when from example where active and name = ?", name);
    }

    /**
     * Returns the active instances order by when.
     */
    public List<Example> findActive() throws AutoRecordException {
        return query("select id, name, description, active, when from example where active order by when");
    }

    /**
     * Returns the instances within the time period order by when.
     */
    public List<Example> findWithinPeriod(Timestamp inclusiveStart, Timestamp exclusiveEnd) throws AutoRecordException {
        return query("select id, name, description, active, when from example where when >= ? and when < ? order by when", inclusiveStart, exclusiveEnd);
    }

    /**
     * Deactivates the (active) instances with the given name.
     */
    public void deactivateByName(String name) throws AutoRecordException {
        update("update example set active = false where active and name = ?", name);
    }
}

// END