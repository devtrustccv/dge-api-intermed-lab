# Teste Acolhimento Utente

Endpoint JSON:

```http
POST http://localhost:9001/api/v1/acolhimentos
Content-Type: application/json
```

Use este JSON direto no body do Postman em `Body > raw > JSON`.

```json
{
  "tipoServico": "EMPREGO",
  "tipoUtente": "UTENTE",
  "canal": "CEFP",
  "fonteInformacao": "CEFP",
  "denominacaoUtente": "Utente Teste",
  "idPessoa": 2001101700869,
  "nif": 123456789,
  "utente": {
    "idPessoa": 2001101700869,
    "nome": "Utente Teste",
    "tipoDocumento": "CNI",
    "numDocumento": "2001101700869",
    "dataNascimento": "1990-01-10",
    "sexo": "M",
    "nif": 123456789,
    "habilitacaoLiteraria": "12"
  },
  "dadosEmprego": {
    "situacao_face_ao_emprego": "DESEMPREGADO",
    "profissao": "Tecnico de Informatica",
    "empresa": "Empresa Teste Lda",
    "setor_de_atividade": "TECNOLOGIA",
    "ilha_empresa": "ST",
    "concelho_empresa": "PR",
    "zona_empresa": "PALMAREJO"
  },
  "detalhes": {
    "csu": "CSU-001",
    "como_obteve_informacao": "CEFP",
    "ilha": "ST",
    "zona": "ACHADA_SANTO_ANTONIO",
    "area_": "INFORMATICA",
    "email": "utente.teste@email.cv",
    "outro": "Informacao adicional de teste",
    "empresa": "Empresa Teste Lda",
    "concelho": "PR",
    "endereco": "Achada Santo Antonio, Praia",
    "telefone": "2600000",
    "link_foto": "https://sniacapps.gov.cv/sniac_prod/SNIAC.IGRP_PORTAL.download_file_img_bio?p_tipo_doc=1&p_id_pessoa=2001101700869&p_id_tp_imagem=1&p_id_documento=89959228",
    "profissao": "Tecnico de Informatica",
    "telemovel": "9912345",
    "observacoes": "Registo de teste completo",
    "estado_civil": "S",
    "ilha_empresa": "ST",
    "naturalidade": "238",
    "zona_empresa": "PALMAREJO",
    "data_validade": "09-01-2030",
    "carta_conducao": "S",
    "zona_empresa_1": "9911122",
    "concelho_empresa": "PR",
    "local_de_emissao": "CASA DO CIDADAO DA PRAIA",
    "o_que_deseja_criar": "NEGOCIO_PROPRIO",
    "setor_de_atividade": "TECNOLOGIA",
    "o_que_deseja_criar_1": "DESENVOLVIMENTO_SOFTWARE",
    "setor_de_atividade_1": "SERVICOS",
    "tipo_servico_solicitado": "EMPREGO",
    "situacao_face_ao_emprego": "DESEMPREGADO",
    "local_de_trabalho_preferencial": "PRAIA",
    "autoriza_a_divulgacao_dos_seus_dados_para_efeito__de_emprego": "S"
  },
  "anexos": [
    {
      "id": "22",
      "ver_documento": "",
      "ver_documento_desc": "",
      "tipo_documento_anexo": "CNI",
      "tipo_documento_anexo_desc": "CNI"
    },
    {
      "id": "23",
      "ver_documento": "",
      "ver_documento_desc": "",
      "tipo_documento_anexo": "TRE",
      "tipo_documento_anexo_desc": "TRE"
    }
  ]
}
```

## Teste Com Ficheiro

Para upload de ficheiros, use `multipart/form-data` com uma parte chamada `dados`, porque o JSON vai junto com ficheiro.
O array `anexos` dentro de `dados` serve para identificar cada ficheiro enviado para o MinIO.

Regra:

- `anexos[0]` corresponde ao primeiro campo `ficheiros`
- `anexos[1]` corresponde ao segundo campo `ficheiros`
- depois do upload, o backend preenche `ver_documento` e `ver_documento_desc` com o path gravado

```http
POST http://localhost:9001/api/v1/acolhimentos
Content-Type: multipart/form-data
```

No Postman:

1. Body: `form-data`
2. Campo `dados`: tipo `Text`
3. Campos `ficheiros`: tipo `File`
4. Para mais de um ficheiro, repita a chave `ficheiros`

Exemplo dos campos:

| Key | Type | Value |
| --- | --- | --- |
| dados | Text | JSON abaixo numa unica linha |
| ficheiros | File | C:\temp\cni.pdf |
| ficheiros | File | C:\temp\tre.pdf |

Valor do campo `dados`:

```json
{
  "tipoServico": "EMPREGO",
  "tipoUtente": "UTENTE",
  "canal": "CEFP",
  "fonteInformacao": "CEFP",
  "denominacaoUtente": "Utente Teste",
  "idPessoa": 2001101700869,
  "nif": 123456789,
  "utente": {
    "idPessoa": 2001101700869,
    "nome": "Utente Teste",
    "tipoDocumento": "CNI",
    "numDocumento": "2001101700869",
    "dataNascimento": "1990-01-10",
    "sexo": "M",
    "nif": 123456789,
    "habilitacaoLiteraria": "12"
  },
  "dadosEmprego": {
    "situacao_face_ao_emprego": "DESEMPREGADO",
    "profissao": "Tecnico de Informatica",
    "empresa": "Empresa Teste Lda",
    "setor_de_atividade": "TECNOLOGIA",
    "ilha_empresa": "ST",
    "concelho_empresa": "PR",
    "zona_empresa": "PALMAREJO"
  },
  "detalhes": {
    "csu": "CSU-001",
    "como_obteve_informacao": "CEFP",
    "ilha": "ST",
    "zona": "ACHADA_SANTO_ANTONIO",
    "area_": "INFORMATICA",
    "email": "utente.teste@email.cv",
    "outro": "",
    "empresa": "",
    "concelho": "PR",
    "endereco": "Achada Santo Antonio, Praia",
    "telefone": "2600000",
    "link_foto": "",
    "profissao": "Tecnico de Informatica",
    "telemovel": "9912345",
    "observacoes": "Registo de teste com MinIO",
    "estado_civil": "S",
    "ilha_empresa": "ST",
    "naturalidade": "238",
    "zona_empresa": "PALMAREJO",
    "data_validade": "09-01-2030",
    "carta_conducao": "S",
    "zona_empresa_1": "",
    "concelho_empresa": "PR",
    "local_de_emissao": "CASA DO CIDADAO DA PRAIA",
    "o_que_deseja_criar": "",
    "setor_de_atividade": "TECNOLOGIA",
    "o_que_deseja_criar_1": "",
    "setor_de_atividade_1": "",
    "tipo_servico_solicitado": "EMPREGO",
    "situacao_face_ao_emprego": "DESEMPREGADO",
    "local_de_trabalho_preferencial": "PRAIA",
    "autoriza_a_divulgacao_dos_seus_dados_para_efeito__de_emprego": "S"
  },
  "anexos": [
    {
      "id": "22",
      "tipo_documento_anexo": "CNI",
      "tipo_documento_anexo_desc": "CNI"
    },
    {
      "id": "23",
      "tipo_documento_anexo": "TRE",
      "tipo_documento_anexo_desc": "TRE"
    }
  ]
}
```

PowerShell:

```powershell
curl.exe -X POST "http://localhost:9001/api/v1/acolhimentos" `
  -F "dados={\"tipoServico\":\"EMPREGO\",\"tipoUtente\":\"UTENTE\",\"canal\":\"CEFP\",\"fonteInformacao\":\"CEFP\",\"denominacaoUtente\":\"Utente Teste\",\"idPessoa\":2001101700869,\"nif\":123456789,\"utente\":{\"idPessoa\":2001101700869,\"nome\":\"Utente Teste\",\"tipoDocumento\":\"CNI\",\"numDocumento\":\"2001101700869\",\"dataNascimento\":\"1990-01-10\",\"sexo\":\"M\",\"nif\":123456789,\"habilitacaoLiteraria\":\"12\"},\"dadosEmprego\":{\"situacao_face_ao_emprego\":\"DESEMPREGADO\",\"profissao\":\"Tecnico de Informatica\",\"empresa\":\"Empresa Teste Lda\",\"setor_de_atividade\":\"TECNOLOGIA\",\"ilha_empresa\":\"ST\",\"concelho_empresa\":\"PR\",\"zona_empresa\":\"PALMAREJO\"},\"detalhes\":{\"csu\":\"CSU-001\",\"como_obteve_informacao\":\"CEFP\",\"ilha\":\"ST\",\"zona\":\"ACHADA_SANTO_ANTONIO\",\"area_\":\"INFORMATICA\",\"email\":\"utente.teste@email.cv\",\"outro\":\"Informacao adicional de teste\",\"empresa\":\"Empresa Teste Lda\",\"concelho\":\"PR\",\"endereco\":\"Achada Santo Antonio, Praia\",\"telefone\":\"2600000\",\"profissao\":\"Tecnico de Informatica\",\"telemovel\":\"9912345\",\"observacoes\":\"Registo de teste completo\",\"estado_civil\":\"S\",\"ilha_empresa\":\"ST\",\"naturalidade\":\"238\",\"zona_empresa\":\"PALMAREJO\",\"data_validade\":\"09-01-2030\",\"carta_conducao\":\"S\",\"zona_empresa_1\":\"9911122\",\"concelho_empresa\":\"PR\",\"local_de_emissao\":\"CASA DO CIDADAO DA PRAIA\",\"o_que_deseja_criar\":\"NEGOCIO_PROPRIO\",\"setor_de_atividade\":\"TECNOLOGIA\",\"o_que_deseja_criar_1\":\"DESENVOLVIMENTO_SOFTWARE\",\"setor_de_atividade_1\":\"SERVICOS\",\"tipo_servico_solicitado\":\"EMPREGO\",\"situacao_face_ao_emprego\":\"DESEMPREGADO\",\"local_de_trabalho_preferencial\":\"PRAIA\",\"autoriza_a_divulgacao_dos_seus_dados_para_efeito__de_emprego\":\"S\"},\"anexos\":[{\"id\":\"22\",\"ver_documento\":\"\",\"ver_documento_desc\":\"\",\"tipo_documento_anexo\":\"CNI\",\"tipo_documento_anexo_desc\":\"CNI\"},{\"id\":\"23\",\"ver_documento\":\"\",\"ver_documento_desc\":\"\",\"tipo_documento_anexo\":\"TRE\",\"tipo_documento_anexo_desc\":\"TRE\"}]}" `
  -F "ficheiros=@C:\temp\cni.pdf" `
  -F "ficheiros=@C:\temp\tre.pdf"
```

## Consultar Reporter

Depois de criar, use o `id` retornado na resposta:

```http
GET http://localhost:9001/api/v1/acolhimentos/reporter/1
```
