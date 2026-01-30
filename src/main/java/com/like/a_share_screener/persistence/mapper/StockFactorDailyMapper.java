package com.like.a_share_screener.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.like.a_share_screener.persistence.entity.StockFactorDailyEntity;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StockFactorDailyMapper extends BaseMapper<StockFactorDailyEntity> {
	LocalDate selectLatestTradeDate(@Param("code") String code);

	int upsertBatch(@Param("items") List<StockFactorDailyEntity> items);
}
