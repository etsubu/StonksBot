package Core.Configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Config {
    private String oauth;
    private List<String> admins;
    private List<String> whitelistedChannels;

    public Config() {

    }
}
