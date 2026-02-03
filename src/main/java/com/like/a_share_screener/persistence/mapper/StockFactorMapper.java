package com.like.a_share_screener.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.like.a_share_screener.persistence.entity.StockFactorEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StockFactorMapper extends BaseMapper<StockFactorEntity> {
	LocalDateTime selectLatestBarTime(@Param("code") String code, @Param("timeframe") String timeframe,
			@Param("fqt") int fqt);

	LocalDateTime selectNthNewestBarTime(@Param("code") String code, @Param("timeframe") String timeframe,
			@Param("fqt") int fqt, @Param("offset") int offset);

	int deleteOlderThan(@Param("code") String code, @Param("timeframe") String timeframe,
			@Param("fqt") int fqt, @Param("cutoff") LocalDateTime cutoff);

	int upsertBatch(@Param("items") List<StockFactorEntity> items);
}
