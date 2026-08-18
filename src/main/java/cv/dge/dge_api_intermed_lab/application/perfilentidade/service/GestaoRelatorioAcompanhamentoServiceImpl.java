package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoVinculo;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoRelatorioAcompanhamentoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GestaoRelatorioAcompanhamentoServiceImpl implements GestaoRelatorioAcompanhamentoService {

    private static final String ESTADO_ATIVO = "A";
    private static final String ESTADO_INATIVO = "I";

    private final GestaoRelatorioAcompanhamentoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<RelatorioAcompanhamentoListaResponse> listar(RelatorioAcompanhamentoFiltro filtro) {
        validarEntidade(filtro == null ? null : filtro.entidadeId());
        validarIntervalo(filtro.dataInicio(), filtro.dataFim(),
                "A data final da pesquisa não pode ser anterior à data inicial.");
        RelatorioAcompanhamentoFiltro dados = new RelatorioAcompanhamentoFiltro(
                filtro.entidadeId(), filtro.pessoaId(), texto(filtro.codigoReferencia()),
                filtro.dataInicio(), filtro.dataFim());
        return repository.listar(dados).stream().map(this::enriquecerLista).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RelatorioAcompanhamentoDetalheResponse buscarPorId(Integer id, Integer entidadeId) {
        validarId(id);
        validarEntidade(entidadeId);
        return repository.buscarPorId(id, entidadeId)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "O relatório de acompanhamento selecionado não foi encontrado. Atualize a página e tente novamente."
                ));
    }

    @Override
    @Transactional
    public RelatorioAcompanhamentoDetalheResponse criar(
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request
    ) {
        validarEntidade(entidadeId);
        RelatorioAcompanhamentoRequest dados = validarRequest(request);
        RelatorioAcompanhamentoVinculo vinculo = resolverVinculo(entidadeId, dados);
        Integer id = repository.inserir(vinculo, dados, ESTADO_ATIVO, dados.utilizador());
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public RelatorioAcompanhamentoDetalheResponse atualizar(
            Integer id,
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request
    ) {
        RelatorioAcompanhamentoDetalheResponse atual = buscarPorId(id, entidadeId);
        garantirAtivo(atual);
        RelatorioAcompanhamentoRequest dados = validarRequest(request);
        RelatorioAcompanhamentoVinculo vinculo = resolverVinculo(entidadeId, dados);
        repository.atualizar(id, vinculo, dados, dados.utilizador());
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public RelatorioAcompanhamentoDetalheResponse remover(
            Integer id,
            Integer entidadeId,
            RelatorioAcompanhamentoRemoverRequest request
    ) {
        RelatorioAcompanhamentoDetalheResponse atual = buscarPorId(id, entidadeId);
        garantirAtivo(atual);
        if (request == null) {
            throw erro("Não foi possível confirmar a eliminação do relatório. Tente novamente.");
        }
        String utilizador = obrigatorio(request.utilizador(),
                "Não foi possível identificar o utilizador. Inicie sessão novamente e repita a operação.");
        repository.remover(id, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelatorioAcompanhamentoOfertaSelectResponse> listarOpcoes(Integer entidadeId) {
        validarEntidade(entidadeId);
        Map<Integer, List<RelatorioAcompanhamentoEstagiarioSelectResponse>> estagiariosPorOferta =
                repository.listarEstagiariosPorOferta(entidadeId);
        return repository.listarOfertas(entidadeId).stream()
                .map(oferta -> new RelatorioAcompanhamentoOfertaSelectResponse(
                        oferta.ofertaId(),
                        oferta.codigoReferencia(),
                        oferta.titulo(),
                        oferta.oferta(),
                        estagiariosPorOferta.getOrDefault(oferta.ofertaId(), List.of())
                ))
                .toList();
    }

    private RelatorioAcompanhamentoRequest validarRequest(RelatorioAcompanhamentoRequest request) {
        if (request == null) {
            throw erro("Preencha os dados do relatório de acompanhamento antes de gravar.");
        }
        if (request.pessoaId() == null) {
            throw erro("Selecione o estagiário a que o relatório se refere.");
        }
        String codigoReferencia = obrigatorio(request.codigoReferencia(), "Informe a oferta de estágio.");
        if (request.dataInicio() == null) {
            throw erro("Informe a data de início do período do relatório.");
        }
        if (request.dataFim() == null) {
            throw erro("Informe a data de fim do período do relatório.");
        }
        validarIntervalo(request.dataInicio(), request.dataFim(),
                "A data de fim do relatório não pode ser anterior à data de início.");
        String utilizador = obrigatorio(request.utilizador(),
                "Não foi possível identificar o utilizador. Inicie sessão novamente e repita a operação.");
        return new RelatorioAcompanhamentoRequest(
                request.pessoaId(), codigoReferencia, request.dataInicio(), request.dataFim(),
                texto(request.atividadesRealizadas()), texto(request.dificuldades()),
                texto(request.recomendacoes()), texto(request.relatorioAnexo()), utilizador);
    }

    private RelatorioAcompanhamentoVinculo resolverVinculo(
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request
    ) {
        return repository.buscarVinculo(entidadeId, request.pessoaId(), request.codigoReferencia())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi encontrada uma colocação ativa para o estagiário e a oferta selecionados. "
                                + "Confirme os dados escolhidos e tente novamente."
                ));
    }

    private RelatorioAcompanhamentoListaResponse enriquecerLista(RelatorioAcompanhamentoListaResponse item) {
        String estado = valorEstado(item.estado());
        return new RelatorioAcompanhamentoListaResponse(
                item.id(), item.pessoaId(), item.estagiario(), item.ofertaId(), item.codigoReferencia(),
                item.dataRegisto(), item.relatorioAnexo(), estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, estado));
    }

    private RelatorioAcompanhamentoDetalheResponse enriquecerDetalhe(RelatorioAcompanhamentoDetalheResponse item) {
        String estado = valorEstado(item.estado());
        return new RelatorioAcompanhamentoDetalheResponse(
                item.id(), item.ofertaId(), item.codigoReferencia(), item.colocacaoId(), item.entidadeId(),
                item.denominacaoEntidade(), item.pessoaId(), item.estagiario(), item.dataInicio(), item.dataFim(),
                item.atividadesRealizadas(), item.dificuldades(), item.recomendacoes(), item.relatorioAnexo(),
                estado, EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO, estado),
                item.dateCreate(), item.userCreate(), item.dateUpdate(), item.userUpdate());
    }

    private void garantirAtivo(RelatorioAcompanhamentoDetalheResponse item) {
        if (ESTADO_INATIVO.equals(valorEstado(item.estado()))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este relatório já foi eliminado e não pode ser alterado."
            );
        }
    }

    private String valorEstado(String estado) {
        return EmpregoDominio.valorOficial(EmpregoDominio.DOMINIO_ESTADO, estado).orElse(estado);
    }

    private void validarIntervalo(LocalDate inicio, LocalDate fim, String mensagem) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw erro(mensagem);
        }
    }

    private void validarId(Integer id) {
        if (id == null || id <= 0) {
            throw erro("Não foi possível identificar o relatório selecionado. Atualize a página e tente novamente.");
        }
    }

    private void validarEntidade(Integer entidadeId) {
        if (entidadeId == null || entidadeId <= 0) {
            throw erro("Não foi possível identificar a entidade selecionada. Selecione uma entidade e tente novamente.");
        }
    }

    private String obrigatorio(String valor, String mensagem) {
        String resultado = texto(valor);
        if (resultado == null) {
            throw erro(mensagem);
        }
        return resultado;
    }

    private String texto(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
