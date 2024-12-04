package de.ait.smallBusiness_be.products.controller;


import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.controller.api.ProductsApi;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.UpdateProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductsApi {

    final ProductService productService;
    // final AuthService authService;

    @Override
    public ProductDto createProduct(
            NewProductDto newProductDto
            //, AuthenticatedUser currentUser
            ) {

        // User authenticatedUser = currentUser.getUser();
        return productService.addProduct(newProductDto
                //, authenticatedUser
        );
    }

    @Override
    public ProductDto getProductById(Long id) throws RestApiException {
            return productService.getProductById(id);

    }

    @Override
    public ProductDto updateProductById(
            Long id,
            UpdateProductDto updateProductDto
            //, AuthenticatedUser currentUser
    ) {
        // User authenticatedUser = currentUser.getUser();
            return productService.updateProduct(
                    id,
                    updateProductDto
                    //, authenticatedUser
            );
    }

    @Override
    public void removeProductById(Long id
            //, AuthenticatedUser currentUser
    ) {
            // User authenticatedUser = currentUser.getUser();
               productService.deleteProductById(id
                //, authenticatedUser
        );
    }

    @Override
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productService.findAllProducts(pageable);
    }

    @Override
    public Page<ProductDto> getProductsByCategory(int categoryId, Pageable pageable) {
        return productService.findProductsByCategoryId(categoryId, pageable);
    }

}
