package de.ait.smallBusiness_be.purchases.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseDocumentDto {

    private Long id;
    private Long purchaseId;
    private String originFileName;
    private String fileUrl;
    private String contentType;
    private LocalDateTime createdDate;
}
