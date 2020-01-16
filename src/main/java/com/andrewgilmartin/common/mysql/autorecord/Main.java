package com.andrewgilmartin.common.mysql.autorecord;

import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.util.Date;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Main {

    private static final CommonLogger logger = CommonLogger.getLogger(Main.class);

    // constants
    // configured
    // runtime
    public static void main(String... args) throws Exception {

        DataSource dataSource = new DriverManagerDataSource("jdbc:mysql://localhost:3306/x", "x", "x");

        AutoRecordDao<Example> dao = new AutoRecordDao<>(Example.class);
        dao.setDataSource(dataSource);
        dao.afterPropertiesSet();

        dao.dropTable();
        dao.createTable();

        Example f = dao.create("n1", "d1", false, new Date());

        Example g = dao.findById(f.getId());

        Example h = dao.create("n2", "d2", Boolean.TRUE, new Date());

        for ( Example i : dao.findAll() ) {
            i.getId();
        }

        // dao.delete(f.getId());

        // dao.dropTable();

        ExampleDao exampleDao = new ExampleDao();
        exampleDao.setDataSource(dataSource);
        exampleDao.afterPropertiesSet();

        for ( Example i : exampleDao.findActive() ) {
            i.getId();
        }

        exampleDao.deactivateByName("n1");
    }
}

// END