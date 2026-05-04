package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEmpresaRequest;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEmpresaResponse;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesAcolhimentoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.EntidadeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AcolhimentoEmpresaService {

    private static final String TIPO_RELACAO_EMPRESA = "EMPREGO_T_DETALHES_ACOLHIMENTO_EMPRESA";
    private static final String TIPO_UTENTE_EMPRESA = "ENTIDADE";
    private static final String CANAL_CEFP = "CEFP";
    private static final String ESTADO_ATIVO = "A";
    private static final DateTimeFormatter INSCRICAO_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final EntidadeRepository entidadeRepository;
    private final DetalhesAcolhimentoRepository detalhesAcolhimentoRepository;
    private final DocumentService documentService;

    @Value("${document.empresa.app-code:interm_laboral}")
    private String appCodeDocumentoEmpresa;

    @Transactional
    public AcolhimentoEmpresaResponse registar(AcolhimentoEmpresaRequest request) {
        return registar(request, List.of());
    }

    @Transactional
    public AcolhimentoEmpresaResponse registar(AcolhimentoEmpresaRequest request, List<MultipartFile> ficheiros) {
        validar(request);

        String utilizador = texto(campo(request, request.getUtilizador(), "user_create", "utilizador"));
        Entidade entidade = obterOuCriarEntidade(request, utilizador);
        Map<String, Object> detalhes = montarDetalhes(request);

        DetalhesAcolhimento acolhimento = obterOuCriarAcolhimento(request);
        boolean novo = acolhimento.getId() == null;

        acolhimento.setIdEntidade(entidade.getId());
        acolhimento.setDenominacaoUtente(entidade.getDenominacao());
        acolhimento.setNif(entidade.getNif() == null ? null : BigDecimal.valueOf(entidade.getNif()));
        acolhimento.setCefpId(inteiro(campo(request, request.getIdCefp(), "id_cefp", "cefp_id", "cefp_de_acolhimento")));
        acolhimento.setOrgId(inteiro(campo(request, request.getOrgId(), "org_id", "organization_id", "organizationId")));
        acolhimento.setCanal(CANAL_CEFP);
        acolhimento.setCanalDesc(CANAL_CEFP);
        acolhimento.setTipoUtente(TIPO_UTENTE_EMPRESA);
        acolhimento.setTipoUtenteDesc(TIPO_UTENTE_EMPRESA);
        acolhimento.setFonteInformacao(texto(campo(request, request.getFonteInformacao(), "fonte_informacao", "como_obteve_informacoes_sobre_servicos_do_iefp")));
        acolhimento.setIdTecnicoAtendimento(inteiro(campo(request, request.getIdTecnico(), "id_tecnico", "id_tecnico_atendimento", "tecnico_id")));
        acolhimento.setTecnicoAtendimento(texto(campo(request, request.getTecnicoAtendimento(), "tecnico_atendimento", "tecnico")));
        acolhimento.setDetalhes(detalhes);

        if (novo) {
            acolhimento.setNumInscricao(gerarNumeroInscricao());
            acolhimento.setUserCreate(utilizador);
        } else {
            acolhimento.setDateUpdate(LocalDateTime.now());
            acolhimento.setUserUpdate(utilizador);
        }

        DetalhesAcolhimento salvo = detalhesAcolhimentoRepository.save(acolhimento);
        guardarAnexosSeExistirem(salvo, ficheiros);

        return new AcolhimentoEmpresaResponse(
                salvo.getId(),
                entidade.getId(),
                salvo.getNumInscricao(),
                salvo.getCefpId(),
                salvo.getOrgId(),
                salvo.getDetalhes()
        );
    }

    private void validar(AcolhimentoEmpresaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corpo do pedido e obrigatorio.");
        }
        String nif = texto(campo(request, request.getNif(), "nif"));
        if (nif == null || nif.length() != 9) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O NIF deve ter 9 digitos.");
        }
        if (texto(campo(request, request.getNomeDaEntidade(), "nome_da_entidade", "denominacao", "denominacao_utente")) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome da entidade.");
        }
        if (texto(campo(request, request.getRegistoComercial(), "registo_comercial", "registo_social")) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o registo comercial.");
        }
    }

    private Entidade obterOuCriarEntidade(AcolhimentoEmpresaRequest request, String utilizador) {
        Entidade entidade = null;
        Integer idEntidade = inteiro(campo(request, request.getIdEntidade(), "id_entidade", "entidade_id"));
        if (idEntidade != null) {
            entidade = entidadeRepository.findById(idEntidade).orElse(null);
        }
        if (entidade == null) {
            entidade = entidadeRepository.findFirstByNif(inteiro(campo(request, request.getNif(), "nif"))).orElseGet(Entidade::new);
        }

        boolean novo = entidade.getId() == null;
        entidade.setDenominacao(texto(campo(request, request.getNomeDaEntidade(), "nome_da_entidade", "denominacao", "denominacao_utente")));
        entidade.setNif(inteiro(campo(request, request.getNif(), "nif")));
        entidade.setRegistoSocial(texto(campo(request, request.getRegistoComercial(), "registo_comercial", "registo_social")));
        entidade.setNaturezaJuridica(texto(campo(request, request.getNaturezaJuridica(), "natureza_juridica")));
        if (novo) {
            entidade.setUserCreate(utilizador);
        } else {
            entidade.setDateUpdate(LocalDateTime.now());
            entidade.setUserUpdate(utilizador);
        }
        return entidadeRepository.save(entidade);
    }

    private DetalhesAcolhimento obterOuCriarAcolhimento(AcolhimentoEmpresaRequest request) {
        if (request.getId() == null) {
            return new DetalhesAcolhimento();
        }
        return detalhesAcolhimentoRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acolhimento nao encontrado."));
    }

    private Map<String, Object> montarDetalhes(AcolhimentoEmpresaRequest request) {
        Map<String, Object> detalhes = new LinkedHashMap<>(request.getDetalhes());
        colocar(detalhes, "nome_da_entidade", campo(request, request.getNomeDaEntidade(), "nome_da_entidade", "denominacao", "denominacao_utente"));
        colocar(detalhes, "nif", campo(request, request.getNif(), "nif"));
        colocar(detalhes, "registo_comercial", campo(request, request.getRegistoComercial(), "registo_comercial", "registo_social"));
        colocar(detalhes, "natureza_juridica", campo(request, request.getNaturezaJuridica(), "natureza_juridica"));
        colocar(detalhes, "n_colaboradores", campo(request, request.getNColaboradores(), "n_colaboradores"));
        colocar(detalhes, "morada_sede", campo(request, request.getMoradaSede(), "morada_sede", "morada__da_sede"));
        colocar(detalhes, "codigo_postal", campo(request, request.getCodigoPostal(), "codigo_postal"));
        colocar(detalhes, "responsavel_pela_entidade", campo(request, request.getResponsavelPelaEntidade(), "responsavel_pela_entidade", "responavel_pela_entidade"));
        colocar(detalhes, "ilha", campo(request, request.getIlha(), "ilha"));
        colocar(detalhes, "concelho", campo(request, request.getConcelho(), "concelho"));
        colocar(detalhes, "zona", campo(request, request.getZona(), "zona", "zona_"));
        colocar(detalhes, "endereco", campo(request, request.getEndereco(), "endereco"));
        colocar(detalhes, "telefone", campo(request, request.getTelefone(), "telefone"));
        colocar(detalhes, "telemovel", campo(request, request.getTelemovel(), "telemovel"));
        colocar(detalhes, "email", campo(request, request.getEmail(), "email"));
        colocar(detalhes, "fax", campo(request, request.getFax(), "fax"));
        colocar(detalhes, "possui_vagas_de_emprego", campo(request, request.getPossuiVagasDeEmprego(), "possui_vagas_de_emprego"));
        colocar(detalhes, "identificacao_posto_trabalho", campo(request, request.getIdentificacaoPostoTrabalho(), "identificacao_posto_trabalho", "identificacao"));
        detalhes.put("anexos", mapearAnexos(lista(request.getAnexos(), campo(request, null, "anexos", "documentos", "formlist_1"))));
        detalhes.put("caes", mapearCaes(lista(request.getCaes(), campo(request, null, "caes", "separatorlist_1"))));
        return detalhes;
    }

    private List<Map<String, Object>> mapearCaes(List<Map<String, Object>> origem) {
        List<Map<String, Object>> destino = new ArrayList<>();
        for (Map<String, Object> itemOrigem : origem == null ? List.<Map<String, Object>>of() : origem) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", valor(itemOrigem, "id", "separatorlist_1_id"));
            item.put("cae", valor(itemOrigem, "cae"));
            item.put("cae_desc", valor(itemOrigem, "cae_desc"));
            item.put("setor_de_atividade", valor(itemOrigem, "setor_de_atividade"));
            item.put("setor_de_atividade_desc", valor(itemOrigem, "setor_de_atividade_desc"));
            item.put("principal", valor(itemOrigem, "principal"));
            item.put("principal_desc", valor(itemOrigem, "principal_desc"));
            destino.add(item);
        }
        return destino;
    }

    private List<Map<String, Object>> mapearAnexos(List<Map<String, Object>> origem) {
        List<Map<String, Object>> destino = new ArrayList<>();
        for (Map<String, Object> itemOrigem : origem == null ? List.<Map<String, Object>>of() : origem) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", valor(itemOrigem, "id"));
            item.put("tipo_documento_anexo", valor(itemOrigem, "tipo_documento_anexo", "tipo_documento", "documento"));
            item.put("tipo_documento_anexo_desc", valor(itemOrigem, "tipo_documento_anexo_desc", "tipo_documento_desc", "documento_desc"));
            Object anexo = valor(itemOrigem, "anexo", "path");
            if (anexo != null) {
                item.put("anexo", anexo);
            }
            destino.add(item);
        }
        return destino;
    }

    private void guardarAnexosSeExistirem(DetalhesAcolhimento acolhimento, List<MultipartFile> ficheiros) {
        List<MultipartFile> ficheirosValidos = ficheiros == null
                ? List.of()
                : ficheiros.stream().filter(this::temConteudo).toList();
        List<Map<String, Object>> anexos = anexos(acolhimento.getDetalhes());
        if (ficheirosValidos.isEmpty() || anexos.isEmpty()) {
            return;
        }

        for (int indice = 0; indice < ficheirosValidos.size() && indice < anexos.size(); indice++) {
            MultipartFile ficheiro = ficheirosValidos.get(indice);
            Map<String, Object> anexo = anexos.get(indice);
            String tipoDocumento = texto(anexo.get("tipo_documento_anexo"));
            if (tipoDocumento == null) {
                continue;
            }
            String nomeBase = sanitizar(textoOuPadrao(anexo.get("tipo_documento_anexo_desc"), removerExtensao(ficheiro.getOriginalFilename())));
            String path = construirPathDocumento(acolhimento.getId(), nomeBase, ficheiro);
            documentService.save(DocRelacaoDTO.builder()
                    .idRelacao(acolhimento.getId())
                    .tipoRelacao(TIPO_RELACAO_EMPRESA)
                    .estado(ESTADO_ATIVO)
                    .idTpDoc(tipoDocumento)
                    .name(textoOuPadrao(anexo.get("tipo_documento_anexo_desc"), nomeBase))
                    .fileName(nomeBase)
                    .path(path)
                    .appCode(appCodeDocumentoEmpresa)
                    .file(ficheiro)
                    .build());
            anexo.put("anexo", path);
        }

        Map<String, Object> detalhes = new LinkedHashMap<>(acolhimento.getDetalhes());
        detalhes.put("anexos", anexos);
        acolhimento.setDetalhes(detalhes);
        detalhesAcolhimentoRepository.save(acolhimento);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> anexos(Map<String, Object> detalhes) {
        Object valor = detalhes == null ? null : detalhes.get("anexos");
        if (!(valor instanceof List<?> lista)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> anexos = new ArrayList<>();
        for (Object item : lista) {
            if (item instanceof Map<?, ?> mapa) {
                Map<String, Object> convertido = new LinkedHashMap<>();
                mapa.forEach((chave, conteudo) -> {
                    if (chave != null) {
                        convertido.put(chave.toString(), conteudo);
                    }
                });
                anexos.add(convertido);
            }
        }
        return anexos;
    }

    private String gerarNumeroInscricao() {
        String prefixo = "ACO" + LocalDate.now().format(INSCRICAO_DATE) + "/";
        Integer sequencia = detalhesAcolhimentoRepository.proximoNumeroInscricao(prefixo);
        return prefixo + (sequencia == null || sequencia < 1 ? 1 : sequencia);
    }

    private void colocar(Map<String, Object> destino, String chave, Object valor) {
        if (valor != null) {
            destino.put(chave, valor);
        } else {
            destino.putIfAbsent(chave, "");
        }
    }

    private Object valor(Map<String, Object> origem, String... chaves) {
        if (origem == null || chaves == null) {
            return null;
        }
        for (String chave : chaves) {
            for (Map.Entry<String, Object> entrada : origem.entrySet()) {
                if (entrada.getKey() != null && entrada.getKey().equalsIgnoreCase(chave)) {
                    return entrada.getValue();
                }
            }
        }
        return null;
    }

    private Object campo(AcolhimentoEmpresaRequest request, Object valorDireto, String... chaves) {
        if (valorDireto != null) {
            return valorDireto;
        }
        return valor(request.getDetalhes(), chaves);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> lista(List<Map<String, Object>> valorDireto, Object valorDetalhes) {
        if (valorDireto != null && !valorDireto.isEmpty()) {
            return valorDireto;
        }
        if (!(valorDetalhes instanceof List<?> lista)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> destino = new ArrayList<>();
        for (Object item : lista) {
            if (item instanceof Map<?, ?> mapa) {
                Map<String, Object> convertido = new LinkedHashMap<>();
                mapa.forEach((chave, valor) -> {
                    if (chave != null) {
                        convertido.put(chave.toString(), valor);
                    }
                });
                destino.add(convertido);
            }
        }
        return destino;
    }

    private boolean temConteudo(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }

    private String construirPathDocumento(Integer idAcolhimento, String nomeBase, MultipartFile ficheiro) {
        return appCodeDocumentoEmpresa
                + "/"
                + LocalDateTime.now().getYear()
                + "/modulos/ACOLHIMENTO_EMPRESA/"
                + idAcolhimento
                + "/"
                + nomeBase
                + extensao(ficheiro.getOriginalFilename());
    }

    private String removerExtensao(String nomeFicheiro) {
        if (nomeFicheiro == null || nomeFicheiro.isBlank()) {
            return null;
        }
        int indice = nomeFicheiro.lastIndexOf('.');
        return indice <= 0 ? nomeFicheiro : nomeFicheiro.substring(0, indice);
    }

    private String extensao(String nomeFicheiro) {
        if (nomeFicheiro == null || !nomeFicheiro.contains(".")) {
            return "";
        }
        return nomeFicheiro.substring(nomeFicheiro.lastIndexOf('.'));
    }

    private String sanitizar(String valor) {
        String texto = valor == null ? "documento" : valor.trim();
        return texto.isEmpty() ? "documento" : texto.replaceAll("[\\\\/:*?\"<>|]+", "_").replace(' ', '_');
    }

    private String textoOuPadrao(Object valor, String padrao) {
        String texto = texto(valor);
        return texto == null ? padrao : texto;
    }

    private String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.toString().trim();
        return texto.isEmpty() ? null : texto;
    }

    private Integer inteiro(Object valor) {
        String texto = texto(valor);
        if (texto == null) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
