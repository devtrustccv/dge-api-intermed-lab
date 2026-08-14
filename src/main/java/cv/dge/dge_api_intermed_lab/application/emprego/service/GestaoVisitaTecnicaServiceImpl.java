package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaAtualizacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaAvaliacaoItemRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaCefpSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaExecutadoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaObservacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository.GestaoVisitaTecnicaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public VisitaTecnicaDetalheResponse buscarPorId(Integer id) {
        validarId(id);
        return visitaRepository.buscarPorId(id)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visita tecnica nao encontrada."));
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse criar(VisitaTecnicaRequest request) {
        VisitaTecnicaRequest dados = validarENormalizarCriacao(request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        Integer id = visitaRepository.inserir(dados, ESTADO_PENDENTE, AGENDADO_POR_ENTIDADE_ACOLHEDORA, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse atualizar(Integer id, VisitaTecnicaAtualizacaoRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da visita tecnica sao obrigatorios.");
        }
        buscarPorId(id);
        VisitaTecnicaAtualizacaoRequest dados = validarENormalizarAtualizacao(request);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        visitaRepository.atualizar(id, dados, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse validar(Integer id, VisitaTecnicaValidacaoRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da validacao sao obrigatorios.");
        }

        VisitaTecnicaDetalheResponse atual = buscarPorId(id);
        String estadoAtual = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, atual.estado());
        String agendadoPor = valorDominio(EmpregoDominio.DOMINIO_AGENDADO_POR, atual.agendadoPor());
        if (ESTADO_REALIZADO.equals(estadoAtual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visita tecnica REALIZADA nao pode ser validada.");
        }
        if (!AGENDADO_POR_CEFP.equals(agendadoPor)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Apenas visitas tecnicas agendadas pelo CEFP podem ser validadas."
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
                    "motivoIndeferimento e obrigatorio quando o parecer for INDEFERIR."
            );
        }
        if (PARECER_PROPOSTA_NOVA_DATA.equals(parecer)) {
            if (request.novaData() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "novaData e obrigatoria quando o parecer for PROPOSTA_NOVA_DATA."
                );
            }
            novaData = request.novaData();
        }

        visitaRepository.validar(id, novoEstado, novaData, motivoIndeferimento, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse marcarComoExecutado(Integer id, VisitaTecnicaExecutadoRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados de execucao sao obrigatorios.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        VisitaTecnicaDetalheResponse atual = buscarPorId(id);
        String estadoAtual = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, atual.estado());
        if (ESTADO_REALIZADO.equals(estadoAtual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visita tecnica ja esta REALIZADA.");
        }
        visitaRepository.alterarEstado(id, ESTADO_REALIZADO, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VisitaTecnicaDetalheResponse registarObservacoes(Integer id, VisitaTecnicaObservacaoRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados de observacao sao obrigatorios.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        VisitaTecnicaDetalheResponse atual = buscarPorId(id);
        String estadoAtual = valorDominio(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, atual.estado());
        if (!ESTADO_REALIZADO.equals(estadoAtual)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Observacoes da entidade so podem ser registadas em visitas REALIZADAS."
            );
        }
        visitaRepository.registarObservacoes(
                id,
                texto(request.observacoesEntidade()),
                texto(request.supervisorParticipante()),
                utilizador
        );
        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitaTecnicaCandidatoSelectResponse> listarCandidatos(Integer entidadeId) {
        validarEntidadeId(entidadeId);
        return visitaRepository.listarCandidatos(entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitaTecnicaCefpSelectResponse> listarCefps() {
        return visitaRepository.listarCefps();
    }

    private VisitaTecnicaFiltro normalizarFiltro(VisitaTecnicaFiltro filtro) {
        if (filtro == null) {
            return new VisitaTecnicaFiltro(null, null, null, null, null, null, null);
        }
        if (filtro.dataInicio() != null && filtro.dataFim() != null && filtro.dataFim().isBefore(filtro.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataFim nao pode ser anterior a dataInicio.");
        }
        return new VisitaTecnicaFiltro(
                filtro.entidadeId(),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_ESTADO_VISITA_TECNICA, filtro.estado()),
                normalizarDominioOpcional(EmpregoDominio.DOMINIO_AGENDADO_POR, filtro.agendadoPor()),
                filtro.cefpId(),
                filtro.dataVisita(),
                filtro.dataInicio(),
                filtro.dataFim()
        );
    }

    private VisitaTecnicaRequest validarENormalizarCriacao(VisitaTecnicaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da visita tecnica sao obrigatorios.");
        }
        validarEntidadeId(request.entidadeId());
        if (request.dataVisita() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataVisita e obrigatoria.");
        }
        textoObrigatorio(request.visitante(), "visitante e obrigatorio.");
        if (request.horaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "horaInicio e obrigatoria.");
        }
        if (request.horaFim() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "horaFim e obrigatoria.");
        }
        validarIntervaloHoras(request.horaInicio(), request.horaFim());
        textoObrigatorio(request.objetivos(), "objetivos e obrigatorio.");
        if (request.cefpId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cefpId e obrigatorio.");
        }
        List<VisitaTecnicaCandidatoRequest> candidatos = normalizarCandidatosObrigatorios(
                request.entidadeId(),
                request.candidatos()
        );

        String cefp = textoOuPadrao(
                request.cefp(),
                visitaRepository.buscarCefpDenominacao(request.cefpId()).orElse(null)
        );
        return new VisitaTecnicaRequest(
                request.entidadeId(),
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

    private VisitaTecnicaAtualizacaoRequest validarENormalizarAtualizacao(VisitaTecnicaAtualizacaoRequest request) {
        validarIntervaloHoras(request.horaInicio(), request.horaFim());
        return new VisitaTecnicaAtualizacaoRequest(
                request.dataVisita(),
                texto(request.visitante()),
                request.horaInicio(),
                request.horaFim(),
                texto(request.objetivos()),
                normalizarCandidatosOpcionais(request.candidatos()),
                texto(request.observacoesEntidade()),
                texto(request.supervisorParticipante()),
                texto(request.observacoesIefp()),
                normalizarDetalhesAvaliacao(request.detalhesAvaliacao()),
                texto(request.utilizador())
        );
    }

    private List<VisitaTecnicaCandidatoRequest> normalizarCandidatosObrigatorios(
            Integer entidadeId,
            List<VisitaTecnicaCandidatoRequest> candidatos
    ) {
        List<VisitaTecnicaCandidatoRequest> normalizados = normalizarCandidatosOpcionais(candidatos);
        if (normalizados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "candidatos e obrigatorio.");
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
                        "Candidato nao encontrado para a entidade informada: " + candidato.pessoaId() + "."
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
        return candidatos.stream()
                .map(candidato -> {
                    if (candidato == null || candidato.pessoaId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pessoaId do candidato e obrigatorio.");
                    }
                    return new VisitaTecnicaCandidatoRequest(candidato.pessoaId(), texto(candidato.nome()));
                })
                .toList();
    }

    private List<VisitaTecnicaAvaliacaoItemRequest> normalizarDetalhesAvaliacao(
            List<VisitaTecnicaAvaliacaoItemRequest> detalhes
    ) {
        if (detalhes == null) {
            return Collections.emptyList();
        }
        return detalhes.stream()
                .map(item -> {
                    if (item == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item de avaliacao invalido.");
                    }
                    return new VisitaTecnicaAvaliacaoItemRequest(
                            normalizarCandidatosOpcionais(item.candidatos()),
                            normalizarDominioObrigatorio(
                                    EmpregoDominio.DOMINIO_CRITERIO_AVALIACAO,
                                    item.criterio(),
                                    "criterio e obrigatorio na avaliacao."
                            ),
                            normalizarDominioObrigatorio(
                                    EmpregoDominio.DOMINIO_AVALIACAO,
                                    item.avaliacao(),
                                    "avaliacao e obrigatoria na avaliacao."
                            ),
                            texto(item.observacao())
                    );
                })
                .toList();
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
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "parecer invalido.");
    }

    private String normalizarParecerObrigatorio(String parecer) {
        return normalizarDominioObrigatorio(
                EmpregoDominio.DOMINIO_PARECER_VISITA,
                parecer,
                "parecer e obrigatorio."
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
                        dominio + " invalido: " + texto + "."
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
                        dominio + " invalido: " + texto + "."
                ));
    }

    private String valorDominio(String dominio, String valor) {
        return EmpregoDominio.valorOficial(dominio, valor).orElse(valor);
    }

    private void validarIntervaloHoras(LocalTime horaInicio, LocalTime horaFim) {
        if (horaInicio != null && horaFim != null && horaFim.isBefore(horaInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "horaFim nao pode ser anterior a horaInicio.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id da visita tecnica e obrigatorio.");
        }
    }

    private void validarEntidadeId(Integer entidadeId) {
        if (entidadeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "entidadeId e obrigatorio.");
        }
    }

    private String utilizadorObrigatorio(String utilizador) {
        return textoObrigatorio(utilizador, "utilizador e obrigatorio para auditoria.");
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
