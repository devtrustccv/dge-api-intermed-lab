package cv.dge.dge_api_intermed_lab.domain.acolhimento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "date_create", insertable = false, updatable = false)
    private LocalDateTime dateCreate;
}
