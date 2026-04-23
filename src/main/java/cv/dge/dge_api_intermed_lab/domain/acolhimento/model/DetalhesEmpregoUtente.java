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
@Table(name = "emprego_t_detalhes_emprego_utente")
public class DetalhesEmpregoUtente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_pessoa")
    private Integer idPessoa;

    @Column(name = "id_utente")
    private Integer idUtente;

    @Column(name = "situacao_emprego")
    private String situacaoEmprego;

    private String profissao;

    private String empresa;

    @Column(name = "setor_atividade")
    private String setorAtividade;

    private String ilha;

    private String concelho;

    private String zona;

    private String telefone;

    @Column(name = "num_trabalhador")
    private String numTrabalhador;

    private String duracao;

    @Column(name = "date_create", insertable = false, updatable = false)
    private LocalDateTime dateCreate;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "user_update")
    private String userUpdate;
}
