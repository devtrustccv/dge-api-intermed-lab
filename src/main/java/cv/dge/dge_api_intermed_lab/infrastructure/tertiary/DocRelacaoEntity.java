package cv.dge.dge_api_intermed_lab.infrastructure.tertiary;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "doc_t_doc_relacao")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DocRelacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "estado")
    private String estado;

    @Column(name = "name")
    private String name;

    @Column(name = "id_relacao")
    private Long idRelacao;

    @Column(name = "id_tp_doc")
    private Long idTpDoc;

    @Column(name = "mimetype")
    private String mimetype;

    @Column(name = "path")
    private String path;

    @Column(name = "tipo_relacao")
    private String tipoRelacao;

    @Column(name = "user_create")
    private String userCreate;


    @Column(name = "file_name")
    private String fileName;

    @Column(name = "app_code")
    private String appCode;

//    @Column(name = " ")
//    private String name; // Note que tem espaço no nome da coluna original — evite usar esse nome no banco!

}

