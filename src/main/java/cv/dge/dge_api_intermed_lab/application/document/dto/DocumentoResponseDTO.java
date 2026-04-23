package cv.dge.dge_api_intermed_lab.application.document.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoResponseDTO {
    private Long id;
    private String fileName;
    private String path;
    private String tipoRelacao;
    private Integer idRelacao;
    private String idTpDoc;
    private String estado;
    private String appCode;
    private String previewUrl;
    private String name;
    // Adicione outros campos se necessário, como data de criação
    private String dataCriacao;
}
