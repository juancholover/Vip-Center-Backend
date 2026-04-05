package com.gimnasio.fit.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(toBuilder = true)
public class PagoResponseDto {
    private String preferenceId;
    private String initPoint;
    private String status;
    private String message;
    private String qrCodeBase64;

    public PagoResponseDto(String preferenceId, String initPoint, String status, String message, String qrCodeBase64) {
        this.preferenceId = preferenceId;
        this.initPoint = initPoint;
        this.status = status;
        this.message = message;
        this.qrCodeBase64 = qrCodeBase64;
    }

}
