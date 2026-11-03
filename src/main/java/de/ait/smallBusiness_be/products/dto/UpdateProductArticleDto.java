
package de.ait.smallBusiness_be.products.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductArticleDto {

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 30, message = "{validation.article.size}")
    private String article;
}

