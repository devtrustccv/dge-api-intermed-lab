package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaDuplicacaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaDuplicacaoDadosResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaEstadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaColaboradorSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.enums.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoVagaRepository;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
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
    private static final String TIPO_ORIENTADOR = "ORIENTADOR";
    private static final String TIPO_COORDENADOR = "COORDENADOR";

    private final GestaoVagaRepository vagaRepository;
    private final GlobalGeografiaService globalGeografiaService;

    @Override
    @Transactional(readOnly = true)
    public List<VagaListaResponse> listar(VagaFiltro filtro) {
        VagaFiltro dados = normalizarFiltro(filtro);
        return vagaRepository.listar(semFiltrosGeografia(dados)).stream()
                .map(this::enriquecerLista)
                .filter(vaga -> correspondeGeografia(dados.ilha(), vaga.ilha(), vaga.ilhaDesc()))
                .filter(vaga -> correspondeGeografia(dados.concelho(), vaga.concelho(), vaga.concelhoDesc()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VagaColaboradorSelectResponse> listarColaboradores(Integer entidadeId, String tipo) {
        validarEntidade(entidadeId);
        return vagaRepository.listarColaboradoresPorTipo(
                entidadeId,
                normalizarTipoColaboradorObrigatorio(tipo)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VagaColaboradorSelectResponse> listarOrientadores(Integer entidadeId) {
        return listarColaboradores(entidadeId, TIPO_ORIENTADOR);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VagaColaboradorSelectResponse> listarCoordenadores(Integer entidadeId) {
        return listarColaboradores(entidadeId, TIPO_COORDENADOR);
    }

    @Override
    @Transactional(readOnly = true)
    public VagaResponse buscarPorId(Integer id, Integer entidadeId) {
        validarId(id);
        validarEntidade(entidadeId);
        return vagaRepository.buscarPorId(id, entidadeId)
                .map(this::enriquecerDetalhe)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A oferta selecionada não foi encontrada. Atualize a página e tente novamente."));
    }

    @Override
    @Transactional
    public VagaResponse criar(Integer entidadeId, VagaRequest request) {
        validarEntidade(entidadeId);
        validarRequest(request, false);
        VagaRequest dados = normalizarRequest(request);
        validarColaboradores(entidadeId, dados);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        Integer id = vagaRepository.inserir(entidadeId, dados, ESTADO_ATIVA, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VagaResponse criarRascunho(Integer entidadeId, VagaRequest request) {
        validarEntidade(entidadeId);
        validarRequest(request, true);
        VagaRequest dados = normalizarRequest(request);
        validarColaboradores(entidadeId, dados);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        Integer id = vagaRepository.inserir(entidadeId, dados, ESTADO_RASCUNHO, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VagaResponse atualizar(Integer id, Integer entidadeId, VagaRequest request) {
        validarId(id);
        validarEntidade(entidadeId);
        validarRequest(request, false);
        VagaResponse atual = buscarPorId(id, entidadeId);
        garantirEditavel(atual);
        VagaRequest dados = normalizarRequest(request);
        validarColaboradores(entidadeId, dados);
        String utilizador = utilizadorObrigatorio(dados.utilizador());
        vagaRepository.atualizar(id, entidadeId, dados, utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VagaResponse alterarEstado(Integer id, Integer entidadeId, VagaEstadoRequest request) {
        validarId(id);
        validarEntidade(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecione o novo estado da oferta antes de confirmar.");
        }
        String novoEstado = normalizarEstadoOfertaObrigatorio(request.novoEstado());
        String utilizador = utilizadorObrigatorio(request.utilizador());
        buscarPorId(id, entidadeId);
        vagaRepository.alterarEstado(id, entidadeId, novoEstado, request.observacao(), utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional
    public VagaResponse validar(Integer id, Integer entidadeId, VagaValidacaoRequest request) {
        validarId(id);
        validarEntidade(entidadeId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível confirmar a validação da oferta. Tente novamente.");
        }
        String utilizador = utilizadorObrigatorio(request.utilizador());
        VagaResponse atual = buscarPorId(id, entidadeId);
        if (!ESTADO_RASCUNHO.equalsIgnoreCase(String.valueOf(atual.estado()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Apenas ofertas que ainda estejam em rascunho podem ser enviadas para validação.");
        }
        vagaRepository.alterarEstado(id, entidadeId, ESTADO_ATIVA, atual.observacao(), utilizador);
        return buscarPorId(id, entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public VagaDuplicacaoResponse prepararDuplicacao(Integer id, Integer entidadeId) {
        VagaResponse origem = buscarPorId(id, entidadeId);
        VagaDuplicacaoDadosResponse dados = new VagaDuplicacaoDadosResponse(
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
                origem.orientadorDenominacao(),
                origem.coordenadorDenominacao(),
                origem.emailContacto(),
                origem.contacto(),
                origem.observacao(),
                null
        );
        return new VagaDuplicacaoResponse(id, dados);
    }

    private VagaFiltro normalizarFiltro(VagaFiltro filtro) {
        if (filtro == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Os filtros da pesquisa de ofertas da entidade não foram enviados.");
        }
        validarEntidade(filtro.entidadeId());
        validarIntervaloDatas(filtro.dataInicio(), filtro.dataFim());
        return new VagaFiltro(
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_TIPO_OFERTA,
                        filtro.tipoOferta(),
                        "Tipo de oferta"
                ),
                filtro.entidadeId(),
                texto(filtro.entidade()),
                texto(filtro.ilha()),
                texto(filtro.concelho()),
                normalizarEstadoOfertaOpcional(filtro.estado()),
                texto(filtro.codigoReferencia()),
                filtro.orientadorId(),
                filtro.coordenadorId(),
                filtro.dataInicio(),
                filtro.dataFim(),
                texto(filtro.pesquisa())
        );
    }

    private VagaFiltro semFiltrosGeografia(VagaFiltro filtro) {
        return new VagaFiltro(
                filtro.tipoOferta(),
                filtro.entidadeId(),
                filtro.entidade(),
                null,
                null,
                filtro.estado(),
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha os dados da oferta antes de gravar.");
        }
        utilizadorObrigatorio(request.utilizador());

        if (!rascunho) {
        textoObrigatorio(request.tipoOferta(), "Selecione o tipo de oferta.");
        textoObrigatorio(request.titulo(), "Informe o título da oferta.");
            if (request.numVagas() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o número de vagas disponíveis.");
            }
        }

        if (request.numVagas() != null && request.numVagas() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O número de vagas deve ser igual ou superior a zero.");
        }
        if (request.duracaoContrato() != null && request.duracaoContrato() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A duração do contrato deve ser igual ou superior a zero.");
        }
        validarIntervaloDatas(request.dataInicioCandidatura(), request.dataFimCandidatura());
    }

    private void validarIntervaloDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data de fim das candidaturas não pode ser anterior à data de início."
            );
        }
    }

    private void garantirEditavel(VagaResponse vaga) {
        if (ESTADO_FECHADA.equalsIgnoreCase(String.valueOf(vaga.estado()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta oferta já está fechada e não pode ser alterada.");
        }
    }

    private void validarId(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a oferta selecionada. Atualize a página e tente novamente.");
        }
    }

    private String utilizadorObrigatorio(String utilizador) {
        return textoObrigatorio(utilizador,
                "Não foi possível identificar o utilizador. Inicie sessão novamente e repita a operação.");
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
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, vaga.tipoOferta()),
                descricaoDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, vaga.tipoOferta()),
                vaga.ilha(),
                ilhaDesc,
                vaga.concelho(),
                concelhoDesc,
                localOferta(ilhaDesc, concelhoDesc),
                vaga.numVagas(),
                vaga.entidadeId(),
                vaga.denominacaoEntidade(),
                vaga.orientadorId(),
                vaga.orientadorDenominacao(),
                vaga.orientadorNome(),
                vaga.coordenadorId(),
                vaga.coordenadorDenominacao(),
                vaga.coordenadorNome(),
                vaga.coordenadorEmail(),
                vaga.coordenadorTelefone(),
                vaga.codigoReferencia(),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO_OFERTA, vaga.estado()),
                descricaoDominio(EmpregoDominio.DOMINIO_ESTADO_OFERTA, vaga.estado()),
                vaga.dataInicio(),
                vaga.dataFimCandidatura()
        );
    }

    private VagaResponse enriquecerDetalhe(VagaResponse vaga) {
        String ilhaDesc = descricaoGeografia(vaga.ilha());
        String concelhoDesc = descricaoGeografia(vaga.concelho());
        return new VagaResponse(
                vaga.id(),
                vaga.codigoReferencia(),
                valorDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, vaga.tipoOferta()),
                descricaoDominio(EmpregoDominio.DOMINIO_TIPO_OFERTA, vaga.tipoOferta()),
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
                vaga.orientadorDenominacao(),
                vaga.coordenadorId(),
                vaga.coordenadorDenominacao(),
                vaga.coordenadorEmail(),
                vaga.coordenadorTelefone(),
                vaga.emailContacto(),
                vaga.contacto(),
                vaga.observacao(),
                valorDominio(EmpregoDominio.DOMINIO_ESTADO_OFERTA, vaga.estado()),
                descricaoDominio(EmpregoDominio.DOMINIO_ESTADO_OFERTA, vaga.estado()),
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

    private void validarEntidade(Integer entidadeId) {
        if (entidadeId == null || entidadeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a entidade selecionada. Selecione uma entidade e tente novamente.");
        }
    }

    private void validarColaboradores(Integer entidadeId, VagaRequest request) {
        validarColaborador(entidadeId, request.orientadorId(), TIPO_ORIENTADOR, "orientador");
        validarColaborador(entidadeId, request.coordenadorId(), TIPO_COORDENADOR, "coordenador");
    }

    private void validarColaborador(Integer entidadeId, Integer colaboradorId, String tipo, String descricao) {
        if (colaboradorId == null) {
            return;
        }
        if (!vagaRepository.existeColaborador(entidadeId, colaboradorId, tipo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O " + descricao + " selecionado não pertence à entidade ou não está ativo."
            );
        }
    }

    private boolean correspondeGeografia(String filtro, String codigo, String descricao) {
        if (filtro == null || filtro.isBlank()) {
            return true;
        }
        String procurado = normalizarParaPesquisa(filtro);
        String codigoNormalizado = normalizarParaPesquisa(codigo);
        String descricaoNormalizada = normalizarParaPesquisa(descricao);
        return procurado.equals(codigoNormalizado)
                || (descricaoNormalizada != null && descricaoNormalizada.contains(procurado));
    }

    private String normalizarParaPesquisa(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizarEstadoOfertaObrigatorio(String estado) {
        String normalizado = normalizarEstadoOfertaOpcional(estado);
        if (normalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o novo estado da oferta.");
        }
        return normalizado;
    }

    private String normalizarTipoColaboradorObrigatorio(String tipo) {
        String normalizado = normalizarDominioOpcional(
                EmpregoDominio.DOMINIO_TIPO_COLABORADOR,
                tipo,
                "Tipo de colaborador"
        );
        if (normalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o tipo de colaborador.");
        }
        return normalizado;
    }

    private String normalizarEstadoOfertaOpcional(String estado) {
        return normalizarDominioOpcional(
                EmpregoDominio.DOMINIO_ESTADO_OFERTA,
                estado,
                "Estado da oferta"
        );
    }

    private VagaRequest normalizarRequest(VagaRequest request) {
        return new VagaRequest(
                texto(request.codigoReferencia()),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_TIPO_OFERTA,
                        request.tipoOferta(),
                        "Tipo de oferta"
                ),
                texto(request.titulo()),
                texto(request.descricao()),
                request.dataInicioCandidatura(),
                request.dataFimCandidatura(),
                request.dataInicioPrevisto(),
                request.duracaoContrato(),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_REGIME_CONTRATO,
                        request.regimeContrato(),
                        "Regime de contrato"
                ),
                texto(request.denominacaoEntidade()),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA,
                        request.habilitacaoMinima(),
                        "Habilitação mínima"
                ),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO,
                        request.nivelQualificacao(),
                        "Nível de qualificação"
                ),
                request.numVagas(),
                normalizarDominioOpcional(
                        EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA,
                        request.habilitacaoMaxima(),
                        "Habilitação máxima"
                ),
                request.conhecimentoLinguistico(),
                request.competenciasValorizadas(),
                request.horaInicio(),
                request.horaFim(),
                request.diasSemana(),
                request.cursosAreaFormacao(),
                request.experienciaProfissional(),
                texto(request.ilha()),
                texto(request.concelho()),
                request.orientadorId(),
                request.coordenadorId(),
                texto(request.orientadorDenominacao()),
                texto(request.coordenadorDenominacao()),
                texto(request.emailContacto()),
                texto(request.contacto()),
                texto(request.observacao()),
                texto(request.utilizador())
        );
    }

    private String normalizarDominioOpcional(String dominio, String valor, String nomeCampo) {
        String texto = texto(valor);
        if (texto == null) {
            return null;
        }
        return EmpregoDominio.valorOficial(dominio, texto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        EmpregoDominio.mensagemValorInvalido(dominio, texto, nomeCampo)
                ));
    }

    private String valorDominio(String dominio, String valor) {
        return EmpregoDominio.valorOficial(dominio, valor).orElse(valor);
    }

    private String descricaoDominio(String dominio, String valor) {
        return EmpregoDominio.descricao(dominio, valor);
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

    private String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
