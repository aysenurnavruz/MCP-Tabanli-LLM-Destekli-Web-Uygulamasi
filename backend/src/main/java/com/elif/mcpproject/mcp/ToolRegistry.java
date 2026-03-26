package com.elif.mcpproject.mcp;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ToolRegistry {
    private final Map<String, McpTool> tools;

    public ToolRegistry(List<McpTool> toolList){
        this.tools = toolList.stream().collect(Collectors.toMap(McpTool::name, t -> t));
    }

    public Collection<McpTool> all() {return tools.values();}
    public McpTool require(String name){
        McpTool t = tools.get(name);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Tool not found: " + name);
        return t;
    }

    }
