package cv.dge.dge_api_intermed_lab.infrastructure.secundary;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "t_notificacao_config_email")
public class TNotificacaoConfigEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "assunto")
    private String assunto;

    @Column(name = "mensagem", columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "processo_code")
    private String processoCode;

    @Column(name = "etapa_code")
    private String etapaCode;

    @Column(name = "app_code")
    private String appCode;

    @Column(name = "status")
    private String status;

    @Column(name = "data_registo")
    private LocalDateTime dataRegisto;

    @Column(name = "user_registo")
    private Integer userRegisto;

    @Column(name = "data_update")
    private LocalDateTime dataUpdate;

    @Column(name = "user_update")
    private Integer userUpdate;

    @Column(name = "id_organica")
    private BigDecimal idOrganica;

    @Column(name = "id_aplicacao")
    private BigDecimal idAplicacao;

}
