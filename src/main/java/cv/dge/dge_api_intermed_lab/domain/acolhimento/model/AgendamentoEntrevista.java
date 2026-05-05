package cv.dge.dge_api_intermed_lab.domain.acolhimento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
@Table(name = "emprego_t_agendamento_entrevista")
public class AgendamentoEntrevista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_acolhimento")
    private Integer idAcolhimento;

    @Column(name = "id_utente")
    private Integer idUtente;

    @Column(name = "id_tecnico")
    private Integer idTecnico;

    @Column(name = "nome_tecnico")
    private String nomeTecnico;

    @Column(name = "data_entrevista")
    private LocalDate dataEntrevista;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    private String local;

    @Column(name = "dm_status_entrevista")
    private String statusEntrevista;

    @Column(name = "id_cefp")
    private Integer idCefp;

    private String cefp;

    @Column(name = "tipo_servico")
    private String tipoServico;

    private String canal;

    @Column(name = "local_entrevista")
    private String localEntrevista;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resultado_entrevista", columnDefinition = "jsonb")
    private Map<String, Object> resultadoEntrevista;

    @Column(name = "parecer_io")
    private String parecerIo;

    @Column(name = "obs_parecer_io")
    private String obsParecerIo;

    @Column(name = "path_resultado")
    private String pathResultado;

    @Column(name = "date_create", insertable = false, updatable = false)
    private LocalDateTime dateCreate;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "user_update")
    private String userUpdate;
}
