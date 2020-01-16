package com.andrewgilmartin.common.mysql.autorecord;

import com.andrewgilmartin.common.annotations.Name;
import com.andrewgilmartin.common.annotations.validation.Null;
import com.andrewgilmartin.common.annotations.validation.Size;
import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.sql.Timestamp;

@Name("example")
public class Example {

    private static final CommonLogger logger = CommonLogger.getLogger(Example.class);
    // constants
    // configured
    private int id;
    private String name;
    private String description;
    private boolean active;
    private Timestamp when;
    // runtime

    public Example(
            @Name("id") int id,
            @Name("name") @Size(50) String name,
            @Name("description") @Size(100) @Null String description,
            @Name("active") boolean active,
            @Name("when") Timestamp when) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.when = when;
    }

    public static CommonLogger getLogger() {
        return logger;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public Timestamp getWhen() {
        return when;
    }
}

// END