package cv.dge.dge_api_intermed_lab.application.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DefaultReponseDTO {
    private String msg;
    private String status;


}