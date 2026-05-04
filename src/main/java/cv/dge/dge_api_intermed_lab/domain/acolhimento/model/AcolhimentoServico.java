package cv.dge.dge_api_intermed_lab.domain.acolhimento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "emprego_t_acolhimento_servico")
public class AcolhimentoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_acolhimento")
    private Integer idAcolhimento;

    @Column(name = "id_utente")
    private Integer idUtente;

    @Column(name = "tipo_utente")
    private String tipoUtente;

    @Column(name = "tipo_utente_desc")
    private String tipoUtenteDesc;

    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "tipo_servico_desc")
    private String tipoServicoDesc;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalhes_servico", columnDefinition = "jsonb")
    private Map<String, Object> detalhesServico;

    @Column(name = "id_entrevista")
    private Integer idEntrevista;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalhes_analise", columnDefinition = "jsonb")
    private Map<String, Object> detalhesAnalise;

    @Column(name = "necessidade_analise")
    private Boolean necessidadeAnalise;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "user_update")
    private String userUpdate;
}
