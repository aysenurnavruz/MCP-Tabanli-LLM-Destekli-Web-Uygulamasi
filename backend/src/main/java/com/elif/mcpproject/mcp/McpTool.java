package com.elif.mcpproject.mcp;

import java.security.Principal;
import java.util.Map;

public interface McpTool {
    String name();
    String title();
    String description();
    Map<String, Object> inputSchema();
    Object call(Map<String, Object> arguments, Principal principal);
}
