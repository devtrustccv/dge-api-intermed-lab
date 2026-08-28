package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaCandidaturaService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/candidaturas")
public class PerfilCandidatoCandidaturaController {

    private final MinhaCandidaturaService candidaturaService;

    @GetMapping
    public PerfilCandidatoApiResponse<List<MinhaCandidaturaListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String tipoOferta,
            @RequestParam(required = false) Integer entidadeId,
            @RequestParam(required = false) String ilha,
            @RequestParam(required = false) String concelho,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String codigoReferencia,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Candidaturas carregadas com sucesso.",
                candidaturaService.listar(new MinhaCandidaturaFiltro(
                        pessoaId,
                        tipoOferta,
                        entidadeId,
                        ilha,
                        concelho,
                        estado,
                        codigoReferencia,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("opcoes")
    public PerfilCandidatoApiResponse<MinhaCandidaturaOpcoesResponse> listarOpcoes(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String ilha
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Opções das candidaturas carregadas com sucesso.",
                candidaturaService.listarOpcoes(pessoaId, ilha)
        );
    }

    @GetMapping("{candidaturaId}")
    public PerfilCandidatoApiResponse<MinhaCandidaturaDetalheResponse> buscarPorId(
            @PathVariable Integer candidaturaId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes da candidatura carregados com sucesso.",
                candidaturaService.buscarPorId(candidaturaId, pessoaId)
        );
    }
}
