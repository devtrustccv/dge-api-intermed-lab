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
public class AcolhimentoEmpresaRequest {

    private Integer id;

    @JsonAlias({"id_entidade", "entidade_id"})
    private Integer idEntidade;

    @JsonAlias({"id_tecnico", "id_tecnico_atendimento", "tecnico_id"})
    private Integer idTecnico;

    @JsonAlias({"id_cefp", "cefp_id", "cefp_de_acolhimento"})
    private Integer idCefp;

    @JsonAlias({"org_id", "organization_id", "organizationId"})
    private Integer orgId;

    @JsonAlias({"nome_da_entidade", "denominacao", "denominacao_utente"})
    private String nomeDaEntidade;

    private String nif;

    @JsonAlias({"registo_comercial", "registo_social"})
    private String registoComercial;

    @JsonAlias({"natureza_juridica"})
    private String naturezaJuridica;

    @JsonAlias({"n_colaboradores"})
    private Integer nColaboradores;

    @JsonAlias({"morada_sede", "morada__da_sede"})
    private String moradaSede;

    @JsonAlias({"codigo_postal"})
    private String codigoPostal;

    @JsonAlias({"responsavel_pela_entidade", "responavel_pela_entidade"})
    private String responsavelPelaEntidade;

    private String ilha;
    private String concelho;

    @JsonAlias({"zona", "zona_"})
    private String zona;

    private String endereco;
    private String telefone;
    private String telemovel;
    private String email;
    private String fax;

    @JsonAlias({"possui_vagas_de_emprego"})
    private String possuiVagasDeEmprego;

    @JsonAlias({"identificacao_posto_trabalho", "identificacao"})
    private String identificacaoPostoTrabalho;

    @JsonAlias({"fonte_informacao", "como_obteve_informacoes_sobre_servicos_do_iefp"})
    private String fonteInformacao;

    @JsonAlias({"tecnico_atendimento", "tecnico"})
    private String tecnicoAtendimento;

    @JsonAlias({"user_create", "utilizador"})
    private String utilizador;

    @JsonAlias({"caes", "separatorlist_1"})
    private List<Map<String, Object>> caes = new ArrayList<>();

    @JsonAlias({"anexos", "documentos", "formlist_1"})
    private List<Map<String, Object>> anexos = new ArrayList<>();

    @JsonAlias({"detalhes", "formulario"})
    private Map<String, Object> detalhes = new LinkedHashMap<>();

    public void setCaes(List<Map<String, Object>> caes) {
        this.caes = caes == null ? new ArrayList<>() : new ArrayList<>(caes);
    }

    public void setAnexos(List<Map<String, Object>> anexos) {
        this.anexos = anexos == null ? new ArrayList<>() : new ArrayList<>(anexos);
    }

    public void setDetalhes(Map<String, Object> detalhes) {
        this.detalhes = detalhes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(detalhes);
    }

    @JsonAnySetter
    public void adicionarCampoFormulario(String chave, Object valor) {
        if (chave != null && valor != null) {
            detalhes.put(chave, valor);
        }
    }
}
