package cv.dge.dge_api_intermed_lab.domain.acolhimento.business;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesEmpregoUtente;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.ParamReport;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.CefpRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesAcolhimentoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesEmpregoUtenteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.EntidadeRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.ParamReportRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.UtenteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.orientacao.repository.AcolhimentoServicoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.orientacao.repository.AgendamentoEntrevistaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcolhimentoBusImpl implements AcolhimentoBus {

    private final DetalhesAcolhimentoRepository detalhesAcolhimentoRepository;
    private final DetalhesEmpregoUtenteRepository detalhesEmpregoUtenteRepository;
    private final UtenteRepository utenteRepository;
    private final CefpRepository cefpRepository;
    private final EntidadeRepository entidadeRepository;
    private final ParamReportRepository paramReportRepository;
    private final AgendamentoEntrevistaRepository agendamentoEntrevistaRepository;
    private final AcolhimentoServicoRepository acolhimentoServicoRepository;

    @Override
    public Optional<DetalhesAcolhimento> findAcolhimentoById(Integer id) {
        return detalhesAcolhimentoRepository.findById(id);
    }

    @Override
    public List<DetalhesAcolhimento> findAcolhimentosByPessoa(Integer idPessoa) {
        return detalhesAcolhimentoRepository.findAllByIdPessoaOrderByDateCreateDescIdDesc(idPessoa);
    }

    @Override
    public List<DetalhesAcolhimento> findAcolhimentosByEntidades(List<Integer> idsEntidade) {
        return detalhesAcolhimentoRepository.findAllByIdEntidadeInOrderByDateCreateDescIdDesc(idsEntidade);
    }

    @Override
    public DetalhesAcolhimento saveAcolhimento(DetalhesAcolhimento acolhimento) {
        return detalhesAcolhimentoRepository.save(acolhimento);
    }

    @Override
    public Integer proximoNumeroInscricao() {
        return detalhesAcolhimentoRepository.proximoNumeroInscricao();
    }

    @Override
    public Optional<DetalhesEmpregoUtente> findUltimoEmpregoByPessoa(Integer idPessoa) {
        return detalhesEmpregoUtenteRepository.findFirstByIdPessoaOrderByDateCreateDescIdDesc(idPessoa);
    }

    @Override
    public DetalhesEmpregoUtente saveEmprego(DetalhesEmpregoUtente emprego) {
        return detalhesEmpregoUtenteRepository.save(emprego);
    }

    @Override
    public Optional<Utente> findUtenteById(Integer id) {
        return utenteRepository.findById(id);
    }

    @Override
    public Optional<Utente> findUtenteByPessoa(Integer idPessoa) {
        return utenteRepository.findFirstByPessoaId(idPessoa);
    }

    @Override
    public Optional<Utente> findUtenteByDocumento(String tipoDocumento, String numDocumento) {
        return utenteRepository.findFirstByTipoDocumentoIgnoreCaseAndNumDocumentoIgnoreCase(tipoDocumento, numDocumento);
    }

    @Override
    public Optional<Utente> findUtenteByNumDocumento(String numDocumento) {
        return utenteRepository.findFirstByNumDocumentoIgnoreCase(numDocumento);
    }

    @Override
    public List<Utente> findAllUtentes() {
        return utenteRepository.findAll();
    }

    @Override
    public Utente saveUtente(Utente utente) {
        return utenteRepository.save(utente);
    }

    @Override
    public Optional<Cefp> findCefpById(Integer id) {
        return cefpRepository.findById(id);
    }

    @Override
    public Optional<Cefp> findCefpByOrganizationId(Integer organizationId) {
        return cefpRepository.findFirstByOrganizationId(organizationId);
    }

    @Override
    public Optional<Cefp> findCefpByAreaAbrangida(String ilha, String concelho) {
        return cefpRepository.findFirstByAreaAbrangida(ilha, concelho);
    }

    @Override
    public Optional<Entidade> findEntidadeById(Integer id) {
        return entidadeRepository.findById(id);
    }

    @Override
    public Optional<Entidade> findEntidadeByNif(Integer nif) {
        return entidadeRepository.findFirstByNif(nif);
    }

    @Override
    public List<Entidade> findEntidadesByGlobalId(Integer globalIdEntidade) {
        return entidadeRepository.findAllByGlobalIdEntidade(globalIdEntidade);
    }

    @Override
    public Entidade saveEntidade(Entidade entidade) {
        return entidadeRepository.save(entidade);
    }

    @Override
    public Optional<ParamReport> findUltimoParamReport() {
        return paramReportRepository.findFirstByOrderByIdDesc();
    }

    @Override
    public Optional<AgendamentoEntrevista> findUltimaEntrevistaByAcolhimento(Integer idAcolhimento) {
        return agendamentoEntrevistaRepository.findFirstByIdAcolhimentoOrderByDateCreateDescIdDesc(idAcolhimento);
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
    public Optional<AcolhimentoServico> findUltimoServicoByAcolhimento(Integer idAcolhimento) {
        return acolhimentoServicoRepository.findFirstByIdAcolhimentoOrderByIdDesc(idAcolhimento);
    }
}
