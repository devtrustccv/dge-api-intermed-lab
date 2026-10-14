package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoVinculo;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.constants.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.ColocacaoCandidatoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
public class ColocacaoCandidatoServiceImpl implements ColocacaoCandidatoService {

    private final EmpregoDominioService empregoDominioService;
    private static final String ESTADO_ATIVO = "A";
    private static final String ESTADO_INATIVO = "I";
    private static final DateTimeFormatter SUFIXO_DOCUMENTO = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ColocacaoCandidatoRepository colocacaoRepository;
    private final DocumentService documentService;

    @Value("${document.colocacao.app-code:interm_laboral}")
    private String appCodeDocumento;

    @Value("${document.colocacao.estado:A}")
    private String estadoDocumento;

    @Value("${document.colocacao.tipo-relacao:EMPREGO_T_COLOCACAO_CANDIDATO}")
    private String tipoRelacaoDocumento;

    @Override
    @Transactional(readOnly = true)
    public List<ColocacaoCandidatoListaResponse> listar(ColocacaoCandidatoFiltro filtro) {
        return colocacaoRepository.listar(normalizarFiltro(filtro)).stream()
                .map(this::enriquecerLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ColocacaoCandidatoResponse buscarPorId(Integer id, Integer entidadeId) {
        validarId(id);
        validarEntidade(entidadeId);
        return colocacaoRepository.buscarPorId(id, entidadeId)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A colocação selecionada não foi encontrada. Atualize a página e tente novamente."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColocacaoOfertaSelectResponse> listarOfertasPorTipoEEntidade(String tipoOferta, Integer entidadeId) {
        String tipo = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                tipoOferta,
                "Selecione o tipo de oferta."
        );
        if (entidadeId == null || entidadeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a entidade selecionada. Selecione uma entidade e tente novamente.");
        }
        return colocacaoRepository.listarOfertasPorTipoEEntidade(tipo, entidadeId).stream()
                .map(item -> new ColocacaoOfertaSelectResponse(
                        item.ofertaId(),
                        item.codigoReferencia(),
                        item.titulo(),
                        item.oferta(),
                        valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                        empregoDominioService.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                        item.entidadeId(),
                        item.denominacaoEntidade()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColocacaoCandidatoSelectResponse> listarCandidatos(Integer entidadeId, Integer ofertaId) {
        if (entidadeId == null || entidadeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma entidade.");
        }
        if (ofertaId != null && ofertaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O parâmetro \"ofertaId\" deve conter um identificador positivo. Valor recebido: \""
                            + ofertaId + "\"."
            );
        }
        return colocacaoRepository.listarCandidatos(entidadeId, ofertaId);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse criar(Integer entidadeId, ColocacaoCandidatoRequest request) {
        return criar(entidadeId, request, null);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse criar(
            Integer entidadeId,
            ColocacaoCandidatoRequest request,
            MultipartFile contrato
    ) {
        validarEntidade(entidadeId);
        ColocacaoCandidatoRequest dados = validarENormalizarRequest(request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        ColocacaoCandidatoVinculo vinculo = resolverVinculoObrigatorio(entidadeId, dados);
        validarTipoOferta(dados.tipoOferta(), vinculo.tipoOferta());

        Integer id = colocacaoRepository.inserir(dados, vinculo, ESTADO_ATIVO, false, utilizador);
        if (temFicheiro(contrato)) {
            colocacaoRepository.atualizarContratoPath(
                    id,
                    entidadeId,
                    guardarContrato(id, contrato),
                    utilizador
            );
        }
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse atualizar(
            Integer id,
            Integer entidadeId,
            ColocacaoCandidatoRequest request
    ) {
        return atualizar(id, entidadeId, request, null);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse atualizar(
            Integer id,
            Integer entidadeId,
            ColocacaoCandidatoRequest request,
            MultipartFile contrato
    ) {
        validarId(id);
        validarEntidade(entidadeId);
        ColocacaoCandidatoResponse atual = buscarPorId(id, entidadeId);
        garantirAtivo(atual);

        ColocacaoCandidatoRequest dados = validarENormalizarRequest(request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        ColocacaoCandidatoVinculo vinculo = resolverVinculoObrigatorio(entidadeId, dados);
        validarTipoOferta(dados.tipoOferta(), vinculo.tipoOferta());

        String contratoPath = temFicheiro(contrato)
                ? guardarContrato(id, contrato)
                : primeiroTexto(dados.contratoPath(), atual.contratoPath());
        dados = comContrato(dados, contratoPath);

        colocacaoRepository.atualizar(id, entidadeId, dados, vinculo, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public ColocacaoCandidatoResponse remover(
            Integer id,
            Integer entidadeId,
            ColocacaoCandidatoRemoverRequest request
    ) {
        validarId(id);
        validarEntidade(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível confirmar a eliminação da colocação. Tente novamente.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        buscarPorId(id, entidadeId);
        colocacaoRepository.remover(id, entidadeId, ESTADO_INATIVO, utilizador);
        return buscarPorId(id, entidadeId);
    }

    private ColocacaoCandidatoFiltro normalizarFiltro(ColocacaoCandidatoFiltro filtro) {
        if (filtro == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Os filtros da pesquisa de colocações não foram enviados.");
        }
        validarEntidade(filtro.entidadeId());
        return new ColocacaoCandidatoFiltro(
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_TIPO_OFERTA, filtro.tipoOferta()),
                texto(filtro.codigoReferencia()),
                filtro.pessoaId(),
                texto(filtro.candidato()),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_REGIME_CONTRATO, filtro.tipoContrato()),
                filtro.dataInicioPrevisto(),
                filtro.dataRegistoInicio(),
                filtro.dataRegistoFim(),
                filtro.entidadeId(),
                texto(filtro.search())
        );
    }

    private ColocacaoCandidatoRequest validarENormalizarRequest(ColocacaoCandidatoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha os dados da colocação antes de gravar.");
        }
        String tipoOferta = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_TIPO_OFERTA,
                request.tipoOferta(),
                "Selecione o tipo de oferta."
        );
        if (request.ofertaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma oferta.");
        }
        String codigoReferencia = texto(request.codigoReferencia());
        if (request.pessoaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o candidato.");
        }
        String tipoContrato = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_REGIME_CONTRATO,
                request.tipoContrato(),
                "Selecione o tipo de contrato."
        );
        if (request.dataInicioPrevisto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data prevista para o início.");
        }
        utilizadorObrigatorio(request.utilizador());
        validarNumeros(request.duracaoContrato());
        validarDatas(request.dataInicioPrevisto(), request.dataFimPrevisto());

        return new ColocacaoCandidatoRequest(
                tipoOferta,
                request.ofertaId(),
                codigoReferencia,
                request.pessoaId(),
                tipoContrato,
                request.duracaoContrato(),
                request.dataInicioPrevisto(),
                request.dataFimPrevisto(),
                texto(request.descricao()),
                normalizarReferenciaDocumento(request.contratoPath()),
                texto(request.utilizador())
        );
    }

    private ColocacaoCandidatoVinculo resolverVinculoObrigatorio(
            Integer entidadeId,
            ColocacaoCandidatoRequest request
    ) {
        return colocacaoRepository.buscarVinculoCandidatura(
                        request.ofertaId(),
                        request.pessoaId(),
                        entidadeId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi encontrada uma candidatura para o candidato e a oferta selecionados. "
                                + "Confirme os dados e tente novamente."
                ));
    }

    private void validarTipoOferta(String tipoOfertaRequest, String tipoOfertaOferta) {
        String tipoOferta = valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, tipoOfertaOferta);
        if (!String.valueOf(tipoOfertaRequest).equals(tipoOferta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tipo de oferta selecionado não corresponde à oferta escolhida."
            );
        }
    }

    private void garantirAtivo(ColocacaoCandidatoResponse colocacao) {
        String estado = valorDominio(EmpregoDominio.DOMINIO_ESTADO, colocacao.estado());
        if (ESTADO_INATIVO.equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta colocação já foi eliminada e não pode ser alterada.");
        }
    }

    private void validarNumeros(Integer duracaoContrato) {
        if (duracaoContrato != null && duracaoContrato < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A duração do contrato deve ser igual ou superior a zero.");
        }
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data prevista para o fim não pode ser anterior à data de início."
            );
        }
    }

    private ColocacaoCandidatoListaResponse enriquecerLista(ColocacaoCandidatoListaResponse item) {
        return new ColocacaoCandidatoListaResponse(
                item.id(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                empregoDominioService.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                item.codigoReferencia(),
                item.pessoaId(),
                item.nomeCandidato(),
                valorDominio(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                empregoDominioService.descricao(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                item.dataInicioPrevisto(),
                item.dataRegisto()
        );
    }

    private ColocacaoCandidatoResponse enriquecerDetalhe(ColocacaoCandidatoResponse item) {
        return new ColocacaoCandidatoResponse(
                item.id(),
                item.idOferta(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                empregoDominioService.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                item.codigoReferencia(),
                item.entidadeId(),
                item.denominacaoEntidade(),
                item.pessoaId(),
                item.nomeCandidato(),
                item.idCandidatura(),
                valorDominio(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                empregoDominioService.descricao(EmpregoDominio.DOMINIO_REGIME_CONTRATO, item.tipoContrato()),
                item.duracaoContrato(),
                item.dataInicioPrevisto(),
                item.dataFimPrevisto(),
                item.descricao(),
                linkDocumentoParaResposta(item.contratoPath()),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                empregoDominioService.descricao(EmpregoDominio.DOMINIO_ESTADO, item.estado()),
                item.registadoCefp(),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a colocação selecionada. Atualize a página e tente novamente.");
        }
    }

    private String normalizarReferenciaDocumento(String referencia) {
        String valor = texto(referencia);
        if (valor == null) {
            return null;
        }
        if (valor.regionMatches(true, 0, "data:", 0, 5)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O campo \"contratoPath\" não aceita ficheiros em Base64. Envie uma referência documental válida."
            );
        }
        return valor;
    }

    private String linkDocumentoParaResposta(String referencia) {
        String valor = normalizarReferenciaDocumento(referencia);
        if (valor == null) {
            return null;
        }
        return texto(documentService.gerarLinkPublico(valor));
    }

    private String guardarContrato(Integer colocacaoId, MultipartFile ficheiro) {
        String nomeOriginal = StringUtils.cleanPath(
                Optional.ofNullable(ficheiro.getOriginalFilename()).orElse("contrato")
        );
        String extensao = extensao(nomeOriginal);
        String nomeArmazenamento = "CONTRATO-"
                + LocalDateTime.now().format(SUFIXO_DOCUMENTO);
        String path = appCodeDocumento
                + "/" + LocalDateTime.now().getYear()
                + "/modulos/" + sanitizarSegmentoPath(tipoRelacaoDocumento)
                + "/" + colocacaoId
                + "/" + nomeArmazenamento + extensao;
        try {
            String pathGuardado = documentService.save(DocRelacaoDTO.builder()
                    .idRelacao(colocacaoId)
                    .tipoRelacao(tipoRelacaoDocumento)
                    .estado(estadoDocumento)
                    .name(nomeOriginal)
                    .fileName(nomeArmazenamento)
                    .path(path)
                    .appCode(appCodeDocumento)
                    .file(ficheiro)
                    .build());
            String link = documentService.gerarLinkPublico(pathGuardado);
            if (!temTexto(link)) {
                throw new IllegalStateException("O serviço documental devolveu um link inválido.");
            }
            return link;
        } catch (RuntimeException ex) {
            log.error(
                    "Falha ao guardar contrato da colocação: colocacaoId={}, ficheiro={}",
                    colocacaoId,
                    nomeOriginal,
                    ex
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível guardar o contrato da colocação. Tente novamente mais tarde.",
                    ex
            );
        }
    }

    private ColocacaoCandidatoRequest comContrato(ColocacaoCandidatoRequest request, String contratoPath) {
        return new ColocacaoCandidatoRequest(
                request.tipoOferta(),
                request.ofertaId(),
                request.codigoReferencia(),
                request.pessoaId(),
                request.tipoContrato(),
                request.duracaoContrato(),
                request.dataInicioPrevisto(),
                request.dataFimPrevisto(),
                request.descricao(),
                contratoPath,
                request.utilizador()
        );
    }

    private String primeiroTexto(String... valores) {
        for (String valor : valores) {
            String texto = texto(valor);
            if (texto != null) {
                return texto;
            }
        }
        return null;
    }

    private boolean temFicheiro(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private String extensao(String nome) {
        int indice = nome.lastIndexOf('.');
        return indice < 0 || indice == nome.length() - 1 ? "" : nome.substring(indice);
    }

    private String sanitizarSegmentoPath(String valor) {
        String limpo = texto(valor);
        return limpo == null
                ? "documento"
                : limpo.replaceAll("[\\\\/:*?\"<>|]+", "_").replace(' ', '_');
    }

    private void validarEntidade(Integer entidadeId) {
        if (entidadeId == null || entidadeId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O campo \"entidadeId\" deve conter um identificador positivo. Valor recebido: \""
                            + entidadeId + "\"."
            );
        }
    }

    private String utilizadorObrigatorio(String utilizador) {
        return textoObrigatorio(utilizador,
                "Não foi possível identificar o utilizador. Inicie sessão novamente e repita a operação.");
    }

    private String textoObrigatorio(String valor, String mensagem) {
        String texto = texto(valor);
        if (texto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return texto;
    }

    private String normalizarDominioObrigatorio(String dominio, String valor, String mensagem) {
        String normalizado = normalizarDominioOpcional(dominio, valor);
        if (normalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return normalizado;
    }

    private String normalizarDominioOpcional(String dominio, String valor) {
        String texto = texto(valor);
        if (texto == null) {
            return null;
        }
        return empregoDominioService.valorOficial(dominio, texto)
                .or(() -> empregoDominioService.listarPorDominio(dominio).stream()
                        .filter(item -> EmpregoDominio.normalizar(item.description())
                                .equals(EmpregoDominio.normalizar(texto)))
                        .map(item -> item.valor())
                        .findFirst())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        empregoDominioService.mensagemValorInvalido(dominio, texto)
                ));
    }

    private String valorDominio(String dominio, String valor) {
        return empregoDominioService.valorOficial(dominio, valor).orElse(valor);
    }

    private String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
