package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class OrientacaoServicoDetalhesMapper {

    private OrientacaoServicoDetalhesMapper() {
    }

    static Map<String, Object> mapear(String tipoServico, Map<String, Object> origem) {
        Spec spec = spec(tipoServico);
        if (spec == null) {
            return origem == null ? new LinkedHashMap<>() : new LinkedHashMap<>(origem);
        }
        return spec.mapear(origem);
    }

    private static Spec spec(String tipoServico) {
        String tipo = normalizar(tipoServico);
        if (tipo.contains("pepe")) {
            return pepe();
        }
        if (tipo.contains("fpif") || tipo.contains("fpeif")) {
            return fpif();
        }
        if (tipo.contains("germe")) {
            return germe();
        }
        if (tipo.contains("subdesemp") || tipo.contains("subsidio") || tipo.contains("desemprego")) {
            return subsidioDesemprego();
        }
        if (tipo.contains("formador")) {
            return formador();
        }
        if (tipo.contains("educacaofinanceira") || tipo.contains("educacaofinaceira") || tipo.contains("financeira")) {
            return educacaoFinanceira();
        }
        if (tipo.contains("emprego")) {
            return servicoEmprego();
        }
        return null;
    }

    private static Spec educacaoFinanceira() {
        return new Spec()
                .campo("area")
                .campo("nivel")
                .campo("disponibilidade")
                .campo("objetivos_formacao", "objetivos_que_pretende_alcancar_com_a_formacao")
                .campo("situacao_socioeconomico_familiar", "situacao_socioeconomicofamiliar")
                .lista("cursos_formacao",
                        item("nome_curso"),
                        item("periodo"),
                        item("ch"),
                        item("nivel_formacao"),
                        item("instituicao"));
    }

    private static Spec pepe() {
        return new Spec()
                .campo("organica_a_que_recorre")
                .campo("localidade")
                .campo("area_funcional_estagio", "area_funcional_de_estagio")
                .campo("habilitacao_academica")
                .campo("designacao_certificado_diploma", "designacao_do_certificado_ou_diploma_atribuido")
                .campo("estabelecimento_ensino")
                .campo("data_inicio", "data_inicio_")
                .campo("data_conclusao_curso", "data_de_conclusao_de_curso")
                .campo("area")
                .campo("esta_desempregado")
                .campo("ja_alguma_vez_trabalhou")
                .campo("competencias_informaticas", "competencia_s")
                .campo("outras_competencias")
                .campo("declaracao_honra", "declaro_sob_compromisso_de_honra_que_as_informacoes_acima_prestadas_sao_verdadeiras_check")
                .campo("data_declaracao", "data")
                .lista("experiencias_profissionais",
                        item("natureza_funcoes", "natureza_das_funcoes"),
                        item("data_inicio"),
                        item("data_fim"),
                        item("empresa"),
                        item("principais_tarefas_responsabilidades", "principais_tarefas_e_responsabilidades"))
                .lista("linguas_estrangeiras",
                        item("idioma"),
                        item("nivel_oralidade"),
                        item("nivel_escrita", "nivel_de_escrita"),
                        item("nivel_leitura", "nivel_de_leitura"),
                        item("certificacao"));
    }

    private static Spec fpif() {
        return new Spec()
                .campo("principais_razoes", "principais_razoes_pelas_quais_pretende_realizar_o_curso_de_fpeif")
                .campo("concluiu_o_curso_em")
                .campo("classificacao_final")
                .campo("coordenador_curso", "cooredenador_do_curso")
                .campo("observacoes")
                .campo("recebido_por")
                .campo("data_inscricao", "data")
                .lista("documentos_entregues",
                        item("documento"),
                        item("documento_desc"),
                        item("entregue"));
    }

    private static Spec germe() {
        return new Spec()
                .campo("ja_frequentou_formacao", "ja_frequentou_alguma_acao_de_formacao")
                .campo("tem_experiencia_profissional", "tem_alguma_experiencia_profissional")
                .campo("cite_a_experiencia")
                .campo("tens_ideia_negocio", "tens_uma_ideia_de_negocio")
                .campo("tens_empresa_constituida")
                .campo("valor_investimento", "qual_o_valor_de_investimento")
                .campo("finalidade_investimento", "finalidade_do_investimento")
                .campo("fonte_financiamento", "fonte_de_financiamento")
                .campo("tem_formacao_germe")
                .campo("tipo_formacao", "tipo_de_formacao")
                .campo("beneficiou_apoio_iefp_proempresa", "ja_beneficiou_de_algum_apoio_do_iefpproempresa")
                .campo("tipo_apoio", "tipo_de_apoio")
                .lista("cursos_formacoes",
                        item("curso_formacao_estagio"),
                        item("periodo"),
                        item("ch"),
                        item("instituicao"))
                .lista("ideias_negocio",
                        item("ideia_projeto_negocio"),
                        item("local_implementacao"),
                        item("area_negocio"));
    }

    private static Spec servicoEmprego() {
        return new Spec()
                .campo("area_de_formacao")
                .campo("media_final")
                .campo("tem_carta_conducao")
                .campo("tipo_carta_conducao", "tipo")
                .campo("ja_frequentou_formacao", "ja_frequentou_alguma_acao_de_formacao")
                .campo("situacao_face_ao_emprego")
                .campo("profissao_exercida")
                .campo("duracao_desemprego_meses", "duracao_no_desemprego_em_meses")
                .campo("profissao_pretendida")
                .campo("emprego_pretendido")
                .campo("ja_tem_experiencia", "ja_tem_alguma_experiencia")
                .campo("descricao_experiencia", "descricao_da_experiencia")
                .campo("descricao_situacao_socioeconomico", "descricao_da_situacao_socioecomico", "descricao_da_situacao_socioeconomico")
                .campo("deficiente")
                .lista("linguas_estrangeiras",
                        item("idioma"),
                        item("nivel_oralidade"),
                        item("nivel_escrita", "nivel_de_escrita"),
                        item("nivel_leitura", "nivel_de_leitura"),
                        item("certificacao"))
                .lista("acoes_formacao",
                        item("curso"),
                        item("periodo_ch"),
                        item("instituicao"));
    }

    private static Spec subsidioDesemprego() {
        return new Spec()
                .campo("area_de_formacao")
                .campo("media_final")
                .campo("carta_conducao")
                .campo("tipo_carta", "tipo")
                .campo("ja_frequentou_formacao", "ja_frequentou_alguma_acao_de_formacao")
                .campo("situacao_face_ao_emprego")
                .campo("profissao_exercida")
                .campo("duracao_desemprego_meses", "duracao_no_desemprego_em_meses")
                .campo("profissao_pretendida")
                .campo("emprego_pretendido")
                .campo("ja_tem_experiencia", "ja_tem_alguma_experiencia")
                .campo("descricao_experiencia", "descricao_da_experiencia")
                .campo("descricao_situacao_socioeconomico", "descricao_da_situacao_socioeconomico")
                .campo("deficiente")
                .campo("pretende_requerer_subsidio_desemprego", "pretende_requerer_o_subsidio_de_desemprego")
                .lista("linguas_estrangeiras",
                        item("idioma"),
                        item("nivel_oralidade"),
                        item("nivel_escrita", "nivel_de_escrita"),
                        item("nivel_leitura", "nivel_de_leitura"),
                        item("certificacao"))
                .lista("acoes_formacao",
                        item("curso"),
                        item("periodo_ch"),
                        item("instituicao"))
                .lista("requisitos",
                        item("requisito"),
                        item("requisito_desc"),
                        item("selecionado"))
                .lista("anexos",
                        item("documento"),
                        item("documento_desc"),
                        item("anexo"));
    }

    private static Spec formador() {
        return new Spec()
                .campo("area_dominio", "area_de_dominio")
                .campo("descricao")
                .lista("disponibilidade_horario",
                        item("dia_da_semana"),
                        item("horario"));
    }

    private static Campo item(String chave, String... aliases) {
        return new Campo(chave, aliases);
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private record Campo(String chave, String... aliases) {
        Object valor(Map<String, Object> origem) {
            return buscar(origem, chave, aliases);
        }
    }

    private static final class Lista {
        private final String chave;
        private final List<Campo> campos;

        private Lista(String chave, Campo... campos) {
            this.chave = chave;
            this.campos = List.of(campos);
        }

        Object valor(Map<String, Object> origem) {
            Object bruto = buscar(origem, chave);
            if (!(bruto instanceof Collection<?> colecao)) {
                return new ArrayList<>();
            }
            List<Map<String, Object>> destino = new ArrayList<>();
            for (Object item : colecao) {
                if (!(item instanceof Map<?, ?> mapaItem)) {
                    continue;
                }
                Map<String, Object> origemItem = copiarMapa(mapaItem);
                Map<String, Object> destinoItem = new LinkedHashMap<>();
                for (Campo campo : campos) {
                    destinoItem.put(campo.chave(), campo.valor(origemItem));
                }
                destino.add(destinoItem);
            }
            return destino;
        }
    }

    private static final class Spec {
        private final List<Campo> campos = new ArrayList<>();
        private final List<Lista> listas = new ArrayList<>();

        Spec campo(String chave, String... aliases) {
            campos.add(new Campo(chave, aliases));
            return this;
        }

        Spec lista(String chave, Campo... campos) {
            listas.add(new Lista(chave, campos));
            return this;
        }

        Map<String, Object> mapear(Map<String, Object> origem) {
            Map<String, Object> dados = origem == null ? new LinkedHashMap<>() : new LinkedHashMap<>(origem);
            Map<String, Object> destino = new LinkedHashMap<>();
            for (Campo campo : campos) {
                destino.put(campo.chave(), campo.valor(dados));
            }
            for (Lista lista : listas) {
                destino.put(lista.chave, lista.valor(dados));
            }
            return destino;
        }
    }

    private static Object buscar(Map<String, Object> origem, String chave, String... aliases) {
        if (origem == null || origem.isEmpty()) {
            return null;
        }
        List<String> chaves = new ArrayList<>();
        chaves.add(chave);
        if (aliases != null) {
            chaves.addAll(List.of(aliases));
        }
        for (String nome : chaves) {
            for (Map.Entry<String, Object> entrada : origem.entrySet()) {
                if (entrada.getKey() != null && normalizar(entrada.getKey()).equals(normalizar(nome))) {
                    return entrada.getValue();
                }
            }
        }
        return null;
    }

    private static Map<String, Object> copiarMapa(Map<?, ?> origem) {
        Map<String, Object> destino = new LinkedHashMap<>();
        origem.forEach((chave, valor) -> {
            if (chave != null) {
                destino.put(chave.toString(), valor);
            }
        });
        return destino;
    }
}
