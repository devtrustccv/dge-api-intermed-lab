package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.ComboboxService;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaRequest;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConsultaVagaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConsultaVagaRepository.OfertaDetalhe;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConsultaVagaServiceImplTest {

    @Mock
    private ConsultaVagaRepository vagaRepository;

    @Mock
    private DocumentService documentService;

    @Mock
    private ComboboxService comboboxService;

    @InjectMocks
    private ConsultaVagaServiceImpl service;

    @BeforeEach
    void configurarDocumentos() {
        ReflectionTestUtils.setField(service, "appCodeDocumento", "interm_laboral");
        ReflectionTestUtils.setField(service, "estadoDocumento", "A");
        ReflectionTestUtils.setField(
                service,
                "tipoRelacaoDocumento",
                "EMPREGO_T_CANDIDATURA_OFERTA"
        );
        ReflectionTestUtils.setField(service, "tipoCurriculoIdConfigurado", "");
        ReflectionTestUtils.setField(service, "tipoOutroIdConfigurado", "");
    }

    @Test
    void deveEnviarIdsNumericosECaminhosSegurosParaOServicoDocumental() {
        Integer ofertaId = 22;
        Long pessoaId = 123456L;
        Integer candidaturaId = 77;
        when(vagaRepository.buscarOferta(ofertaId, pessoaId))
                .thenReturn(Optional.of(ofertaDisponivel(ofertaId)));
        when(vagaRepository.buscarUltimaCandidatura(pessoaId)).thenReturn(Optional.empty());
        when(vagaRepository.buscarNomePessoa(pessoaId)).thenReturn(Optional.of("Kevin Sousa"));
        when(vagaRepository.inserirCandidatura(any(), any(), any(), any(), any(), any()))
                .thenReturn(candidaturaId);
        when(comboboxService.listarDocumentosAtivos()).thenReturn(List.of(
                tipoDocumento(15, "Currículo Vitae"),
                tipoDocumento(16, "Outros documentos")
        ));
        when(documentService.save(any(DocRelacaoDTO.class))).thenAnswer(invocacao ->
                invocacao.getArgument(0, DocRelacaoDTO.class).getPath()
        );
        when(documentService.gerarLinkPublico(any())).thenAnswer(invocacao ->
                "https://documentos.test/?path=" + invocacao.getArgument(0, String.class)
        );

        MockMultipartFile curriculo = new MockMultipartFile(
                "novaVersaoCv",
                "Kevin Sousa.pdf",
                "application/pdf",
                "cv".getBytes()
        );
        MockMultipartFile outro = new MockMultipartFile(
                "outrosDocumentos",
                "Diploma final.pdf",
                "application/pdf",
                "diploma".getBytes()
        );

        service.candidatar(
                ofertaId,
                pessoaId,
                new CandidaturaVagaRequest("LICENCIATURA", "Programação", "kevin@dge.cv"),
                curriculo,
                List.of(outro)
        );

        ArgumentCaptor<DocRelacaoDTO> captor = ArgumentCaptor.forClass(DocRelacaoDTO.class);
        verify(documentService, times(2)).save(captor.capture());
        List<DocRelacaoDTO> enviados = captor.getAllValues();

        assertThat(enviados.get(0).getIdTpDoc()).isEqualTo("15");
        assertThat(enviados.get(1).getIdTpDoc()).isEqualTo("16");
        assertThat(enviados.get(0).getPath()).isEqualTo(
                caminhoEsperado(candidaturaId, "CURRICULO_VITAE-1-Kevin_Sousa.pdf")
        );
        assertThat(enviados.get(1).getPath()).isEqualTo(
                caminhoEsperado(candidaturaId, "OUTRO_DOCUMENTO-1-Diploma_final.pdf")
        );
        assertThat(enviados.get(0).getPath()).isNotEqualTo(enviados.get(1).getPath());
        verify(vagaRepository).atualizarAnexos(org.mockito.Mockito.eq(candidaturaId), any());
    }

    private Map<String, Object> tipoDocumento(Integer id, String descricao) {
        Map<String, Object> tipo = new LinkedHashMap<>();
        tipo.put("id", id);
        tipo.put("tipo_documento_desc", descricao);
        return tipo;
    }

    private String caminhoEsperado(Integer candidaturaId, String ficheiro) {
        return "interm_laboral/"
                + LocalDateTime.now().getYear()
                + "/modulos/EMPREGO_T_CANDIDATURA_OFERTA/"
                + candidaturaId
                + "/"
                + ficheiro;
    }

    private OfertaDetalhe ofertaDisponivel(Integer ofertaId) {
        return new OfertaDetalhe(
                ofertaId,
                "VG-TESTE",
                "OFERTA_EMPREGO",
                "Programador",
                "Oferta de teste",
                null,
                null,
                null,
                null,
                null,
                1,
                "Empresa Teste",
                null,
                null,
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "ST",
                "PR",
                null,
                null,
                null,
                null,
                null,
                "ATIVA",
                false
        );
    }
}
