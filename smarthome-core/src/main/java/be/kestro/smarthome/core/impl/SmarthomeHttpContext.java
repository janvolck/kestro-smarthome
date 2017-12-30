package be.kestro.smarthome.core.impl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
        service = ServletContextHelper.class,
        scope = ServiceScope.BUNDLE,
        property = {
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + SmarthomeHttpContext.NAME,
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH + "=/api"
        }
)
public class SmarthomeHttpContext extends ServletContextHelper {

    public static final String NAME = "smarthome";

}
