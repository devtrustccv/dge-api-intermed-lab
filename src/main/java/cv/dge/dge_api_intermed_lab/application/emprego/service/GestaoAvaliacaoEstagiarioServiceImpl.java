package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.*;
import cv.dge.dge_api_intermed_lab.application.emprego.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository.GestaoAvaliacaoEstagiarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GestaoAvaliacaoEstagiarioServiceImpl implements GestaoAvaliacaoEstagiarioService {

    private final GestaoAvaliacaoEstagiarioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AvaliacaoEstagiarioListaResponse> listar(AvaliacaoEstagiarioFiltro filtro) {
        validarEntidade(filtro == null ? null : filtro.entidadeId());
        if (filtro.dataInicio() != null && filtro.dataFim() != null && filtro.dataFim().isBefore(filtro.dataInicio())) {
            throw erro("dataFim nao pode ser anterior a dataInicio.");
        }
        AvaliacaoEstagiarioFiltro dados = new AvaliacaoEstagiarioFiltro(
                filtro.entidadeId(), filtro.pessoaId(), dominioOpcional(EmpregoDominio.DOMINIO_TIPO_AVALIACAO,
                filtro.tipoAvaliacao()), texto(filtro.periodoReferencia()), filtro.dataInicio(), filtro.dataFim());
        return repository.listar(dados).stream().map(this::enriquecerLista).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AvaliacaoEstagiarioDetalheResponse buscarPorId(Integer id, Integer entidadeId) {
        validarId(id); validarEntidade(entidadeId);
        return repository.buscarPorId(id, entidadeId).map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avaliacao de estagiario nao encontrada."));
    }

    @Override
    @Transactional
    public AvaliacaoEstagiarioDetalheResponse criar(Integer entidadeId, AvaliacaoEstagiarioRequest request) {
        validarEntidade(entidadeId);
        AvaliacaoEstagiarioRequest dados = validarRequest(request);
        AvaliacaoEstagiarioVinculo vinculo = repository.buscarEstagiarioElegivel(entidadeId, dados.pessoaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estagiario nao encontrado numa colocacao ativa de oferta de estagio para a entidade."));
        Integer id = repository.inserir(vinculo, dados, dados.utilizador());
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public AvaliacaoEstagiarioDetalheResponse atualizar(Integer id, Integer entidadeId, AvaliacaoEstagiarioRequest request) {
        buscarPorId(id, entidadeId);
        AvaliacaoEstagiarioRequest dados = validarRequest(request);
        AvaliacaoEstagiarioVinculo vinculo = repository.buscarEstagiarioElegivel(entidadeId, dados.pessoaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estagiario nao encontrado numa colocacao ativa de oferta de estagio para a entidade."));
        repository.atualizar(id, vinculo, dados, dados.utilizador());
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvaliacaoEstagiarioSelectResponse> listarEstagiarios(Integer entidadeId) {
        validarEntidade(entidadeId);
        return repository.listarEstagiarios(entidadeId);
    }

    private AvaliacaoEstagiarioRequest validarRequest(AvaliacaoEstagiarioRequest request) {
        if (request == null) throw erro("Dados da avaliacao sao obrigatorios.");
        if (request.pessoaId() == null) throw erro("pessoaId e obrigatorio.");
        String tipo = dominioObrigatorio(EmpregoDominio.DOMINIO_TIPO_AVALIACAO, request.tipoAvaliacao());
        String periodo = obrigatorio(request.periodoReferencia(), "periodoReferencia e obrigatorio.");
        String grau = dominioObrigatorio(EmpregoDominio.DOMINIO_GRAU_SATISFACAO, request.grauSatisfacao());
        String utilizador = obrigatorio(request.utilizador(), "utilizador e obrigatorio para auditoria.");
        if (request.classificacao() == null) throw erro("classificacao e obrigatoria.");
        if (request.avaliacaoDesempenho() == null || request.avaliacaoDesempenho().isEmpty())
            throw erro("avaliacaoDesempenho deve conter pelo menos um item.");
        List<AvaliacaoDesempenhoRequest> desempenho = request.avaliacaoDesempenho().stream().map(item -> {
            if (item == null) throw erro("Item de avaliacaoDesempenho invalido.");
            return new AvaliacaoDesempenhoRequest(
                    dominioObrigatorio(EmpregoDominio.DOMINIO_TIPO_COMPETENCIA, item.tipoCompetencia()),
                    dominioObrigatorio(EmpregoDominio.DOMINIO_AVALIACAO, item.avaliacao()));
        }).toList();
        return new AvaliacaoEstagiarioRequest(request.pessoaId(), tipo, periodo, desempenho, grau,
                texto(request.interesseContratacao()), request.classificacao(), texto(request.observacao()), utilizador);
    }

    private AvaliacaoEstagiarioListaResponse enriquecerLista(AvaliacaoEstagiarioListaResponse i) {
        return new AvaliacaoEstagiarioListaResponse(i.id(), i.pessoaId(), i.estagiario(), i.tipoAvaliacao(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_AVALIACAO, i.tipoAvaliacao()),
                i.periodoReferencia(), i.classificacao(), i.dataRegisto());
    }

    private AvaliacaoEstagiarioDetalheResponse enriquecerDetalhe(AvaliacaoEstagiarioDetalheResponse i) {
        return new AvaliacaoEstagiarioDetalheResponse(i.id(), i.pessoaId(), i.estagiario(), i.candidaturaId(),
                i.tipoAvaliacao(), EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_AVALIACAO, i.tipoAvaliacao()),
                i.periodoReferencia(), i.avaliacaoDesempenho().stream().map(item -> new AvaliacaoDesempenhoResponse(
                        item.tipoCompetencia(),
                        EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_COMPETENCIA, item.tipoCompetencia()),
                        item.avaliacao(),
                        EmpregoDominio.descricao(EmpregoDominio.DOMINIO_AVALIACAO, item.avaliacao())
                )).toList(), i.grauSatisfacao(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_GRAU_SATISFACAO, i.grauSatisfacao()),
                i.interesseContratacao(), i.classificacao(), i.observacao(), i.dateCreate(), i.userCreate(),
                i.dateUpdate(), i.userUpdate());
    }

    private String dominioObrigatorio(String dominio, String valor) {
        String resultado = dominioOpcional(dominio, valor);
        if (resultado == null) throw erro(dominio + " e obrigatorio.");
        return resultado;
    }
    private String dominioOpcional(String dominio, String valor) {
        String v = texto(valor); if (v == null) return null;
        return EmpregoDominio.valorOficial(dominio, v)
                .orElseThrow(() -> erro(dominio + " invalido: " + v + "."));
    }
    private void validarId(Integer id) { if (id == null || id <= 0) throw erro("id e obrigatorio."); }
    private void validarEntidade(Integer id) { if (id == null || id <= 0) throw erro("entidadeId e obrigatorio."); }
    private String obrigatorio(String v, String m) { String t = texto(v); if (t == null) throw erro(m); return t; }
    private String texto(String v) { return v == null || v.trim().isEmpty() ? null : v.trim(); }
    private ResponseStatusException erro(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}
