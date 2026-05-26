package cv.dge.dge_api_intermed_lab.application.acolhimento.mapper;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoDadosEmpregoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoCompletoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEmpresaResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEntidadeResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoPessoaResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoReporterResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.CefpReporterDTO;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.EntidadeReporterDTO;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.UtenteReporterDTO;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesEmpregoUtente;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.ParamReport;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AcolhimentoMapper {

    public AcolhimentoPessoaResponse toPessoaResponse(
            Integer idPessoa,
            List<AcolhimentoCompletoResponse> acolhimentos
    ) {
        return new AcolhimentoPessoaResponse(idPessoa, acolhimentos.size(), acolhimentos);
    }

    public AcolhimentoEntidadeResponse toEntidadeResponse(
            Integer globalIdEntidade,
            List<AcolhimentoCompletoResponse> acolhimentos
    ) {
        return new AcolhimentoEntidadeResponse(globalIdEntidade, acolhimentos.size(), acolhimentos);
    }

    public AcolhimentoCompletoResponse toCompletoResponse(
            DetalhesAcolhimento acolhimento,
            Cefp cefp,
            Utente utente,
            Entidade entidade,
            AcolhimentoDadosEmpregoResponse dadosEmprego,
            OrientacaoEntrevistaResponse entrevista,
            List<DocumentoResponseDTO> documentos,
            Map<String, Object> detalhes
    ) {
        return new AcolhimentoCompletoResponse(
                acolhimento.getId(),
                acolhimento.getIdPessoa(),
                acolhimento.getIdUtente(),
                acolhimento.getIdEntidade(),
                acolhimento.getDenominacaoUtente(),
                acolhimento.getNif(),
                acolhimento.getCefpId(),
                acolhimento.getOrgId(),
                acolhimento.getTipoUtente(),
                acolhimento.getTipoUtenteDesc(),
                acolhimento.getTipoServico(),
                acolhimento.getTipoServicoDesc(),
                acolhimento.getCanal(),
                acolhimento.getCanalDesc(),
                acolhimento.getFonteInformacao(),
                acolhimento.getStatusEntrevista(),
                acolhimento.getNumInscricao(),
                acolhimento.getIdTecnicoAtendimento(),
                acolhimento.getTecnicoAtendimento(),
                acolhimento.getDateCreate(),
                acolhimento.getUserCreate(),
                acolhimento.getDateUpdate(),
                acolhimento.getUserUpdate(),
                toCefpReporterDTO(cefp),
                toUtenteReporterDTO(utente, detalhes),
                toEntidadeReporterDTO(entidade),
                dadosEmprego,
                entrevista,
                null,
                documentos,
                detalhes
        );
    }

    public AcolhimentoReporterResponse toReporterResponse(
            DetalhesAcolhimento acolhimento,
            ParamReport paramReport,
            Cefp cefp,
            Utente utente,
            Entidade entidade,
            Map<String, Object> detalhesReporter
    ) {
        return new AcolhimentoReporterResponse(
                acolhimento.getId(),
                paramReport == null ? null : paramReport.getLogotipoIefp(),
                paramReport == null ? null : paramReport.getLogotipoDge(),
                cefp == null ? null : cefp.getDenominacao(),
                cefp == null ? null : cefp.getEndereco(),
                cefp == null ? null : cefp.getTelefone(),
                cefp == null ? null : cefp.getEmail(),
                acolhimento.getDateCreate(),
                acolhimento.getNumInscricao(),
                utente == null ? null : utente.getId(),
                utente == null ? null : utente.getNome(),
                acolhimento.getTecnicoAtendimento(),
                toCefpReporterDTO(cefp),
                toUtenteReporterDTO(utente, acolhimento.getDetalhes()),
                toEntidadeReporterDTO(entidade),
                detalhesReporter
        );
    }

    public AcolhimentoRegistoResponse toRegistoResponse(DetalhesAcolhimento acolhimento, Utente utente) {
        return new AcolhimentoRegistoResponse(
                acolhimento.getId(),
                utente.getId(),
                acolhimento.getIdPessoa(),
                acolhimento.getNumInscricao(),
                acolhimento.getCefpId(),
                acolhimento.getOrgId(),
                acolhimento.getDetalhes()
        );
    }

    public AcolhimentoEmpresaResponse toEmpresaResponse(DetalhesAcolhimento acolhimento, Entidade entidade) {
        return new AcolhimentoEmpresaResponse(
                acolhimento.getId(),
                entidade.getId(),
                acolhimento.getNumInscricao(),
                acolhimento.getCefpId(),
                acolhimento.getOrgId(),
                acolhimento.getDetalhes()
        );
    }

    public CefpReporterDTO toCefpReporterDTO(Cefp cefp) {
        if (cefp == null) {
            return null;
        }
        return new CefpReporterDTO(
                cefp.getId(),
                cefp.getOrganizationId(),
                cefp.getDenominacao(),
                cefp.getEndereco(),
                cefp.getTelefone(),
                cefp.getEmail()
        );
    }

    public UtenteReporterDTO toUtenteReporterDTO(Utente utente, Map<String, Object> detalhes) {
        if (utente == null) {
            return null;
        }
        return new UtenteReporterDTO(
                utente.getId(),
                utente.getPessoaId(),
                utente.getNome(),
                utente.getDataNascimento(),
                utente.getTipoDocumento(),
                utente.getNumDocumento(),
                dataEmissaoDocumento(detalhes),
                utente.getSexo(),
                utente.getNif(),
                utente.getHabilitacaoLiteraria()
        );
    }

    public EntidadeReporterDTO toEntidadeReporterDTO(Entidade entidade) {
        if (entidade == null) {
            return null;
        }
        return new EntidadeReporterDTO(
                entidade.getId(),
                entidade.getGlobalIdEntidade(),
                entidade.getDenominacao(),
                entidade.getNif(),
                entidade.getRegistoSocial(),
                entidade.getNaturezaJuridica()
        );
    }

    public AcolhimentoDadosEmpregoResponse toDadosEmpregoResponse(DetalhesEmpregoUtente emprego) {
        return new AcolhimentoDadosEmpregoResponse(
                emprego.getId(),
                emprego.getIdPessoa(),
                emprego.getIdUtente(),
                emprego.getSituacaoEmprego(),
                emprego.getProfissao(),
                emprego.getEmpresa(),
                emprego.getSetorAtividade(),
                emprego.getIlha(),
                emprego.getConcelho(),
                emprego.getZona(),
                emprego.getTelefone(),
                emprego.getNumTrabalhador(),
                emprego.getDuracao(),
                emprego.getDateCreate(),
                emprego.getUserCreate(),
                emprego.getDateUpdate(),
                emprego.getUserUpdate()
        );
    }

    private LocalDate dataEmissaoDocumento(Map<String, Object> detalhes) {
        return data(primeiro(
                valor(detalhes, "dataEmissaoDocumento", "data_emissao_documento", "dataEmissao", "data_emissao", "data_de_emissao"),
                procurarProfundo(detalhes, "dataEmissaoDocumento", "data_emissao_documento", "dataEmissao", "data_emissao", "data_de_emissao")
        ));
    }

    private Object procurarProfundo(Object origem, String... chaves) {
        if (origem == null || chaves == null || chaves.length == 0) {
            return null;
        }
        if (origem instanceof Map<?, ?> mapa) {
            Object direto = valor(mapa, chaves);
            if (direto != null) {
                return direto;
            }
            for (Object valor : mapa.values()) {
                Object encontrado = procurarProfundo(valor, chaves);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        if (origem instanceof List<?> lista) {
            for (Object item : lista) {
                Object encontrado = procurarProfundo(item, chaves);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private Object valor(Map<?, ?> origem, String... chaves) {
        if (origem == null || origem.isEmpty()) {
            return null;
        }
        for (String chave : chaves) {
            for (Map.Entry<?, ?> entrada : origem.entrySet()) {
                if (entrada.getKey() != null && entrada.getKey().toString().equalsIgnoreCase(chave)) {
                    return entrada.getValue();
                }
            }
        }
        return null;
    }

    private Object primeiro(Object... valores) {
        if (valores == null) {
            return null;
        }
        for (Object valor : valores) {
            if (valor != null && (!(valor instanceof String texto) || !texto.trim().isEmpty())) {
                return valor;
            }
        }
        return null;
    }

    private LocalDate data(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty()) {
            return null;
        }
        List<DateTimeFormatter> formatos = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );
        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDate.parse(texto, formato);
            } catch (DateTimeParseException ignored) {
                // Tenta o proximo formato aceito pelo formulario.
            }
        }
        return null;
    }
}
