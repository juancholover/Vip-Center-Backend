package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.ClienteInactividadDTO;
import com.gimnasio.fit.dto.InactividadResumenDTO;
import com.gimnasio.fit.repository.AsistenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para detectar clientes inactivos y generar listas exportables (HU-34 + HU-35).
 * 
 * Niveles de riesgo por inactividad:
 * - BAJO (verde):    < 15 días sin asistir
 * - MEDIO (amarillo): 15-30 días sin asistir
 * - ALTO (naranja):  30-60 días sin asistir
 * - CRITICO (rojo):  > 60 días sin asistir
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteInactividadService {

    private final AsistenciaRepository asistenciaRepository;

    /**
     * Obtiene todos los clientes con su nivel de inactividad (HU-34).
     * 
     * @param diasMinimo Filtro mínimo de días de inactividad (default 0 = todos).
     *                   Usar 15 para solo mostrar medio+alto+critico.
     * @return Resumen agrupado con lista de clientes inactivos
     */
    @Transactional(readOnly = true)
    public InactividadResumenDTO obtenerClientesInactivos(Integer diasMinimo) {
        try {
            log.info("😴 Generando reporte de inactividad (díasMinimo: {})", diasMinimo);

            List<Object[]> resultados = asistenciaRepository.obtenerClientesConUltimaAsistencia();
            List<ClienteInactividadDTO> clientes = new ArrayList<>();

            LocalDateTime ahora = LocalDateTime.now();
            int contBajo = 0, contMedio = 0, contAlto = 0, contCritico = 0;

            for (Object[] fila : resultados) {
                Long clienteId = (Long) fila[0];
                String nombre = (String) fila[1];
                String apellido = (String) fila[2];
                String telefono = (String) fila[3];
                String email = (String) fila[4];
                String plan = (String) fila[5];
                LocalDate fechaVencimiento = (LocalDate) fila[6];
                LocalDateTime ultimaAsistencia = fila[7] != null ? (LocalDateTime) fila[7] : null;

                // Calcular días de inactividad
                int diasInactivo;
                if (ultimaAsistencia != null) {
                    diasInactivo = (int) ChronoUnit.DAYS.between(ultimaAsistencia, ahora);
                } else {
                    diasInactivo = 999; // Nunca ha asistido
                }

                // Filtrar por mínimo de días
                int minDias = (diasMinimo != null && diasMinimo > 0) ? diasMinimo : 0;
                if (diasInactivo < minDias) {
                    continue;
                }

                // Determinar nivel de riesgo y color
                String nivelRiesgo;
                String colorBadge;
                if (diasInactivo < 15) {
                    nivelRiesgo = "BAJO";
                    colorBadge = "green";
                    contBajo++;
                } else if (diasInactivo < 30) {
                    nivelRiesgo = "MEDIO";
                    colorBadge = "yellow";
                    contMedio++;
                } else if (diasInactivo < 60) {
                    nivelRiesgo = "ALTO";
                    colorBadge = "orange";
                    contAlto++;
                } else {
                    nivelRiesgo = "CRITICO";
                    colorBadge = "red";
                    contCritico++;
                }

                clientes.add(new ClienteInactividadDTO(
                    clienteId,
                    nombre + " " + apellido,
                    telefono,
                    email,
                    plan,
                    fechaVencimiento,
                    ultimaAsistencia,
                    diasInactivo,
                    nivelRiesgo,
                    colorBadge
                ));
            }

            int total = clientes.size();
            log.info("✅ Reporte de inactividad: {} clientes (bajo={}, medio={}, alto={}, critico={})",
                    total, contBajo, contMedio, contAlto, contCritico);

            return new InactividadResumenDTO(
                total, contBajo, contMedio, contAlto, contCritico, clientes
            );

        } catch (Exception e) {
            log.error("❌ Error al generar reporte de inactividad: {}", e.getMessage(), e);
            return new InactividadResumenDTO(0, 0, 0, 0, 0, new ArrayList<>());
        }
    }

    /**
     * Exporta la lista de clientes inactivos a Excel (HU-35).
     * Incluye columnas útiles para Marketing: Teléfono, Email, Días Inactivo.
     * 
     * @param diasMinimo Filtro mínimo de días (default 15 para exportar solo inactivos relevantes)
     * @return Bytes del archivo Excel
     */
    @Transactional(readOnly = true)
    public byte[] exportarClientesInactivosExcel(Integer diasMinimo) {
        try {
            InactividadResumenDTO resumen = obtenerClientesInactivos(diasMinimo);

            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Clientes Inactivos");

                // Estilo del header
                Font fontBold = workbook.createFont();
                fontBold.setBold(true);
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFont(fontBold);

                // Estilos para badges de riesgo
                CellStyle styleBajo = crearCellStyleColor(workbook, IndexedColors.LIGHT_GREEN);
                CellStyle styleMedio = crearCellStyleColor(workbook, IndexedColors.LIGHT_YELLOW);
                CellStyle styleAlto = crearCellStyleColor(workbook, IndexedColors.LIGHT_ORANGE);
                CellStyle styleCritico = crearCellStyleColor(workbook, IndexedColors.CORAL);

                // Headers
                String[] headers = {
                    "ID", "Nombre Completo", "Teléfono", "Email",
                    "Plan", "Fecha Vencimiento", "Última Asistencia",
                    "Días Inactivo", "Nivel de Riesgo"
                };

                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Datos
                int rowIdx = 1;
                for (ClienteInactividadDTO dto : resumen.getClientes()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(dto.getClienteId() != null ? dto.getClienteId() : 0);
                    row.createCell(1).setCellValue(safe(dto.getNombreCompleto()));
                    row.createCell(2).setCellValue(safe(dto.getTelefono()));
                    row.createCell(3).setCellValue(safe(dto.getEmail()));
                    row.createCell(4).setCellValue(safe(dto.getPlan()));
                    row.createCell(5).setCellValue(dto.getFechaVencimiento() != null ? dto.getFechaVencimiento().toString() : "");
                    row.createCell(6).setCellValue(dto.getUltimaAsistencia() != null
                            ? dto.getUltimaAsistencia().toLocalDate().toString() : "Nunca");
                    row.createCell(7).setCellValue(dto.getDiasInactivo() != null ? dto.getDiasInactivo() : 0);

                    Cell riesgoCell = row.createCell(8);
                    riesgoCell.setCellValue(safe(dto.getNivelRiesgo()));

                    // Colorear celda de riesgo
                    switch (dto.getNivelRiesgo()) {
                        case "BAJO" -> riesgoCell.setCellStyle(styleBajo);
                        case "MEDIO" -> riesgoCell.setCellStyle(styleMedio);
                        case "ALTO" -> riesgoCell.setCellStyle(styleAlto);
                        case "CRITICO" -> riesgoCell.setCellStyle(styleCritico);
                    }
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Hoja resumen
                Sheet resumenSheet = workbook.createSheet("Resumen");
                Row r0 = resumenSheet.createRow(0);
                r0.createCell(0).setCellValue("Categoría");
                r0.createCell(1).setCellValue("Cantidad");
                r0.getCell(0).setCellStyle(headerStyle);
                r0.getCell(1).setCellStyle(headerStyle);

                resumenSheet.createRow(1).createCell(0).setCellValue("Total Inactivos");
                resumenSheet.getRow(1).createCell(1).setCellValue(resumen.getTotalInactivos());
                resumenSheet.createRow(2).createCell(0).setCellValue("Bajo Riesgo (<15 días)");
                resumenSheet.getRow(2).createCell(1).setCellValue(resumen.getBajo());
                resumenSheet.createRow(3).createCell(0).setCellValue("Medio Riesgo (15-30 días)");
                resumenSheet.getRow(3).createCell(1).setCellValue(resumen.getMedio());
                resumenSheet.createRow(4).createCell(0).setCellValue("Alto Riesgo (30-60 días)");
                resumenSheet.getRow(4).createCell(1).setCellValue(resumen.getAlto());
                resumenSheet.createRow(5).createCell(0).setCellValue("Crítico (>60 días)");
                resumenSheet.getRow(5).createCell(1).setCellValue(resumen.getCritico());
                resumenSheet.autoSizeColumn(0);
                resumenSheet.autoSizeColumn(1);

                workbook.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("❌ Error al exportar clientes inactivos a Excel: {}", e.getMessage(), e);
            return new byte[0];
        }
    }

    private CellStyle crearCellStyleColor(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
