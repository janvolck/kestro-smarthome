package be.kestro.smarthome.core.impl;

import be.kestro.smarthome.core.api.SmarthomeResource;
import org.osgi.service.component.annotations.Component;

@Component(
        immediate = true,
        name = "SmarthomeResource",
        property =
                {
                        "service.exported.interfaces=*",
                        "service.exported.configs=org.apache.cxf.rs",
                        "org.apache.cxf.rs.address=/smarthome"
                }
)
public class SmarthomeResourceImpl implements SmarthomeResource {

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVersion(String id) {
        return id + "=1.0";
    }
}
