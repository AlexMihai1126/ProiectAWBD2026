package ro.fmi.awbd.config;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component("mediaLink")
public class MediaLinkHelper {

    public String toHref(String fileRef) {
        if (fileRef == null || fileRef.isBlank()) {
            return null;
        }

        String trimmedFileRef = fileRef.trim();
        try {
            URI uri = new URI(trimmedFileRef);
            String scheme = uri.getScheme();
            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getRawAuthority() != null
                    && !uri.getRawAuthority().isBlank()) {
                return trimmedFileRef;
            }
        } catch (URISyntaxException ignored) {
        }

        return null;
    }
}
