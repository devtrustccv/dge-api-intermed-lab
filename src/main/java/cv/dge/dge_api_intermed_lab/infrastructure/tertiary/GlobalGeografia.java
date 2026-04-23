package cv.dge.dge_api_intermed_lab.infrastructure.tertiary;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "global_geografia")
public class GlobalGeografia {

    @Id
    private String id;
    private String codigo;
    private String pais;
    private String nome;

    // getters e setters
}
