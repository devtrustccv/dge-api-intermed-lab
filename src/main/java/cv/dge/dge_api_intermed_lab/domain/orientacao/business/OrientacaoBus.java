package cv.dge.dge_api_intermed_lab.domain.orientacao.business;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.Requisito;
import java.util.List;
import java.util.Optional;

public interface OrientacaoBus {

    List<Requisito> listarRequisitosAtivosPorTipoServico(String estado, String tipoServico);

    Optional<AgendamentoEntrevista> findEntrevistaById(Integer id);

    Optional<DetalhesAcolhimento> findAcolhimentoById(Integer id);

    Optional<AcolhimentoServico> findServicoById(Integer id);

    Optional<AcolhimentoServico> findUltimoServicoByEntrevista(Integer idEntrevista);

    Optional<AcolhimentoServico> findUltimoServicoByEntrevistaAndTipo(Integer idEntrevista, String tipoServico);

    AcolhimentoServico saveServico(AcolhimentoServico servico);
}
