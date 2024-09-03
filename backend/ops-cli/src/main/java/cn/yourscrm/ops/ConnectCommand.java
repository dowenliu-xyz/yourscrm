package cn.yourscrm.ops;

import cn.yourscrm.ops.rest.RestUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.http.HttpHeaders;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.ResultMode;
import org.springframework.shell.context.InteractionMode;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.net.URL;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Command
public class ConnectCommand {
    private final StateHolder stateHolder;
    private final OpsDefaultsProps defaultsProps;
    private final ComponentFlow.Builder componentFlowBuilder;
    private final RestClient.Builder restClientBuilder;

    @SuppressWarnings("unused")
    @Command(command = "connect", description = "Set up and test the connection to the server",
            interactionMode = InteractionMode.INTERACTIVE)
    public void runSetupFlowAndTest() {
        StateHolder.ConnectInfo connectInfo = runConnectInfoCollectLoop();
        if (connectInfo == null) return;
        if (verify(connectInfo)) {
            stateHolder.setConnectInfo(connectInfo);
        }
    }

    @Nullable
    private StateHolder.ConnectInfo runConnectInfoCollectLoop() {
        StateHolder.ConnectInfo connectInfo = new StateHolder.ConnectInfo();
        connectInfo.setBackendBaseUrl("http://localhost:8080");
        while (true) {
            ComponentFlow.Builder builder = componentFlowBuilder.clone().reset();

            var backendBaseUrlInput = builder.withStringInput("backendBaseUrl")
                    .name("backendBaseUrl")
                    .next(ctx -> {
                        String backendBaseUrl = ctx.getResultValue();
                        try {
                            URI uri = URI.create(backendBaseUrl);
                            URL ignored = uri.toURL();
                            connectInfo.setBackendBaseUrl(backendBaseUrl);
                            return "dbHost";
                        } catch (Exception e) {
                            System.out.println(new AttributedString("Invalid URL: " + backendBaseUrl,
                                    AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
                            return null;
                        }
                    });
            if (StringUtils.hasText(connectInfo.getBackendBaseUrl())) {
                backendBaseUrlInput.resultValue(connectInfo.getBackendBaseUrl()).resultMode(ResultMode.VERIFY);
            } else {
                backendBaseUrlInput.defaultValue(defaultsProps.getBackendBaseUrl());
            }

            var dbHostInput = backendBaseUrlInput.and().withStringInput("dbHost").name("dbHost")
                    .next(ctx -> {
                        String dbHost = ctx.getResultValue();
                        if (StringUtils.hasText(dbHost)) {
                            connectInfo.setDbHost(dbHost);
                            return "dbPort";
                        }
                        System.out.println(new AttributedString("dbHost is required",
                                AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
                        return null;
                    });
            if (StringUtils.hasText(connectInfo.getDbHost())) {
                dbHostInput.resultValue(connectInfo.getDbHost()).resultMode(ResultMode.VERIFY);
            } else {
                dbHostInput.defaultValue(defaultsProps.getDbHost());
            }

            var dbPortInput = dbHostInput.and().withStringInput("dbPort").name("dbPort")
                    .next(ctx -> {
                        String dbPort = ctx.getResultValue();
                        try {
                            int port = Integer.parseInt(dbPort);
                            if (port > 0 && port < 65536) {
                                connectInfo.setDbPort(port);
                                return "dbSchema";
                            }
                        } catch (NumberFormatException ignored) {
                        }
                        System.out.println(new AttributedString("Invalid port: " + dbPort,
                                AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
                        return null;
                    });
            if (connectInfo.getDbPort() != 0) {
                dbPortInput.resultValue(connectInfo.getDbPort() + "").resultMode(ResultMode.VERIFY);
            } else {
                dbPortInput.defaultValue(defaultsProps.getDbPort() + "");
            }

            var dbSchemaInput = dbPortInput.and().withStringInput("dbSchema").name("dbSchema")
                    .next(ctx -> {
                        String dbSchema = ctx.getResultValue();
                        if (StringUtils.hasText(dbSchema)) {
                            connectInfo.setDbSchema(dbSchema);
                            return "dbUsername";
                        }
                        System.out.println(new AttributedString("dbSchema is required",
                                AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
                        return null;
                    });
            if (StringUtils.hasText(connectInfo.getDbSchema())) {
                dbSchemaInput.resultValue(connectInfo.getDbSchema()).resultMode(ResultMode.VERIFY);
            } else {
                dbSchemaInput.defaultValue(defaultsProps.getDbSchema());
            }

            var dbUsernameInput = dbSchemaInput.and().withStringInput("dbUsername").name("dbUsername")
                    .next(ctx -> {
                        String dbUsername = ctx.getResultValue();
                        if (StringUtils.hasText(dbUsername)) {
                            connectInfo.setDbUsername(dbUsername);
                            return "dbPassword";
                        }
                        System.out.println(new AttributedString("dbUsername is required",
                                AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
                        return null;
                    });
            if (StringUtils.hasText(connectInfo.getDbUsername())) {
                dbUsernameInput.resultValue(connectInfo.getDbUsername()).resultMode(ResultMode.VERIFY);
            } else {
                dbUsernameInput.defaultValue(defaultsProps.getDbUsername());
            }

            var dbPasswordInput = dbUsernameInput.and().withStringInput("dbPassword").name("dbPassword")
                    .maskCharacter('*')
                    .next(ctx -> {
                        String dbPassword = ctx.getResultValue();
                        if (StringUtils.hasText(dbPassword)) {
                            connectInfo.setDbPassword(dbPassword);
                            return "confirm";
                        }
                        System.out.println(new AttributedString("dbPassword is required",
                                AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
                        return null;
                    });
            if (StringUtils.hasText(connectInfo.getDbPassword())) {
                dbPasswordInput.resultValue(connectInfo.getDbPassword()).resultMode(ResultMode.VERIFY);
            } else {
                dbPasswordInput.defaultValue(defaultsProps.getDbPassword());
            }

            var confirmInput = dbPasswordInput.and().withConfirmationInput("confirm").name("confirm");
            ComponentFlow flow = confirmInput.and().build();

            ComponentFlow.ComponentFlowResult result = flow.run();
            if (!result.getContext().containsKey("confirm")) continue;
            Boolean confirm = result.getContext().get("confirm", Boolean.class);
            return confirm ? connectInfo : null;
        }
    }

    private boolean verify(StateHolder.ConnectInfo connectInfo) {
        StateHolder mockHolder = new StateHolder();
        mockHolder.setConnectInfo(connectInfo);
        String token;
        try {
            token = RestUtil.genToken(mockHolder);
        } catch (IllegalStateException e) {
            System.out.println("Failed to sign a token: " + e.getMessage());
            return false;
        }
        boolean restCallOk = testCallBackend(connectInfo, token);
        if (restCallOk) {
            stateHolder.setSimpleSignKey(mockHolder.getSimpleSignKey());
        }
        return restCallOk;
    }

    private boolean testCallBackend(StateHolder.ConnectInfo connectInfo, String token) {
        RestClient client = restClientBuilder.clone().baseUrl(connectInfo.getBackendBaseUrl()).build();
        try {
            String body = client.get()
                    .uri("/ops/hello")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(String.class);
            if (!StringUtils.hasText(body)) {
                System.out.println("Unexpected blank response");
                return false;
            }
        } catch (RestClientResponseException e) {
            System.out.println("Failed to call the backend: " + e.getMessage());
            return false;
        }
        return true;
    }
}
