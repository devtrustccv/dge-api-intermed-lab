# Perfil Candidato — Minhas colocações

Esta API alimenta os ecrãs **Minhas colocações — Lista** e **Detalhes** descritos no PowerPoint. Ela é de leitura e é independente das operações administrativas de colocação do Perfil Entidade.

## Responsabilidades

### Front-end

- Obter `pessoaId` da sessão autenticada; o valor não deve ser editável pelo utilizador.
- Apresentar os campos e ações definidos no PowerPoint.
- Abrir o detalhe usando o `colocacaoId` devolvido na lista.
- Abrir `contratoUrl` para visualizar o contrato e tratar `null` como contrato indisponível.
- Mostrar a ação **Ver avaliação** apenas quando `tipoOferta` for `OFERTA_ESTAGIO`.
- Encaminhar **Ver avaliação** para a futura área do Perfil Candidato especificada na secção “Avaliação Estagiário”. Os endpoints administrativos de avaliação do Perfil Entidade não devem ser chamados diretamente por este ecrã.

### API

- Validar `pessoaId` e `colocacaoId`.
- Limitar a lista ao candidato e procurar o detalhe por `(colocacaoId, pessoaId)`.
- Responder `404` sem expor dados quando a colocação não existe ou pertence a outra pessoa.
- Normalizar os domínios `TIPO_OFERTA`, `REGIME_CONTRATO` e `ESTADO`.
- Devolver códigos e descrições em campos separados.
- Relacionar a colocação com `emprego_t_oferta` para obter o título, preservando a colocação mesmo se a oferta não estiver disponível.
- Gerar `contratoUrl` a partir de `contratoPath`.

> Nota de segurança: o projeto mantém o padrão atual de receber `pessoaId` por parâmetro. Em produção, o gateway/autenticação deve garantir que esse valor corresponde à identidade da sessão. A filtragem de propriedade na base de dados já está preparada para essa integração.

## Endpoints

Base: `/api/v1/perfil-candidato/colocacoes`

### `GET /`

Parâmetro:

- `pessoaId`: identificador do candidato da sessão.

A lista é ordenada da colocação mais recente para a mais antiga.

Resposta `dados`:

```json
[
  {
    "colocacaoId": 31,
    "ofertaId": 501,
    "tipoOferta": "OFERTA_ESTAGIO",
    "tipoOfertaDescricao": "Oferta estágio",
    "titulo": "Estágio em desenvolvimento",
    "codigoReferencia": "REF-EST-2026-01",
    "dataColocacao": "2026-08-27T10:30:00",
    "contratoPath": "/contratos/31/contrato.pdf",
    "contratoUrl": "http://servidor/document-viewer?..."
  }
]
```

O `ofertaId` é informação adicional para rastreabilidade. A navegação para o detalhe deve usar `colocacaoId`.

### `GET /{colocacaoId}`

Parâmetro de query: `pessoaId`.

Resposta `dados`:

```json
{
  "colocacaoId": 31,
  "ofertaId": 501,
  "tipoOferta": "OFERTA_ESTAGIO",
  "tipoOfertaDescricao": "Oferta estágio",
  "titulo": "Estágio em desenvolvimento",
  "codigoReferencia": "REF-EST-2026-01",
  "dataInicioPrevisto": "2026-09-01",
  "dataFimPrevisto": "2027-02-28",
  "tipoContrato": "CONTRATO_TERMO",
  "tipoContratoDescricao": "Contrato a termo",
  "duracaoContrato": 6,
  "descricao": "Colocação para estágio profissional.",
  "estado": "A",
  "estadoDescricao": "Ativo",
  "dataColocacao": "2026-08-27T10:30:00",
  "contratoPath": "/contratos/31/contrato.pdf",
  "contratoUrl": "http://servidor/document-viewer?..."
}
```

## Contrato comum de resposta

Sucesso:

```json
{
  "sucesso": true,
  "mensagem": "Colocações carregadas com sucesso.",
  "dados": [],
  "erros": []
}
```

Tentativa de acesso a colocação alheia:

```json
{
  "sucesso": false,
  "mensagem": "A colocação selecionada não existe ou não pertence ao candidato.",
  "dados": null,
  "erros": ["A colocação selecionada não existe ou não pertence ao candidato."]
}
```

Coleção de teste: `postman_minhas_colocacoes.postman_collection.json`.
