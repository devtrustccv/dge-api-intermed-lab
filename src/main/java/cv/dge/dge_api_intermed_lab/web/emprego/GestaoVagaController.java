package cv.dge.dge_api_intermed_lab.web.emprego;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaDuplicacaoResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaEstadoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.service.GestaoVagaService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vagas")
public class GestaoVagaController {

    private final GestaoVagaService gestaoVagaService;

    @GetMapping
    public List<VagaListaResponse> listar(
            @RequestParam(value = "tipoOferta", required = false) String tipoOferta,
            @RequestParam(value = "entidadeId", required = false) Integer entidadeId,
            @RequestParam(value = "ilha", required = false) String ilha,
            @RequestParam(value = "concelho", required = false) String concelho,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "codigoReferencia", required = false) String codigoReferencia,
            @RequestParam(value = "orientadorId", required = false) Integer orientadorId,
            @RequestParam(value = "coordenadorId", required = false) Integer coordenadorId,
            @RequestParam(value = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "pesquisa", required = false) String pesquisa
    ) {
        return gestaoVagaService.listar(new VagaFiltro(
                tipoOferta,
                entidadeId,
                ilha,
                concelho,
                estado,
                codigoReferencia,
                orientadorId,
                coordenadorId,
                dataInicio,
                dataFim,
                pesquisa
        ));
    }

    @GetMapping("{id}")
    public VagaResponse buscarPorId(@PathVariable Integer id) {
        return gestaoVagaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VagaResponse criar(@RequestBody VagaRequest request) {
        return gestaoVagaService.criar(request);
    }

    @PostMapping("rascunho")
    @ResponseStatus(HttpStatus.CREATED)
    public VagaResponse criarRascunho(@RequestBody VagaRequest request) {
        return gestaoVagaService.criarRascunho(request);
    }

    @PutMapping("{id}")
    public VagaResponse atualizar(@PathVariable Integer id, @RequestBody VagaRequest request) {
        return gestaoVagaService.atualizar(id, request);
    }

    @PatchMapping("{id}/estado")
    public VagaResponse alterarEstado(@PathVariable Integer id, @RequestBody VagaEstadoRequest request) {
        return gestaoVagaService.alterarEstado(id, request);
    }

    @PatchMapping("{id}/validar")
    public VagaResponse validar(@PathVariable Integer id, @RequestBody VagaValidacaoRequest request) {
        return gestaoVagaService.validar(id, request);
    }

    @GetMapping("{id}/duplicar")
    public VagaDuplicacaoResponse prepararDuplicacao(@PathVariable Integer id) {
        return gestaoVagaService.prepararDuplicacao(id);
    }
}
