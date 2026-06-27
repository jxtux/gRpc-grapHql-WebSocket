package com.demo.gateway.dto;

import java.util.List;

public record UserBatchRequest(java.util.List<Long> ids) {}
