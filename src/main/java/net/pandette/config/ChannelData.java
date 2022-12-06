package net.pandette.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * This Pojo represents the data necessary for channel deletion to occur.
 */
@Data
public class ChannelData {

    /**
     * The channel ID that is going to be referenced for deletion.
     */
    @SerializedName("channel-id")
    private long channelId;

    /**
     * This is the time represented in millis. If the number is -1 the bot will not delete through time.
     */
    @SerializedName("time")
    private long time;

    /**
     * This is the message count, if the number is -1 it will not delete based on message count.
     */
    @SerializedName("message-count")
    private int messageCount;

}
