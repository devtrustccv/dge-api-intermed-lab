package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaDuplicacaoResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaEstadoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository.GestaoVagaRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GestaoVagaServiceImpl implements GestaoVagaService {

    private static final String ESTADO_ATIVA = "ATIVA";
    private static final String ESTADO_RASCUNHO = "RASCUNHO";
    private static final String ESTADO_FECHADA = "FECHADA";
    private static final String ESTADO_EM_APROVACAO = "EM_APROVACAO";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final String ESTADO_INATIVA = "INATIVA";

    private final GestaoVagaRepository vagaRepository;
    private final GlobalGeografiaService globalGeografiaService;

    @Override
    @Transactional(readOnly = true)
    public List<VagaListaResponse> listar(VagaFiltro filtro) {
        return vagaRepository.listar(normalizarFiltro(filtro)).stream()
                .map(this::enriquecerLista)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VagaResponse buscarPorId(Integer id) {
        validarId(id);
        return vagaRepository.buscarPorId(id)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga nao encontrada."));
    }

    @Override
    @Transactional
    public VagaResponse criar(VagaRequest request) {
        validarRequest(request, false);
        String utilizador = utilizadorObrigatorio(request.utilizador());
        Integer id = vagaRepository.inserir(request, ESTADO_ATIVA, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VagaResponse criarRascunho(VagaRequest request) {
        validarRequest(request, true);
        String utilizador = utilizadorObrigatorio(request.utilizador());
        Integer id = vagaRepository.inserir(request, ESTADO_RASCUNHO, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VagaResponse atualizar(Integer id, VagaRequest request) {
        validarId(id);
        validarRequest(request, false);
        VagaResponse atual = buscarPorId(id);
        garantirEditavel(atual);
        String utilizador = utilizadorObrigatorio(request.utilizador());
        vagaRepository.atualizar(id, request, utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VagaResponse alterarEstado(Integer id, VagaEstadoRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da alteracao de estado sao obrigatorios.");
        }
        String novoEstado = normalizarEstadoOfertaObrigatorio(request.novoEstado());
        String utilizador = utilizadorObrigatorio(request.utilizador());
        buscarPorId(id);
        vagaRepository.alterarEstado(id, novoEstado, request.observacao(), utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional
    public VagaResponse validar(Integer id, VagaValidacaoRequest request) {
        validarId(id);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da validacao sao obrigatorios.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        VagaResponse atual = buscarPorId(id);
        if (!ESTADO_RASCUNHO.equalsIgnoreCase(String.valueOf(atual.estado()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apenas vagas em RASCUNHO podem ser validadas.");
        }
        vagaRepository.alterarEstado(id, ESTADO_ATIVA, atual.observacao(), utilizador);
        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public VagaDuplicacaoResponse prepararDuplicacao(Integer id) {
        VagaResponse origem = buscarPorId(id);
        VagaRequest dados = new VagaRequest(
                origem.codigoReferencia(),
                origem.tipoOferta(),
                origem.titulo(),
                origem.descricao(),
                origem.dataInicioCandidatura(),
                origem.dataFimCandidatura(),
                origem.dataInicioPrevisto(),
                origem.duracaoContrato(),
                origem.regimeContrato(),
                origem.entidadeId(),
                origem.denominacaoEntidade(),
                origem.habilitacaoMinima(),
                origem.nivelQualificacao(),
                origem.numVagas(),
                origem.habilitacaoMaxima(),
                origem.conhecimentoLinguistico(),
                origem.competenciasValorizadas(),
                origem.horaInicio(),
                origem.horaFim(),
                origem.diasSemana(),
                origem.cursosAreaFormacao(),
                origem.experienciaProfissional(),
                origem.ilha(),
                origem.concelho(),
                origem.orientadorId(),
                origem.coordenadorId(),
                origem.emailContacto(),
                origem.contacto(),
                origem.observacao(),
                null
        );
        return new VagaDuplicacaoResponse(id, dados);
    }

    private VagaFiltro normalizarFiltro(VagaFiltro filtro) {
        return new VagaFiltro(
                filtro.tipoOferta(),
                filtro.entidadeId(),
                filtro.ilha(),
                filtro.concelho(),
                normalizarEstadoOfertaOpcional(filtro.estado()),
                filtro.codigoReferencia(),
                filtro.orientadorId(),
                filtro.coordenadorId(),
                filtro.dataInicio(),
                filtro.dataFim(),
                filtro.pesquisa()
        );
    }

    private void validarRequest(VagaRequest request, boolean rascunho) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da vaga sao obrigatorios.");
        }
        utilizadorObrigatorio(request.utilizador());

        if (!rascunho) {
            textoObrigatorio(request.tipoOferta(), "tipoOferta e obrigatorio.");
            textoObrigatorio(request.titulo(), "titulo e obrigatorio.");
            if (request.numVagas() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numVagas e obrigatorio.");
            }
        }

        if (request.numVagas() != null && request.numVagas() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numVagas nao pode ser negativo.");
        }
        if (request.duracaoContrato() != null && request.duracaoContrato() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "duracaoContrato nao pode ser negativo.");
        }
        validarIntervaloDatas(request.dataInicioCandidatura(), request.dataFimCandidatura());
    }

    private void validarIntervaloDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataFimCandidatura nao pode ser anterior a dataInicioCandidatura."
            );
        }
    }

    private void garantirEditavel(VagaResponse vaga) {
        if (ESTADO_FECHADA.equalsIgnoreCase(String.valueOf(vaga.estado()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vagas FECHADAS nao podem ser alteradas.");
        }
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id da vaga e obrigatorio.");
        }
    }

    private String utilizadorObrigatorio(String utilizador) {
        return textoObrigatorio(utilizador, "utilizador e obrigatorio para auditoria.");
    }

    private String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return valor.trim();
    }

    private VagaListaResponse enriquecerLista(VagaListaResponse vaga) {
        String ilhaDesc = descricaoGeografia(vaga.ilha());
        String concelhoDesc = descricaoGeografia(vaga.concelho());
        return new VagaListaResponse(
                vaga.id(),
                vaga.titulo(),
                vaga.tipoOferta(),
                descricaoPadrao(vaga.tipoOfertaDesc(), vaga.tipoOferta()),
                vaga.ilha(),
                ilhaDesc,
                vaga.concelho(),
                concelhoDesc,
                localOferta(ilhaDesc, concelhoDesc),
                vaga.numVagas(),
                vaga.entidadeId(),
                vaga.denominacaoEntidade(),
                vaga.codigoReferencia(),
                vaga.estado(),
                descricaoEstadoOferta(vaga.estado()),
                vaga.dataFimCandidatura()
        );
    }

    private VagaResponse enriquecerDetalhe(VagaResponse vaga) {
        String ilhaDesc = descricaoGeografia(vaga.ilha());
        String concelhoDesc = descricaoGeografia(vaga.concelho());
        return new VagaResponse(
                vaga.id(),
                vaga.codigoReferencia(),
                vaga.tipoOferta(),
                descricaoPadrao(vaga.tipoOfertaDesc(), vaga.tipoOferta()),
                vaga.titulo(),
                vaga.descricao(),
                vaga.dataInicioCandidatura(),
                vaga.dataFimCandidatura(),
                vaga.dataInicioPrevisto(),
                vaga.duracaoContrato(),
                vaga.regimeContrato(),
                vaga.entidadeId(),
                vaga.denominacaoEntidade(),
                vaga.habilitacaoMinima(),
                vaga.nivelQualificacao(),
                vaga.numVagas(),
                vaga.habilitacaoMaxima(),
                vaga.conhecimentoLinguistico(),
                vaga.competenciasValorizadas(),
                vaga.horaInicio(),
                vaga.horaFim(),
                vaga.diasSemana(),
                vaga.cursosAreaFormacao(),
                vaga.experienciaProfissional(),
                vaga.ilha(),
                ilhaDesc,
                vaga.concelho(),
                concelhoDesc,
                localOferta(ilhaDesc, concelhoDesc),
                vaga.orientadorId(),
                vaga.coordenadorId(),
                vaga.emailContacto(),
                vaga.contacto(),
                vaga.observacao(),
                vaga.estado(),
                descricaoEstadoOferta(vaga.estado()),
                vaga.editavel(),
                vaga.dateCreate(),
                vaga.userCreate(),
                vaga.dateUpdate(),
                vaga.userUpdate()
        );
    }

    private String descricaoGeografia(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return codigo;
        }
        try {
            return globalGeografiaService.buscarNomePorCodigo(codigo)
                    .orElse(codigo);
        } catch (Exception ex) {
            return codigo;
        }
    }

    private String descricaoPadrao(String descricao, String codigo) {
        if (descricao != null && !descricao.trim().isEmpty()) {
            return descricao;
        }
        return codigo;
    }

    private String normalizarEstadoOfertaObrigatorio(String estado) {
        String normalizado = normalizarEstadoOfertaOpcional(estado);
        if (normalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "novoEstado e obrigatorio.");
        }
        return normalizado;
    }

    private String normalizarEstadoOfertaOpcional(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return null;
        }

        String valor = estado.trim().toUpperCase().replace(" ", "_");
        return switch (valor) {
            case ESTADO_RASCUNHO -> ESTADO_RASCUNHO;
            case ESTADO_EM_APROVACAO -> ESTADO_EM_APROVACAO;
            case ESTADO_ATIVA -> ESTADO_ATIVA;
            case ESTADO_FECHADA -> ESTADO_FECHADA;
            case ESTADO_CANCELADA -> ESTADO_CANCELADA;
            case ESTADO_INATIVA -> ESTADO_INATIVA;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de oferta invalido. Use RASCUNHO, EM_APROVACAO, ATIVA, FECHADA, CANCELADA ou INATIVA."
            );
        };
    }

    private String descricaoEstadoOferta(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return estado;
        }

        String valor = estado.trim().toUpperCase();
        return switch (valor) {
            case ESTADO_RASCUNHO -> "Rascunho";
            case ESTADO_EM_APROVACAO -> "Em aprovacao";
            case ESTADO_ATIVA -> "Ativa";
            case ESTADO_FECHADA -> "Fechada";
            case ESTADO_CANCELADA -> "Cancelada";
            case ESTADO_INATIVA -> "Inativa";
            default -> estado;
        };
    }

    private String localOferta(String ilha, String concelho) {
        if (ilha == null || ilha.trim().isEmpty()) {
            return concelho;
        }
        if (concelho == null || concelho.trim().isEmpty()) {
            return ilha;
        }
        return ilha + " - " + concelho;
    }
}
