package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CertificadoEstagioEmissaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CertificadoEstagioResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.PerfilCandidatoCertificadoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.PerfilCandidatoCertificadoRepository.CertificadoEmitido;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.PerfilCandidatoCertificadoRepository.FonteEmprego;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.PerfilCandidatoCertificadoRepository.FontePessoa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PerfilCandidatoCertificadoServiceImpl implements PerfilCandidatoCertificadoService {

    private static final DateTimeFormatter FORMATO_CERTIFICADO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PerfilCandidatoCertificadoRepository certificadoRepository;

    @Override
    @Transactional(readOnly = true)
    public CertificadoEstagioResponse consultar(Integer colocacaoId, Long pessoaId) {
        validarIdentificadores(colocacaoId, pessoaId);
        return certificadoRepository.buscarEmitido(colocacaoId, pessoaId)
                .map(this::paraResponse)
                .orElseGet(() -> montarPrevisualizacao(colocacaoId, pessoaId));
    }

    @Override
    @Transactional
    public CertificadoEstagioResponse emitir(
            Integer colocacaoId,
            Long pessoaId,
            CertificadoEstagioEmissaoRequest request
    ) {
        validarIdentificadores(colocacaoId, pessoaId);
        if (request == null) {
            throw erro("Informe a assinatura do responsável pela emissão do certificado.");
        }

        String assinatura = obrigatorio(
                request.assinatura(),
                "Informe a assinatura do responsável pela emissão do certificado."
        );
        String utilizador = obrigatorio(
                request.utilizador(),
                "Não foi possível identificar quem está a emitir o certificado. Inicie sessão novamente e tente de novo."
        );
        if (utilizador.length() > 150) {
            throw erro("Não foi possível identificar corretamente quem está a emitir o certificado.");
        }

        if (certificadoRepository.buscarEmitido(colocacaoId, pessoaId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O certificado deste estágio já foi emitido. Consulte o certificado existente."
            );
        }

        CertificadoEstagioResponse previsualizacao = montarPrevisualizacao(colocacaoId, pessoaId);
        validarDadosParaEmissao(previsualizacao);

        CertificadoEmitido certificado = new CertificadoEmitido(
                null,
                previsualizacao.colocacaoId(),
                previsualizacao.pessoaId(),
                previsualizacao.candidaturaId(),
                previsualizacao.nome(),
                previsualizacao.naturalidade(),
                previsualizacao.dataNascimento(),
                previsualizacao.numeroDocumento(),
                previsualizacao.habilitacaoAcademica(),
                previsualizacao.nomeEntidade(),
                previsualizacao.dataInicio(),
                previsualizacao.dataFim(),
                previsualizacao.classificacaoFinal(),
                assinatura,
                gerarCodigoContraprova(),
                LocalDateTime.now(),
                utilizador
        );

        try {
            certificadoRepository.inserir(certificado);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O certificado deste estágio já foi emitido. Consulte o certificado existente.",
                    ex
            );
        }

        return certificadoRepository.buscarEmitido(colocacaoId, pessoaId)
                .map(this::paraResponse)
                .orElseThrow(() -> new IllegalStateException(
                        "Não foi possível carregar o certificado depois da emissão."
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public CertificadoEstagioResponse validar(String codigoContraprova) {
        String codigo = obrigatorio(
                codigoContraprova,
                "Informe o código apresentado no certificado."
        ).toUpperCase(Locale.ROOT);
        return certificadoRepository.buscarEmitidoPorCodigo(codigo)
                .map(this::paraResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi encontrado um certificado válido com o código informado. Confirme o código e tente novamente."
                ));
    }

    private CertificadoEstagioResponse montarPrevisualizacao(Integer colocacaoId, Long pessoaId) {
        FonteEmprego emprego = certificadoRepository.buscarFonteEmprego(colocacaoId, pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi encontrado um estágio associado ao seu perfil. Atualize a página ou contacte o serviço de atendimento."
                ));
        FontePessoa pessoa = certificadoRepository.buscarFontePessoa(pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Não foi possível encontrar os seus dados pessoais. Atualize a página ou contacte o serviço de atendimento."
                ));

        return criarResponse(
                null,
                emprego.colocacaoId(),
                emprego.pessoaId(),
                emprego.candidaturaId(),
                emprego.nome(),
                pessoa.naturalidade(),
                pessoa.dataNascimento(),
                pessoa.numeroDocumento(),
                emprego.habilitacaoAcademica(),
                emprego.nomeEntidade(),
                emprego.dataInicio(),
                emprego.dataFim(),
                emprego.classificacaoFinal(),
                null,
                null,
                null
        );
    }

    private CertificadoEstagioResponse paraResponse(CertificadoEmitido certificado) {
        return criarResponse(
                certificado.id(),
                certificado.colocacaoId(),
                certificado.pessoaId(),
                certificado.candidaturaId(),
                certificado.nome(),
                certificado.naturalidade(),
                certificado.dataNascimento(),
                certificado.numeroDocumento(),
                certificado.habilitacaoAcademica(),
                certificado.nomeEntidade(),
                certificado.dataInicio(),
                certificado.dataFim(),
                certificado.classificacaoFinal(),
                certificado.assinatura(),
                certificado.dataEmissao(),
                certificado.codigoContraprova()
        );
    }

    private CertificadoEstagioResponse criarResponse(
            Integer certificadoId,
            Integer colocacaoId,
            Long pessoaId,
            Integer candidaturaId,
            String nome,
            String naturalidade,
            LocalDate dataNascimento,
            String numeroDocumento,
            String habilitacaoAcademica,
            String nomeEntidade,
            LocalDate dataInicio,
            LocalDate dataFim,
            BigDecimal classificacaoFinal,
            String assinatura,
            LocalDateTime dataEmissao,
            String codigoContraprova
    ) {
        Map<String, String> substituicoes = new LinkedHashMap<>();
        substituicoes.put("$NOME$", texto(nome));
        substituicoes.put("$NATURALIDADE$", texto(naturalidade));
        substituicoes.put("$DATA_NASCIMENTO$", data(dataNascimento));
        substituicoes.put("$NUM_DOCUMENTO$", texto(numeroDocumento));
        substituicoes.put("$HABILITACAO_ACADEMICA$", texto(habilitacaoAcademica));
        substituicoes.put("$NOME_ENTIDADE$", texto(nomeEntidade));
        substituicoes.put("$DATA_INICIO$", data(dataInicio));
        substituicoes.put("$DATA_FIM$", data(dataFim));
        substituicoes.put("$CLASSIFICACAO_FINAL$", decimal(classificacaoFinal));
        substituicoes.put("$ASSINATURA$", texto(assinatura));
        substituicoes.put("$DATA$", dataEmissao == null ? "" : data(dataEmissao.toLocalDate()));
        substituicoes.put("$CODIGO_CONTRAPROVA$", texto(codigoContraprova));

        return new CertificadoEstagioResponse(
                certificadoId,
                colocacaoId,
                pessoaId,
                candidaturaId,
                nome,
                naturalidade,
                dataNascimento,
                numeroDocumento,
                habilitacaoAcademica,
                nomeEntidade,
                dataInicio,
                dataFim,
                classificacaoFinal,
                assinatura,
                dataEmissao,
                codigoContraprova,
                certificadoId != null,
                Collections.unmodifiableMap(substituicoes)
        );
    }

    private void validarDadosParaEmissao(CertificadoEstagioResponse certificado) {
        List<String> camposEmFalta = new ArrayList<>();
        adicionarSeVazio(camposEmFalta, certificado.nome(), "nome do estagiário");
        adicionarSeVazio(camposEmFalta, certificado.naturalidade(), "naturalidade");
        if (certificado.dataNascimento() == null) {
            camposEmFalta.add("data de nascimento");
        }
        adicionarSeVazio(camposEmFalta, certificado.numeroDocumento(), "número do documento");
        adicionarSeVazio(camposEmFalta, certificado.habilitacaoAcademica(), "habilitação académica");
        adicionarSeVazio(camposEmFalta, certificado.nomeEntidade(), "nome da entidade");
        if (certificado.dataInicio() == null) {
            camposEmFalta.add("data de início do estágio");
        }
        if (certificado.dataFim() == null) {
            camposEmFalta.add("data de fim do estágio");
        }
        if (certificado.classificacaoFinal() == null) {
            camposEmFalta.add("classificação da avaliação final");
        }

        if (!camposEmFalta.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O certificado ainda não pode ser emitido porque faltam os seguintes dados: "
                            + String.join(", ", camposEmFalta)
                            + ". Atualize os dados indicados e tente novamente."
            );
        }
    }

    private void adicionarSeVazio(List<String> campos, String valor, String descricao) {
        if (valor == null || valor.isBlank()) {
            campos.add(descricao);
        }
    }

    private void validarIdentificadores(Integer colocacaoId, Long pessoaId) {
        if (pessoaId == null || pessoaId <= 0) {
            throw erro("Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo.");
        }
        if (colocacaoId == null || colocacaoId <= 0) {
            throw erro("Selecione o estágio para consultar ou emitir o certificado.");
        }
    }

    private String obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw erro(mensagem);
        }
        return valor.trim();
    }

    private String gerarCodigoContraprova() {
        return "CERT-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private String data(LocalDate valor) {
        return valor == null ? "" : FORMATO_CERTIFICADO.format(valor);
    }

    private String decimal(BigDecimal valor) {
        return valor == null ? "" : valor.stripTrailingZeros().toPlainString();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}

