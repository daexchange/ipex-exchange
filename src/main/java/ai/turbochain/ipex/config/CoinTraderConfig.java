package ai.turbochain.ipex.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import ai.turbochain.ipex.Trader.CoinTrader;
import ai.turbochain.ipex.Trader.CoinTraderFactory;
import ai.turbochain.ipex.entity.ExchangeCoin;
import ai.turbochain.ipex.service.ExchangeCoinService;
import ai.turbochain.ipex.service.ExchangeOrderService;

import java.util.List;

@Slf4j
@Configuration
public class CoinTraderConfig {

    /**
     * 配置交易处理类
     * @param exchangeCoinService
     * @param kafkaTemplate
     * @return
     */
    @Bean
    public CoinTraderFactory getCoinTrader(ExchangeCoinService exchangeCoinService, KafkaTemplate<String,String> kafkaTemplate, ExchangeOrderService exchangeOrderService){
        CoinTraderFactory factory = new CoinTraderFactory();
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        for(ExchangeCoin coin:coins) {
            log.info("init trader,symbol={}",coin.getSymbol());
            CoinTrader trader = new CoinTrader(coin.getSymbol());
            trader.setKafkaTemplate(kafkaTemplate);
            trader.setBaseCoinScale(coin.getBaseCoinScale());
            trader.setCoinScale(coin.getCoinScale());
            trader.stopTrading();
            factory.addTrader(coin.getSymbol(),trader);
        }
        return factory;
    }

}
