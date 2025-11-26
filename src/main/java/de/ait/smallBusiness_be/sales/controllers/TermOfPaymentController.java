package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.TermOfPaymentApi;
import de.ait.smallBusiness_be.sales.dto.NewTermOfPaymentDto;
import de.ait.smallBusiness_be.sales.dto.TermOfPaymentDto;
import de.ait.smallBusiness_be.sales.services.TermOfPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TermOfPaymentController implements TermOfPaymentApi {

    private final TermOfPaymentService termOfPaymentService;

    @Override
    public TermOfPaymentDto addTermOfPayment(NewTermOfPaymentDto newTerm) {
        return termOfPaymentService.createTermOfPayment(newTerm);
    }

    @Override
    public List<TermOfPaymentDto> getAllTermsOfPayment() {
        return termOfPaymentService.findAllTermsOfPayment();
    }

    @Override
    public TermOfPaymentDto getTermOfPaymentById(Long id) {
        return termOfPaymentService.findTermOfPaymentById(id);
    }

    @Override
    public TermOfPaymentDto updateTermOfPayment(Long id, NewTermOfPaymentDto newTerm) {
        return termOfPaymentService.updateTermOfPayment(id, newTerm);
    }

    @Override
    public void deleteTermOfPayment(Long id) {
        termOfPaymentService.deleteTermOfPayment(id);
    }
}
