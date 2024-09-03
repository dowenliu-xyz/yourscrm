package cn.yourscrm.ops;

import cn.yourscrm.ops.db.SimpleSignKey;
import lombok.Getter;
import lombok.Setter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class StateHolder {
    private ConnectInfo connectInfo = null;
    private SimpleSignKey simpleSignKey;

    public boolean isConnected() {
        return connectInfo != null;
    }

    public AttributedString describe() {
        if (connectInfo == null) {
            return new AttributedString("⛓️‍💥 Not connected",
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
        }
        return new AttributedString("🔑 Connected",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @Getter
    @Setter
    public static class ConnectInfo {
        private String backendBaseUrl;
        private String dbHost;
        private int dbPort;
        private String dbSchema;
        private String dbUsername;
        private String dbPassword;
    }
}
