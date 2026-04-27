package cv.dge.dge_api_intermed_lab.domain.acolhimento.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "emprego_t_parametrizacao_report")
public class ParamReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo_report")
    private String tipoReport;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> detalhes;

    @Column(name = "logotipo_iefp")
    private String logotipoIefp;

    @Column(name = "logotipo_dge")
    private String logotipoDge;

    @Column(name = "date_create", insertable = false, updatable = false)
    private LocalDateTime dateCreate;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "user_update")
    private String userUpdate;
}
