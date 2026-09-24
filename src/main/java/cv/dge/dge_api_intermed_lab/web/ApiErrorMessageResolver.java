package cv.dge.dge_api_intermed_lab.web;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public final class ApiErrorMessageResolver {

    private ApiErrorMessageResolver() {
    }

    public static String parametroObrigatorio(MissingServletRequestParameterException ex) {
        return "O parâmetro obrigatório \"" + ex.getParameterName() + "\" não foi informado."
                + " Informe " + descricaoTipo(ex.getParameterType()) + ".";
    }

    public static String parteObrigatoria(MissingServletRequestPartException ex) {
        return "A parte obrigatória \"" + ex.getRequestPartName()
                + "\" não foi enviada no pedido multipart/form-data.";
    }

    public static String parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return "O valor \"" + valorParaMensagem(ex.getValue()) + "\" informado no parâmetro \""
                + ex.getName() + "\" não tem o formato esperado. Informe "
                + descricaoTipo(ex.getRequiredType()) + ".";
    }

    public static String corpoInvalido(Throwable ex, String contexto) {
        InvalidFormatException formato = encontrarCausa(ex, InvalidFormatException.class);
        if (formato != null) {
            return "O valor \"" + valorParaMensagem(formato.getValue()) + "\" informado no campo \""
                    + caminho(formato) + "\" de " + contexto + " não tem o formato esperado. Informe "
                    + descricaoTipo(formato.getTargetType()) + ".";
        }

        UnrecognizedPropertyException desconhecido = encontrarCausa(ex, UnrecognizedPropertyException.class);
        if (desconhecido != null) {
            return "O campo \"" + desconhecido.getPropertyName() + "\" enviado em " + contexto
                    + " não é reconhecido pela API. Remova-o ou confirme o nome esperado.";
        }

        MismatchedInputException incompatibilidade = encontrarCausa(ex, MismatchedInputException.class);
        if (incompatibilidade != null) {
            return "O campo \"" + caminho(incompatibilidade) + "\" de " + contexto
                    + " contém um valor incompatível. Informe "
                    + descricaoTipo(incompatibilidade.getTargetType()) + ".";
        }

        JsonParseException sintaxe = encontrarCausa(ex, JsonParseException.class);
        if (sintaxe != null) {
            JsonLocation local = sintaxe.getLocation();
            String posicao = local == null
                    ? ""
                    : " na linha " + local.getLineNr() + ", coluna " + local.getColumnNr();
            return "O JSON enviado em " + contexto + " está malformado" + posicao
                    + ". Corrija a sintaxe e envie novamente.";
        }

        return "O corpo enviado em " + contexto
                + " não corresponde à estrutura esperada. Confirme os nomes dos campos, os tipos dos valores"
                + " e a sintaxe JSON.";
    }

    public static String operacao(HttpServletRequest request) {
        if (request == null) {
            return "endpoint não identificado";
        }
        return request.getMethod() + " " + request.getRequestURI();
    }

    public static String novaReferencia() {
        return UUID.randomUUID().toString();
    }

    public static String falhaBaseDados(HttpServletRequest request, String referencia) {
        return "A operação \"" + operacao(request)
                + "\" não pôde ser concluída devido a uma falha de acesso à base de dados."
                + " Referência para suporte: " + referencia + ".";
    }

    public static String falhaInterna(HttpServletRequest request, String referencia) {
        return "A operação \"" + operacao(request)
                + "\" não pôde ser concluída devido a um erro interno não previsto."
                + " Referência para suporte: " + referencia + ".";
    }

    public static String motivoAusente(HttpServletRequest request, int status, String referencia) {
        return "A operação \"" + operacao(request) + "\" foi rejeitada com o estado HTTP " + status
                + ", mas o serviço não informou o motivo. Referência para suporte: " + referencia + ".";
    }

    private static String caminho(JsonMappingException ex) {
        if (ex.getPath() == null || ex.getPath().isEmpty()) {
            return "corpo da requisição";
        }
        StringBuilder caminho = new StringBuilder();
        for (JsonMappingException.Reference referencia : ex.getPath()) {
            if (referencia.getFieldName() != null) {
                if (!caminho.isEmpty()) {
                    caminho.append('.');
                }
                caminho.append(referencia.getFieldName());
            } else if (referencia.getIndex() >= 0) {
                caminho.append('[').append(referencia.getIndex()).append(']');
            }
        }
        return caminho.isEmpty() ? "corpo da requisição" : caminho.toString();
    }

    private static String descricaoTipo(Class<?> tipo) {
        if (tipo == null) {
            return "um valor compatível com o campo";
        }
        if (tipo == String.class || tipo == Character.class || tipo == char.class) {
            return "um texto";
        }
        if (tipo == Integer.class || tipo == int.class || tipo == Long.class || tipo == long.class
                || tipo == Short.class || tipo == short.class) {
            return "um número inteiro";
        }
        if (tipo == Double.class || tipo == double.class || tipo == Float.class || tipo == float.class
                || tipo == BigDecimal.class) {
            return "um número";
        }
        if (tipo == Boolean.class || tipo == boolean.class) {
            return "verdadeiro ou falso";
        }
        if (tipo == LocalDate.class) {
            return "uma data no formato AAAA-MM-DD";
        }
        if (tipo == LocalTime.class) {
            return "uma hora no formato HH:mm:ss";
        }
        if (tipo == LocalDateTime.class) {
            return "uma data e hora no formato AAAA-MM-DDTHH:mm:ss";
        }
        if (tipo.isEnum()) {
            return "uma destas opções: " + Arrays.stream(tipo.getEnumConstants())
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        if (Collection.class.isAssignableFrom(tipo) || tipo.isArray()) {
            return "uma lista de valores";
        }
        return "um valor do tipo " + tipo.getSimpleName();
    }

    private static String descricaoTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "um valor compatível com o parâmetro";
        }
        return switch (tipo) {
            case "Integer", "int", "Long", "long", "Short", "short" -> "um número inteiro";
            case "Double", "double", "Float", "float", "BigDecimal" -> "um número";
            case "Boolean", "boolean" -> "verdadeiro ou falso";
            case "LocalDate" -> "uma data no formato AAAA-MM-DD";
            case "LocalTime" -> "uma hora no formato HH:mm:ss";
            case "LocalDateTime" -> "uma data e hora no formato AAAA-MM-DDTHH:mm:ss";
            case "String" -> "um texto";
            default -> "um valor do tipo " + tipo;
        };
    }

    private static String valorParaMensagem(Object valor) {
        if (valor == null) {
            return "nulo";
        }
        String seguro = String.valueOf(valor)
                .replace('"', '\'')
                .replaceAll("[\\p{Cntrl}]", " ")
                .trim();
        return seguro.length() <= 100 ? seguro : seguro.substring(0, 100) + "…";
    }

    private static <T extends Throwable> T encontrarCausa(Throwable ex, Class<T> tipo) {
        Throwable atual = ex;
        while (atual != null) {
            if (tipo.isInstance(atual)) {
                return tipo.cast(atual);
            }
            if (atual.getCause() == atual) {
                break;
            }
            atual = atual.getCause();
        }
        return null;
    }
}
