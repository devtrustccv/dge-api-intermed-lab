package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcolhimentoRegistoRequest {

    @JsonAlias({"id_pessoa", "pessoa_id"})
    private Object idPessoa;

    @JsonAlias({"id_utente", "utente_id"})
    private Object idUtente;

    @JsonAlias({"id_entidade", "entidade_id"})
    private Object idEntidade;

    @JsonAlias({"denominacao_utente", "denomincacao_utente", "nome_completo", "nomeCompleto"})
    private Object denominacaoUtente;

    private Object nif;

    @JsonAlias({"cefp_id", "cepf_id"})
    private Object cefpId;

    @JsonAlias({"org_id", "organization_id", "organizationId"})
    private Object orgId;

    @JsonAlias({"tipo_utente"})
    private Object tipoUtente;

    @JsonAlias({"tipo_utente_desc"})
    private Object tipoUtenteDesc;

    @JsonAlias({"tipo_servico", "tipo_servico_solicitado", "tipoServicoSolicitado"})
    private Object tipoServico;

    @JsonAlias({"tipo_servico_desc"})
    private Object tipoServicoDesc;

    @JsonAlias({"fonte_informacao", "fonteInformacao"})
    private Object fonteInformacao;

    private Object canal;

    @JsonAlias({"canal_desc"})
    private Object canalDesc;

    @JsonAlias({"status_entrevista"})
    private Object statusEntrevista;

    @JsonAlias({"id_tecnico_atendimento", "tecnico_id"})
    private Object idTecnicoAtendimento;

    @JsonAlias({"tecnico_atendimento", "tecnico"})
    private Object tecnicoAtendimento;

    @JsonAlias({"user_create", "utilizador", "utilizador_registo"})
    private Object userCreate;

    @JsonAlias({"utente", "identificacao", "identificacaoUtente"})
    private Map<String, Object> utente = new LinkedHashMap<>();

    @JsonAlias({"dadosEmprego", "detalhesEmprego", "dadosAcademicosProfissionais", "dados_academicos_profissionais"})
    private Map<String, Object> dadosEmprego = new LinkedHashMap<>();

    @JsonAlias({"detalhes", "formulario"})
    private Map<String, Object> detalhes = new LinkedHashMap<>();

    @JsonAlias({"anexos", "documentos", "docs"})
    private List<Map<String, Object>> documentos = new ArrayList<>();

    public void setUtente(Map<String, Object> utente) {
        this.utente = copiarMapa(utente);
    }

    public void setDadosEmprego(Map<String, Object> dadosEmprego) {
        this.dadosEmprego = copiarMapa(dadosEmprego);
    }

    public void setDetalhes(Map<String, Object> detalhes) {
        this.detalhes.putAll(copiarMapa(detalhes));
    }

    public void setDocumentos(List<Map<String, Object>> documentos) {
        this.documentos = documentos == null ? new ArrayList<>() : new ArrayList<>(documentos);
    }

    @JsonAnySetter
    public void adicionarCampoFormulario(String chave, Object valor) {
        if (valor != null) {
            detalhes.put(chave, valor);
        }
    }

    private Map<String, Object> copiarMapa(Map<String, Object> origem) {
        return origem == null ? new LinkedHashMap<>() : new LinkedHashMap<>(origem);
    }
}
