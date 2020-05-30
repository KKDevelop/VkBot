package me.c1oky;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.ConversationMember;
import com.vk.api.sdk.objects.messages.ConversationWithMessage;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import lombok.Getter;

import java.util.*;

public class VkCore {

    @Getter private static VkCore instance;

    @Getter private String dataPath;

    @Getter private VkApiClient vkApiClient;
    @Getter private GroupActor groupActor;
    private int maxMsgId = -1;
    private int ts;

    public VkCore(String dataPath, GroupActor actor) {
        VkCore.instance = this;
        this.dataPath = dataPath;
        this.groupActor = actor;
    }

    public void boot() {
        this.vkApiClient = new VkApiClient(HttpTransportClient.getInstance());
        //TODO: Initialising
    }

    public void sendMessage(Integer peerId, String text) throws ClientException, ApiException {
        vkApiClient.messages().send(groupActor)
                .randomId(new Random().nextInt(Integer.MAX_VALUE))
                .peerId(peerId)
                .message(text)
                .execute();
    }

    public UserXtrCounters getUser(Integer id) throws ClientException, ApiException {
        return vkApiClient
                .users()
                .get(groupActor)
                .userIds(id.toString())
                .execute()
                .get(0);
    }

    public List<ConversationWithMessage> getConversations() throws ClientException, ApiException {
        return vkApiClient
                .messages()
                .getConversations(groupActor)
                .execute()
                .getItems();
    }

    public List<ConversationMember> getAllConversationMembers(Message message) throws ClientException, ApiException {
        return vkApiClient
                .messages()
                .getConversationMembers(groupActor, message.getPeerId())
                .execute()
                .getItems();
    }

    public Message getMessage() throws ClientException, ApiException {
        this.ts = vkApiClient.messages().getLongPollServer(groupActor).execute().getTs();

        MessagesGetLongPollHistoryQuery eventsQuery = vkApiClient.messages()
                .getLongPollHistory(groupActor)
                .ts(ts);

        if (maxMsgId > 0) {
            eventsQuery.maxMsgId(maxMsgId);
        }

        List<Message> messages = eventsQuery
                .execute()
                .getMessages()
                .getItems();

        if (!messages.isEmpty()) {
            try {
                ts = vkApiClient.messages()
                        .getLongPollServer(groupActor)
                        .execute()
                        .getTs();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        if (!messages.isEmpty() && !messages.get(0).isOut()) {
            int messageId = messages.get(0).getId();
            if (messageId > maxMsgId) {
                maxMsgId = messageId;
            }

            return messages.get(0);
        }

        return null;
    }
}