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
     * This is the date this server got added to magic eraser. This is recorded as a future precaution in case
     * people share the link to my self-hosted copy, since I don't want too many servers on the same instance.
     */
    @SerializedName("date-created")
    private Long dateCreated;

    /**
     * This field will be used in the future is too many people start sharing the bot's link, and allow for me to
     * approve people or not rather than just allowing them to add themselves to my self-hosted version.
     */
    @SerializedName("approved")
    private boolean approved;

    /**
     * This is the channel data list which provides information about each individual channel that is going to be used
     * for deletion.
     */
    @SerializedName("channel-data")
    private List<ChannelData> channelData;
}
