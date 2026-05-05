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

## Multipart Campo A Campo

Tambem pode enviar sem `dados`, usando campos separados. A ordem define qual ficheiro pertence a qual documento:

- `documentos[0]` corresponde ao primeiro campo `ficheiros`
- `documentos[1]` corresponde ao segundo campo `ficheiros`

```bash
curl --location 'http://localhost:9001/api/v1/acolhimentos' \
--form 'denominacaoUtente="Maria da Conceicao Silva"' \
--form 'nif="123456789"' \
--form 'cefpId="1"' \
--form 'orgId="101"' \
--form 'tipoUtente="CIDADAO"' \
--form 'tipoServico="ATENDIMENTO"' \
--form 'tipoServicoDesc="ATENDIMENTO"' \
--form 'fonteInformacao="ONLINE"' \
--form 'canal="ONLINE"' \
--form 'canalDesc="ONLINE"' \
--form 'statusEntrevista="PENDENTE"' \
--form 'idTecnicoAtendimento="5"' \
--form 'tecnicoAtendimento="Joao Fernandes"' \
--form 'userCreate="1"' \
--form 'utente.nome="Maria da Conceicao Silva"' \
--form 'utente.dataNascimento="1995-08-21"' \
--form 'utente.tipoDocumento="BI"' \
--form 'utente.numDocumento="BI123456"' \
--form 'utente.sexo="F"' \
--form 'utente.nif="123456789"' \
--form 'utente.habilitacaoLiteraria="12_ANO"' \
--form 'detalhes.telefone="9911122"' \
--form 'detalhes.telemovel="9911122"' \
--form 'detalhes.email="maria.silva@email.com"' \
--form 'detalhes.endereco="Achada Santo Antonio"' \
--form 'detalhes.ilha="Santiago"' \
--form 'detalhes.concelho="Praia"' \
--form 'detalhes.zona="Achada Santo Antonio"' \
--form 'detalhes.data_validade="2027-12-31"' \
--form 'detalhes.local_de_emissao="Praia"' \
--form 'detalhes.naturalidade="CV"' \
--form 'detalhes.estado_civil="SOLTEIRA"' \
--form 'detalhes.carta_conducao="SIM"' \
--form 'detalhes.autoriza_a_divulgacao_dos_seus_dados_para_efeito__de_emprego="SIM"' \
--form 'detalhes.situacao_face_ao_emprego="DESEMPREGADA"' \
--form 'detalhes.profissao="Administrativa"' \
--form 'detalhes.empresa=""' \
--form 'detalhes.setor_de_atividade="Servicos"' \
--form 'detalhes.setor_de_atividade_1=""' \
--form 'detalhes.ilha_empresa="Santiago"' \
--form 'detalhes.concelho_empresa="Praia"' \
--form 'detalhes.zona_empresa="Palmarejo"' \
--form 'detalhes.zona_empresa_1="9911122"' \
--form 'detalhes.tipo_servico_solicitado="ATENDIMENTO"' \
--form 'detalhes.local_de_trabalho_preferencial="Praia"' \
--form 'detalhes.area_="Comercio"' \
--form 'detalhes.o_que_deseja_criar="Pequeno negocio"' \
--form 'detalhes.o_que_deseja_criar_1="Atendimento ao publico"' \
--form 'detalhes.observacoes="Utente pretende apoio de insercao profissional"' \
--form 'detalhes.outro=""' \
--form 'documentos[0].idTpDoc="1"' \
--form 'documentos[0].name="Bilhete de Identidade"' \
--form 'documentos[0].fileName="bilhete_identidade"' \
--form 'ficheiros=@"/caminho/bilhete_identidade.pdf"' \
--form 'documentos[1].idTpDoc="2"' \
--form 'documentos[1].name="Curriculo"' \
--form 'documentos[1].fileName="curriculo"' \
--form 'ficheiros=@"/caminho/curriculo.pdf"' \
--form 'pessoa_id="1"'
```
