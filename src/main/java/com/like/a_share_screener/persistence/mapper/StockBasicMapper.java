package com.like.a_share_screener.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StockBasicMapper extends BaseMapper<StockBasicEntity> {
	int upsertBatch(@Param("items") List<StockBasicEntity> items);

	List<String> selectExistingSecids(@Param("secids") List<String> secids);
}
