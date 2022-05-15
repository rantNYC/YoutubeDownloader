package com.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class EmitterCacheService {
    private final Map<Integer, SseEmitter> sseEmitterMap;
    private final AtomicInteger counter;

    public EmitterCacheService(){
        sseEmitterMap = new ConcurrentHashMap<>();
        counter = new AtomicInteger();
    }

    public int addNewEmitter() {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        int guid = counter.incrementAndGet();
        sseEmitter.onCompletion(() -> {
            log.info("Emiter {} was Completed", guid);
            sseEmitterMap.remove(guid);
        });
        sseEmitter.onTimeout(() -> sseEmitterMap.remove(guid));
        sseEmitter.onError((ex) -> sseEmitterMap.remove(guid));
        sseEmitterMap.put(guid, sseEmitter);
        return guid;
    }

    public SseEmitter getCachedEmitter(int guid){
        return sseEmitterMap.get(guid);
    }

    public void sendEvent(int guid, String name, Object data) throws IOException {
        this.sendEvent(guid, name, "", data);
    }

    public void sendEvent(int guid, String name, String id, Object data) throws IOException {
        SseEmitter sseEmitter = sseEmitterMap.get(guid);
        if(sseEmitter != null) {
            sseEmitter.send(SseEmitter.event().name(name).id(id).data(data));
        }
    }
}
