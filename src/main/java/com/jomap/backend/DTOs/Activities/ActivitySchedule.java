package com.jomap.backend.DTOs.Activities;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySchedule {

    @NotBlank(message = "التاريخ مطلوب لكل يوم")
    private String date;

    @NotBlank(message = "اسم اليوم مطلوب")
    private String dayName;

    @NotBlank(message = "وقت البدء مطلوب")
    private String startTime;

    @NotBlank(message = "وقت الانتهاء مطلوب")
    private String endTime;
}