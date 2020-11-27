package Core.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CommandConfig {
    private List<String> allowedGroups;
}
