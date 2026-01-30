package com.like.a_share_screener.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.like.a_share_screener.persistence.entity.StockBasicEntity;
import com.like.a_share_screener.persistence.mapper.StockBasicMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StockBasicMapperTest {

	@Autowired
	private StockBasicMapper mapper;

	@BeforeEach
	void setUp() {
		mapper.delete(null);
	}

	@Test
	void upsertUpdatesExistingRow() throws InterruptedException {
		StockBasicEntity first = new StockBasicEntity();
		first.setCode("000001");
		first.setName("Ping An");
		first.setMarket(0);
		first.setSecid("0.000001");
		first.setBoard("MAIN");
		first.setStatus("ACTIVE");

		mapper.upsertBatch(List.of(first));

		StockBasicEntity stored = mapper.selectOne(new LambdaQueryWrapper<StockBasicEntity>()
				.eq(StockBasicEntity::getSecid, "0.000001"));
		Assertions.assertThat(stored).isNotNull();
		LocalDateTime firstUpdatedAt = stored.getUpdatedAt();

		Thread.sleep(10);

		StockBasicEntity second = new StockBasicEntity();
		second.setCode("000001");
		second.setName("Ping An Bank");
		second.setMarket(0);
		second.setSecid("0.000001");
		second.setBoard("MAIN");
		second.setStatus("ACTIVE");

		mapper.upsertBatch(List.of(second));

		StockBasicEntity updated = mapper.selectOne(new LambdaQueryWrapper<StockBasicEntity>()
				.eq(StockBasicEntity::getSecid, "0.000001"));
		Assertions.assertThat(updated.getName()).isEqualTo("Ping An Bank");
		Assertions.assertThat(updated.getUpdatedAt()).isAfter(firstUpdatedAt);
		Assertions.assertThat(mapper.selectCount(null)).isEqualTo(1L);
	}
}
