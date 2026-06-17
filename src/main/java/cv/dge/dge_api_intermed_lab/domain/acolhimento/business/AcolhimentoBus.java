package cv.dge.dge_api_intermed_lab.domain.acolhimento.business;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesEmpregoUtente;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.ParamReport;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import java.util.List;
import java.util.Optional;

public interface AcolhimentoBus {

    Optional<DetalhesAcolhimento> findAcolhimentoById(Integer id);

    List<DetalhesAcolhimento> findAcolhimentosByPessoa(Integer idPessoa);

    List<DetalhesAcolhimento> findAcolhimentosByEntidades(List<Integer> idsEntidade);

    DetalhesAcolhimento saveAcolhimento(DetalhesAcolhimento acolhimento);

    Integer proximoNumeroInscricao();

    Optional<DetalhesEmpregoUtente> findUltimoEmpregoByPessoa(Integer idPessoa);

    DetalhesEmpregoUtente saveEmprego(DetalhesEmpregoUtente emprego);

    Optional<Utente> findUtenteById(Integer id);

    Optional<Utente> findUtenteByPessoa(Integer idPessoa);

    Optional<Utente> findUtenteByDocumento(String tipoDocumento, String numDocumento);

    Optional<Utente> findUtenteByNumDocumento(String numDocumento);

    List<Utente> findAllUtentes();

    Utente saveUtente(Utente utente);

    Optional<Cefp> findCefpById(Integer id);

    Optional<Cefp> findCefpByOrganizationId(Integer organizationId);

    Optional<Cefp> findCefpByAreaAbrangida(String ilha, String concelho);

    Optional<Entidade> findEntidadeById(Integer id);

    Optional<Entidade> findEntidadeByNif(Integer nif);

    List<Entidade> findEntidadesByGlobalId(Integer globalIdEntidade);

    Entidade saveEntidade(Entidade entidade);

    Optional<ParamReport> findUltimoParamReport();

    Optional<AgendamentoEntrevista> findUltimaEntrevistaByAcolhimento(Integer idAcolhimento);

    Optional<AcolhimentoServico> findUltimoServicoByEntrevista(Integer idEntrevista);

    Optional<AcolhimentoServico> findUltimoServicoByEntrevistaAndTipo(Integer idEntrevista, String tipoServico);

    Optional<AcolhimentoServico> findUltimoServicoByAcolhimento(Integer idAcolhimento);
}
