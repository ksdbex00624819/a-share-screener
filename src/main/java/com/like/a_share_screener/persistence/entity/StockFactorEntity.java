package com.like.a_share_screener.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("stock_factor")
public class StockFactorEntity {
	private String code;
	private String timeframe;
	private LocalDateTime barTime;
	private Integer fqt;
	private BigDecimal ma5;
	private BigDecimal ma10;
	private BigDecimal ma20;
	private BigDecimal ma60;
	private BigDecimal ema5;
	private BigDecimal ema10;
	private BigDecimal ema20;
	private BigDecimal ema60;
	private BigDecimal rsi14;
	private BigDecimal macd;
	private BigDecimal macdSignal;
	private BigDecimal macdHist;
	private BigDecimal bollMid;
	private BigDecimal bollUp;
	private BigDecimal bollLow;
	private BigDecimal kdjK;
	private BigDecimal kdjD;
	private BigDecimal kdjJ;
	private BigDecimal volMa5;
	private BigDecimal volMa10;
	private BigDecimal volMa20;
	private BigDecimal volMa60;
	private BigDecimal amtMa20;
	private BigDecimal volRatio20;
	@TableField("created_at")
	private LocalDateTime createdAt;
	@TableField("updated_at")
	private LocalDateTime updatedAt;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(String timeframe) {
		this.timeframe = timeframe;
	}

	public LocalDateTime getBarTime() {
		return barTime;
	}

	public void setBarTime(LocalDateTime barTime) {
		this.barTime = barTime;
	}

	public Integer getFqt() {
		return fqt;
	}

	public void setFqt(Integer fqt) {
		this.fqt = fqt;
	}

	public BigDecimal getMa5() {
		return ma5;
	}

	public void setMa5(BigDecimal ma5) {
		this.ma5 = ma5;
	}

	public BigDecimal getMa10() {
		return ma10;
	}

	public void setMa10(BigDecimal ma10) {
		this.ma10 = ma10;
	}

	public BigDecimal getMa20() {
		return ma20;
	}

	public void setMa20(BigDecimal ma20) {
		this.ma20 = ma20;
	}

	public BigDecimal getMa60() {
		return ma60;
	}

	public void setMa60(BigDecimal ma60) {
		this.ma60 = ma60;
	}

	public BigDecimal getEma5() {
		return ema5;
	}

	public void setEma5(BigDecimal ema5) {
		this.ema5 = ema5;
	}

	public BigDecimal getEma10() {
		return ema10;
	}

	public void setEma10(BigDecimal ema10) {
		this.ema10 = ema10;
	}

	public BigDecimal getEma20() {
		return ema20;
	}

	public void setEma20(BigDecimal ema20) {
		this.ema20 = ema20;
	}

	public BigDecimal getEma60() {
		return ema60;
	}

	public void setEma60(BigDecimal ema60) {
		this.ema60 = ema60;
	}

	public BigDecimal getRsi14() {
		return rsi14;
	}

	public void setRsi14(BigDecimal rsi14) {
		this.rsi14 = rsi14;
	}

	public BigDecimal getMacd() {
		return macd;
	}

	public void setMacd(BigDecimal macd) {
		this.macd = macd;
	}

	public BigDecimal getMacdSignal() {
		return macdSignal;
	}

	public void setMacdSignal(BigDecimal macdSignal) {
		this.macdSignal = macdSignal;
	}

	public BigDecimal getMacdHist() {
		return macdHist;
	}

	public void setMacdHist(BigDecimal macdHist) {
		this.macdHist = macdHist;
	}

	public BigDecimal getBollMid() {
		return bollMid;
	}

	public void setBollMid(BigDecimal bollMid) {
		this.bollMid = bollMid;
	}

	public BigDecimal getBollUp() {
		return bollUp;
	}

	public void setBollUp(BigDecimal bollUp) {
		this.bollUp = bollUp;
	}

	public BigDecimal getBollLow() {
		return bollLow;
	}

	public void setBollLow(BigDecimal bollLow) {
		this.bollLow = bollLow;
	}

	public BigDecimal getKdjK() {
		return kdjK;
	}

	public void setKdjK(BigDecimal kdjK) {
		this.kdjK = kdjK;
	}

	public BigDecimal getKdjD() {
		return kdjD;
	}

	public void setKdjD(BigDecimal kdjD) {
		this.kdjD = kdjD;
	}

	public BigDecimal getKdjJ() {
		return kdjJ;
	}

	public void setKdjJ(BigDecimal kdjJ) {
		this.kdjJ = kdjJ;
	}

	public BigDecimal getVolMa5() {
		return volMa5;
	}

	public void setVolMa5(BigDecimal volMa5) {
		this.volMa5 = volMa5;
	}

	public BigDecimal getVolMa10() {
		return volMa10;
	}

	public void setVolMa10(BigDecimal volMa10) {
		this.volMa10 = volMa10;
	}

	public BigDecimal getVolMa20() {
		return volMa20;
	}

	public void setVolMa20(BigDecimal volMa20) {
		this.volMa20 = volMa20;
	}

	public BigDecimal getVolMa60() {
		return volMa60;
	}

	public void setVolMa60(BigDecimal volMa60) {
		this.volMa60 = volMa60;
	}

	public BigDecimal getAmtMa20() {
		return amtMa20;
	}

	public void setAmtMa20(BigDecimal amtMa20) {
		this.amtMa20 = amtMa20;
	}

	public BigDecimal getVolRatio20() {
		return volRatio20;
	}

	public void setVolRatio20(BigDecimal volRatio20) {
		this.volRatio20 = volRatio20;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
