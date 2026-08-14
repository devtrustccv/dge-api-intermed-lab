ALTER TABLE emprego_t_visitas
    ALTER COLUMN motivo_indeferimento TYPE VARCHAR(500);

ALTER TABLE emprego_t_visitas
    ALTER COLUMN user_create TYPE VARCHAR(150),
    ALTER COLUMN user_update TYPE VARCHAR(150);
