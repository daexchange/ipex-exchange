package ai.turbochain.ipex.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import ai.turbochain.ipex.Trader.CoinTrader;
import ai.turbochain.ipex.Trader.CoinTraderFactory;
import ai.turbochain.ipex.entity.ExchangeOrder;
import ai.turbochain.ipex.entity.ExchangeOrderDetail;
import ai.turbochain.ipex.service.ExchangeOrderDetailService;
import ai.turbochain.ipex.service.ExchangeOrderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ExchangeSpringEvent implements ApplicationListener<ContextRefreshedEvent> {
    private Logger log = LoggerFactory.getLogger(ExchangeSpringEvent.class);
    @Autowired
    CoinTraderFactory coinTraderFactory;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("======程序启动成功======");
        coinTraderFactory.getTraderMap();
        HashMap<String,CoinTrader> traders = coinTraderFactory.getTraderMap();
        traders.forEach((symbol,trader) ->{
            List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
            List<ExchangeOrder> tradingOrders = new ArrayList<>();
            orders.forEach(order -> {
                BigDecimal tradedAmount = BigDecimal.ZERO;
                BigDecimal turnover = BigDecimal.ZERO;
                List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());
                order.setDetail(details);
                for(ExchangeOrderDetail trade:details){
                    tradedAmount = tradedAmount.add(trade.getAmount());
                    turnover = turnover.add(trade.getAmount().multiply(trade.getPrice()));
                }
                order.setTradedAmount(tradedAmount);
                order.setTurnover(turnover);
                if(!order.isCompleted()){
                    tradingOrders.add(order);
                }
            });
            trader.trade(tradingOrders);
        });
    }

}
