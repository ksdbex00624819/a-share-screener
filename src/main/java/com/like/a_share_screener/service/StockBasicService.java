package com.like.a_share_screener.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import com.like.a_share_screener.persistence.mapper.StockBasicMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class StockBasicService {
	private final StockBasicMapper mapper;

	public StockBasicService(StockBasicMapper mapper) {
		this.mapper = mapper;
	}

	public UpsertResult upsertStocks(List<StockBasicEntity> items) {
		if (items == null || items.isEmpty()) {
			return new UpsertResult(0, 0);
		}
		Set<String> secids = new LinkedHashSet<>();
		for (StockBasicEntity item : items) {
			secids.add(item.getSecid());
		}
		List<String> existing = secids.isEmpty() ? new ArrayList<>() : mapper.selectExistingSecids(new ArrayList<>(secids));
		int existingCount = existing.size();
		mapper.upsertBatch(items);
		return new UpsertResult(secids.size() - existingCount, existingCount);
	}

	public List<StockBasicEntity> listMainBoardActive(int limit) {
		LambdaQueryWrapper<StockBasicEntity> wrapper = new LambdaQueryWrapper<StockBasicEntity>()
				.eq(StockBasicEntity::getBoard, "MAIN")
				.eq(StockBasicEntity::getStatus, "ACTIVE")
				.orderByAsc(StockBasicEntity::getCode);
		if (limit > 0) {
			wrapper.last("limit " + limit);
		}
		return mapper.selectList(wrapper);
	}

	public record UpsertResult(int inserted, int updated) {
	}
}
