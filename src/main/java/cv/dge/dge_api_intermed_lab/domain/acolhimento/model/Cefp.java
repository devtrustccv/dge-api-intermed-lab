package cv.dge.dge_api_intermed_lab.domain.acolhimento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "emprego_t_cefp")
public class Cefp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String denominacao;

    private String sigla;

    private String telefone;

    private String email;

    @Column(name = "organization_id")
    private Integer organizationId;

    private String ilha;

    private String concelho;

    private String zona;

    private String endereco;

    private String descricao;
}
