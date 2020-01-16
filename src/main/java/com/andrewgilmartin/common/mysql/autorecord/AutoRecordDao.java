package com.andrewgilmartin.common.mysql.autorecord;

import com.andrewgilmartin.common.annotations.Name;
import com.andrewgilmartin.common.annotations.validation.NotNull;
import com.andrewgilmartin.common.annotations.validation.Null;
import com.andrewgilmartin.common.annotations.validation.Size;
import com.andrewgilmartin.common.exceptions.CommonIllegalStateException;
import com.andrewgilmartin.common.sql.InsertAndReturnGeneratedKeyConnectionCallback;
import com.andrewgilmartin.common.util.Conditions;
import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * DO NOT USE THIS CLASS YET! IT IS ONLY A WORK-IN-PROGRESS.
 *
 * This class automates the creation and management of simple objects in a
 * single table in a database. All that is needed is for the object's class to
 * have one (or the first) constructor that takes an integer id and the instance
 * properties that must be persisted. For example, the class Foo
 *
 * {@code
 *
 * public class Foo
 * {
 *     ...
 *
 *     public Foo( int id, String name, Long code ) { ...  }
 *
 *     public String getName() { ... }
 *
 *     public code getCode() { ... }
 *
 *     ...
 * }
 * }
 *
 * has the two properties "name" and "code" that must be persisted. The
 * constructor will be used by AutoRecordDao to determine the names and types of
 * the columns to create. The class definition does need some additional
 * annotations. These are Name, Size, Null, and NotNull.
 *
 * Name is used to given the property's name (as the parameter's name is not
 * stored in the class's byte-code). Size is used to limit the length of a
 * String type. Null and NotNull are used to specify of the value is null-able:
 * The default is all values are non-null-able. Primatives are NEVER allowed to
 * be null-able. The full constructor would be
 *
 * {@code Foo (
 *
 * @Name("id")int id,
 * @Name("name")
 * @Size(100)
 * @NotNull String name, Long code ) { ... } }
 */
public class AutoRecordDao<T> extends JdbcDaoSupport {

    private static final CommonLogger logger = CommonLogger.getLogger(AutoRecordDao.class);
    // constants
    private static final Set<Class<?>> SUPPORTED_CLASSES = new HashSet<Class<?>>();
    private static final Map<Class<?>, Class<?>> CLASS_TO_PRIMATIVE = new HashMap<Class<?>, Class<?>>();

    static {
        SUPPORTED_CLASSES.add(boolean.class);
        SUPPORTED_CLASSES.add(Boolean.class);
        SUPPORTED_CLASSES.add(int.class);
        SUPPORTED_CLASSES.add(Integer.class);
        SUPPORTED_CLASSES.add(long.class);
        SUPPORTED_CLASSES.add(Long.class);
        SUPPORTED_CLASSES.add(String.class);
        SUPPORTED_CLASSES.add(java.util.Date.class);
        SUPPORTED_CLASSES.add(java.sql.Date.class);
        SUPPORTED_CLASSES.add(java.sql.Timestamp.class);

        CLASS_TO_PRIMATIVE.put(Boolean.class, boolean.class);
        CLASS_TO_PRIMATIVE.put(Character.class, char.class);
        CLASS_TO_PRIMATIVE.put(Byte.class, byte.class);
        CLASS_TO_PRIMATIVE.put(Short.class, short.class);
        CLASS_TO_PRIMATIVE.put(Integer.class, int.class);
        CLASS_TO_PRIMATIVE.put(Long.class, long.class);
        CLASS_TO_PRIMATIVE.put(Float.class, float.class);
        CLASS_TO_PRIMATIVE.put(Double.class, double.class);
        CLASS_TO_PRIMATIVE.put(Void.class, void.class);
    }
    // runtime
    private Table table;
    private List<Column> columns;
    private String insertStatement;
    private String updateStatement;
    private String deleteStatement;
    private String findByIdStatement;
    private String findAllStatement;
    private String dropTableStatement;
    private String createTableStatement;
    private RowMapper<T> rowMapper = new RowMapper<T>() {
        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException, AutoRecordException {
            try {
                Object[] constructorArguments = new Object[columns.size()];
                for (int i = 0; i < constructorArguments.length; i++) {
                    constructorArguments[i] = rs.getObject(i + 1);
                }
                T instance = (T) table.constructor.newInstance(constructorArguments);
                return instance;
            }
            catch (DataAccessException e) {
                throw new AutoRecordException(e, "unable to map record to instance: id={0}", rs.getInt(1));
            }
            catch (IllegalAccessException e) {
                throw new AutoRecordException(e, "unable to map record to instance: id={0}", rs.getInt(1));
            }
            catch (InstantiationException e) {
                throw new AutoRecordException(e, "unable to map record to instance: id={0}", rs.getInt(1));
            }
            catch (InvocationTargetException e) {
                throw new AutoRecordException(e, "unable to map record to instance: id={0}", rs.getInt(1));
            }
        }
    };

    /**
     * Information about the class's concomitant table.
     */
    private static class Table {

        String name;
        Class classx;
        Constructor constructor;
    };

    /**
     * Information about the class instance variable's concomitant column.
     */
    private static class Column {

        String name;
        Class classx;
        int size;
        boolean nullable = false;
        Method getter;
    };

    public AutoRecordDao(Class<? extends T> recordClass) throws AutoRecordException, IllegalStateException {
        // find the instance constructor
        for (Constructor recordConstructor : recordClass.getConstructors()) {
            Class[] parameterTypes = recordConstructor.getParameterTypes();
            // the first parameter must be an int
            if (parameterTypes.length != 0 && parameterTypes[0] == int.class) {
                // find record and field details
                table = new Table();
                table.classx = recordClass;
                table.constructor = recordConstructor;
                table.name = createSqlName(recordClass.getSimpleName());
                // check to see if the class is named
                for (Annotation annotation : recordConstructor.getAnnotations()) {
                    if (annotation.getClass() == Name.class) {
                        Name name = (Name) annotation;
                        table.name = name.value();
                    }
                }
                Annotation[][] annotations = recordConstructor.getParameterAnnotations();
                columns = new ArrayList<Column>(parameterTypes.length);
                // find field details
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!SUPPORTED_CLASSES.contains(parameterTypes[i])) {
                        throw new CommonIllegalStateException("unsupported parameter class: parameter-index={0}", i);
                    }
                    Column column = new Column();
                    column.classx = parameterTypes[i];
                    column.nullable = false;
                    // find field's name, size, and nullable details
                    for (int j = 0; j < annotations[i].length; j++) {
                        Annotation annotation = annotations[i][j];
                        if (annotation.annotationType() == Name.class) {
                            Name name = (Name) annotation;
                            column.name = createSqlName(name.value());
                        }
                        else if (annotation.annotationType() == Size.class) {
                            Size limit = (Size) annotation;
                            column.size = limit.value();
                        }
                        else if (annotation.annotationType() == Null.class) {
                            // only non-primatives are null-able
                            if (column.classx.isPrimitive()) {
                                throw new CommonIllegalStateException("primatives can not have a null value: name={0}", column.name);
                            }
                            column.nullable = true;
                        }
                        else if (annotation.annotationType() == NotNull.class) {
                            column.nullable = false;
                        }
                    }
                    Conditions.isNotNull(column.name, "no name found: parameter-index={0}", i);
                    // find the field's getter
                    try {
                        Method method = recordClass.getDeclaredMethod(((column.classx == boolean.class || column.classx == Boolean.class) ? "is" : "get") + createJavaName(column.name));
                        if (method.getReturnType() == column.classx) {
                            column.getter = method;
                        }
                        else {
                            throw new CommonIllegalStateException("getter has wrong type: name={0}", column.name);
                        }
                    }
                    catch (NoSuchMethodException e) {
                        throw new CommonIllegalStateException("no getter found: name={0}", column.name);
                    }
                    columns.add(column);
                }
                break;
            }
        }

        Column idColumn = columns.get(0);
        List<Column> nonIdColumns = columns.subList(1, columns.size());

        // build INSERT statement
        StringBuilder sb = new StringBuilder("insert into ").append(table.name).append(" (");
        String separater = "";
        for (Column column : nonIdColumns) {
            sb.append(separater).append(column.name);
            separater = ", ";
        }
        sb.append(" ) values (");
        separater = "";
        for (Column column : nonIdColumns) {
            sb.append(separater).append(" ?");
            separater = ", ";
        }
        sb.append(" )");
        insertStatement = sb.toString();

        // build UPDATE statement
        sb = new StringBuilder("update ").append(table.name);
        separater = "";
        for (Column column : nonIdColumns) { // ie skip the id field
            sb.append(separater).append(" set ").append(column.name).append(" = ?");
            separater = ",";
        }
        sb.append(" where ").append(idColumn.name).append(" = ?");
        updateStatement = sb.toString();

        // build DELETE statement
        deleteStatement = "delete from " + table.name + " where " + idColumn.name + " = ?";

        // build FIND BY ID statement
        sb = new StringBuilder("select ");
        separater = "";
        for (Column column : columns) {
            sb.append(separater).append(column.name);
            separater = ", ";
        }
        sb.append(" from ").append(table.name).append(" where ").append(idColumn.name).append(" = ?");
        findByIdStatement = sb.toString();

        // build FIND ALL statement
        sb = new StringBuilder("select ");
        separater = "";
        for (Column column : columns) {
            sb.append(separater).append(column.name);
            separater = ", ";
        }
        sb.append(" from ").append(table.name);
        findAllStatement = sb.toString();

        // build CREATE TABLE statement
        sb = new StringBuilder("create table ").append(table.name).append(" ( ").append(idColumn.name).append(" int auto_increment not null, ");
        for (Column column : nonIdColumns) {
            sb.append(column.name);
            if (column.classx == Integer.class || column.classx == int.class) {
                sb.append(" int");
            }
            else if (column.classx == Long.class || column.classx == long.class) {
                sb.append(" long");
            }
            else if (column.classx == Boolean.class || column.classx == boolean.class) {
                sb.append(" boolean");
            }
            else if (column.classx == String.class) {
                if (column.size == 0) {
                    sb.append(" text");
                }
                else {
                    sb.append(" varchar(").append(column.size).append(")");
                }
            }
            else if (column.classx == java.util.Date.class || column.classx == java.sql.Date.class) {
                sb.append(" date");
            }
            else if (column.classx == java.sql.Timestamp.class) {
                sb.append(" timestamp");
            }
            else {
                // AutoRecord error if we get here
                throw new CommonIllegalStateException("unsupported class: class={0}", column.classx);
            }
            if (column.nullable) {
                sb.append(" null");
            }
            else {
                sb.append(" not null");
            }
            sb.append(", ");
        }
        sb.append(" primary key (").append(idColumn.name).append(") )");
        createTableStatement = sb.toString();

        // build DROP TABLE statement
        dropTableStatement = "drop table if exists " + table.name;

        // done
        // System.out.println(JSON.toJSON(this));
    }

    public void createTable() throws AutoRecordException {
        try {
            getJdbcTemplate().execute(createTableStatement);
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to create table");
        }
    }

    public void dropTable() throws AutoRecordException {
        try {
            getJdbcTemplate().execute(dropTableStatement);
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to drop table");
        }
    }

    /**
     * Creates a new instance in the table using the given colum values. A valid
     * instance is returned if successful.
     */
    public T create(Object... arguments) throws AutoRecordException, IllegalStateException {
        // confirm that we have the needed arguments
        Conditions.isTrue(
                arguments.length == columns.size() - 1,
                "wrong argument count: count-given={0}; count-needed={1}",
                arguments.length,
                columns.size() - 1);
        for (int i = 0; i < arguments.length; i++) {
            Class<?> argumentClass = arguments[i].getClass();
            Class<?> columnClass = columns.get(i + 1).classx;
            Conditions.isTrue(
                    argumentClass == columnClass || CLASS_TO_PRIMATIVE.get(argumentClass) == columnClass,
                    "wrong argument class: class-given={0}; class-needed={1}",
                    argumentClass,
                    columnClass);
        }
        try {
            // insert the record
            int id = getJdbcTemplate().execute(new InsertAndReturnGeneratedKeyConnectionCallback(insertStatement, arguments));
            // now create the instance
            Object[] constructorArguments = new Object[1 + arguments.length];
            constructorArguments[0] = id;
            System.arraycopy(arguments, 0, constructorArguments, 1, arguments.length);
            T instance = (T) table.constructor.newInstance(constructorArguments);
            // done
            return instance;
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to create record");
        }
        catch (IllegalAccessException e) {
            throw new AutoRecordException(e, "unable to create record");
        }
        catch (InstantiationException e) {
            throw new AutoRecordException(e, "unable to create record");
        }
        catch (InvocationTargetException e) {
            throw new AutoRecordException(e, "unable to create record");
        }
    }

    /**
     * Updates the particular instance. Note that all columns are updated
     * (except the primary key).
     */
    public void update(T record) throws AutoRecordException {
        try {
            Object[] arguments = new Object[columns.size()];
            for (int i = 1; i < columns.size(); i++) {
                arguments[i] = columns.get(i).getter.invoke(record);
            }
            arguments[arguments.length - 1] = columns.get(0).getter.invoke(record); // id
            getJdbcTemplate().update(updateStatement, arguments);
        }
        catch (IllegalAccessException e) {
            throw new AutoRecordException(e, "unable to update record: record={0}", record);
        }
        catch (IllegalArgumentException e) {
            throw new AutoRecordException(e, "unable to update record: record={0}", record);
        }
        catch (InvocationTargetException e) {
            throw new AutoRecordException(e, "unable to update record: record={0}", record);
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to update record: record={0}", record);
        }
    }

    /**
     * Removes the particular instance from the table.
     */
    public void delete(int id) throws AutoRecordException {
        try {
            getJdbcTemplate().update(deleteStatement, id);
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to delete record: id={0}", id);
        }
    }

    /**
     * Returns the particular instance or null if not found.
     */
    public T findById(int id) throws AutoRecordException {
        try {
            T instance = getJdbcTemplate().queryForObject(
                    findByIdStatement,
                    rowMapper,
                    id);
            return instance;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to find record: id={0}", id);
        }
    }

    /**
     * Returns all the instances, unordered.
     */
    public List<T> findAll() throws AutoRecordException {
        try {
            List<T> list = getJdbcTemplate().query(
                    findAllStatement,
                    rowMapper);
            return list;
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to find all records");
        }
    }

    /**
     * Allow a sub-class to query for instances. Use the createSelectStatement()
     * method to create the suitable SQL DML.
     */
    protected List<T> query(String selectStatement, Object... parameters) throws AutoRecordException {
        try {
            List<T> list = getJdbcTemplate().query(
                    selectStatement,
                    parameters,
                    rowMapper);
            return list;
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to find records");
        }
    }

    /**
     * Allow a sub-class to update instances. Use the createUpdateStatement()
     * method to create the suitable SQL DML.
     */
    protected void update(String updateStatement, Object... parameters) throws AutoRecordException {
        try {
            getJdbcTemplate().update(
                    updateStatement,
                    parameters);
        }
        catch (DataAccessException e) {
            throw new AutoRecordException(e, "unable to update records");
        }
    }

    /**
     * Converts a CamelCase name to a snake_case name.
     */
    protected final String createSqlName(String s) {
        return ("`" + s.charAt(0) + s.substring(1).replaceAll("[A-Z]", "_$0") + "`").toLowerCase();
    }

    /**
     * Converts a camelCase name to a CamelCase name.
     */
    protected final String createJavaName(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}

// END