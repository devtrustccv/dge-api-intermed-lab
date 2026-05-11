package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoRequest;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.AcolhimentoServicoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.AgendamentoEntrevistaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesAcolhimentoRepository;
import java.time.LocalDateTime;
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
public class OrientacaoServicoService {

    private static final String MSG_PEDIDO_OBRIGATORIO = "Corpo do pedido e obrigatorio.";
    private static final String MSG_ENTREVISTA_OBRIGATORIA = "idEntrevista e obrigatorio.";
    private static final String MSG_DETALHES_OBRIGATORIO = "detalhesServico e obrigatorio.";
    private static final String MSG_ENTREVISTA_NAO_ENCONTRADA = "Entrevista nao encontrada.";
    private static final String MSG_ACOLHIMENTO_NAO_ENCONTRADO = "Acolhimento da entrevista nao encontrado.";
    private static final String MSG_SERVICO_NAO_ENCONTRADO = "Servico de acolhimento nao encontrado.";
    private static final String TIPO_RELACAO_SUB_DESEMP = "SUB_DESEMP";
    private static final String ESTADO_ATIVO = "A";

    private final AcolhimentoServicoRepository acolhimentoServicoRepository;
    private final AgendamentoEntrevistaRepository agendamentoEntrevistaRepository;
    private final DetalhesAcolhimentoRepository detalhesAcolhimentoRepository;
    private final DocumentService documentService;

    @Value("${document.orientacao.app-code:interm_laboral}")
    private String appCodeDocumentoOrientacao;

    @Transactional
    public OrientacaoServicoResponse guardar(OrientacaoServicoRequest request) {
        return guardar(request, List.of());
    }

    @Transactional
    public OrientacaoServicoResponse guardar(OrientacaoServicoRequest request, List<MultipartFile> ficheiros) {
        validarRequest(request);

        AgendamentoEntrevista entrevista = agendamentoEntrevistaRepository.findById(request.getIdEntrevista())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ENTREVISTA_NAO_ENCONTRADA));

        if (entrevista.getIdAcolhimento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_ACOLHIMENTO_NAO_ENCONTRADO);
        }

        DetalhesAcolhimento acolhimento = detalhesAcolhimentoRepository.findById(entrevista.getIdAcolhimento())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ACOLHIMENTO_NAO_ENCONTRADO));

        AcolhimentoServico servico = resolverServico(request);
        boolean novo = servico.getId() == null;
        String utilizador = texto(request.getUtilizador());

        servico.setIdAcolhimento(acolhimento.getId());
        servico.setIdEntrevista(entrevista.getId());
        servico.setIdUtente(primeiroInteiro(entrevista.getIdUtente(), acolhimento.getIdUtente()));
        servico.setTipoUtente(textoOuPadrao(request.getTipoUtente(), acolhimento.getTipoUtente()));
        servico.setTipoUtenteDesc(textoOuPadrao(request.getTipoUtenteDesc(), acolhimento.getTipoUtenteDesc()));
        servico.setTipoServico(textoOuPadrao(request.getTipoServico(), primeiroTexto(entrevista.getTipoServico(), acolhimento.getTipoServico())));
        servico.setTipoServicoDesc(textoOuPadrao(request.getTipoServicoDesc(), acolhimento.getTipoServicoDesc()));
        servico.setNecessidadeAnalise(Boolean.TRUE.equals(request.getNecessidadeAnalise()));
        Map<String, Object> detalhesServico = OrientacaoServicoDetalhesMapper.mapear(servico.getTipoServico(), request.getDetalhesServico());
        servico.setDetalhesServico(detalhesServico);
        servico.setDetalhesAnalise(copiarSeNaoVazio(request.getDetalhesAnalise()));

        if (novo) {
            servico.setDateCreate(LocalDateTime.now());
            servico.setUserCreate(utilizador);
        } else {
            servico.setDateUpdate(LocalDateTime.now());
            servico.setUserUpdate(utilizador);
        }

        AcolhimentoServico salvo = acolhimentoServicoRepository.save(servico);
        guardarAnexosSubDesempSeExistirem(salvo, ficheiros);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public OrientacaoServicoResponse buscarPorEntrevista(Integer idEntrevista) {
        AcolhimentoServico servico = acolhimentoServicoRepository.findFirstByIdEntrevistaOrderByIdDesc(idEntrevista)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_SERVICO_NAO_ENCONTRADO));
        return toResponse(servico);
    }

    @Transactional(readOnly = true)
    public OrientacaoServicoResponse buscarPorEntrevistaETipoServico(Integer idEntrevista, String tipoServico) {
        if (idEntrevista == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_ENTREVISTA_OBRIGATORIA);
        }

        String tipoServicoLimpo = texto(tipoServico);
        if (tipoServicoLimpo == null) {
            return buscarPorEntrevista(idEntrevista);
        }

        AcolhimentoServico servico = acolhimentoServicoRepository
                .findFirstByIdEntrevistaAndTipoServicoOrderByIdDesc(idEntrevista, tipoServicoLimpo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_SERVICO_NAO_ENCONTRADO));
        return toResponse(servico);
    }

    private void validarRequest(OrientacaoServicoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_PEDIDO_OBRIGATORIO);
        }
        if (request.getIdEntrevista() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_ENTREVISTA_OBRIGATORIA);
        }
        if (request.getDetalhesServico() == null || request.getDetalhesServico().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_DETALHES_OBRIGATORIO);
        }
    }

    private AcolhimentoServico resolverServico(OrientacaoServicoRequest request) {
        if (request.getIdAcolhimentoServico() != null) {
            return acolhimentoServicoRepository.findById(request.getIdAcolhimentoServico())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_SERVICO_NAO_ENCONTRADO));
        }
        return acolhimentoServicoRepository.findFirstByIdEntrevistaOrderByIdDesc(request.getIdEntrevista())
                .orElseGet(AcolhimentoServico::new);
    }

    private OrientacaoServicoResponse toResponse(AcolhimentoServico servico) {
        return new OrientacaoServicoResponse(
                servico.getId(),
                servico.getIdEntrevista(),
                servico.getIdAcolhimento(),
                servico.getIdUtente(),
                servico.getTipoUtente(),
                servico.getTipoUtenteDesc(),
                servico.getTipoServico(),
                servico.getTipoServicoDesc(),
                servico.getNecessidadeAnalise(),
                servico.getDetalhesServico(),
                servico.getDetalhesAnalise()
        );
    }

    private void guardarAnexosSubDesempSeExistirem(AcolhimentoServico servico, List<MultipartFile> ficheiros) {
        List<MultipartFile> ficheirosValidos = ficheiros == null
                ? List.of()
                : ficheiros.stream().filter(this::temConteudo).toList();

        if (ficheirosValidos.isEmpty() || !isSubDesemp(servico.getTipoServico())) {
            return;
        }

        List<Map<String, Object>> anexos = anexos(servico.getDetalhesServico());
        if (anexos.isEmpty()) {
            return;
        }

        for (int indice = 0; indice < ficheirosValidos.size() && indice < anexos.size(); indice++) {
            MultipartFile ficheiro = ficheirosValidos.get(indice);
            Map<String, Object> anexo = anexos.get(indice);
            String documento = texto(anexo.get("documento"));
            if (documento == null) {
                continue;
            }

            String nomeBase = nomeBaseDocumento(anexo, ficheiro, indice);
            String path = construirPathDocumento(servico.getId(), nomeBase, ficheiro);
            documentService.save(DocRelacaoDTO.builder()
                    .idRelacao(servico.getId())
                    .tipoRelacao(TIPO_RELACAO_SUB_DESEMP)
                    .estado(ESTADO_ATIVO)
                    .idTpDoc(documento)
                    .name(textoOuPadrao(anexo.get("documento_desc"), nomeBase))
                    .fileName(nomeBase)
                    .path(path)
                    .appCode(appCodeDocumentoOrientacao)
                    .file(ficheiro)
                    .build());

            anexo.put("anexo", path);
        }

        Map<String, Object> detalhesAtualizados = new LinkedHashMap<>(servico.getDetalhesServico());
        detalhesAtualizados.put("anexos", anexos);
        servico.setDetalhesServico(detalhesAtualizados);
        acolhimentoServicoRepository.save(servico);
    }

    private boolean temConteudo(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }

    private boolean isSubDesemp(String tipoServico) {
        if (tipoServico == null) {
            return false;
        }
        String tipo = tipoServico.toLowerCase();
        return tipo.contains("sub") || tipo.contains("desemp");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> anexos(Map<String, Object> detalhesServico) {
        Object valor = detalhesServico == null ? null : detalhesServico.get("anexos");
        if (!(valor instanceof List<?> lista)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> anexos = new ArrayList<>();
        for (Object item : lista) {
            if (item instanceof Map<?, ?> mapa) {
                Map<String, Object> anexo = new LinkedHashMap<>();
                mapa.forEach((chave, conteudo) -> {
                    if (chave != null) {
                        anexo.put(chave.toString(), conteudo);
                    }
                });
                anexos.add(anexo);
            }
        }
        return anexos;
    }

    private String nomeBaseDocumento(Map<String, Object> anexo, MultipartFile ficheiro, int indice) {
        String nome = textoOuPadrao(anexo.get("documento_desc"), removerExtensao(ficheiro.getOriginalFilename()));
        if (nome == null || nome.isBlank()) {
            nome = "documento-" + (indice + 1);
        }
        return sanitizar(nome);
    }

    private String construirPathDocumento(Integer idServico, String nomeBase, MultipartFile ficheiro) {
        String ext = extensao(ficheiro.getOriginalFilename());
        return appCodeDocumentoOrientacao
                + "/"
                + LocalDateTime.now().getYear()
                + "/modulos/"
                + TIPO_RELACAO_SUB_DESEMP
                + "/"
                + idServico
                + "/"
                + nomeBase
                + ext;
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
        return valor.trim().replaceAll("[\\\\/:*?\"<>|]+", "_").replace(' ', '_');
    }

    private Map<String, Object> copiarSeNaoVazio(Map<String, Object> origem) {
        return origem == null || origem.isEmpty() ? null : new LinkedHashMap<>(origem);
    }

    private Integer primeiroInteiro(Integer... valores) {
        if (valores == null) {
            return null;
        }
        for (Integer valor : valores) {
            if (valor != null) {
                return valor;
            }
        }
        return null;
    }

    private String primeiroTexto(String... valores) {
        if (valores == null) {
            return null;
        }
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }

    private String textoOuPadrao(Object valor, String padrao) {
        String texto = texto(valor);
        return texto == null ? padrao : texto;
    }

    private String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof String texto) {
            String limpo = texto.trim();
            return limpo.isEmpty() ? null : limpo;
        }
        if (valor instanceof Number || valor instanceof Boolean) {
            return valor.toString();
        }
        if (valor instanceof Map<?, ?> mapa) {
            List<String> chaves = List.of("descricao", "description", "label", "nome", "denominacao", "valor", "value", "codigo", "code", "id");
            for (String chave : chaves) {
                Object encontrado = mapa.get(chave);
                String texto = texto(encontrado);
                if (texto != null) {
                    return texto;
                }
            }
        }
        return valor.toString();
    }
}
