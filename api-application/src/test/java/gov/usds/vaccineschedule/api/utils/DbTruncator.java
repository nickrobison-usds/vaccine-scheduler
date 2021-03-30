package gov.usds.vaccineschedule.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * Created by nickrobison on 3/30/21
 * <p>
 * Shamelessly borrowed from another project: https://github.com/CDCgov/simple-report
 */
@Component
public class DbTruncator {

    @Value("${spring.jpa.properties.hibernate.default_schema:public}")
    private String hibernateSchema;

    private static final Logger logger = LoggerFactory.getLogger(DbTruncator.class);
    /**
     * Credit:
     * https://stackoverflow.com/questions/2829158/truncating-all-tables-in-a-postgres-database
     */
    private static final String TRUNCATE_FUNCTION_TEMPLATE =
            "DO "
                    + "$func$ "
                    + "BEGIN "
                    + "   EXECUTE "
                    + "   (SELECT 'TRUNCATE TABLE ' || string_agg(oid::regclass::text, ', ') || ' CASCADE' "
                    + "    FROM   pg_class "
                    + "    WHERE  relkind = 'r' "
                    + // only tables
                    "    AND relname not like 'databasechangelog%%' "
                    + // no liquibase
                    // no spatial_ref
                    "  AND relname not like 'spatial%%' "
                    + // tables!
                    "    AND    relnamespace = '%1$s'::regnamespace "
                    + "   ); "
                    + "END "
                    + "$func$;";

    @Autowired
    private JdbcTemplate jdbc;

    @Transactional
    public void truncateAll() {
        logger.info("Truncating all non-liquibase tables in {}", hibernateSchema);
        jdbc.execute(String.format(TRUNCATE_FUNCTION_TEMPLATE, hibernateSchema));
    }
}

