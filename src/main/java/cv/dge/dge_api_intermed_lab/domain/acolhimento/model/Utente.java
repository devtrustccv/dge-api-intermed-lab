package cv.dge.dge_api_intermed_lab.domain.acolhimento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "emprego_t_utente")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pessoa_id")
    private Integer pessoaId;

    private String nome;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "num_documento")
    private String numDocumento;

    private String sexo;

    private Integer nif;

    @Column(name = "habilitacao_literaria")
    private String habilitacaoLiteraria;

    @Column(name = "date_create", insertable = false, updatable = false)
    private LocalDateTime dateCreate;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "user_update")
    private String userUpdate;
}
