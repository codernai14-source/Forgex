package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.web.R;
import com.forgex.sys.domain.param.CommonTableQueryParam;
import com.forgex.sys.service.CommonTableDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/common/table/data")
@RequiredArgsConstructor
public class CommonTableDataController {

    /**
     * 通用表格数据服务
     */
    private final CommonTableDataService commonTableDataService;

    /**
     * 查询通用表格数据
     * 
     * @param param 查询参数，包含tableCode、当前页、每页大小和查询条件
     * @return 分页查询结果
     */
    @PostMapping("/query")
    public R<IPage<?>> query(@RequestBody CommonTableQueryParam param) {
        try {
            // 调用服务层查询数据
            IPage<?> result = commonTableDataService.queryTableData(param);
            return R.ok(result);
        } catch (RuntimeException e) {
            // 处理服务层抛出的异常
            return R.fail(500, e.getMessage());
        }
    }
}
