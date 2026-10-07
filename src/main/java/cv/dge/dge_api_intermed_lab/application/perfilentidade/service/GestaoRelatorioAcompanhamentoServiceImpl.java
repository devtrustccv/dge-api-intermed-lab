package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoVinculo;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.constants.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoRelatorioAcompanhamentoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestaoRelatorioAcompanhamentoServiceImpl implements GestaoRelatorioAcompanhamentoService {

    private final EmpregoDominioService empregoDominioService;
    private static final String ESTADO_ATIVO = "A";
    private static final String ESTADO_INATIVO = "I";
    private static final DateTimeFormatter SUFIXO_DOCUMENTO = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final GestaoRelatorioAcompanhamentoRepository repository;
    private final DocumentService documentService;

    @Value("${document.relatorio-acompanhamento.app-code:interm_laboral}")
    private String appCodeDocumento;

    @Value("${document.relatorio-acompanhamento.tipo-relacao:EMPREGO_T_RELATORIO_ACOMP}")
    private String tipoRelacaoDocumento;

    @Value("${document.relatorio-acompanhamento.estado:A}")
    private String estadoDocumento;

    @Override
    @Transactional(readOnly = true)
    public List<RelatorioAcompanhamentoListaResponse> listar(RelatorioAcompanhamentoFiltro filtro) {
        if (filtro == null) {
            throw erro("Os filtros da pesquisa de relatórios de acompanhamento não foram enviados.");
        }
        validarEntidade(filtro.entidadeId());
        validarIntervalo(filtro.dataInicio(), filtro.dataFim(),
                "A data final da pesquisa não pode ser anterior à data inicial.");
        RelatorioAcompanhamentoFiltro dados = new RelatorioAcompanhamentoFiltro(
                filtro.entidadeId(), filtro.pessoaId(), texto(filtro.estagiario()),
                texto(filtro.codigoReferencia()),
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
        return criar(entidadeId, request, null);
    }

    @Override
    @Transactional
    public RelatorioAcompanhamentoDetalheResponse criar(
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request,
            MultipartFile relatorioAnexo
    ) {
        validarEntidade(entidadeId);
        RelatorioAcompanhamentoRequest dados = validarRequest(request);
        RelatorioAcompanhamentoVinculo vinculo = resolverVinculo(entidadeId, dados);
        String referencia = temFicheiro(relatorioAnexo)
                ? null
                : normalizarReferenciaDocumento(dados.relatorioAnexo());
        Integer id = repository.inserir(
                vinculo,
                comRelatorioAnexo(dados, referencia),
                ESTADO_ATIVO,
                dados.utilizador()
        );
        if (temFicheiro(relatorioAnexo)) {
            String linkCompleto = guardarRelatorio(id, relatorioAnexo);
            repository.atualizarRelatorioAnexo(id, entidadeId, linkCompleto, dados.utilizador());
        }
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public RelatorioAcompanhamentoDetalheResponse atualizar(
            Integer id,
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request
    ) {
        return atualizar(id, entidadeId, request, null);
    }

    @Override
    @Transactional
    public RelatorioAcompanhamentoDetalheResponse atualizar(
            Integer id,
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request,
            MultipartFile relatorioAnexo
    ) {
        RelatorioAcompanhamentoDetalheResponse atual = buscarPorId(id, entidadeId);
        garantirAtivo(atual);
        RelatorioAcompanhamentoRequest dados = validarRequest(request);
        RelatorioAcompanhamentoVinculo vinculo = resolverVinculo(entidadeId, dados);
        String referencia;
        if (temFicheiro(relatorioAnexo)) {
            referencia = guardarRelatorio(id, relatorioAnexo);
        } else if (temTexto(dados.relatorioAnexo())) {
            referencia = normalizarReferenciaDocumento(dados.relatorioAnexo());
        } else {
            referencia = normalizarReferenciaDocumento(atual.relatorioAnexo());
        }
        repository.atualizar(
                id,
                entidadeId,
                vinculo,
                comRelatorioAnexo(dados, referencia),
                dados.utilizador()
        );
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
        repository.remover(id, entidadeId, utilizador);
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
        rejeitarBase64(request.relatorioAnexo());
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
                item.dataRegisto(), linkDocumentoParaResposta(item.relatorioAnexo()), estado,
                empregoDominioService.descricao(EmpregoDominio.DOMINIO_ESTADO, estado));
    }

    private RelatorioAcompanhamentoDetalheResponse enriquecerDetalhe(RelatorioAcompanhamentoDetalheResponse item) {
        String estado = valorEstado(item.estado());
        return new RelatorioAcompanhamentoDetalheResponse(
                item.id(), item.ofertaId(), item.codigoReferencia(), item.colocacaoId(), item.entidadeId(),
                item.denominacaoEntidade(), item.pessoaId(), item.estagiario(), item.dataInicio(), item.dataFim(),
                item.atividadesRealizadas(), item.dificuldades(), item.recomendacoes(),
                linkDocumentoParaResposta(item.relatorioAnexo()),
                estado, empregoDominioService.descricao(EmpregoDominio.DOMINIO_ESTADO, estado),
                item.dateCreate(), item.userCreate(), item.dateUpdate(), item.userUpdate());
    }

    private String guardarRelatorio(Integer relatorioId, MultipartFile ficheiro) {
        String nomeOriginal = StringUtils.cleanPath(
                Optional.ofNullable(ficheiro.getOriginalFilename()).orElse("relatorio")
        );
        String extensao = extensao(nomeOriginal);
        String nomeArmazenamento = "RELATORIO-"
                + LocalDateTime.now().format(SUFIXO_DOCUMENTO);
        String path = appCodeDocumento
                + "/" + LocalDateTime.now().getYear()
                + "/modulos/" + sanitizarSegmentoPath(tipoRelacaoDocumento)
                + "/" + relatorioId
                + "/" + nomeArmazenamento + extensao;
        try {
            String pathGuardado = documentService.save(DocRelacaoDTO.builder()
                    .idRelacao(relatorioId)
                    .tipoRelacao(tipoRelacaoDocumento)
                    .estado(estadoDocumento)
                    .name(nomeOriginal)
                    .fileName(nomeArmazenamento)
                    .path(path)
                    .appCode(appCodeDocumento)
                    .file(ficheiro)
                    .build());
            if (!temTexto(pathGuardado)) {
                throw new IllegalStateException("O serviço documental devolveu um caminho vazio.");
            }
            String linkCompleto = documentService.gerarLinkPublico(pathGuardado);
            if (!temTexto(linkCompleto)) {
                throw new IllegalStateException("O serviço documental devolveu um link inválido.");
            }
            return linkCompleto;
        } catch (RuntimeException ex) {
            log.error(
                    "Falha ao guardar anexo do relatório de acompanhamento: relatorioId={}, ficheiro={}",
                    relatorioId,
                    nomeOriginal,
                    ex
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível guardar o anexo do relatório de acompanhamento. Tente novamente mais tarde.",
                    ex
            );
        }
    }

    private String normalizarReferenciaDocumento(String referencia) {
        String valor = texto(referencia);
        if (valor == null) {
            return null;
        }
        rejeitarBase64(valor);
        String link = documentService.gerarLinkPublico(valor);
        return temTexto(link) ? link : null;
    }

    private String linkDocumentoParaResposta(String referencia) {
        if (!temTexto(referencia) || ehDataUri(referencia)) {
            return null;
        }
        String link = documentService.gerarLinkPublico(referencia);
        return temTexto(link) ? link : null;
    }

    private void rejeitarBase64(String referencia) {
        if (temTexto(referencia) && ehDataUri(referencia)) {
            throw erro(
                    "O campo \"relatorioAnexo\" não aceita ficheiros em Base64. "
                            + "Envie o documento como multipart/form-data."
            );
        }
    }

    private boolean ehDataUri(String valor) {
        return valor.trim().regionMatches(true, 0, "data:", 0, 5);
    }

    private boolean temFicheiro(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private String extensao(String nome) {
        int indice = nome.lastIndexOf('.');
        if (indice < 0 || indice == nome.length() - 1) {
            return "";
        }
        return nome.substring(indice).toLowerCase();
    }

    private String sanitizarSegmentoPath(String valor) {
        String normalizado = valor == null ? "DOCUMENTO" : valor.trim();
        normalizado = normalizado.replaceAll("[^A-Za-z0-9_-]", "-");
        return normalizado.isBlank() ? "DOCUMENTO" : normalizado;
    }

    private RelatorioAcompanhamentoRequest comRelatorioAnexo(
            RelatorioAcompanhamentoRequest dados,
            String relatorioAnexo
    ) {
        return new RelatorioAcompanhamentoRequest(
                dados.pessoaId(),
                dados.codigoReferencia(),
                dados.dataInicio(),
                dados.dataFim(),
                dados.atividadesRealizadas(),
                dados.dificuldades(),
                dados.recomendacoes(),
                relatorioAnexo,
                dados.utilizador()
        );
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
        return empregoDominioService.valorOficial(EmpregoDominio.DOMINIO_ESTADO, estado).orElse(estado);
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
