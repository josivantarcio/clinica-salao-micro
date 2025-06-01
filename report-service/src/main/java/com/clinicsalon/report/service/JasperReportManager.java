package com.clinicsalon.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JasperReportManager {

    /**
     * Gera um relatório em formato PDF com base em um template JasperReports
     * @param reportName Nome do arquivo de template (.jrxml)
     * @param parameters Parâmetros a serem passados para o relatório
     * @param dataSource Dados a serem utilizados no relatório
     * @return Array de bytes contendo o relatório em PDF
     */
    public byte[] generatePdfReport(String reportName, Map<String, Object> parameters, Collection<?> dataSource) {
        return generateReport(reportName, parameters, dataSource, "pdf");
    }

    /**
     * Gera um relatório em formato Excel com base em um template JasperReports
     * @param reportName Nome do arquivo de template (.jrxml)
     * @param parameters Parâmetros a serem passados para o relatório
     * @param dataSource Dados a serem utilizados no relatório
     * @return Array de bytes contendo o relatório em formato Excel
     */
    public byte[] generateExcelReport(String reportName, Map<String, Object> parameters, Collection<?> dataSource) {
        return generateReport(reportName, parameters, dataSource, "xlsx");
    }

    /**
     * Método genérico para geração de relatórios em diferentes formatos
     * @param reportName Nome do arquivo de template (.jrxml)
     * @param parameters Parâmetros a serem passados para o relatório
     * @param dataSource Dados a serem utilizados no relatório
     * @param format Formato de saída (pdf, xlsx)
     * @return Array de bytes contendo o relatório no formato especificado
     */
    public byte[] generateReport(String reportName, Map<String, Object> parameters, Collection<?> dataSource, String format) {
        try {
            // Carrega o template do relatório
            InputStream reportInputStream = new ClassPathResource("reports/" + reportName + ".jrxml").getInputStream();
            
            // Compila o template
            JasperReport jasperReport = JasperCompileManager.compileReport(reportInputStream);
            
            // Cria o datasource usando a coleção de objetos
            JRDataSource jrDataSource = new JRBeanCollectionDataSource(dataSource);
            
            // Preenche o relatório com os dados
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jrDataSource);
            
            // Exporta para o formato desejado
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            switch (format.toLowerCase()) {
                case "pdf":
                    JRPdfExporter pdfExporter = new JRPdfExporter();
                    pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                    pdfExporter.setConfiguration(new SimplePdfReportConfiguration());
                    pdfExporter.exportReport();
                    break;
                case "xlsx":
                    JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                    xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                    xlsxExporter.setConfiguration(new SimpleXlsxReportConfiguration());
                    xlsxExporter.exportReport();
                    break;
                case "xls":
                    JRXlsExporter xlsExporter = new JRXlsExporter();
                    xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                    xlsExporter.setConfiguration(new SimpleXlsReportConfiguration());
                    xlsExporter.exportReport();
                    break;
                default:
                    throw new IllegalArgumentException("Formato de relatório não suportado: " + format);
            }
            
            return outputStream.toByteArray();
        } catch (JRException | IOException e) {
            log.error("Erro ao gerar relatório {}: {}", reportName, e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar relatório: " + e.getMessage(), e);
        }
    }
}
