package com.RQ.tuyunthinktank.config;

import com.RQ.tuyunthinktank.manage.websocket.PictureEditHandler;
import com.RQ.tuyunthinktank.manage.websocket.WsHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private WsHandshakeInterceptor wsHandshakeInterceptor;

    @Resource
    private TuyunProperties tuyunProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // websocket
        registry.addHandler(pictureEditHandler, tuyunProperties.getWebsocket().getPath())
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOrigins(tuyunProperties.getWebsocket().getAllowedOrigins().toArray(new String[0]));
    }
}
