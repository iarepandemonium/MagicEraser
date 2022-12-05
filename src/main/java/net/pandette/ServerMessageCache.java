package net.pandette;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ServerMessageCache {

    private Map<Long, List<Message>> messages = new HashMap<>();
}
