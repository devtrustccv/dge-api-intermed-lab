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
@Table(name = "emprego_t_entidade")
public class Entidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String denominacao;

    @Column(name = "global_id_entidade")
    private Integer globalIdEntidade;

    private Integer nif;

    @Column(name = "registo_social")
    private String registoSocial;

    @Column(name = "natureza_juridica")
    private String naturezaJuridica;
}
