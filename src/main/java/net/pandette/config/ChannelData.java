package net.pandette.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ChannelData {

    @SerializedName("channel-id")
    private long channelId;

    @SerializedName("time")
    private long time;

    @SerializedName("message-count")
    private int messageCount;

}
