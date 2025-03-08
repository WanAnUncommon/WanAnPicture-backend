package com.example.manager.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.example.manager.websocket.disruptor.PictureEditEventProducer;
import com.example.manager.websocket.model.PictureEditActionEnum;
import com.example.manager.websocket.model.PictureEditMessageTypeEnum;
import com.example.manager.websocket.model.PictureEditRequestMessage;
import com.example.manager.websocket.model.PictureEditResponseMessage;
import com.example.model.entity.User;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片编辑 WebSocket 处理器
 *
 * @author WanAn
 */
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;

    @Resource
    PictureEditEventProducer pictureEditEventProducer;

    // 每张图片当前的编辑用户id
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 每张图片的连接会话
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存到会话集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        // 构造响应体
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 加入会话", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        // 广播
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        // 将消息解析为 PictureEditRequestMessage
        PictureEditRequestMessage pictureEditRequestMessage =
                JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        // 从Session中获取公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 生产事务到disruptor中
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);
    }

    /**
     * 处理进入编辑状态的消息
     *
     * @param pictureEditRequestMessage 图片编辑请求消息
     * @param session                   会话
     * @param pictureId                 图片id
     * @param user                      用户
     * @throws IOException IO异常
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                       WebSocketSession session, Long pictureId, User user) throws IOException {
        // 如果没有用户正在编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            // 进入编辑
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户 %s 开始编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 处理编辑操作的消息
     *
     * @param pictureEditRequestMessage 图片编辑请求消息
     * @param session                   会话
     * @param pictureId                 图片id
     * @param user                      用户
     * @throws IOException IO异常
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, Long pictureId, User user) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null) {
            return;
        }
        // 如果是当前编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("用户 %s 进行了编辑：%s", user.getUserName(), editAction);
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 处理退出编辑状态的消息
     *
     * @param pictureEditRequestMessage 图片编辑请求消息
     * @param session                   会话
     * @param pictureId                 图片id
     * @param user                      用户
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, Long pictureId, User user) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 移除编辑态
            pictureEditingUsers.remove(pictureId);
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("用户 %s 退出编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 退出编辑
        handleExitEditMessage(null, session, pictureId, user);
        // 移除会话
        Set<WebSocketSession> webSocketSessionSet = pictureSessions.get(pictureId);
        if (webSocketSessionSet != null) {
            webSocketSessionSet.remove(session);
            if (webSocketSessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        // 广播
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 退出会话", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 广播消息到图片会话
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 图片编辑响应消息
     * @param excludeSession             排除的会话
     * @throws IOException IO异常
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage,
                                    WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> webSocketSessionSet = pictureSessions.get(pictureId);
        if (ObjectUtil.isEmpty(webSocketSessionSet)) {
            return;
        }
        // 解决精度丢失问题
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        // 遍历所有会话，发送消息
        for (WebSocketSession webSocketSession : webSocketSessionSet) {
            // 不需发送给自己
            if (webSocketSession.equals(excludeSession)) {
                continue;
            }
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
            }
        }
    }

    /**
     * 广播消息到图片会话
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 图片编辑响应消息
     * @throws IOException IO异常
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage)
            throws IOException {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }
}
