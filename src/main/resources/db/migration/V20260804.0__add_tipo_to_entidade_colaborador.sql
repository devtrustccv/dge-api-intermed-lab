ALTER TABLE emprego_t_entidade_colaborador
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(25);

UPDATE emprego_t_entidade_colaborador
SET tipo = UPPER(cargo)
WHERE tipo IS NULL
  AND UPPER(cargo) IN ('ORIENTADOR', 'COORDENADOR');

UPDATE emprego_t_entidade_colaborador
SET estado = 'A'
WHERE UPPER(estado) = 'ATIVO';

UPDATE emprego_t_entidade_colaborador
SET estado = 'I'
WHERE UPPER(estado) IN ('INATIVO', 'INACTIVO');
