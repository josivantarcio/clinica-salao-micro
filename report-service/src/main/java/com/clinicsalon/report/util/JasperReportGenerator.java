package com.clinicsalon.report.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitária para geração de relatórios usando JasperReports
 */
@Component
@Slf4j
public class JasperReportGenerator {

    /**
     * Gera um relatório PDF a partir de um template JRXML e parâmetros
     *
     * @param reportPath Caminho do template JRXML no classpath
     * @param parameters Parâmetros para preencher o relatório
     * @param dataSource Lista de dados para o relatório (opcional)
     * @return Array de bytes contendo o PDF gerado
     */
    public byte[] generatePdfReport(String reportPath, Map<String, Object> parameters, List<?> dataSource) {
        try {
            // Carrega o template do relatório
            InputStream reportTemplate = new ClassPathResource(reportPath).getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);

            // Prepara a fonte de dados
            JRDataSource jrDataSource = dataSource != null 
                ? new JRBeanCollectionDataSource(dataSource) 
                : new JREmptyDataSource();

            // Preenche o relatório com os dados
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, 
                    parameters != null ? parameters : new HashMap<>(), 
                    jrDataSource);

            // Exporta para PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            
            return outputStream.toByteArray();
        } catch (JRException | IOException e) {
            log.error("Erro ao gerar relatório PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar relatório: " + e.getMessage(), e);
        }
    }

    /**
     * Gera um relatório PDF com um mapa de dados como fonte de dados principal
     * e possíveis sub-relatórios
     *
     * @param reportPath Caminho do template JRXML no classpath
     * @param parameters Parâmetros para preencher o relatório, incluindo sub-relatórios
     * @param dataMap Mapa principal de dados para o relatório
     * @return Array de bytes contendo o PDF gerado
     */
    public byte[] generatePdfReportFromMap(String reportPath, Map<String, Object> parameters, Map<String, Object> dataMap) {
        try {
            // Carrega o template do relatório
            InputStream reportTemplate = new ClassPathResource(reportPath).getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);

            // Preenche o relatório com os dados do mapa
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, 
                    parameters != null ? parameters : new HashMap<>(), 
                    new JREmptyDataSource(1)); // Uma linha vazia

            // Exporta para PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            
            return outputStream.toByteArray();
        } catch (JRException | IOException e) {
            log.error("Erro ao gerar relatório PDF a partir de mapa: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar relatório: " + e.getMessage(), e);
        }
    }
}
