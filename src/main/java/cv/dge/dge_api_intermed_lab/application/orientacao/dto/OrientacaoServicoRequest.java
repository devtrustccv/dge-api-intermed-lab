package cv.dge.dge_api_intermed_lab.application.orientacao.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrientacaoServicoRequest {

    @JsonAlias({"id_entrevista", "entrevista_id"})
    private Integer idEntrevista;

    @JsonAlias({"id_acolhimento_servico", "acolhimento_servico_id"})
    private Integer idAcolhimentoServico;

    @JsonAlias({"tipo_servico"})
    private Object tipoServico;

    @JsonAlias({"tipo_servico_desc"})
    private Object tipoServicoDesc;

    @JsonAlias({"tipo_utente"})
    private Object tipoUtente;

    @JsonAlias({"tipo_utente_desc"})
    private Object tipoUtenteDesc;

    @JsonAlias({"necessidade_analise"})
    private Boolean necessidadeAnalise;

    @JsonAlias({"detalhes_servico", "detalhes", "formulario"})
    private Map<String, Object> detalhesServico = new LinkedHashMap<>();

    @JsonAlias({"detalhes_analise"})
    private Map<String, Object> detalhesAnalise = new LinkedHashMap<>();

    @JsonAlias({"user_create", "user_update", "utilizador"})
    private Object utilizador;

    public void setDetalhesServico(Map<String, Object> detalhesServico) {
        this.detalhesServico = copiarMapa(detalhesServico);
    }

    public void setDetalhesAnalise(Map<String, Object> detalhesAnalise) {
        this.detalhesAnalise = copiarMapa(detalhesAnalise);
    }

    @JsonAnySetter
    public void adicionarCampoFormulario(String chave, Object valor) {
        if (chave != null && valor != null) {
            detalhesServico.put(chave, valor);
        }
    }

    private Map<String, Object> copiarMapa(Map<String, Object> origem) {
        return origem == null ? new LinkedHashMap<>() : new LinkedHashMap<>(origem);
    }
}
