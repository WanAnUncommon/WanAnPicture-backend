package com.example.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * 图片编辑事件的Disruptor配置
 *
 * @author WanAn
 */
@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptorr")
    public Disruptor<PictureEditEvent> messageModelRingDisruptor() {
        // 定义disruptor的大小
        int bufferSize = 1024 * 256;
        // 创建disruptor
        Disruptor<PictureEditEvent> disruptor =
                new Disruptor<>(PictureEditEvent::new, bufferSize,
                        ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build());
        // 设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        // 启动
        disruptor.start();
        return disruptor;
    }
}
