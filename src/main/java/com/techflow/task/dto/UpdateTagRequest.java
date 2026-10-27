package com.techflow.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTagRequest {

    @NotBlank(message = "Tag name cannot be blank")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    private String name;
}