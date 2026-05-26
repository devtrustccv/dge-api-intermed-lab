package cv.dge.dge_api_intermed_lab.application.acolhimento.mapper;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AcolhimentoValueHelper {

    private static final List<String> CODIGO_KEYS = List.of(
            "codigo", "code", "id", "valor", "value", "descricao", "description", "label", "nome");
    private static final List<String> TEXTO_KEYS = List.of(
            "descricao", "description", "label", "nome", "denominacao", "valor", "value", "codigo", "code", "id");
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    private AcolhimentoValueHelper() {
    }

    public static Object procurarProfundo(Object origem, String... chaves) {
        if (origem == null || chaves == null || chaves.length == 0) {
            return null;
        }
        if (origem instanceof Map<?, ?> mapa) {
            Object direto = valor(mapa, chaves);
            if (!vazio(direto)) {
                return direto;
            }
            for (Object item : mapa.values()) {
                Object encontrado = procurarProfundo(item, chaves);
                if (!vazio(encontrado)) {
                    return encontrado;
                }
            }
        }
        if (origem instanceof Collection<?> colecao) {
            for (Object item : colecao) {
                Object encontrado = procurarProfundo(item, chaves);
                if (!vazio(encontrado)) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    public static Object valor(Map<?, ?> origem, String... chaves) {
        if (origem == null || origem.isEmpty()) {
            return null;
        }
        List<String> normalizadas = List.of(chaves).stream().map(AcolhimentoValueHelper::normalizar).toList();
        for (Map.Entry<?, ?> entrada : origem.entrySet()) {
            if (entrada.getKey() != null && normalizadas.contains(normalizar(entrada.getKey().toString()))) {
                return entrada.getValue();
            }
        }
        return null;
    }

    public static Object primeiro(Object... valores) {
        if (valores == null) {
            return null;
        }
        for (Object valor : valores) {
            if (!vazio(valor)) {
                return valor;
            }
        }
        return null;
    }

    public static <T> void aplicarSePresente(T valor, java.util.function.Consumer<T> consumer) {
        if (!vazio(valor)) {
            consumer.accept(valor);
        }
    }

    public static boolean vazio(Object valor) {
        if (valor == null) {
            return true;
        }
        if (valor instanceof String texto) {
            return texto.trim().isEmpty();
        }
        if (valor instanceof Map<?, ?> mapa) {
            return mapa.isEmpty();
        }
        if (valor instanceof Collection<?> colecao) {
            return colecao.isEmpty();
        }
        return false;
    }

    public static boolean emBranco(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    public static boolean todosEmBranco(String... valores) {
        for (String valor : valores) {
            if (!emBranco(valor)) {
                return false;
            }
        }
        return true;
    }

    public static String codigo(Object valor) {
        return textoDeMapa(valor, CODIGO_KEYS);
    }

    public static String texto(Object valor) {
        return textoDeMapa(valor, TEXTO_KEYS);
    }

    public static Integer inteiro(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        String texto = texto(valor);
        if (emBranco(texto)) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static BigDecimal decimal(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }
        if (valor instanceof Number numero) {
            return BigDecimal.valueOf(numero.doubleValue());
        }
        String texto = texto(valor);
        if (emBranco(texto)) {
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static LocalDate data(Object valor) {
        String texto = texto(valor);
        if (emBranco(texto)) {
            return null;
        }
        for (DateTimeFormatter formato : DATE_FORMATS) {
            try {
                return LocalDate.parse(texto, formato);
            } catch (DateTimeParseException ignored) {
                // Tenta o proximo formato aceito pelo formulario.
            }
        }
        return null;
    }

    public static Map<String, Object> mapa(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            Map<String, Object> convertido = new LinkedHashMap<>();
            mapa.forEach((chave, conteudo) -> {
                if (chave != null) {
                    convertido.put(chave.toString(), conteudo);
                }
            });
            return convertido;
        }
        return new LinkedHashMap<>();
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private static String textoDeMapa(Object valor, List<String> preferencias) {
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
            for (String chave : preferencias) {
                String texto = textoDeMapa(valor(mapa, chave), preferencias);
                if (!emBranco(texto)) {
                    return texto;
                }
            }
            return mapa.size() == 1 ? textoDeMapa(mapa.values().iterator().next(), preferencias) : null;
        }
        if (valor instanceof Collection<?> colecao) {
            for (Object item : colecao) {
                String texto = textoDeMapa(item, preferencias);
                if (!emBranco(texto)) {
                    return texto;
                }
            }
            return null;
        }
        String texto = valor.toString().trim();
        return texto.isEmpty() ? null : texto;
    }
}
