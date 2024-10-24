package de.ait.smallBusiness_be.products.controller;


import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.service.ProductService;
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
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody NewProductDto newProductDto) throws RestApiException {
        try{
            ProductDto productDto = productService.addProduct(newProductDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) throws RestApiException {
        try{
            ProductDto productDto = productService.getProductById(id);
            return ResponseEntity.ok(productDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProductById(@PathVariable Long id, @RequestBody NewProductDto newProductDto) throws RestApiException {
        try{
            ProductDto updatedProductDto = productService.updateProduct(id, newProductDto);
            return ResponseEntity.ok(updatedProductDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductDto> removeProductById(@PathVariable Long id) throws RestApiException {
        try{
            ProductDto deletedProductDto = productService.deleteProductById(id);
            return ResponseEntity.ok(deletedProductDto);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<Iterable<ProductDto>> getAllProducts() throws RestApiException {
        try{
            Iterable<ProductDto> products = productService.findAllProducts();
            return ResponseEntity.ok(products);
        } catch (RestApiException e){
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }

}
