package com.myfinal.redis.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myfinal.redis.exception.BusinessException;
import com.myfinal.redis.pojo.PingcodeTokenLog;
import com.myfinal.redis.service.PingcodeTokenLogService;
import com.myfinal.redis.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PingCode Token日志", description = "PingCode token获取记录表 CRUD")
@RestController
@RequestMapping("/api/pingcode-token-logs")
public class PingcodeTokenController {

    private final PingcodeTokenLogService pingcodeTokenLogService;

    public PingcodeTokenController(PingcodeTokenLogService pingcodeTokenLogService) {
        this.pingcodeTokenLogService = pingcodeTokenLogService;
    }

    @Operation(summary = "新增token记录")
    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody PingcodeTokenLog request) {
        boolean saved = pingcodeTokenLogService.save(request);
        if (!saved) {
            throw new BusinessException(500, "新增token记录失败");
        }
        return ApiResponse.success("新增token记录成功", true);
    }

    @Operation(summary = "根据ID查询token记录")
    @GetMapping("/{id}")
    public ApiResponse<PingcodeTokenLog> getById(@Parameter(description = "主键ID") @PathVariable Long id) {
        PingcodeTokenLog data = pingcodeTokenLogService.getById(id);
        if (data == null) {
            throw new BusinessException(404, "token记录不存在");
        }
        return ApiResponse.success("查询token记录成功", data);
    }

    @Operation(summary = "分页查询token记录")
    @GetMapping("/page")
    public ApiResponse<Page<PingcodeTokenLog>> page(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "来源筛选") @RequestParam(required = false) String source,
            @Parameter(description = "获取状态：1成功，0失败") @RequestParam(required = false) Integer acquireStatus) {

        QueryWrapper<PingcodeTokenLog> wrapper = new QueryWrapper<PingcodeTokenLog>()
                .eq(source != null && !source.isBlank(), "source", source)
                .eq(acquireStatus != null, "acquire_status", acquireStatus)
                .orderByDesc("id");

        Page<PingcodeTokenLog> page = pingcodeTokenLogService.page(new Page<>(pageNum, pageSize), wrapper);
        return ApiResponse.success("分页查询token记录成功", page);
    }

    @Operation(summary = "根据ID更新token记录")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateById(
            @Parameter(description = "主键ID") @PathVariable Long id,
            @RequestBody PingcodeTokenLog request) {
        if (pingcodeTokenLogService.getById(id) == null) {
            throw new BusinessException(404, "token记录不存在");
        }
        boolean updated = pingcodeTokenLogService.update(request, new QueryWrapper<PingcodeTokenLog>().eq("id", id));
        if (!updated) {
            throw new BusinessException(500, "更新token记录失败");
        }
        return ApiResponse.success("更新token记录成功", true);
    }

    @Operation(summary = "根据ID删除token记录")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteById(@Parameter(description = "主键ID") @PathVariable Long id) {
        if (pingcodeTokenLogService.getById(id) == null) {
            throw new BusinessException(404, "token记录不存在");
        }
        boolean deleted = pingcodeTokenLogService.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除token记录失败");
        }
        return ApiResponse.success("删除token记录成功", true);
    }
}
