package com.techflow.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubtaskRequest {

    @NotBlank(message = "title cannot be blank")
    @Size(max = 255, message = "title cannot exceed 255 characters")
    private String title;
}