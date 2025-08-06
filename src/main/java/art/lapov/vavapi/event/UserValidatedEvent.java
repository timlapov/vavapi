package art.lapov.vavapi.event;

import art.lapov.vavapi.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
public class UserValidatedEvent extends ApplicationEvent {

    public UserValidatedEvent(Object source) {
        super(source);
    }

    public UserValidatedEvent(Object source, Clock clock) {
        super(source, clock);
    }

}
