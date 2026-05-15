package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DetalhesAcolhimentoRepository extends JpaRepository<DetalhesAcolhimento, Integer> {

    List<DetalhesAcolhimento> findAllByIdPessoaOrderByDateCreateDescIdDesc(Integer idPessoa);

    List<DetalhesAcolhimento> findAllByIdEntidadeOrderByDateCreateDescIdDesc(Integer idEntidade);

    List<DetalhesAcolhimento> findAllByIdEntidadeInOrderByDateCreateDescIdDesc(List<Integer> idsEntidade);

    @Query(value = """
            SELECT COALESCE(MAX(CAST(substring(num_inscricao from '/([0-9]+)$') AS INTEGER)), 0) + 1
            FROM emprego_t_detalhes_acolhimento
            WHERE num_inscricao LIKE 'ACO%/%'
            """, nativeQuery = true)
    Integer proximoNumeroInscricao();
}
