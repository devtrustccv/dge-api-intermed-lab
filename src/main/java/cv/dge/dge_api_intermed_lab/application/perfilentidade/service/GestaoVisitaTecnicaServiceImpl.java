package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaAtualizacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaAvaliacaoItemRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCefpSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaExecutadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaObservacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoVisitaTecnicaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GestaoVisitaTecnicaServiceImpl implements GestaoVisitaTecnicaService {

    private static final String AGENDADO_POR_CEFP = "CEFP";
    private static final String AGENDADO_POR_ENTIDADE_ACOLHEDORA = "ENTIDADE_ACOLHEDORA";
    private static final String ESTADO_AGENDADO = "AGENDADO";
    private static final String ESTADO_INDEFERIDO = "INDEFERIDO";
    private static final String ESTADO_PENDENTE = "PENDENTE";
    private static final String ESTADO_REALIZADO = "REALIZADO";
    private static final String PARECER_DEFERIR = "DEFERIR";
    private static final String PARECER_INDEFERIR = "INDEFERIR";
    private static final String PARECER_PROPOSTA_NOVA_DATA = "PROPOSTA_NOVA_DATA";

    private final GestaoVisitaTecnicaRepository visitaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VisitaTecnicaListaResponse> listar(VisitaTecnicaFiltro filtro) {
        return visitaRepository.listar(normalizarFiltro(filtro)).stream()
                .map(this::enriquecerLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VisitaTecnicaDetalheResponse buscarPorId(Integer id, Integer entidadeId) {
        validarId(id);
        validarEntidadeId(entidadeId);
        return visitaRepository.buscarPorId(id, entidadeId)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A visita técnica selecionada não foi encontrada. Atualize a página e tente novamente."));
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse criar(Integer entidadeId, VisitaTecnicaRequest request) {
        validarEntidadeId(entidadeId);
        VisitaTecnicaRequest dados = validarENormalizarCriacao(entidadeId, request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        Integer id = visitaRepository.inserir(
                entidadeId,
                dados,
                ESTADO_PENDENTE,
                AGENDADO_POR_ENTIDADE_ACOLHEDORA,
                utilizador
        );
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse atualizar(
            Integer id,
            Integer entidadeId,
            VisitaTecnicaAtualizacaoRequest request
    ) {
        validarId(id);
        validarEntidadeId(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha os dados da visita técnica antes de gravar.");
        }
        buscarPorId(id, entidadeId);
        VisitaTecnicaAtualizacaoRequest dados = validarENormalizarAtualizacao(entidadeId, request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        visitaRepository.atualizar(id, entidadeId, dados, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse validar(
            Integer id,
            Integer entidadeId,
            VisitaTecnicaValidacaoRequest request
    ) {
        validarId(id);
        validarEntidadeId(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecione um parecer antes de confirmar a validação.");
        }

        VisitaTecnicaDetalheResponse atual = buscarPorId(id, entidadeId);
        String estadoAtual = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, atual.estado());
        String agendadoPor = valorDominio(EmpregoDominio.DOMINIO_AGENDADO_POR, atual.agendadoPor());
        if (ESTADO_REALIZADO.equals(estadoAtual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta visita já foi realizada e não pode ser validada novamente.");
        }
        if (!AGENDADO_POR_CEFP.equals(agendadoPor)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta visita não pode ser validada porque não foi agendada pelo Centro de Emprego e Formação Profissional."
            );
        }

        String parecer = normalizarParecerObrigatorio(request.parecer());
        String utilizador = utilizadorObrigatorio(request.utilizador());
        String novoEstado = estadoPorParecer(parecer);
        LocalDateTime novaData = null;
        String motivoIndeferimento = null;

        if (PARECER_INDEFERIR.equals(parecer)) {
            motivoIndeferimento = textoObrigatorio(
                    request.motivoIndeferimento(),
                    "Informe o motivo do indeferimento da visita."
            );
        }
        if (PARECER_PROPOSTA_NOVA_DATA.equals(parecer)) {
            if (request.novaData() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Informe a nova data e hora propostas para a visita."
                );
            }
            novaData = request.novaData();
        }

        visitaRepository.validar(id, entidadeId, novoEstado, novaData, motivoIndeferimento, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse marcarComoExecutado(
            Integer id,
            Integer entidadeId,
            VisitaTecnicaExecutadoRequest request
    ) {
        validarId(id);
        validarEntidadeId(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível confirmar a realização da visita. Tente novamente.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        VisitaTecnicaDetalheResponse atual = buscarPorId(id, entidadeId);
        String estadoAtual = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, atual.estado());
        if (ESTADO_REALIZADO.equals(estadoAtual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta visita já está marcada como realizada.");
        }
        visitaRepository.alterarEstado(id, entidadeId, ESTADO_REALIZADO, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse registarObservacoes(
            Integer id,
            Integer entidadeId,
            VisitaTecnicaObservacaoRequest request
    ) {
        validarId(id);
        validarEntidadeId(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha as observações antes de gravar.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        VisitaTecnicaDetalheResponse atual = buscarPorId(id, entidadeId);
        String estadoAtual = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, atual.estado());
        if (!ESTADO_REALIZADO.equals(estadoAtual)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "As observações só podem ser registadas depois de a visita ser marcada como realizada."
            );
        }
        visitaRepository.registarObservacoes(
                id,
                entidadeId,
                texto(request.observacoesEntidade()),
                texto(request.supervisorParticipante()),
                utilizador
        );
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitaTecnicaCandidatoSelectResponse> listarCandidatos(Integer entidadeId) {
        validarEntidadeId(entidadeId);
        return visitaRepository.listarCandidatos(entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitaTecnicaCefpSelectResponse> listarCefps(Integer entidadeId) {
        validarEntidadeId(entidadeId);
        return visitaRepository.listarCefps();
    }

    private VisitaTecnicaFiltro normalizarFiltro(VisitaTecnicaFiltro filtro) {
        if (filtro == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Os filtros da pesquisa de visitas técnicas não foram enviados.");
        }
        validarEntidadeId(filtro.entidadeId());
        if (filtro.dataInicio() != null && filtro.dataFim() != null && filtro.dataFim().isBefore(filtro.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data final da pesquisa não pode ser anterior à data inicial.");
        }
        return new VisitaTecnicaFiltro(
                filtro.entidadeId(),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, filtro.estado()),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_AGENDADO_POR, filtro.agendadoPor()),
                filtro.cefpId(),
                texto(filtro.cefp()),
                filtro.dataVisita(),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private VisitaTecnicaRequest validarENormalizarCriacao(
            Integer entidadeId,
            VisitaTecnicaRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha os dados da visita técnica antes de gravar.");
        }
        if (request.dataVisita() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data da visita.");
        }
        textoObrigatorio(request.visitante(), "Informe o nome do visitante.");
        if (request.horaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a hora de início da visita.");
        }
        if (request.horaFim() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a hora de fim da visita.");
        }
        validarIntervaloHoras(request.horaInicio(), request.horaFim());
        textoObrigatorio(request.objetivos(), "Informe os objetivos da visita.");
        if (request.cefpId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecione o Centro de Emprego e Formação Profissional responsável.");
        }
        List<VisitaTecnicaCandidatoRequest> candidatos = normalizarCandidatosObrigatorios(
                entidadeId,
                request.candidatos()
        );

        String cefp = textoOuPadrao(
                request.cefp(),
                visitaRepository.buscarCefpDenominacao(request.cefpId()).orElse(null)
        );
        return new VisitaTecnicaRequest(
                request.dataVisita(),
                texto(request.visitante()),
                candidatos,
                request.horaInicio(),
                request.horaFim(),
                texto(request.objetivos()),
                request.cefpId(),
                cefp,
                texto(request.utilizador())
        );
    }

    private VisitaTecnicaAtualizacaoRequest validarENormalizarAtualizacao(
            Integer entidadeId,
            VisitaTecnicaAtualizacaoRequest request
    ) {
        validarIntervaloHoras(request.horaInicio(), request.horaFim());
        return new VisitaTecnicaAtualizacaoRequest(
                request.dataVisita(),
                texto(request.visitante()),
                request.horaInicio(),
                request.horaFim(),
                texto(request.objetivos()),
                normalizarCandidatosAssociados(entidadeId, request.candidatos(), false),
                texto(request.observacoesEntidade()),
                texto(request.supervisorParticipante()),
                texto(request.observacoesIefp()),
                normalizarDetalhesAvaliacao(entidadeId, request.detalhesAvaliacao()),
                texto(request.utilizador())
        );
    }

    private List<VisitaTecnicaCandidatoRequest> normalizarCandidatosObrigatorios(
            Integer entidadeId,
            List<VisitaTecnicaCandidatoRequest> candidatos
    ) {
        return normalizarCandidatosAssociados(entidadeId, candidatos, true);
    }

    private List<VisitaTecnicaCandidatoRequest> normalizarCandidatosAssociados(
            Integer entidadeId,
            List<VisitaTecnicaCandidatoRequest> candidatos,
            boolean obrigatorios
    ) {
        List<VisitaTecnicaCandidatoRequest> normalizados = normalizarCandidatosOpcionais(candidatos);
        if (obrigatorios && normalizados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione pelo menos um candidato.");
        }
        if (normalizados.isEmpty()) {
            return normalizados;
        }

        Map<Long, VisitaTecnicaCandidatoSelectResponse> candidatosDisponiveis = visitaRepository.listarCandidatos(entidadeId)
                .stream()
                .collect(Collectors.toMap(
                        VisitaTecnicaCandidatoSelectResponse::pessoaId,
                        Function.identity(),
                        (atual, ignorado) -> atual
                ));
        for (VisitaTecnicaCandidatoRequest candidato : normalizados) {
            VisitaTecnicaCandidatoSelectResponse disponivel = candidatosDisponiveis.get(candidato.pessoaId());
            if (disponivel == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O candidato com o identificador \"" + candidato.pessoaId()
                                + "\" não está associado à entidade ou não está disponível para a visita técnica."
                );
            }
        }
        return normalizados.stream()
                .map(candidato -> {
                    String nome = textoOuPadrao(
                            candidato.nome(),
                            candidatosDisponiveis.get(candidato.pessoaId()).nome()
                    );
                    return new VisitaTecnicaCandidatoRequest(candidato.pessoaId(), nome);
                })
                .toList();
    }

    private List<VisitaTecnicaCandidatoRequest> normalizarCandidatosOpcionais(
            List<VisitaTecnicaCandidatoRequest> candidatos
    ) {
        if (candidatos == null) {
            return Collections.emptyList();
        }
        List<VisitaTecnicaCandidatoRequest> normalizados = new ArrayList<>();
        for (int indice = 0; indice < candidatos.size(); indice++) {
            VisitaTecnicaCandidatoRequest candidato = candidatos.get(indice);
            if (candidato == null || candidato.pessoaId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O candidato na posição " + (indice + 1)
                                + " está incompleto: o campo \"pessoaId\" é obrigatório."
                );
            }
            normalizados.add(new VisitaTecnicaCandidatoRequest(
                    candidato.pessoaId(),
                    texto(candidato.nome())
            ));
        }
        return List.copyOf(normalizados);
    }

    private List<VisitaTecnicaAvaliacaoItemRequest> normalizarDetalhesAvaliacao(
            Integer entidadeId,
            List<VisitaTecnicaAvaliacaoItemRequest> detalhes
    ) {
        if (detalhes == null) {
            return Collections.emptyList();
        }
        List<VisitaTecnicaAvaliacaoItemRequest> normalizados = new ArrayList<>();
        for (int indice = 0; indice < detalhes.size(); indice++) {
            VisitaTecnicaAvaliacaoItemRequest item = detalhes.get(indice);
            if (item == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A avaliação na posição " + (indice + 1)
                                + " está vazia. Informe o critério e a classificação."
                );
            }
            normalizados.add(new VisitaTecnicaAvaliacaoItemRequest(
                    normalizarCandidatosAssociados(entidadeId, item.candidatos(), false),
                    normalizarDominioObrigatorio(
                            EmpregoDominio.DOMINIO_CRITERIO_AVALIACAO,
                            item.criterio(),
                            "Selecione o critério da avaliação."
                    ),
                    normalizarDominioObrigatorio(
                            EmpregoDominio.DOMINIO_AVALIACAO,
                            item.avaliacao(),
                            "Selecione a classificação da avaliação."
                    ),
                    texto(item.observacao())
            ));
        }
        return List.copyOf(normalizados);
    }

    private VisitaTecnicaListaResponse enriquecerLista(VisitaTecnicaListaResponse item) {
        String estado = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, item.estado());
        String agendadoPor = valorDominio(EmpregoDominio.DOMINIO_AGENDADO_POR, item.agendadoPor());
        return new VisitaTecnicaListaResponse(
                item.id(),
                item.entidadeId(),
                item.dataVisita(),
                item.horaInicio(),
                item.horaFim(),
                formatarHorario(item.horaInicio(), item.horaFim()),
                item.visitante(),
                item.objetivos(),
                agendadoPor,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_AGENDADO_POR, agendadoPor),
                item.cefpId(),
                item.cefp(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, estado),
                item.candidatos(),
                item.novaData(),
                item.motivoIndeferimento(),
                item.observacoesEntidade(),
                item.supervisorParticipante(),
                item.observacoesIefp(),
                item.detalhesAvaliacao(),
                item.conteudoReuniao(),
                podeValidar(estado, agendadoPor),
                !ESTADO_REALIZADO.equals(estado),
                ESTADO_REALIZADO.equals(estado),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
    }

    private VisitaTecnicaDetalheResponse enriquecerDetalhe(VisitaTecnicaDetalheResponse item) {
        String estado = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, item.estado());
        String agendadoPor = valorDominio(EmpregoDominio.DOMINIO_AGENDADO_POR, item.agendadoPor());
        return new VisitaTecnicaDetalheResponse(
                item.id(),
                item.entidadeId(),
                item.dataVisita(),
                item.visitante(),
                item.horaInicio(),
                item.horaFim(),
                formatarHorario(item.horaInicio(), item.horaFim()),
                item.objetivos(),
                agendadoPor,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_AGENDADO_POR, agendadoPor),
                item.cefpId(),
                item.cefp(),
                estado,
                EmpregoDominio.descricao(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, estado),
                item.candidatos(),
                item.novaData(),
                item.motivoIndeferimento(),
                item.observacoesEntidade(),
                item.supervisorParticipante(),
                item.observacoesIefp(),
                item.detalhesAvaliacao(),
                item.conteudoReuniao(),
                podeValidar(estado, agendadoPor),
                !ESTADO_REALIZADO.equals(estado),
                ESTADO_REALIZADO.equals(estado),
                item.dateCreate(),
                item.userCreate(),
                item.dateUpdate(),
                item.userUpdate()
        );
    }

    private boolean podeValidar(String estado, String agendadoPor) {
        return !ESTADO_REALIZADO.equals(estado) && AGENDADO_POR_CEFP.equals(agendadoPor);
    }

    private String estadoPorParecer(String parecer) {
        if (PARECER_DEFERIR.equals(parecer)) {
            return ESTADO_AGENDADO;
        }
        if (PARECER_INDEFERIR.equals(parecer)) {
            return ESTADO_INDEFERIDO;
        }
        if (PARECER_PROPOSTA_NOVA_DATA.equals(parecer)) {
            return ESTADO_PENDENTE;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                EmpregoDominio.mensagemValorInvalido(
                        EmpregoDominio.DOMINIO_PARECER_VISITA,
                        parecer
                ));
    }

    private String normalizarParecerObrigatorio(String parecer) {
        return normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_PARECER_VISITA,
                parecer,
                "Selecione o parecer da visita."
        );
    }

    private String normalizarDominioObrigatorio(String dominio, String valor, String mensagemObrigatorio) {
        String texto = texto(valor);
        if (texto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemObrigatorio);
        }
        return EmpregoDominio.valorOficial(dominio, texto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        EmpregoDominio.mensagemValorInvalido(dominio, texto)
                ));
    }

    private String normalizarDominioOpcional(String dominio, String valor) {
        String texto = texto(valor);
        if (texto == null) {
            return null;
        }
        return EmpregoDominio.valorOficial(dominio, texto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        EmpregoDominio.mensagemValorInvalido(dominio, texto)
                ));
    }

    private String valorDominio(String dominio, String valor) {
        return EmpregoDominio.valorOficial(dominio, valor).orElse(valor);
    }

    private void validarIntervaloHoras(LocalTime horaInicio, LocalTime horaFim) {
        if (horaInicio != null && horaFim != null && horaFim.isBefore(horaInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A hora de fim não pode ser anterior à hora de início da visita.");
        }
    }

    private String formatarHorario(LocalTime horaInicio, LocalTime horaFim) {
        if (horaInicio == null && horaFim == null) {
            return null;
        }
        if (horaInicio == null) {
            return horaFim.toString();
        }
        if (horaFim == null) {
            return horaInicio.toString();
        }
        return horaInicio + " - " + horaFim;
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a visita selecionada. Atualize a página e tente novamente.");
        }
    }

    private void validarEntidadeId(Integer entidadeId) {
        if (entidadeId == null || entidadeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a entidade selecionada. Selecione uma entidade e tente novamente.");
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

    private String textoOuPadrao(String valor, String padrao) {
        String texto = texto(valor);
        return texto == null ? texto(padrao) : texto;
    }

    private String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }
}
