package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GestaoCandidaturaServiceImpl implements GestaoCandidaturaService {

    private static final String STATUS_TRIAGEM = "TRIAGEM";
    private static final String STATUS_APROVADO = "APROVADO";
    private static final String ESTADO_ENTREVISTA_PENDENTE = "PENDENTE";
    private static final String ESTADO_ENTREVISTA_REALIZADO = "REALIZADO";

    private final GestaoCandidaturaRepository candidaturaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CandidaturaListaResponse> listar(CandidaturaFiltro filtro) {
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
        if (candidaturaRepository.existeEntrevista(candidaturaId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta candidatura já possui uma entrevista. Consulte a entrevista existente."
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
        Integer entrevistaId;
        try {
            entrevistaId = candidaturaRepository.inserirEntrevista(
                    candidaturaId,
                    candidatura,
                    dados,
                    ESTADO_ENTREVISTA_PENDENTE
            );
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta candidatura já possui uma entrevista. Consulte a entrevista existente.",
                    ex
            );
        }
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
        EntrevistaResponse entrevista = buscarEntrevistaObrigatoria(candidaturaId, entrevistaId);
        if (entrevista.estado() != null && !ESTADO_ENTREVISTA_PENDENTE.equals(entrevista.estado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O resultado desta entrevista já foi registado. Consulte os dados da entrevista."
            );
        }

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
                filtro.candidatoId(),
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
        return new CandidaturaListaResponse(
                item.id(),
                item.pessoaId(),
                item.nomeCandidato(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipoOferta()),
                item.ofertaId(),
                item.tituloOferta(),
                valorDominio(EmpregoDominio.DOMINIO_CANAL_OFERTA, item.canal()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_CANAL_OFERTA, item.canal()),
                valorDominio(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, item.statusCandidatura()),
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_STATUS_CANDIDATURA, item.statusCandidatura()),
                item.selecaoIefp(),
                Boolean.TRUE.equals(item.selecaoIefp()),
                podeAgendarEntrevista(item.statusCandidatura()) && item.entrevistaId() == null,
                item.entrevistaId(),
                Boolean.TRUE.equals(item.podeRegistarResultadoEntrevista()),
                item.dataCandidatura()
        );
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

    private boolean temTexto(String valor) {
        return texto(valor) != null;
    }
}
