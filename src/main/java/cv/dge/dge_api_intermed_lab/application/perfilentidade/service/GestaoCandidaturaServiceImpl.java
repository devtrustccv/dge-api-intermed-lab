package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaDocumentoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaAvaliacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaAgendamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaResultadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoCandidaturaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestaoCandidaturaServiceImpl implements GestaoCandidaturaService {

    private static final String STATUS_TRIAGEM = "TRIAGEM";
    private static final String STATUS_APROVADO = "APROVADO";
    private static final String ESTADO_ENTREVISTA_PENDENTE = "PENDENTE";
    private static final String ESTADO_ENTREVISTA_REALIZADO = "REALIZADO";
    private static final String TIPO_DOCUMENTO_CURRICULO = "CURRICULO_VITAE";
    private static final String TIPO_DOCUMENTO_OUTRO = "OUTRO_DOCUMENTO";

    private final GestaoCandidaturaRepository candidaturaRepository;
    private final DocumentService documentService;

    @Value("${document.candidatura.app-code:interm_laboral}")
    private String appCodeDocumento;

    @Value("${document.candidatura.tipo-relacao:EMPREGO_T_CANDIDATURA_OFERTA}")
    private String tipoRelacaoDocumento;

    @Override
    @Transactional(readOnly = true)
    public List<CandidaturaListaResponse> listar(CandidaturaFiltro filtro) {
        validarEntidade(filtro == null ? null : filtro.entidadeId());
        return candidaturaRepository.listar(normalizarFiltro(filtro)).stream()
                .map(this::enriquecerLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CandidaturaDetalheResponse buscarPorId(Integer id) {
        validarId(id, "Não foi possível identificar a candidatura selecionada. Atualize a página e tente novamente.");
        return candidaturaRepository.buscarPorId(id)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A candidatura selecionada não foi encontrada. Atualize a página e tente novamente."));
    }

    @Override
    @Transactional
    public CandidaturaDetalheResponse avaliar(Integer id, CandidaturaAvaliacaoRequest request) {
        validarId(id, "Não foi possível identificar a candidatura selecionada. Atualize a página e tente novamente.");
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecione o resultado da avaliação antes de gravar.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        String parecer = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_STATUS_CANDIDATURA,
                request.parecer(),
                "Selecione o parecer da candidatura."
        );
        if (STATUS_TRIAGEM.equals(parecer)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecione um parecer final para a candidatura."
            );
        }

        CandidaturaDetalheResponse atual = buscarPorId(id);
        if (!Boolean.TRUE.equals(atual.selecaoIefp())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta candidatura ainda não está disponível para avaliação."
            );
        }

        String motivoRecusa = texto(request.motivoRecusa());
        if (isRecusa(parecer) && !temTexto(motivoRecusa)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o motivo da recusa da candidatura."
            );
        }

        candidaturaRepository.atualizarAvaliacao(id, parecer, isRecusa(parecer) ? motivoRecusa : null, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public EntrevistaResponse agendarEntrevista(Integer candidaturaId, EntrevistaAgendamentoRequest request) {
        validarId(candidaturaId,
                "Não foi possível identificar a candidatura selecionada. Atualize a página e tente novamente.");
        validarAgendamento(request);
        CandidaturaDetalheResponse candidatura = buscarPorId(candidaturaId);
        if (!podeAgendarEntrevista(candidatura.statusCandidatura())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A entrevista só pode ser agendada depois de a candidatura ser aprovada."
            );
        }
        EntrevistaAgendamentoRequest dados = new EntrevistaAgendamentoRequest(
                request.dataEntrevista(),
                request.horario(),
                normalizarDominioObrigatorio(
                        EmpregoDominio.DOMINIO_CANAL_ENTREVISTA,
                        request.canal(),
                        "Selecione a modalidade da entrevista."
                ),
                textoObrigatorio(request.localEntrevista(), "Informe o local ou o acesso da entrevista."),
                utilizadorObrigatorio(request.utilizador())
        );
        Integer entrevistaId = candidaturaRepository.inserirEntrevista(
                candidaturaId,
                candidatura,
                dados,
                ESTADO_ENTREVISTA_PENDENTE
        );
        return buscarEntrevistaObrigatoria(candidaturaId, entrevistaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntrevistaResponse> listarEntrevistas(Integer candidaturaId) {
        validarId(candidaturaId,
                "Não foi possível identificar a candidatura selecionada. Atualize a página e tente novamente.");
        buscarPorId(candidaturaId);
        return candidaturaRepository.listarEntrevistas(candidaturaId).stream()
                .map(this::enriquecerEntrevista)
                .toList();
    }

    @Override
    @Transactional
    public EntrevistaResponse registarResultadoEntrevista(
            Integer candidaturaId,
            Integer entrevistaId,
            EntrevistaResultadoRequest request
    ) {
        validarId(candidaturaId,
                "Não foi possível identificar a candidatura selecionada. Atualize a página e tente novamente.");
        validarId(entrevistaId,
                "Não foi possível identificar a entrevista selecionada. Atualize a página e tente novamente.");
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha o resultado da entrevista antes de gravar.");
        }
        String parecer = normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_PARECER_ENTREVISTA,
                request.parecer(),
                "Selecione o parecer da entrevista."
        );
        String utilizador = utilizadorObrigatorio(request.utilizador());
        buscarPorId(candidaturaId);
        buscarEntrevistaObrigatoria(candidaturaId, entrevistaId);

        candidaturaRepository.atualizarResultadoEntrevista(
                candidaturaId,
                entrevistaId,
                parecer,
                texto(request.observacao()),
                ESTADO_ENTREVISTA_REALIZADO,
                utilizador
        );
        return buscarEntrevistaObrigatoria(candidaturaId, entrevistaId);
    }

    private EntrevistaResponse buscarEntrevistaObrigatoria(Integer candidaturaId, Integer entrevistaId) {
        return candidaturaRepository.buscarEntrevista(candidaturaId, entrevistaId)
                .map(this::enriquecerEntrevista)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A entrevista selecionada não foi encontrada. Atualize a página e tente novamente."));
    }

    private CandidaturaFiltro normalizarFiltro(CandidaturaFiltro filtro) {
        return new CandidaturaFiltro(
                filtro.entidadeId(),
                filtro.candidatoId(),
                texto(filtro.candidato()),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, filtro.estado()),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_TIPO_OFERTA, filtro.tipoOferta()),
                filtro.ofertaId(),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_CANAL_OFERTA, filtro.canal()),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private void validarAgendamento(EntrevistaAgendamentoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha os dados do agendamento antes de gravar.");
        }
        if (request.dataEntrevista() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data da entrevista.");
        }
        if (request.horario() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o horário da entrevista.");
        }
        textoObrigatorio(request.localEntrevista(), "Informe o local ou o acesso da entrevista.");
        utilizadorObrigatorio(request.utilizador());
        normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_CANAL_ENTREVISTA,
                request.canal(),
                "Selecione a modalidade da entrevista."
        );
    }

    private CandidaturaListaResponse enriquecerLista(CandidaturaListaResponse item) {
        List<CandidaturaDocumentoResponse> anexos = resolverAnexos(item);
        CandidaturaDocumentoResponse primeiroAnexo = anexos.stream().findFirst().orElse(null);
        String tipoDocumento = temTexto(item.tipoDocumento())
                ? item.tipoDocumento()
                : primeiroAnexo == null ? null : primeiroAnexo.tipo();

        return new CandidaturaListaResponse(
                item.id(),
                item.pessoaId(),
                item.nomeCandidato(),
                item.dataNascCandidato(),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_SEXO, item.sexoCandidato()),
                item.emailCandidato(),
                item.telefoneCandidato(),
                item.ilhaConcelhoCandidato(),
                item.moradaCandidato(),
                item.habilitacaoLiterariaCandidato(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                item.ofertaId(),
                item.codigoOferta(),
                item.tituloOferta(),
                valorDominio(EmpregoDominio.DOMINIO_CANAL_OFERTA, item.canal()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_CANAL_OFERTA, item.canal()),
                tipoDocumento,
                primeiroAnexo,
                anexos,
                valorDominio(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, item.statusCandidatura()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, item.statusCandidatura()),
                item.motivoRecusa(),
                item.selecaoIefp(),
                Boolean.TRUE.equals(item.selecaoIefp()),
                podeAgendarEntrevista(item.statusCandidatura()) && item.entrevistaId() == null,
                item.entrevistaId(),
                Boolean.TRUE.equals(item.podeRegistarResultadoEntrevista()),
                item.dataCandidatura()
        );
    }

    private List<CandidaturaDocumentoResponse> resolverAnexos(CandidaturaListaResponse item) {
        List<CandidaturaDocumentoResponse> documentos = new ArrayList<>();
        Set<String> identidades = new LinkedHashSet<>();
        adicionarAnexos(item.anexo(), null, documentos, identidades);

        if (documentos.isEmpty()) {
            adicionarAnexosDaRelacao(item.id(), documentos, identidades);
        }
        return List.copyOf(documentos);
    }

    private void adicionarAnexos(
            Object valor,
            String tipoPadrao,
            List<CandidaturaDocumentoResponse> documentos,
            Set<String> identidades
    ) {
        if (valor == null) {
            return;
        }
        if (valor instanceof Collection<?> valores) {
            valores.forEach(item -> adicionarAnexos(item, tipoPadrao, documentos, identidades));
            return;
        }
        if (valor instanceof Map<?, ?> mapa) {
            CandidaturaDocumentoResponse documento = converterDocumento(mapa, tipoPadrao);
            if (documento != null) {
                adicionarSemDuplicar(documento, documentos, identidades);
                return;
            }

            Object curriculo = primeiroValor(mapa, "curriculumVitae", "curriculoVitae", "curriculo", "cv");
            Object outros = primeiroValor(mapa, "outrosDocumentos", "documentos", "outros", "anexos");
            adicionarAnexos(curriculo, TIPO_DOCUMENTO_CURRICULO, documentos, identidades);
            adicionarAnexos(outros, TIPO_DOCUMENTO_OUTRO, documentos, identidades);
            if (curriculo == null && outros == null) {
                mapa.values().forEach(item -> adicionarAnexos(item, tipoPadrao, documentos, identidades));
            }
            return;
        }
        if (valor instanceof CandidaturaDocumentoResponse documento) {
            adicionarSemDuplicar(normalizarDocumento(documento), documentos, identidades);
            return;
        }

        String path = texto(valor);
        if (path != null) {
            adicionarSemDuplicar(new CandidaturaDocumentoResponse(
                    tipoPadrao,
                    nomeDoPath(path),
                    path,
                    documentService.gerarLinkPublico(path)
            ), documentos, identidades);
        }
    }

    private CandidaturaDocumentoResponse converterDocumento(Map<?, ?> mapa, String tipoPadrao) {
        String path = texto(primeiroValor(mapa, "path", "caminho", "anexo"));
        String url = texto(primeiroValor(mapa, "url", "previewUrl", "ver_documento"));
        if (!temTexto(path) && !temTexto(url)) {
            return null;
        }

        String tipo = texto(primeiroValor(mapa, "tipo", "tipoDocumento", "idTpDoc", "id_tp_doc"));
        String nome = texto(primeiroValor(mapa, "nome", "name", "fileName", "file_name", "ficheiro"));
        if (!temTexto(url) && temTexto(path)) {
            url = documentService.gerarLinkPublico(path);
        }
        if (!temTexto(nome)) {
            nome = nomeDoPath(temTexto(path) ? path : url);
        }
        return new CandidaturaDocumentoResponse(
                temTexto(tipo) ? tipo : tipoPadrao,
                nome,
                path,
                url
        );
    }

    private CandidaturaDocumentoResponse normalizarDocumento(CandidaturaDocumentoResponse documento) {
        String url = documento.url();
        if (!temTexto(url) && temTexto(documento.path())) {
            url = documentService.gerarLinkPublico(documento.path());
        }
        String nome = temTexto(documento.nome())
                ? documento.nome()
                : nomeDoPath(temTexto(documento.path()) ? documento.path() : url);
        return new CandidaturaDocumentoResponse(documento.tipo(), nome, documento.path(), url);
    }

    private void adicionarAnexosDaRelacao(
            Integer candidaturaId,
            List<CandidaturaDocumentoResponse> documentos,
            Set<String> identidades
    ) {
        try {
            List<DocumentoResponseDTO> documentosRelacionados = documentService.getDocumentosPorRelacao(
                    candidaturaId,
                    tipoRelacaoDocumento,
                    appCodeDocumento
            );
            if (documentosRelacionados == null) {
                return;
            }
            documentosRelacionados.stream()
                    .map(this::converterDocumentoDaRelacao)
                    .forEach(documento -> adicionarSemDuplicar(documento, documentos, identidades));
        } catch (RuntimeException ex) {
            log.warn("Nao foi possivel consultar os anexos da candidatura {} no SGF.", candidaturaId, ex);
        }
    }

    private CandidaturaDocumentoResponse converterDocumentoDaRelacao(DocumentoResponseDTO documento) {
        String path = texto(documento.getPath());
        String url = texto(documento.getPreviewUrl());
        if (!temTexto(url) && temTexto(path)) {
            url = documentService.gerarLinkPublico(path);
        }
        String nome = primeiroTexto(documento.getName(), documento.getFileName());
        if (!temTexto(nome)) {
            nome = nomeDoPath(temTexto(path) ? path : url);
        }
        return new CandidaturaDocumentoResponse(
                texto(documento.getIdTpDoc()),
                nome,
                path,
                url
        );
    }

    private void adicionarSemDuplicar(
            CandidaturaDocumentoResponse documento,
            List<CandidaturaDocumentoResponse> documentos,
            Set<String> identidades
    ) {
        if (documento == null || (!temTexto(documento.path()) && !temTexto(documento.url()))) {
            return;
        }
        String identidade = primeiroTexto(documento.path(), documento.url(), documento.nome());
        if (identidade == null || identidades.add(identidade)) {
            documentos.add(documento);
        }
    }

    private Object primeiroValor(Map<?, ?> mapa, String... chaves) {
        for (String chave : chaves) {
            if (mapa.containsKey(chave)) {
                return mapa.get(chave);
            }
        }
        return null;
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

    private String nomeDoPath(String valor) {
        String texto = texto(valor);
        if (texto == null) {
            return null;
        }
        int indiceQuery = texto.indexOf('?');
        String semQuery = indiceQuery >= 0 ? texto.substring(0, indiceQuery) : texto;
        int indiceSeparador = Math.max(semQuery.lastIndexOf('/'), semQuery.lastIndexOf('\\'));
        return indiceSeparador >= 0 ? semQuery.substring(indiceSeparador + 1) : semQuery;
    }

    private CandidaturaDetalheResponse enriquecerDetalhe(CandidaturaDetalheResponse item) {
        return new CandidaturaDetalheResponse(
                item.id(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                item.ofertaId(),
                item.codigoOferta(),
                item.tituloOferta(),
                item.entidadeId(),
                item.denominacaoEntidade(),
                item.dataCandidatura(),
                item.candidato(),
                item.anexos(),
                valorDominio(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, item.statusCandidatura()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, item.statusCandidatura()),
                item.motivoRecusa(),
                item.selecaoIefp(),
                Boolean.TRUE.equals(item.selecaoIefp()),
                podeAgendarEntrevista(item.statusCandidatura()),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
    }

    private EntrevistaResponse enriquecerEntrevista(EntrevistaResponse item) {
        return new EntrevistaResponse(
                item.id(),
                item.candidaturaId(),
                item.pessoaId(),
                item.nomeCandidato(),
                item.dataEntrevista(),
                item.horario(),
                valorDominio(EmpregoDominio.DOMINIO_CANAL_ENTREVISTA, item.canal()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_CANAL_ENTREVISTA, item.canal()),
                item.localEntrevista(),
                valorDominio(EmpregoDominio.DOMINIO_PARECER_ENTREVISTA, item.parecer()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_PARECER_ENTREVISTA, item.parecer()),
                item.observacao(),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO_ENTREVISTA, item.estado()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_ENTREVISTA, item.estado()),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
    }

    private boolean podeAgendarEntrevista(String status) {
        String valor = valorDominio(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, status);
        return STATUS_APROVADO.equals(valor);
    }

    private boolean isRecusa(String status) {
        String valor = valorDominio(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, status);
        return "RECUSADO".equals(valor);
    }

    private void validarId(Integer id, String mensagem) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }

    private void validarEntidade(Integer entidadeId) {
        if (entidadeId == null || entidadeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nao foi possivel identificar a entidade selecionada.");
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
        return EmpregoDominio.valorOficial(dominio, texto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Uma das opções selecionadas não é válida. Atualize a página e tente novamente."
                ));
    }

    private String valorDominio(String dominio, String valor) {
        return EmpregoDominio.valorOficial(dominio, valor).orElse(valor);
    }

    private String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }

    private String texto(Object valor) {
        return valor == null ? null : texto(valor.toString());
    }

    private boolean temTexto(String valor) {
        return texto(valor) != null;
    }
}
