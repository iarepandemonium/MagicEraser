package net.pandette.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ServerConfig {

    /**
     * This is the server name which is not used for the bot, but only used for debugging purposes.
     */
    @SerializedName("server-name")
    private String servername;

    /**
     * This is the channel data list which provides information about each individual channel that is going to be used
     * for deletion.
     */
    @SerializedName("channel-data")
    private List<ChannelData> channelData;
}
