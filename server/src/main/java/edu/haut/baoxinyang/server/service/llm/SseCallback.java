package edu.haut.baoxinyang.server.service.llm;

import lombok.Data;

/**
 * SSE回调接口
 */
public interface SseCallback {
    
    /**
     * 当收到消息时调用
     * @param event SSE事件
     */
    void onEvent(SseEvent event);
    
    /**
     * 当发生错误时调用
     * @param throwable 错误信息
     */
    void onError(Throwable throwable);
    
    /**
     * 当连接关闭时调用
     */
    void onComplete();
    
    /**
     * SSE事件
     */
    @Data
    public static class SseEvent {
        /**
         * 事件ID
         */
        private String id;
        
        /**
         * 事件类型
         */
        private String event;
        
        /**
         * 事件数据
         */
        private String data;
        
        /**
         * 事件重连时间（毫秒）
         */
        private Long retry;
        
        /**
         * 是否是最后一条消息
         */
        private boolean isLastEvent;
    }
} 