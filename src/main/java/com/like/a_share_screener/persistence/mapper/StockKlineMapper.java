package com.like.a_share_screener.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.like.a_share_screener.persistence.entity.StockKlineEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StockKlineMapper extends BaseMapper<StockKlineEntity> {
	LocalDateTime selectLatestBarTime(@Param("code") String code, @Param("timeframe") String timeframe,
			@Param("fqt") int fqt);

	List<StockKlineEntity> selectBars(@Param("code") String code, @Param("timeframe") String timeframe,
			@Param("fqt") int fqt, @Param("fromInclusive") LocalDateTime fromInclusive,
			@Param("toInclusive") LocalDateTime toInclusive, @Param("limit") Integer limit,
			@Param("asc") boolean asc);

	LocalDateTime selectNthNewestBarTime(@Param("code") String code, @Param("timeframe") String timeframe,
			@Param("fqt") int fqt, @Param("offset") int offset);

	int deleteOlderThan(@Param("code") String code, @Param("timeframe") String timeframe,
			@Param("fqt") int fqt, @Param("cutoff") LocalDateTime cutoff);

	int upsertBatch(@Param("items") List<StockKlineEntity> items);
}
