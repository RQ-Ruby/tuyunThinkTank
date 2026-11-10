package com.RQ.tuyunthinktank.manage.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.RQ.tuyunthinktank.config.TuyunProperties;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Resource
    private TuyunProperties tuyunProperties;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        // ringBuffer 的大小必须是 2 的幂
        int bufferSize = tuyunProperties.getWebsocket().getDisruptorBufferSize();
        if (bufferSize <= 0 || (bufferSize & (bufferSize - 1)) != 0) {
            throw new IllegalArgumentException("tuyun.websocket.disruptor-buffer-size must be a positive power of two");
        }
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        // 设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        // 开启 disruptor
        disruptor.start();
        return disruptor;
    }
}
