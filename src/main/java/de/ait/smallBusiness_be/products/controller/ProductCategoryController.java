package de.ait.smallBusiness_be.products.controller;


import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import de.ait.smallBusiness_be.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@RestController
@RequestMapping("api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    @Autowired
    final ProductCategoryService productCategoryService;

    @PostMapping
    public ResponseEntity<ProductCategoryDto> createProductCategory(@RequestBody NewProductCategoryDto newProductCategoryDto) throws RestApiException {
        try{
            ProductCategoryDto newCategoryDto = productCategoryService.addProductCategory(newProductCategoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCategoryDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryDto>  getProductCategoryById(@PathVariable int id) throws RestApiException {
        try{
            ProductCategoryDto categoryDto = productCategoryService.getProductCategoryById(id);
            return ResponseEntity.ok(categoryDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDto>  updateProductCategoryById(@PathVariable int id, @RequestBody NewProductCategoryDto newProductCategoryDto) throws RestApiException {
        try{
            ProductCategoryDto updatedCategoryDto = productCategoryService.updateProductCategory(id, newProductCategoryDto);
            return ResponseEntity.ok(updatedCategoryDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductCategoryDto>  removeProductCategoryById(@PathVariable int id) throws RestApiException {
        try{
            ProductCategoryDto deletedCategoryDto = productCategoryService.deleteProductCategoryById(id);
            return ResponseEntity.ok(deletedCategoryDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<Iterable<ProductCategoryDto>> getAllProductCategories() throws RestApiException {
        try{
            Iterable<ProductCategoryDto> categories = productCategoryService.findAllProductCategories();
            return ResponseEntity.ok(categories);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }
}
