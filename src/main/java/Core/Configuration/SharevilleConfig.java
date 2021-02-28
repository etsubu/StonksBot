package Core.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class SharevilleConfig {
    private List<String> sharevilleProfiles;
    private Long sharevilleChannel;

    public List<String> getSharevilleProfiles() { return Optional.ofNullable(sharevilleProfiles).orElseGet(LinkedList::new); }

    public Long getSharevilleChannel() { return sharevilleChannel; }
}
