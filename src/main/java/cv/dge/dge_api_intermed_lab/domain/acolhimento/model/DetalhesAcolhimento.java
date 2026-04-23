package cv.dge.dge_api_intermed_lab.domain.acolhimento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "emprego_t_detalhes_acolhimento")
public class DetalhesAcolhimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_pessoa")
    private Integer idPessoa;

    @Column(name = "id_utente")
    private Integer idUtente;

    @Column(name = "id_entidade")
    private Integer idEntidade;

    @Column(name = "denominacao_utente")
    private String denominacaoUtente;

    private BigDecimal nif;

    @Column(name = "cefp_id")
    private Integer cefpId;

    @Column(name = "org_id")
    private Integer orgId;

    @Column(name = "tipo_utente")
    private String tipoUtente;

    @Column(name = "tipo_utente_desc")
    private String tipoUtenteDesc;

    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "tipo_servico_desc")
    private String tipoServicoDesc;

    private String canal;

    @Column(name = "canal_desc")
    private String canalDesc;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> detalhes;

    @Column(name = "id_tecnico_atendimento")
    private Integer idTecnicoAtendimento;

    @Column(name = "tecnico_atendimento")
    private String tecnicoAtendimento;

    @Column(name = "fonte_informacao")
    private String fonteInformacao;

    @Column(name = "status_entrevista")
    private String statusEntrevista;

    @Column(name = "num_inscricao")
    private String numInscricao;

    @Column(name = "date_create", insertable = false, updatable = false)
    private LocalDateTime dateCreate;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "user_update")
    private String userUpdate;
}
