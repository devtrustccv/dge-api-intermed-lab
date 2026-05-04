# Testes API Orientacao Servicos

Endpoint base:

```http
POST http://localhost:9001/api/v1/orientacao/servicos
Content-Type: application/json
```

Trocar `idEntrevista` por um ID real existente em `emprego_t_agendamento_entrevista`.

Para consultar o registo gravado:

```http
GET http://localhost:9001/api/v1/orientacao/servicos/entrevista/1
```

## PEPE

```json
{
  "idEntrevista": 1,
  "tipoServico": "PEPE",
  "utilizador": "teste.api",
  "detalhesServico": {
    "organica_a_que_recorre": "CEFP Praia",
    "localidade": "Praia",
    "area_funcional_estagio": "Administracao",
    "habilitacao_academica": "Licenciatura",
    "designacao_certificado_diploma": "Licenciado em Gestao",
    "estabelecimento_ensino": "Universidade de Cabo Verde",
    "data_inicio": "2022-01-10",
    "data_conclusao_curso": "2025-07-20",
    "area": "Gestao",
    "esta_desempregado": "Sim",
    "ja_alguma_vez_trabalhou": "Sim",
    "competencias_informaticas": ["Word", "Excel", "Internet"],
    "outras_competencias": "Comunicacao e trabalho em equipa",
    "declaracao_honra": true,
    "data_declaracao": "2026-05-04",
    "experiencias_profissionais": [
      {
        "natureza_funcoes": "Assistente administrativo",
        "data_inicio": "2024-01-01",
        "data_fim": "2024-12-31",
        "empresa": "Empresa Teste",
        "principais_tarefas_responsabilidades": "Arquivo, atendimento e apoio administrativo"
      }
    ],
    "linguas_estrangeiras": [
      {
        "idioma": "Ingles",
        "nivel_oralidade": "Bom",
        "nivel_escrita": "Bom",
        "nivel_leitura": "Muito Bom",
        "certificacao": "Nao"
      }
    ]
  }
}
```

## FPIF

```json
{
  "idEntrevista": 1,
  "tipoServico": "FPIF",
  "utilizador": "teste.api",
  "detalhesServico": {
    "principais_razoes": "Pretendo melhorar as minhas competencias profissionais.",
    "concluiu_o_curso_em": "2026-04-30",
    "classificacao_final": 16,
    "coordenador_curso": "Joao Silva",
    "observacoes": "Teste de inscricao FPIF",
    "recebido_por": "Tecnico Teste",
    "data_inscricao": "2026-05-04",
    "documentos_entregues": [
      {
        "documento": "1",
        "documento_desc": "Documento de Identificacao",
        "entregue": true
      },
      {
        "documento": "2",
        "documento_desc": "Certificado de Habilitacoes",
        "entregue": false
      }
    ]
  }
}
```

## GERME

```json
{
  "idEntrevista": 1,
  "tipoServico": "GERME",
  "utilizador": "teste.api",
  "detalhesServico": {
    "ja_frequentou_formacao": "Sim",
    "tem_experiencia_profissional": "Sim",
    "cite_a_experiencia": "Experiencia em comercio e atendimento ao cliente.",
    "tens_ideia_negocio": "Sim",
    "tens_empresa_constituida": "Nao",
    "valor_investimento": "250000",
    "finalidade_investimento": "Compra de equipamentos e stock inicial.",
    "fonte_financiamento": "Poupanca propria e credito",
    "tem_formacao_germe": "Nao",
    "tipo_formacao": "Empreendedorismo",
    "beneficiou_apoio_iefp_proempresa": "Nao",
    "tipo_apoio": "",
    "cursos_formacoes": [
      {
        "curso_formacao_estagio": "Gestao de Pequenos Negocios",
        "periodo": "2025",
        "ch": "40",
        "instituicao": "IEFP"
      }
    ],
    "ideias_negocio": [
      {
        "ideia_projeto_negocio": "Mercearia de bairro",
        "local_implementacao": "Praia",
        "area_negocio": "Comercio"
      }
    ]
  }
}
```

## Servico Emprego

```json
{
  "idEntrevista": 1,
  "tipoServico": "SERVICO_EMPREGO",
  "utilizador": "teste.api",
  "detalhesServico": {
    "area_de_formacao": "Informatica",
    "media_final": "15",
    "tem_carta_conducao": "Sim",
    "tipo_carta_conducao": "B",
    "ja_frequentou_formacao": "Sim",
    "situacao_face_ao_emprego": "Desempregado",
    "profissao_exercida": "Tecnico informatico",
    "duracao_desemprego_meses": 6,
    "profissao_pretendida": "Tecnico de suporte",
    "emprego_pretendido": "Contrato de trabalho",
    "ja_tem_experiencia": "Sim",
    "descricao_experiencia": "Suporte tecnico e manutencao de computadores.",
    "descricao_situacao_socioeconomico": "Sem rendimento fixo.",
    "deficiente": "Nao",
    "linguas_estrangeiras": [
      {
        "idioma": "Ingles",
        "nivel_oralidade": "Medio",
        "nivel_escrita": "Medio",
        "nivel_leitura": "Bom",
        "certificacao": "Nao"
      }
    ],
    "acoes_formacao": [
      {
        "curso": "Redes de computadores",
        "periodo_ch": "60h",
        "instituicao": "Centro de Formacao"
      }
    ]
  }
}
```

## Subsidio Desemprego

```json
{
  "idEntrevista": 1,
  "tipoServico": "SUB_DESEMP",
  "utilizador": "teste.api",
  "necessidadeAnalise": true,
  "detalhesServico": {
    "area_de_formacao": "Contabilidade",
    "media_final": "14",
    "carta_conducao": "Sim",
    "tipo_carta": "B",
    "ja_frequentou_formacao": "Sim",
    "situacao_face_ao_emprego": "Desempregado",
    "profissao_exercida": "Auxiliar administrativo",
    "duracao_desemprego_meses": 8,
    "profissao_pretendida": "Assistente administrativo",
    "emprego_pretendido": "Emprego formal",
    "ja_tem_experiencia": "Sim",
    "descricao_experiencia": "Atendimento, arquivo e apoio administrativo.",
    "descricao_situacao_socioeconomico": "Agregado com baixo rendimento.",
    "deficiente": "Nao",
    "pretende_requerer_subsidio_desemprego": "Sim",
    "linguas_estrangeiras": [
      {
        "idioma": "Frances",
        "nivel_oralidade": "Basico",
        "nivel_escrita": "Basico",
        "nivel_leitura": "Medio",
        "certificacao": "Nao"
      }
    ],
    "acoes_formacao": [
      {
        "curso": "Atendimento ao publico",
        "periodo_ch": "30h",
        "instituicao": "IEFP"
      }
    ],
    "requisitos": [
      {
        "requisito": "1",
        "requisito_desc": "Inscricao no centro de emprego",
        "selecionado": true
      },
      {
        "requisito": "2",
        "requisito_desc": "Comprovativo de desemprego",
        "selecionado": true
      }
    ],
    "anexos": [
      {
        "documento": "1",
        "documento_desc": "Documento de Identificacao",
        "anexo": ""
      },
      {
        "documento": "2",
        "documento_desc": "Declaracao de desemprego",
        "anexo": ""
      }
    ]
  }
}
```

### Subsidio Desemprego com ficheiros

Endpoint:

```http
POST http://localhost:9001/api/v1/orientacao/servicos
Content-Type: multipart/form-data
```

PowerShell:

```powershell
curl.exe -X POST "http://localhost:9001/api/v1/orientacao/servicos" `
  -F "dados={\"idEntrevista\":1,\"tipoServico\":\"SUB_DESEMP\",\"utilizador\":\"teste.api\",\"necessidadeAnalise\":true,\"detalhesServico\":{\"area_de_formacao\":\"Contabilidade\",\"media_final\":\"14\",\"carta_conducao\":\"Sim\",\"tipo_carta\":\"B\",\"ja_frequentou_formacao\":\"Sim\",\"situacao_face_ao_emprego\":\"Desempregado\",\"profissao_exercida\":\"Auxiliar administrativo\",\"duracao_desemprego_meses\":8,\"profissao_pretendida\":\"Assistente administrativo\",\"emprego_pretendido\":\"Emprego formal\",\"ja_tem_experiencia\":\"Sim\",\"descricao_experiencia\":\"Atendimento, arquivo e apoio administrativo.\",\"descricao_situacao_socioeconomico\":\"Agregado com baixo rendimento.\",\"deficiente\":\"Nao\",\"pretende_requerer_subsidio_desemprego\":\"Sim\",\"linguas_estrangeiras\":[{\"idioma\":\"Frances\",\"nivel_oralidade\":\"Basico\",\"nivel_escrita\":\"Basico\",\"nivel_leitura\":\"Medio\",\"certificacao\":\"Nao\"}],\"acoes_formacao\":[{\"curso\":\"Atendimento ao publico\",\"periodo_ch\":\"30h\",\"instituicao\":\"IEFP\"}],\"requisitos\":[{\"requisito\":\"1\",\"requisito_desc\":\"Inscricao no centro de emprego\",\"selecionado\":true},{\"requisito\":\"2\",\"requisito_desc\":\"Comprovativo de desemprego\",\"selecionado\":true}],\"anexos\":[{\"documento\":\"1\",\"documento_desc\":\"Documento de Identificacao\",\"anexo\":\"\"},{\"documento\":\"2\",\"documento_desc\":\"Declaracao de desemprego\",\"anexo\":\"\"}]}}" `
  -F "ficheiros=@C:\temp\bi.pdf" `
  -F "ficheiros=@C:\temp\declaracao.pdf"
```

## Educacao Financeira

```json
{
  "idEntrevista": 1,
  "tipoServico": "EDUCACAO_FINANCEIRA",
  "utilizador": "teste.api",
  "detalhesServico": {
    "area": "Financas pessoais",
    "nivel": "Inicial",
    "disponibilidade": "Manha",
    "objetivos_formacao": "Aprender a gerir rendimento, poupanca e despesas.",
    "situacao_socioeconomico_familiar": "Baixo rendimento familiar.",
    "cursos_formacao": [
      {
        "nome_curso": "Educacao Financeira Basica",
        "periodo": "2026",
        "ch": "20",
        "nivel_formacao": "Inicial",
        "instituicao": "IEFP"
      }
    ]
  }
}
```

## Formador

```json
{
  "idEntrevista": 1,
  "tipoServico": "FORMADOR",
  "utilizador": "teste.api",
  "detalhesServico": {
    "area_dominio": "Gestao e empreendedorismo",
    "descricao": "Formador com experiencia em gestao de pequenos negocios.",
    "disponibilidade_horario": [
      {
        "dia_da_semana": "Segunda-feira",
        "horario": "09:00-12:00"
      },
      {
        "dia_da_semana": "Quarta-feira",
        "horario": "14:00-17:00"
      }
    ]
  }
}
```
