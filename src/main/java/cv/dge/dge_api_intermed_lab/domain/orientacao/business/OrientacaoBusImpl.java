package cv.dge.dge_api_intermed_lab.domain.orientacao.business;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.Requisito;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesAcolhimentoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.orientacao.repository.AcolhimentoServicoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.orientacao.repository.AgendamentoEntrevistaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.orientacao.repository.RequisitoRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrientacaoBusImpl implements OrientacaoBus {

    private final RequisitoRepository requisitoRepository;
    private final AgendamentoEntrevistaRepository agendamentoEntrevistaRepository;
    private final DetalhesAcolhimentoRepository detalhesAcolhimentoRepository;
    private final AcolhimentoServicoRepository acolhimentoServicoRepository;

    @Override
    public List<Requisito> listarRequisitosAtivosPorTipoServico(String estado, String tipoServico) {
        return requisitoRepository.findAllByEstadoIgnoreCaseAndTipoServicoIgnoreCaseOrderByDateCreateDescIdDesc(
                estado,
                tipoServico
        );
    }

    @Override
    public Optional<AgendamentoEntrevista> findEntrevistaById(Integer id) {
        return agendamentoEntrevistaRepository.findById(id);
    }

    @Override
    public Optional<DetalhesAcolhimento> findAcolhimentoById(Integer id) {
        return detalhesAcolhimentoRepository.findById(id);
    }

    @Override
    public Optional<AcolhimentoServico> findServicoById(Integer id) {
        return acolhimentoServicoRepository.findById(id);
    }

    @Override
    public Optional<AcolhimentoServico> findUltimoServicoByEntrevista(Integer idEntrevista) {
        return acolhimentoServicoRepository.findFirstByIdEntrevistaOrderByIdDesc(idEntrevista);
    }

    @Override
    public Optional<AcolhimentoServico> findUltimoServicoByEntrevistaAndTipo(Integer idEntrevista, String tipoServico) {
        return acolhimentoServicoRepository.findFirstByIdEntrevistaAndTipoServicoOrderByIdDesc(idEntrevista, tipoServico);
    }

    @Override
    public AcolhimentoServico saveServico(AcolhimentoServico servico) {
        return acolhimentoServicoRepository.save(servico);
    }
}
