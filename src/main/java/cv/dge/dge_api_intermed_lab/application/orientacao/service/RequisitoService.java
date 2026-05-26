package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.RequisitoResponse;
import java.util.List;

public interface RequisitoService {

    List<RequisitoResponse> listarAtivos();
}
