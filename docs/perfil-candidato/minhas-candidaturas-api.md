# Perfil Candidato — Minhas candidaturas

Esta API alimenta os ecrãs de **Minhas candidaturas (emprego e estágio)** descritos no PowerPoint. Ela é independente dos endpoints administrativos de Gestão de Candidaturas do Perfil Entidade.

## Responsabilidades

### Front-end

- Apresentar os filtros e controlar a obrigatoriedade visual definida no PowerPoint.
- Carregar novamente os concelhos quando a ilha for alterada.
- Enviar os valores (`valor`, `id` ou `codigo`) devolvidos por `/opcoes`, nunca as descrições.
- Obter `pessoaId` da sessão autenticada; este valor não deve ser editável nem informado manualmente pelo utilizador.
- Exibir `motivoRecusa` apenas quando aplicável e usar `anexos[].url` para visualizar documentos.

### API

- Validar `pessoaId`, identificadores, datas e valores de domínio.
- Limitar listagem, opções e detalhe ao candidato informado.
- Ocultar com `404` uma candidatura inexistente ou pertencente a outro candidato.
- Normalizar códigos de tipo, estado e canal e devolver também as respetivas descrições.
- Resolver ilha e concelho através de `global_geografia`, mantendo o código original como fallback.
- Converter o JSONB de anexos numa lista estável com `tipo`, `nome`, `path` e `url`.

> Nota de segurança: o projeto ainda segue o padrão existente de receber `pessoaId` como parâmetro. Em produção, o gateway/autenticação deve garantir que esse valor corresponde à identidade autenticada. A proteção de propriedade no repositório já consulta o detalhe por `(candidaturaId, pessoaId)`.

## Endpoints

Base: `/api/v1/perfil-candidato/candidaturas`

### `GET /opcoes`

Parâmetros:

- `pessoaId`: candidato da sessão.
- `ilha`: opcional; ao ser enviado, carrega os concelhos dessa ilha.

Resposta `dados`:

```json
{
  "tiposOferta": [{ "valor": "OFERTA_EMPREGO", "descricao": "Oferta Emprego" }],
  "entidades": [{ "id": 45, "codigo": null, "descricao": "Entidade Exemplo" }],
  "ilhas": [{ "id": 101, "codigo": "101", "descricao": "Santiago" }],
  "concelhos": [{ "id": 102, "codigo": "102", "descricao": "Praia" }],
  "estadosCandidatura": [{ "valor": "TRIAGEM", "descricao": "Triagem" }]
}
```

Entidades e geografias são limitadas aos valores existentes nas candidaturas do candidato. Sem `ilha`, `concelhos` é uma lista vazia.

### `GET /`

Parâmetros:

- `pessoaId`
- `tipoOferta`: domínio `TIPO_OFERTA`.
- `entidadeId`
- `ilha`: código devolvido por `/opcoes`.
- `concelho`: código devolvido por `/opcoes`.
- `estado`: domínio `STATUS_CANDIDATURA`.
- `codigoReferencia`: pesquisa parcial, sem diferenciar maiúsculas/minúsculas.
- `dataInicio` e `dataFim`: formato `yyyy-MM-dd`; intervalo inclusivo sobre a data de criação da candidatura.

Resposta `dados`:

```json
[
  {
    "candidaturaId": 77,
    "tipoOferta": "OFERTA_ESTAGIO",
    "tipoOfertaDescricao": "Oferta estágio",
    "ofertaId": 501,
    "titulo": "Programador Java",
    "codigoReferencia": "REF-2026-001",
    "entidadeId": 45,
    "entidade": "Entidade Exemplo",
    "ilhaId": "101",
    "ilha": "Santiago",
    "concelhoId": "102",
    "concelho": "Praia",
    "estado": "TRIAGEM",
    "estadoDescricao": "Triagem",
    "dataCandidatura": "2026-08-21T09:00:00"
  }
]
```

### `GET /{candidaturaId}`

Parâmetro de query obrigatório para o contrato: `pessoaId`.

O detalhe contém os campos da listagem e acrescenta:

```json
{
  "motivoRecusa": null,
  "canal": "PORTAL",
  "canalDescricao": "Portal",
  "anexos": [
    {
      "tipo": "CURRICULO_VITAE",
      "nome": "cv.pdf",
      "path": "/candidaturas/77/cv.pdf",
      "url": "http://servidor/document-viewer?..."
    }
  ]
}
```

## Contrato comum de resposta

Sucesso:

```json
{
  "sucesso": true,
  "mensagem": "Candidaturas carregadas com sucesso.",
  "dados": [],
  "erros": []
}
```

Erro:

```json
{
  "sucesso": false,
  "mensagem": "A candidatura selecionada não existe ou não pertence ao candidato.",
  "dados": null,
  "erros": ["A candidatura selecionada não existe ou não pertence ao candidato."]
}
```

Coleção de teste: `postman_minhas_candidaturas.postman_collection.json`.
