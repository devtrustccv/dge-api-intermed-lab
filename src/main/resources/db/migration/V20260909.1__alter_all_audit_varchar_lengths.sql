DO $$
DECLARE
    audit_column RECORD;
BEGIN
    FOR audit_column IN
        SELECT
            columns.table_schema,
            columns.table_name,
            columns.column_name
        FROM information_schema.columns AS columns
        INNER JOIN information_schema.tables AS tables
            ON tables.table_schema = columns.table_schema
           AND tables.table_name = columns.table_name
        WHERE columns.table_schema = CURRENT_SCHEMA()
          AND tables.table_type = 'BASE TABLE'
          AND columns.column_name IN ('user_create', 'user_update')
          AND columns.data_type = 'character varying'
          AND columns.character_maximum_length < 100
        ORDER BY columns.table_name, columns.column_name
    LOOP
        EXECUTE FORMAT(
            'ALTER TABLE %I.%I ALTER COLUMN %I TYPE VARCHAR(100)',
            audit_column.table_schema,
            audit_column.table_name,
            audit_column.column_name
        );
    END LOOP;
END
$$;
