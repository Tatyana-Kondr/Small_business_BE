package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.sales.dto.NewTermOfPaymentDto;
import de.ait.smallBusiness_be.sales.dto.TermOfPaymentDto;

import java.util.List;

public interface TermOfPaymentService {
   TermOfPaymentDto createTermOfPayment(NewTermOfPaymentDto newTermOfPaymentDto);
    List<TermOfPaymentDto> findAllTermsOfPayment();
    TermOfPaymentDto findTermOfPaymentById(long id);
    TermOfPaymentDto updateTermOfPayment(Long id, NewTermOfPaymentDto newTermOfPaymentDto);
    void deleteTermOfPayment(long id);
}
