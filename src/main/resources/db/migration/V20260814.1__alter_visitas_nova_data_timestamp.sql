ALTER TABLE emprego_t_visitas
    ALTER COLUMN nova_data TYPE TIMESTAMP
    USING nova_data::TIMESTAMP;
