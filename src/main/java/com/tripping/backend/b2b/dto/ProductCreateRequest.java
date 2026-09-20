package com.tripping.backend.b2b.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 초안은 제목만 있어도 만들 수 있다. 둘 다 비워 보내면 기본 제목이 붙는다.
@Getter
@Setter
@NoArgsConstructor
public class ProductCreateRequest {
    private String productName;
    private String regionId;
}
