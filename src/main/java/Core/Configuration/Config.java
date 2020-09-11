package Core.Configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class Config {
    private String oauth;
    private List<String> globalAdmins;
    private List<ServerConfig> servers;

    public Config() {

    }

    public Optional<ServerConfig> getServerConfig(String serverName) {
        if(servers == null) {
            return Optional.empty();
        }
        return servers.stream().filter(x -> x.getName().trim().equalsIgnoreCase(serverName)).findFirst();
    }
}
