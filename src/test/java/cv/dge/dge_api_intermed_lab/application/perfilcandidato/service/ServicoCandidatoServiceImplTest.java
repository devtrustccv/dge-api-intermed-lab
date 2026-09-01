package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoOpcoesResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.AnexoArmazenado;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.ServicoCandidatoRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.ServicoRegisto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ServicoCandidatoServiceImplTest {

    @Mock
    private ServicoContratanteRepository servicoRepository;

    @Mock
    private GlobalGeografiaService globalGeografiaService;

    @Mock
    private DocumentService documentService;

    private ServicoCandidatoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServicoCandidatoServiceImpl(
                servicoRepository,
                globalGeografiaService,
                documentService
        );
    }

    @Test
    void deveListarSomenteIndicacoesDoCandidatoComDominiosCorretos() {
        when(servicoRepository.listarParaCandidato(argThat(filtro ->
                filtro.pessoaId().equals(2001101700869L)
                        && "Canalização".equals(filtro.tipoServico())
                        && "I".equals(filtro.estado())
        ))).thenReturn(List.of(registo("I", "sim", null)));

        List<ServicoCandidatoListaResponse> resultado = service.listar(new ServicoCandidatoFiltro(
                2001101700869L,
                " Canalização ",
                "inativo",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        ));

        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.estado()).isEqualTo("I");
            assertThat(item.estadoDescricao()).isEqualTo("Inativo");
            assertThat(item.selecaoIefp()).isEqualTo("S");
            assertThat(item.selecaoIefpDescricao()).isEqualTo("Sim");
            assertThat(item.statusAceitacao()).isEqualTo("PENDENTE");
            assertThat(item.statusAceitacaoDescricao()).isEqualTo("Pendente");
        });
    }

    @Test
    void deveExporTodosOsEstadosServicoDaImagemDoDominio() {
        ServicoCandidatoOpcoesResponse resultado = service.listarOpcoes();

        assertThat(resultado.estadosServico()).extracting("valor")
                .containsExactly("A", "I", "C", "R", "E");
    }

    @Test
    void deveCarregarDetalheComDescricoesGeograficasEAnexos() {
        when(servicoRepository.buscarParaCandidato(41, 2001101700869L))
                .thenReturn(Optional.of(registo("A", "S", "PENDENTE")));
        when(globalGeografiaService.buscarNomePorCodigo("ST")).thenReturn(Optional.of("Santiago"));
        when(globalGeografiaService.buscarNomePorCodigo("PR")).thenReturn(Optional.of("Praia"));
        when(globalGeografiaService.buscarNomePorCodigo("ASA"))
                .thenReturn(Optional.of("Achada Santo António"));
        when(documentService.gerarLinkPublico("docs/termos.pdf"))
                .thenReturn("https://documentos/termos.pdf");

        ServicoCandidatoDetalheResponse resultado = service.buscarPorId(41, 2001101700869L);

        assertThat(resultado.requerente()).isEqualTo("Kevin Sousa");
        assertThat(resultado.ilhaDescricao()).isEqualTo("Santiago");
        assertThat(resultado.concelhoDescricao()).isEqualTo("Praia");
        assertThat(resultado.zonaDescricao()).isEqualTo("Achada Santo António");
        assertThat(resultado.anexos()).singleElement().satisfies(anexo -> {
            assertThat(anexo.path()).isEqualTo("docs/termos.pdf");
            assertThat(anexo.url()).isEqualTo("https://documentos/termos.pdf");
        });
    }

    @Test
    void deveAceitarIndicacaoAtivaESelecionadaPeloIefp() {
        when(servicoRepository.buscarParaCandidato(41, 2001101700869L))
                .thenReturn(Optional.of(registo("A", "S", null)))
                .thenReturn(Optional.of(registo("A", "S", "ACEITE")));
        when(servicoRepository.atualizarAceitacaoCandidato(
                41,
                71,
                2001101700869L,
                "ACEITE",
                "candidato.teste"
        )).thenReturn(true);

        ServicoCandidatoDetalheResponse resultado = service.aceitar(
                41,
                2001101700869L,
                "candidato.teste"
        );

        assertThat(resultado.statusAceitacao()).isEqualTo("ACEITE");
        assertThat(resultado.statusAceitacaoDescricao()).isEqualTo("Aceite");
        verify(servicoRepository).atualizarAceitacaoCandidato(
                41,
                71,
                2001101700869L,
                "ACEITE",
                "candidato.teste"
        );
    }

    @Test
    void naoDeveResponderServicoNaoSelecionadoPeloIefp() {
        when(servicoRepository.buscarParaCandidato(41, 2001101700869L))
                .thenReturn(Optional.of(registo("A", "N", "PENDENTE")));

        assertThatThrownBy(() -> service.recusar(41, 2001101700869L, "candidato.teste"))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getReason()).contains("não foi indicado pelo IEFP");
                });

        verify(servicoRepository, never()).atualizarAceitacaoCandidato(
                41,
                71,
                2001101700869L,
                "RECUSADO",
                "candidato.teste"
        );
    }

    @Test
    void naoDeveTrocarRespostaJaRegistada() {
        when(servicoRepository.buscarParaCandidato(41, 2001101700869L))
                .thenReturn(Optional.of(registo("A", "S", "ACEITE")));

        assertThatThrownBy(() -> service.recusar(41, 2001101700869L, "candidato.teste"))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(servicoRepository, never()).atualizarAceitacaoCandidato(
                41,
                71,
                2001101700869L,
                "RECUSADO",
                "candidato.teste"
        );
    }

    private ServicoCandidatoRegisto registo(String estadoServico, String selecaoIefp, String statusAceitacao) {
        return new ServicoCandidatoRegisto(
                servico(estadoServico),
                71,
                2001101700869L,
                "PRE_SELECIONADO",
                selecaoIefp,
                statusAceitacao
        );
    }

    private ServicoRegisto servico(String estado) {
        return new ServicoRegisto(
                41,
                9001L,
                "Kevin Sousa",
                "Canalização",
                "Reparação da instalação",
                "Reparar uma fuga de água.",
                LocalDate.of(2026, 10, 10),
                new BigDecimal("15000.00"),
                "Experiência em canalização.",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                "ST",
                "PR",
                "ASA",
                "+238 999 00 00",
                "kevin@dge.cv",
                List.of(new AnexoArmazenado("termos.pdf", "docs/termos.pdf")),
                estado,
                LocalDateTime.of(2026, 9, 1, 10, 0),
                "kevin@dge.cv",
                null,
                null
        );
    }
}
