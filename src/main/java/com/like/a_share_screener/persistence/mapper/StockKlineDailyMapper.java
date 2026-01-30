package com.like.a_share_screener.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.like.a_share_screener.persistence.entity.StockKlineDailyEntity;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @deprecated Use {@link StockKlineMapper} for multi-timeframe storage.
 */
@Deprecated
public interface StockKlineDailyMapper extends BaseMapper<StockKlineDailyEntity> {
	LocalDate selectLatestTradeDate(@Param("code") String code, @Param("fqt") int fqt);

	List<StockKlineDailyEntity> selectRecentByCode(@Param("code") String code, @Param("limit") int limit);

	int upsertBatch(@Param("items") List<StockKlineDailyEntity> items);
}
