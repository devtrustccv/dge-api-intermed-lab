package cv.dge.dge_api_intermed_lab.application.perfilentidade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CoordenadorOrientadorRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaDuplicacaoDadosResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.ColocacaoCandidatoServiceImpl;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.CoordenadorOrientadorServiceImpl;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoCandidaturaServiceImpl;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVagaService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVagaServiceImpl;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVisitaTecnicaServiceImpl;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.ColocacaoCandidatoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.CoordenadorOrientadorRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoCandidaturaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoVagaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoVisitaTecnicaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PerfilEntidadeEntidadeIdFluxoTest {

    private static final Integer ENTIDADE_ID = 40;

    @Mock
    private GestaoVagaRepository vagaRepository;
    @Mock
    private GlobalGeografiaService geografiaService;
    @Mock
    private CoordenadorOrientadorRepository colaboradorRepository;
    @Mock
    private GestaoVagaService vagaService;
    @Mock
    private ColocacaoCandidatoRepository colocacaoRepository;
    @Mock
    private GestaoVisitaTecnicaRepository visitaRepository;
    @Mock
    private GestaoCandidaturaRepository candidaturaRepository;
    @Mock
    private DocumentService documentService;

    @Test
    void bodiesDeEntidadeNaoDevemDeclararEntidadeId() {
        List<Class<?>> requests = List.of(
                VagaRequest.class,
                CoordenadorOrientadorRequest.class,
                VisitaTecnicaRequest.class
        );

        requests.forEach(request -> assertThat(Arrays.stream(request.getRecordComponents())
                        .map(component -> component.getName()))
                .as("%s não deve receber entidadeId no body", request.getSimpleName())
                .doesNotContain("entidadeId"));
    }

    @Test
    void respostaDeDuplicacaoDevePreservarEntidadeId() {
        assertThat(Arrays.stream(VagaDuplicacaoDadosResponse.class.getRecordComponents())
                        .map(component -> component.getName()))
                .contains("entidadeId");
    }

    @Test
    void criacaoDeVagaDeveGravarEntidadeDaUrl() {
        GestaoVagaServiceImpl service = new GestaoVagaServiceImpl(vagaRepository, geografiaService);
        VagaRequest request = novaVaga();
        when(vagaRepository.inserir(eq(ENTIDADE_ID), any(), eq("ATIVA"), eq("utilizador")))
                .thenReturn(29);
        when(vagaRepository.buscarPorId(29, ENTIDADE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(ENTIDADE_ID, request))
                .isInstanceOf(ResponseStatusException.class);

        verify(vagaRepository).inserir(eq(ENTIDADE_ID), any(), eq("ATIVA"), eq("utilizador"));
        verify(vagaRepository).buscarPorId(29, ENTIDADE_ID);
    }

    @Test
    void criacaoDeColaboradorDeveGravarEntidadeDaUrl() {
        CoordenadorOrientadorServiceImpl service = new CoordenadorOrientadorServiceImpl(
                colaboradorRepository,
                vagaService
        );
        CoordenadorOrientadorRequest request = new CoordenadorOrientadorRequest(
                "19990415M001H",
                126L,
                "João Monteiro",
                "COORDENADOR",
                "Analista",
                "joao@example.cv",
                "9785881",
                "utilizador"
        );
        when(colaboradorRepository.inserir(eq(ENTIDADE_ID), any(), eq(126L), eq("A"), eq("utilizador")))
                .thenReturn(22);
        when(colaboradorRepository.buscarPorId(22, ENTIDADE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(ENTIDADE_ID, request))
                .isInstanceOf(ResponseStatusException.class);

        verify(colaboradorRepository).inserir(eq(ENTIDADE_ID), any(), eq(126L), eq("A"), eq("utilizador"));
        verify(colaboradorRepository).buscarPorId(22, ENTIDADE_ID);
    }

    @Test
    void criacaoDeVisitaDeveGravarEntidadeDaUrlEValidarCandidatos() {
        GestaoVisitaTecnicaServiceImpl service = new GestaoVisitaTecnicaServiceImpl(visitaRepository);
        VisitaTecnicaRequest request = new VisitaTecnicaRequest(
                LocalDate.of(2026, 9, 30),
                "Visitante",
                List.of(new VisitaTecnicaCandidatoRequest(126L, "Candidato")),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Acompanhamento",
                7,
                "CEFP",
                "utilizador"
        );
        when(visitaRepository.listarCandidatos(ENTIDADE_ID))
                .thenReturn(List.of(new VisitaTecnicaCandidatoSelectResponse(126L, "Candidato")));
        when(visitaRepository.buscarCefpDenominacao(7)).thenReturn(Optional.of("CEFP"));
        when(visitaRepository.inserir(eq(ENTIDADE_ID), any(), eq("PENDENTE"), any(), eq("utilizador")))
                .thenReturn(10);
        when(visitaRepository.buscarPorId(10, ENTIDADE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(ENTIDADE_ID, request))
                .isInstanceOf(ResponseStatusException.class);

        verify(visitaRepository).listarCandidatos(ENTIDADE_ID);
        verify(visitaRepository).inserir(eq(ENTIDADE_ID), any(), eq("PENDENTE"), any(), eq("utilizador"));
        verify(visitaRepository).buscarPorId(10, ENTIDADE_ID);
    }

    @Test
    void colocacaoDeveResolverOfertaECandidaturaDentroDaEntidadeDaUrl() {
        ColocacaoCandidatoServiceImpl service = new ColocacaoCandidatoServiceImpl(colocacaoRepository);
        ColocacaoCandidatoRequest request = new ColocacaoCandidatoRequest(
                "OFERTA_ESTAGIO",
                29,
                "4/2026",
                126L,
                "CONTRATO_TERMO",
                6,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2027, 4, 1),
                null,
                null,
                "utilizador"
        );
        when(colocacaoRepository.buscarVinculoCandidatura(29, 126L, ENTIDADE_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(ENTIDADE_ID, request))
                .isInstanceOf(ResponseStatusException.class);

        verify(colocacaoRepository).buscarVinculoCandidatura(29, 126L, ENTIDADE_ID);
    }

    @Test
    void candidaturaDeveSerConsultadaDentroDaEntidadeDaUrl() {
        GestaoCandidaturaServiceImpl service = new GestaoCandidaturaServiceImpl(
                candidaturaRepository,
                documentService
        );
        when(candidaturaRepository.buscarPorId(15, ENTIDADE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(15, ENTIDADE_ID))
                .isInstanceOf(ResponseStatusException.class);

        verify(candidaturaRepository).buscarPorId(15, ENTIDADE_ID);
    }

    private VagaRequest novaVaga() {
        return new VagaRequest(
                "4/2026",
                "OFERTA_ESTAGIO",
                "Estágio",
                null,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 10, 1),
                6,
                "CONTRATO_TERMO",
                "Entidade",
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
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "utilizador"
        );
    }
}
