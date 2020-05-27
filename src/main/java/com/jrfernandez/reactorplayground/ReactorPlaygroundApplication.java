package com.jrfernandez.reactorplayground;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.UnicastProcessor;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
@EnableScheduling
public class ReactorPlaygroundApplication {

	public static void main(String[] args) {
//		ReactorDebugAgent.init();
		SpringApplication.run(ReactorPlaygroundApplication.class, args);
	}

	@Bean
	public HandlerMapping handlerMapping(MatchmakingHandler matchmakingHandler) {
		Map<String, WebSocketHandler> map = new HashMap<>();
		map.put("/match", matchmakingHandler);

		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setUrlMap(map);
		mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return mapping;
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
		return new WebSocketHandlerAdapter(webSocketService);
	}

	@Bean
	public WebSocketService webSocketService() {
		return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
	}

	@Bean
	public Map<WebSocketSession, UnicastProcessor<String>> sessionUnicastProcessorMap() {
		return new ConcurrentHashMap<>();
	}
}
