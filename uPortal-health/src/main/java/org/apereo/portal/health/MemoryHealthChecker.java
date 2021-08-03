package org.apereo.portal.health;

import com.sun.istack.NotNull;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MemoryHealthChecker implements IHealthChecker {
    @Override
    @NotNull
    public String getName() {
        return "Memory Health Checker";
    }

    @Override
    @NotNull
    public String getDetailIdentifier() {
        return "MEMORY";
    }

    @Override
    @NotNull
    public Map<String, Object> runCheck() throws RuntimeException {
        Map<String, Object> map = new HashMap<>();
        map.put("detail", getDetailIdentifier());
        map.put("Total Memory", Runtime.getRuntime().totalMemory());
        map.put("Max Memory", Runtime.getRuntime().maxMemory());
        map.put("Free Memory", Runtime.getRuntime().freeMemory());
        return map;
    }
}
