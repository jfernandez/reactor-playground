package com.jrfernandez.reactorplayground;

import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

@Component
public class MatchmakingHandler implements WebSocketHandler {

    @Autowired
    private Map<WebSocketSession, UnicastProcessor<String>> sessionUnicastProcessorMap;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        UnicastProcessor<String> unicastProcessor = UnicastProcessor.create();
        sessionUnicastProcessorMap.put(session, unicastProcessor);

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(unicastProcessor::onNext)
                .then();

        Mono<Void> output = session.send(
                unicastProcessor.map(session::textMessage)
                        .doOnTerminate(() -> unicastProcessor.remove(session.getId())));

        return Mono.zip(input, output).then();
    }

    // TODO: Move this to its own background tasks component
    @Scheduled(fixedRate = 5000)
    public void matchMake() {
        if (sessionUnicastProcessorMap.size() == 2) {
            ArrayList<WebSocketSession> sessions = new ArrayList<>(sessionUnicastProcessorMap.keySet());
            WebSocketSession sessionPlayerA = sessions.get(0);
            WebSocketSession sessionPlayerB = sessions.get(1);
            UnicastProcessor<String> processorPlayerA = sessionUnicastProcessorMap.get(sessionPlayerA);
            UnicastProcessor<String> processorPlayerB = sessionUnicastProcessorMap.get(sessionPlayerB);

            processorPlayerA.sink().next("Hello Player A, You have been matched with " + sessionPlayerB.getId());
            processorPlayerB.sink().next("Hello Player B, You have been matched with " + sessionPlayerA.getId());

            // TODO: This isn't working :(
            // Send messages from Player A to Player B
            processorPlayerA.doOnNext(msg -> processorPlayerB.sink().next(msg));
            // Send messages from Player B to Player A
            processorPlayerB.doOnNext(msg -> processorPlayerA.sink().next(msg));
        }
    }
}
