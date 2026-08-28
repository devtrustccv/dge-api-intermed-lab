package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaPorTipoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AreaGeograficaProcuradaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ColocacaoRecenteResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.EvolucaoCandidaturaMensalResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoDashboardResponse;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.PerfilCandidatoDashboardRepository;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PerfilCandidatoDashboardServiceImpl implements PerfilCandidatoDashboardService {

    private static final String[] MESES = {
            "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private final PerfilCandidatoDashboardRepository dashboardRepository;
    private final GlobalGeografiaService globalGeografiaService;

    @Override
    @Transactional(readOnly = true)
    public PerfilCandidatoDashboardResponse buscarDashboard(Long pessoaId, Integer ano) {
        validarPessoa(pessoaId);
        int anoConsulta = validarAno(ano);
        Map<String, String> descricoesGeografia = new HashMap<>();

        return new PerfilCandidatoDashboardResponse(
                anoConsulta,
                dashboardRepository.contarColocacoes(pessoaId),
                dashboardRepository.contarCandidaturas(pessoaId),
                dashboardRepository.contarVagasAbertas("OFERTA_EMPREGO"),
                dashboardRepository.contarVagasAbertas("OFERTA_ESTAGIO"),
                completarMeses(dashboardRepository.listarEvolucaoCandidaturas(pessoaId, anoConsulta)),
                enriquecerTipos(dashboardRepository.listarCandidaturasPorTipo(pessoaId)),
                enriquecerAreasGeograficas(
                        dashboardRepository.listarTopAreasGeograficas(),
                        descricoesGeografia
                ),
                enriquecerColocacoes(
                        dashboardRepository.listarColocacoesRecentes(pessoaId),
                        descricoesGeografia
                )
        );
    }

    private void validarPessoa(Long pessoaId) {
        if (pessoaId == null || pessoaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo."
            );
        }
    }

    private int validarAno(Integer ano) {
        int anoAtual = Year.now().getValue();
        if (ano == null) {
            return anoAtual;
        }
        if (ano < 1900 || ano > anoAtual) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecione um ano entre 1900 e " + anoAtual + "."
            );
        }
        return ano;
    }

    private List<EvolucaoCandidaturaMensalResponse> completarMeses(
            List<EvolucaoCandidaturaMensalResponse> evolucao
    ) {
        Map<Integer, EvolucaoCandidaturaMensalResponse> porMes = evolucao.stream()
                .collect(Collectors.toMap(EvolucaoCandidaturaMensalResponse::mes, Function.identity()));

        return IntStream.rangeClosed(1, 12)
                .mapToObj(mes -> {
                    EvolucaoCandidaturaMensalResponse dados = porMes.get(mes);
                    return new EvolucaoCandidaturaMensalResponse(
                            mes,
                            MESES[mes],
                            dados == null ? 0L : dados.totalEmprego(),
                            dados == null ? 0L : dados.totalEstagio()
                    );
                })
                .toList();
    }

    private List<CandidaturaPorTipoResponse> enriquecerTipos(List<CandidaturaPorTipoResponse> tipos) {
        return tipos.stream()
                .map(item -> new CandidaturaPorTipoResponse(
                        normalizarTipoOferta(item.tipoOferta()),
                        descricaoTipoOferta(item.tipoOferta()),
                        item.total()
                ))
                .toList();
    }

    private List<AreaGeograficaProcuradaResponse> enriquecerAreasGeograficas(
            List<AreaGeograficaProcuradaResponse> areas,
            Map<String, String> descricoesGeografia
    ) {
        return areas.stream()
                .map(item -> new AreaGeograficaProcuradaResponse(
                        descricaoGeografia(item.ilha(), descricoesGeografia),
                        descricaoGeografia(item.concelho(), descricoesGeografia),
                        item.total()
                ))
                .toList();
    }

    private List<ColocacaoRecenteResponse> enriquecerColocacoes(
            List<ColocacaoRecenteResponse> colocacoes,
            Map<String, String> descricoesGeografia
    ) {
        return colocacoes.stream()
                .map(item -> new ColocacaoRecenteResponse(
                        item.idColocacao(),
                        item.idOferta(),
                        item.nomeOferta(),
                        item.nomeEmpresa(),
                        descricaoGeografia(item.ilha(), descricoesGeografia),
                        descricaoGeografia(item.concelho(), descricoesGeografia),
                        normalizarTipoOferta(item.tipoOferta()),
                        descricaoTipoOferta(item.tipoOferta()),
                        item.dataColocacao()
                ))
                .toList();
    }

    private String descricaoGeografia(String codigo, Map<String, String> descricoesGeografia) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return codigo;
        }
        String codigoLimpo = codigo.trim();
        return descricoesGeografia.computeIfAbsent(codigoLimpo, this::buscarDescricaoGeografia);
    }

    private String buscarDescricaoGeografia(String codigo) {
        try {
            return globalGeografiaService.buscarNomePorCodigo(codigo).orElse(codigo);
        } catch (Exception ex) {
            return codigo;
        }
    }

    private String normalizarTipoOferta(String tipoOferta) {
        return tipoOferta == null ? null : tipoOferta.trim().toUpperCase(Locale.ROOT);
    }

    private String descricaoTipoOferta(String tipoOferta) {
        String tipoNormalizado = normalizarTipoOferta(tipoOferta);
        if (tipoNormalizado == null) {
            return null;
        }
        return switch (tipoNormalizado) {
            case "OFERTA_EMPREGO" -> "Oferta de emprego";
            case "OFERTA_ESTAGIO" -> "Oferta de estágio";
            default -> tipoOferta;
        };
    }
}
