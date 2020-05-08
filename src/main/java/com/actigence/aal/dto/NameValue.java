package com.actigence.aal.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NameValue {
    private final String name;
    private final String value;
}
