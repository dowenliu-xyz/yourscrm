package cn.yourscrm.ops;

import lombok.RequiredArgsConstructor;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StatefulPromptProvider implements PromptProvider {
    private final StateHolder stateHolder;

    @Override
    public AttributedString getPrompt() {
        var base = new AttributedString("yourscrm",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        AttributedString state = stateHolder.describe();
        var suffix = new AttributedString(":>",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        return AttributedStringBuilder.append(base, "(", state, ")", suffix);
    }
}
