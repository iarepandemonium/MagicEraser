package net.pandette.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ServerConfig {

    @SerializedName("channel-data")
    private List<ChannelData> channelData;
}
