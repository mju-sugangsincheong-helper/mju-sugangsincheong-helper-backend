package kr.mmv.mjusugangsincheonghelper.global.api.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {
    private String ip;
    private String userAgent;
    private String host;
    private String forwardedFor;
}
