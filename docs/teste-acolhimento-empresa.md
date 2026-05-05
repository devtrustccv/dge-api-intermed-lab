# Teste Acolhimento Empresa

Endpoint JSON:

```http
POST http://localhost:9001/api/v1/acolhimentos/empresas
Content-Type: application/json
```

Use este JSON direto no body. Nao precisa colocar `dados` nem `detalhes`.

```json
{
  "id": null,
  "id_entidade": null,
  "global_id_entidade": null,
  "id_tecnico": 1,
  "id_cefp": 1,
  "org_id": 1,
  "utilizador": "teste.api",
  "nome_da_entidade": "",
  "nif": "",
  "registo_comercial": "",
  "natureza_juridica": "",
  "n_colaboradores": 0,
  "morada_sede": "",
  "codigo_postal": "",
  "responsavel_pela_entidade": "",
  "ilha": "",
  "concelho": "",
  "zona": "",
  "endereco": "",
  "telefone": "",
  "telemovel": "",
  "email": "",
  "fax": "",
  "possui_vagas_de_emprego": "",
  "identificacao_posto_trabalho": "",
  "fonte_informacao": "",
  "tecnico_atendimento": "",
  "caes": [
    {
      "id": "",
      "cae": "",
      "cae_desc": "",
      "setor_de_atividade": "",
      "setor_de_atividade_desc": "",
      "principal": "",
      "principal_desc": ""
    }
  ],
  "anexos": [
    {
      "id": "",
      "tipo_documento_anexo": "",
      "tipo_documento_anexo_desc": ""
    }
  ]
}
```

## Exemplo Preenchido

```json
{
  "id": null,
  "id_entidade": null,
  "global_id_entidade": 1001,
  "id_tecnico": 1,
  "id_cefp": 1,
  "org_id": 1,
  "utilizador": "teste.api",
  "nome_da_entidade": "Empresa Teste Lda",
  "nif": "123456789",
  "registo_comercial": "RC-2026-001",
  "natureza_juridica": "Sociedade por Quotas",
  "n_colaboradores": 25,
  "morada_sede": "Achada Santo Antonio",
  "codigo_postal": "7600",
  "responsavel_pela_entidade": "Maria Silva",
  "ilha": "Santiago",
  "concelho": "Praia",
  "zona": "Palmarejo",
  "endereco": "Rua Principal",
  "telefone": "2600000",
  "telemovel": "9900000",
  "email": "empresa@teste.cv",
  "fax": "",
  "possui_vagas_de_emprego": "Sim",
  "identificacao_posto_trabalho": "Assistente administrativo",
  "fonte_informacao": "CEFP",
  "tecnico_atendimento": "Tecnico Teste",
  "caes": [
    {
      "id": "1",
      "cae": "4711",
      "cae_desc": "Comercio a retalho em estabelecimentos nao especializados",
      "setor_de_atividade": "Comercio",
      "setor_de_atividade_desc": "Comercio",
      "principal": "S",
      "principal_desc": "Sim"
    }
  ],
  "anexos": [
    {
      "id": "1",
      "tipo_documento_anexo": "1",
      "tipo_documento_anexo_desc": "Registo Comercial"
    }
  ]
}
```

## Teste Com Ficheiro

Para upload de ficheiros, o multipart precisa ter uma parte chamada `dados`, porque o JSON vai junto com ficheiro.

```powershell
curl.exe -X POST "http://localhost:9001/api/v1/acolhimentos/empresas" `
  -F "dados={\"id_tecnico\":1,\"id_cefp\":1,\"org_id\":1,\"utilizador\":\"teste.api\",\"nome_da_entidade\":\"Empresa Teste Lda\",\"nif\":\"123456789\",\"registo_comercial\":\"RC-2026-001\",\"natureza_juridica\":\"Sociedade por Quotas\",\"n_colaboradores\":25,\"morada_sede\":\"Achada Santo Antonio\",\"codigo_postal\":\"7600\",\"responsavel_pela_entidade\":\"Maria Silva\",\"ilha\":\"Santiago\",\"concelho\":\"Praia\",\"zona\":\"Palmarejo\",\"endereco\":\"Rua Principal\",\"telefone\":\"2600000\",\"telemovel\":\"9900000\",\"email\":\"empresa@teste.cv\",\"fax\":\"\",\"possui_vagas_de_emprego\":\"Sim\",\"identificacao_posto_trabalho\":\"Assistente administrativo\",\"fonte_informacao\":\"CEFP\",\"tecnico_atendimento\":\"Tecnico Teste\",\"caes\":[{\"id\":\"1\",\"cae\":\"4711\",\"cae_desc\":\"Comercio a retalho\",\"setor_de_atividade\":\"Comercio\",\"setor_de_atividade_desc\":\"Comercio\",\"principal\":\"S\",\"principal_desc\":\"Sim\"}],\"anexos\":[{\"id\":\"1\",\"tipo_documento_anexo\":\"1\",\"tipo_documento_anexo_desc\":\"Registo Comercial\"}]}" `
  -F "ficheiros=@C:\temp\registo-comercial.pdf"
```

## Teste Multipart Com Campos Separados

Tambem pode testar sem `dados`, igual ao endpoint de acolhimento de cidadao, enviando cada campo no `--form`.

Endpoint:

```http
POST http://localhost:9001/api/v1/acolhimentos/empresas
Content-Type: multipart/form-data
```

```bash
curl --location 'http://localhost:9001/api/v1/acolhimentos/empresas' \
--form 'nome_da_entidade="Empresa Teste Lda"' \
--form 'nif="123456789"' \
--form 'global_id_entidade="1001"' \
--form 'registo_comercial="RC-2026-001"' \
--form 'natureza_juridica="Sociedade por Quotas"' \
--form 'n_colaboradores="25"' \
--form 'morada_sede="Achada Santo Antonio"' \
--form 'codigo_postal="7600"' \
--form 'responsavel_pela_entidade="Maria Silva"' \
--form 'ilha="Santiago"' \
--form 'concelho="Praia"' \
--form 'zona="Palmarejo"' \
--form 'endereco="Rua Principal"' \
--form 'telefone="2600000"' \
--form 'telemovel="9900000"' \
--form 'email="empresa@teste.cv"' \
--form 'fax=""' \
--form 'possui_vagas_de_emprego="Sim"' \
--form 'identificacao_posto_trabalho="Assistente administrativo"' \
--form 'fonte_informacao="CEFP"' \
--form 'id_tecnico="1"' \
--form 'tecnico_atendimento="Tecnico Teste"' \
--form 'id_cefp="1"' \
--form 'org_id="1"' \
--form 'utilizador="teste.api"' \
--form 'caes[0].id="1"' \
--form 'caes[0].cae="4711"' \
--form 'caes[0].cae_desc="Comercio a retalho em estabelecimentos nao especializados"' \
--form 'caes[0].setor_de_atividade="Comercio"' \
--form 'caes[0].setor_de_atividade_desc="Comercio"' \
--form 'caes[0].principal="S"' \
--form 'caes[0].principal_desc="Sim"' \
--form 'anexos[0].id="1"' \
--form 'anexos[0].tipo_documento_anexo="1"' \
--form 'anexos[0].tipo_documento_anexo_desc="Registo Comercial"' \
--form 'anexos[1].id="2"' \
--form 'anexos[1].tipo_documento_anexo="2"' \
--form 'anexos[1].tipo_documento_anexo_desc="NIF da Empresa"' \
--form 'ficheiros=@"C:\temp\registo-comercial.pdf"' \
--form 'ficheiros=@"C:\temp\nif-empresa.pdf"'
```

Tambem aceita os nomes camelCase se preferir:

```bash
curl --location 'http://localhost:9001/api/v1/acolhimentos/empresas' \
--form 'nomeDaEntidade="Empresa Teste Lda"' \
--form 'nif="123456789"' \
--form 'globalIdEntidade="1001"' \
--form 'registoComercial="RC-2026-001"' \
--form 'naturezaJuridica="Sociedade por Quotas"' \
--form 'nColaboradores="25"' \
--form 'moradaSede="Achada Santo Antonio"' \
--form 'codigoPostal="7600"' \
--form 'responsavelPelaEntidade="Maria Silva"' \
--form 'ilha="Santiago"' \
--form 'concelho="Praia"' \
--form 'zona="Palmarejo"' \
--form 'endereco="Rua Principal"' \
--form 'telefone="2600000"' \
--form 'telemovel="9900000"' \
--form 'email="empresa@teste.cv"' \
--form 'possuiVagasDeEmprego="Sim"' \
--form 'identificacaoPostoTrabalho="Assistente administrativo"' \
--form 'fonteInformacao="CEFP"' \
--form 'idTecnico="1"' \
--form 'tecnicoAtendimento="Tecnico Teste"' \
--form 'idCefp="1"' \
--form 'orgId="1"' \
--form 'userCreate="teste.api"' \
--form 'caes[0].cae="4711"' \
--form 'caes[0].cae_desc="Comercio a retalho"' \
--form 'caes[0].setor_de_atividade="Comercio"' \
--form 'caes[0].principal="S"' \
--form 'anexos[0].tipo_documento_anexo="1"' \
--form 'anexos[0].tipo_documento_anexo_desc="Registo Comercial"' \
--form 'ficheiros=@"C:\temp\registo-comercial.pdf"'
```
