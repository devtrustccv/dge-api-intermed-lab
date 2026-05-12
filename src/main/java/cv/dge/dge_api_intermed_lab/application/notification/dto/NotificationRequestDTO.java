package cv.dge.dge_api_intermed_lab.application.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationRequestDTO {

    private String mensagem;
    private String assunto;
    private String appName;
    private String email;
    private String tipoProcesso;
    private String idProcesso;
    private String tipoRelacao;
    private String idRelacao;
    private String isAlert;

}
