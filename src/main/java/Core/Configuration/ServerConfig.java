package Core.Configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServerConfig {
    private String name;
    private List<String> whitelistedChannels;
    private List<Reaction> reactions;
    private String adminGroup;
    private String trustedGroup;
}
