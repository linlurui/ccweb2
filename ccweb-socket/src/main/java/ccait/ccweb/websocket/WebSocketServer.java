/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */


package ccait.ccweb.websocket;

import ccait.ccweb.config.LangConfig;
import ccait.ccweb.model.UserGroupRoleModel;
import ccait.ccweb.model.UserModel;
import ccait.ccweb.utils.EncryptionUtil;
import entity.query.core.ApplicationConfig;

import entity.tool.util.JsonUtils;
import entity.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ccait.ccweb.utils.StaticVars.*;


@Component
@ServerEndpoint(value="/ccws", configurator=GetHttpSessionConfigurator.class)
@ConditionalOnProperty(prefix = "websocket", name = "enable", havingValue = "true")
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    private static final AtomicInteger OnlineCount = new AtomicInteger(0);
    private final static Hashtable<String, Session> sessionSet = new Hashtable<>();
    private final static Hashtable<String, UserModel> sessionIdUserMap = new Hashtable<>();
    private final static List<Session> allSessions = new ArrayList<>();
    private UserModel currentUser;

    @Value("${entity.security.encrypt.AES.publicKey:ccait}")
    private String aesPublicKey;

    @Value("${entity.table.reservedField.groupId:groupId}")
    private String groupIdField;

    @Value("${entity.table.reservedField.userPath:userPath}")
    private String userPathField;

    @Value("${entity.table.reservedField.userId:userId}")
    private String userIdField;

    @Value("${entity.table.userGroupRole:userGroupRole}")
    private String userGroupRoleTable;

    @Value("${entity.encoding:UTF-8}")
    private String encoding;

    //连接超时
    public static final long MAX_TIME_OUT = 60 * 60 * 24 * 1000;

    @PostConstruct
    public void init() {
        aesPublicKey = ApplicationConfig.getInstance().get("${entity.security.encrypt.AES.publicKey}", aesPublicKey);
        groupIdField = ApplicationConfig.getInstance().get("${entity.table.reservedField.groupId}", groupIdField);
        userPathField = ApplicationConfig.getInstance().get("${entity.table.reservedField.userPath}", userPathField);
        userIdField = ApplicationConfig.getInstance().get("${entity.table.reservedField.userId}", userIdField);
        userGroupRoleTable = ApplicationConfig.getInstance().get("${entity.table.userGroupRole}", userGroupRoleTable);
        encoding = ApplicationConfig.getInstance().get("${entity.encoding}", encoding);
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig conf) {

        session.setMaxIdleTimeout( MAX_TIME_OUT );

        allSessions.add(session);
        String sessionKey = HttpSession.class.getName();
        if(!conf.getUserProperties().containsKey(sessionKey)) {
            return;
        }

        if(!(conf.getUserProperties().get(sessionKey) instanceof HttpSession)) {
            return;
        }

        try {
            HttpSession httpSession = (HttpSession) conf.getUserProperties().get(sessionKey);

            sessionSet.put(httpSession.getId(), session);

            if (httpSession.getAttribute(httpSession.getId() + LOGIN_KEY) != null) {
                currentUser = (UserModel) httpSession.getAttribute(httpSession.getId() + LOGIN_KEY);

                sessionIdUserMap.put(httpSession.getId(), currentUser);
            }

            int cnt = OnlineCount.incrementAndGet(); // 在线数加1
            log.info(LOG_PRE_SUFFIX_BY_SOCKET + String.format("有连接加入，当前连接数为：%s", cnt));
        }
        catch (Exception e) {
            log.error("HttpSession Error=====>", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {

        try {
            List<String> ids = sessionSet.entrySet().stream()
                    .filter(a->a.getValue().getId().equals(session.getId()))
                    .map(b->b.getKey()).collect(Collectors.toList());

            if(ids != null) {
                for (String id :ids) {
                    sessionIdUserMap.remove(id);
                    sessionSet.remove(id);
                }
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }

        int cnt = OnlineCount.decrementAndGet();
        log.info(LOG_PRE_SUFFIX_BY_SOCKET + String.format("有连接关闭，当前连接数为：%s", cnt));
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param text
     * 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String text, Session session) {
        log.info(LOG_PRE_SUFFIX_BY_SOCKET + String.format("来自客户端的消息：%s", text));
        try
        {
            if(StringUtils.isEmpty( text )) {
                throw new Exception(LangConfig.getInstance().get("message_can_not_be_empty"));
            }

            MessageBody body = JsonUtils.parse(text, MessageBody.class);
            if(StringUtils.isEmpty(body.getMessage())) {
                throw new Exception(LangConfig.getInstance().get("message_can_not_be_empty"));
            }

            if(body != null) {
                switch (body.getSendMode()) {
                    case ALL:
                        sendToAll(body.getMessage());
                        break;
                    case USER:
                        sendToUser(body.getReceiver(), body.getMessage());
                        break;
                    case GROUP:
                        sendToGroup(body.getReceiver(), body.getMessage());
                        break;
                    case ROLE:
                        sendToRole(body.getReceiver(), body.getMessage());
                        break;
                }
                return;
            }

            throw new Exception("无效的消息格式");

        } catch ( Exception e )
        {
            log.error(LOG_PRE_SUFFIX_BY_SOCKET + String.format("接收消息发生错误：%s",e.getMessage()));

            sendMessage(session, e.getMessage());
        }
    }

    /**
     * 出现错误
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error(LOG_PRE_SUFFIX_BY_SOCKET + String.format("发生错误：%s，Session ID： %s",error.getMessage(),session.getId()));
    }

    private void sendToRole(ReceiverInfo receiver, String message) {

        List<String> ids = sessionIdUserMap.entrySet().stream()
                .filter(a -> {
                    return a.getValue().getUserGroupRoleModels().stream()
                            .filter(b -> b.getGroupId().equals(receiver.getGroupId()) &&
                                    b.getRoleId().equals(receiver.getRoleId())).isParallel();
                }).map(c->c.getKey()).collect(Collectors.toList());

        List<Session> list = sessionSet.entrySet().stream()
                .filter(a->ids.contains(a.getKey())).map(b->b.getValue())
                .collect(Collectors.toList());

        for (Session session : list) {
            sendMessage(session, message);
        }
    }

    private void sendToGroup(ReceiverInfo receiver, String message) {

        List<String> ids = sessionIdUserMap.entrySet().stream()
                .filter(a -> {
                    try {
                        if(a.getValue().getUserGroupRoleModels() != null && a.getValue().getUserGroupRoleModels().stream().filter(b -> b.getGroupId().equals(receiver.getGroupId())).isParallel()) {
                            return true;
                        }
                        UserGroupRoleModel groupModel = new UserGroupRoleModel();
                        String aesFields = ApplicationConfig.getInstance().get("${entity.security.encrypt.AES.fields}", "");
                        List<String> aesFieldList = StringUtils.splitString2List(aesFields, ",");
                        String groupIdString = receiver.getGroupId();
                        if(aesFieldList.stream()
                                .filter(b-> b.equalsIgnoreCase(String.format("%s.%s", userGroupRoleTable, groupIdField)) ||
                                        b.equalsIgnoreCase(groupIdField)).isParallel()) {
                            groupIdString = EncryptionUtil.decryptByAES(groupIdString, aesPublicKey);
                        }

                        groupModel.setGroupId(Integer.parseInt(groupIdString));
                        List<Integer> userIdList = groupModel
                                .where("[groupId]=#{groupId}")
                                .select("userId")
                                .query(Integer.class);

                        if(userIdList.contains(a.getValue().getUserId())) {
                            return true;
                        }

                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                    }

                    return false;
                }).map(c->c.getKey()).collect(Collectors.toList());

        List<Session> list = sessionSet.entrySet().stream()
                .filter(a->ids.contains(a.getKey())).map(b->b.getValue())
                .collect(Collectors.toList());

        for (Session session : list) {
            sendMessage(session, message);
        }
    }

    private void sendToUser(ReceiverInfo receiver, String message) {

        List<String> ids = sessionIdUserMap.entrySet().stream()
                .filter(a->receiver.getUsernames().contains(a.getValue().getUsername()))
                .map(b->b.getKey()).collect(Collectors.toList());

        List<Session> list = sessionSet.entrySet().stream()
                .filter(a->ids.contains(a.getKey())).map(b->b.getValue())
                .collect(Collectors.toList());

        for (Session session : list) {
            sendMessage(session, message);
        }
    }

    /**
     * 发送消息，实践表明，每次浏览器刷新，session会发生变化。
     * @param session
     * @param message
     */
    public static void sendMessage(Session session, String message) {
        try {
            if(session == null) {
                return;
            }

            if(!session.isOpen()) {
                return;
            }

            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error(LOG_PRE_SUFFIX_BY_SOCKET + String.format("发送消息出错：%s, 消息内容：%s", e.getMessage(), message));
            e.printStackTrace();
        }
    }

    /**
     * 群发消息
     * @param message
     * @throws IOException
     */
    public static void sendToAll(String message) throws Exception {
        for (Session session : allSessions) {
            sendMessage(session, message);
        }
    }

    /**
     * 指定Session发送消息
     * @param httpSessionId
     * @param message
     * @throws IOException
     */
    public static void sendMessage(String httpSessionId,String message) throws Exception {
        Session session = sessionSet.get( httpSessionId );
        if(session!=null){
            sendMessage(session, message);
        }
        else{
            log.warn(LOG_PRE_SUFFIX_BY_SOCKET + String.format("没有找到你指定ID的会话：%s",httpSessionId));
        }
    }
}
