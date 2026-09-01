package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.ComboboxService;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteRequest;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.AnexoArmazenado;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.CandidatoRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ServicoContratanteRepository.ServicoRegisto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ServicoContratanteServiceImplTest {

    @Mock
    private ServicoContratanteRepository servicoRepository;

    @Mock
    private GlobalGeografiaService globalGeografiaService;

    @Mock
    private DocumentService documentService;

    @Mock
    private ComboboxService comboboxService;

    private ServicoContratanteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServicoContratanteServiceImpl(
                servicoRepository,
                globalGeografiaService,
                documentService,
                comboboxService
        );
        ReflectionTestUtils.setField(service, "appCodeDocumento", "interm_laboral");
        ReflectionTestUtils.setField(service, "estadoDocumento", "A");
        ReflectionTestUtils.setField(service, "tipoRelacaoDocumento", "EMPREGO_T_INTERMEDIACAO");
        ReflectionTestUtils.setField(service, "tipoDocumentoIdConfigurado", "");
    }

    @Test
    void deveListarSomenteServicosDoContratanteComEstadoNormalizado() {
        when(servicoRepository.listar(argThat(filtro ->
                filtro.pessoaId().equals(9001L)
                        && "Canalização".equals(filtro.tipoServico())
                        && "A".equals(filtro.estado())
        ))).thenReturn(List.of(servico("ativo", List.of())));

        List<ServicoContratanteListaResponse> resultado = service.listar(new ServicoContratanteFiltro(
                9001L,
                " Canalização ",
                "ativo",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        ));

        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.servicoId()).isEqualTo(41);
            assertThat(item.estado()).isEqualTo("A");
            assertThat(item.estadoDescricao()).isEqualTo("Ativo");
            assertThat(item.tipoServico()).isEqualTo("Canalização");
        });
    }

    @Test
    void deveCriarServicoAtivoSemExigirAnexos() {
        when(servicoRepository.buscarNomePessoa(9001L)).thenReturn(Optional.of("Kevin Sousa"));
        when(servicoRepository.inserir(
                eq(9001L),
                eq("Kevin Sousa"),
                argThat(dados -> "Canalização".equals(dados.tipoServico())),
                eq("A")
        )).thenReturn(41);
        when(servicoRepository.buscarPorId(41, 9001L)).thenReturn(Optional.of(servico("A", List.of())));

        ServicoContratanteDetalheResponse resultado = service.criar(9001L, request(null), null, false);

        assertThat(resultado.servicoId()).isEqualTo(41);
        assertThat(resultado.estado()).isEqualTo("A");
        assertThat(resultado.anexos()).isEmpty();
        verify(servicoRepository, never()).atualizarAnexos(eq(41), eq(9001L), argThat(lista -> true));
        verifyNoInteractions(documentService, comboboxService, globalGeografiaService);
    }

    @Test
    void deveCriarRascunhoComEstadoR() {
        when(servicoRepository.buscarNomePessoa(9001L)).thenReturn(Optional.of("Kevin Sousa"));
        when(servicoRepository.inserir(eq(9001L), eq("Kevin Sousa"), argThat(dados -> true), eq("R")))
                .thenReturn(42);
        when(servicoRepository.buscarPorId(42, 9001L)).thenReturn(Optional.of(servico(42, "R", List.of())));

        ServicoContratanteDetalheResponse resultado = service.criar(9001L, request(null), List.of(), true);

        assertThat(resultado.estado()).isEqualTo("R");
        assertThat(resultado.estadoDescricao()).isEqualTo("Rascunho");
    }

    @Test
    void deveAtualizarListaDeAnexosRemovendoEAdicionandoItens() {
        List<AnexoArmazenado> atuais = List.of(
                new AnexoArmazenado("a.pdf", "docs/a.pdf"),
                new AnexoArmazenado("b.pdf", "docs/b.pdf")
        );
        List<AnexoArmazenado> finais = List.of(
                new AnexoArmazenado("a.pdf", "docs/a.pdf"),
                new AnexoArmazenado("novo.pdf", "docs/novo.pdf")
        );
        MockMultipartFile novo = new MockMultipartFile(
                "novosAnexos",
                "novo.pdf",
                "application/pdf",
                "pdf".getBytes()
        );
        when(servicoRepository.buscarPorId(41, 9001L))
                .thenReturn(Optional.of(servico("A", atuais)))
                .thenReturn(Optional.of(servico("A", finais)));
        when(servicoRepository.atualizar(eq(41), eq(9001L), argThat(dados ->
                dados.anexosMantidos().equals(List.of("docs/a.pdf"))
        ))).thenReturn(true);
        when(comboboxService.listarDocumentosAtivos()).thenReturn(List.of(Map.of(
                "id", 7,
                "tipo_documento_desc", "Documento de prestação de serviço"
        )));
        when(documentService.save(argThat(documento ->
                documento.getIdRelacao().equals(41)
                        && "7".equals(documento.getIdTpDoc())
                        && documento.getFile() == novo
        ))).thenReturn("docs/novo.pdf");
        when(servicoRepository.atualizarAnexos(eq(41), eq(9001L), argThat(anexos ->
                anexos.size() == 2
                        && "docs/a.pdf".equals(anexos.get(0).path())
                        && "docs/novo.pdf".equals(anexos.get(1).path())
        ))).thenReturn(true);
        when(documentService.gerarLinkPublico("docs/a.pdf")).thenReturn("https://docs/a");
        when(documentService.gerarLinkPublico("docs/novo.pdf")).thenReturn("https://docs/novo");

        ServicoContratanteDetalheResponse resultado = service.atualizar(
                41,
                9001L,
                request(List.of("docs/a.pdf")),
                List.of(novo)
        );

        assertThat(resultado.anexos()).extracting("path")
                .containsExactly("docs/a.pdf", "docs/novo.pdf");
        verify(servicoRepository).atualizarAnexos(eq(41), eq(9001L), argThat(anexos -> anexos.size() == 2));
    }

    @Test
    void deveCancelarSemPermitirAcessoARegistroDeOutroContratante() {
        when(servicoRepository.buscarPorId(41, 9001L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(41, 9001L, "kevin@dge.cv"))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).contains("não pertence ao contratante");
                });

        verify(servicoRepository, never()).alterarEstado(41, 9001L, "C", "kevin@dge.cv");
    }

    @Test
    void deveListarCandidatosComDescricoesDosDominios() {
        when(servicoRepository.buscarPorId(41, 9001L)).thenReturn(Optional.of(servico("A", List.of())));
        when(servicoRepository.listarCandidatos(argThat(filtro ->
                filtro.servicoId().equals(41)
                        && filtro.contratanteId().equals(9001L)
                        && "SELECIONADO".equals(filtro.estado())
        ))).thenReturn(List.of(candidato("selecionado", "sim")));

        List<ServicoContratanteCandidatoListaResponse> resultado = service.listarCandidatos(
                new ServicoContratanteCandidatoFiltro(
                        41,
                        9001L,
                        null,
                        "selecionado",
                        null,
                        null
                )
        );

        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.estado()).isEqualTo("SELECIONADO");
            assertThat(item.estadoDescricao()).isEqualTo("Selecionado");
            assertThat(item.selecaoIefp()).isEqualTo("S");
            assertThat(item.selecaoIefpDescricao()).isEqualTo("Sim");
        });
    }

    @Test
    void deveAceitarSelecaoIefpNulaSemFalhar() {
        when(servicoRepository.buscarPorId(41, 9001L)).thenReturn(Optional.of(servico("A", List.of())));
        when(servicoRepository.listarCandidatos(argThat(filtro -> true)))
                .thenReturn(List.of(candidato("TRIAGEM", null)));

        List<ServicoContratanteCandidatoListaResponse> resultado = service.listarCandidatos(
                new ServicoContratanteCandidatoFiltro(41, 9001L, null, null, null, null)
        );

        assertThat(resultado.get(0).selecaoIefp()).isNull();
        assertThat(resultado.get(0).selecaoIefpDescricao()).isNull();
    }

    @Test
    void deveSelecionarCandidatoApenasQuandoServicoEstaAtivo() {
        when(servicoRepository.buscarPorId(41, 9001L)).thenReturn(Optional.of(servico("A", List.of())));
        when(servicoRepository.buscarCandidato(41, 71, 9001L))
                .thenReturn(Optional.of(candidato("PENDENTE", "N")))
                .thenReturn(Optional.of(candidato("SELECIONADO", "N")));
        when(servicoRepository.selecionarCandidato(41, 71, 9001L, "SELECIONADO", "kevin@dge.cv"))
                .thenReturn(true);

        ServicoContratanteCandidatoListaResponse resultado = service.selecionarCandidato(
                41,
                71,
                9001L,
                "kevin@dge.cv"
        );

        assertThat(resultado.estado()).isEqualTo("SELECIONADO");
        verify(servicoRepository).selecionarCandidato(41, 71, 9001L, "SELECIONADO", "kevin@dge.cv");
    }

    private ServicoContratanteRequest request(List<String> anexosMantidos) {
        return new ServicoContratanteRequest(
                "Canalização",
                "Reparação da instalação",
                "Reparar uma fuga de água.",
                LocalDate.of(2026, 10, 10),
                new BigDecimal("15000.00"),
                "Experiência em canalização.",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                null,
                null,
                null,
                "+238 999 00 00",
                "kevin@dge.cv",
                anexosMantidos,
                "kevin@dge.cv"
        );
    }

    private ServicoRegisto servico(String estado, List<AnexoArmazenado> anexos) {
        return servico(41, estado, anexos);
    }

    private ServicoRegisto servico(Integer id, String estado, List<AnexoArmazenado> anexos) {
        return new ServicoRegisto(
                id,
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
                null,
                null,
                null,
                "+238 999 00 00",
                "kevin@dge.cv",
                anexos,
                estado,
                LocalDateTime.of(2026, 9, 1, 10, 0),
                "kevin@dge.cv",
                null,
                null
        );
    }

    private CandidatoRegisto candidato(String estado, String selecaoIefp) {
        return new CandidatoRegisto(
                71,
                2001101700869L,
                "Candidato Teste",
                "Canalização",
                "Reparação da instalação",
                estado,
                selecaoIefp,
                null,
                LocalDate.of(2026, 9, 5)
        );
    }
}
