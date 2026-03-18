package com.visaflow.user.dto;

public record LimitsCheck(
    Boolean users,
    Boolean cases,
    Boolean storage
) {}
