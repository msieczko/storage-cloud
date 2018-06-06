package com.github.mikee2509.storagecloud.admin.domain.dto;

import com.github.mikee2509.storagecloud.proto.Param;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParamDto {
    String paramId;
    String value;

    public static ParamDto from(Param param) {
        String value;
        switch (param.getValueCase()) {
            case SPARAMVAL:
                value = param.getSParamVal();
                break;
            case IPARAMVAL:
                value = String.valueOf(param.getIParamVal());
                break;
            case BPARAMVAL:
                value = String.valueOf(param.getBParamVal().toStringUtf8());
                break;
            case VALUE_NOT_SET:
            default:
                value = null;
        }
        return ParamDto.builder()
                .paramId(param.getParamId())
                .value(value)
                .build();
    }
}
