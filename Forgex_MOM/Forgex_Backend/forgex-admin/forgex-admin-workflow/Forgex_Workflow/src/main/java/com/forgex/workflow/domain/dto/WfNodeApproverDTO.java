package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WfNodeApproverDTO {

    private Integer approverType;

    private List<Long> approverIds = new ArrayList<>();
}
