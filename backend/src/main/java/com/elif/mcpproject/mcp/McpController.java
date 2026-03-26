package com.elif.mcpproject.mcp;

import com.elif.mcpproject.mcp.jsonrpc.JsonRpcRequest;
import org.springframework.web.bind.annotation.PostMapping;
import com.elif.mcpproject.mcp.jsonrpc.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mcp")
public class McpController {
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<JsonRpcResponse> handle(@RequestBody JsonRpcRequest req,
                                                  Principal principal,
                                                  HttpServletRequest httpReq) {
        String origin = httpReq.getHeader("Origin");

        if (req == null || req.id() == null || req.method() == null) {
            return ResponseEntity.badRequest()
                    .body(JsonRpcResponse.err(null, -32600, "Invalid Request",
                            Map.of("hint", "jsonrpc/id/method required")));
        }

        if (!"2.0".equals(req.jsonrpc())) {
            return ResponseEntity.badRequest().body(JsonRpcResponse.err(req.id(), -32600, "Invalid JSON_RPC version", null));
        }

        boolean authRequired = !req.method().equals("initialize")
                && !req.method().equals("notifications/initialized");

        if (authRequired && principal == null) {
            return ResponseEntity.status(401)
                    .body(JsonRpcResponse.err(req.id(), -32001, "Unauthorized",
                            Map.of("hint", "Missing/invalid Authorization: Bearer <accessToken>")));
        }

        return switch (req.method()) {
            case "initialize" -> ResponseEntity.ok(JsonRpcResponse.ok(req.id(), Map.of(
                    "protocolVersion", "2025-03-26",
                    "capabilities", Map.of("tools", Map.of("listChanged", false)),
                    "serverInfo", Map.of("name", "mcp-project-server", "version", "0.1.0")
            )));
            case "notifications/initialized" -> ResponseEntity.accepted().build();
            case "tools/list" -> ResponseEntity.ok(JsonRpcResponse.ok(req.id(), Map.of(
                    "tools", toolRegistry.all().stream().map(t -> Map.of(
                            "name", t.name(),
                            "title", t.title(),
                            "description", t.description(),
                            "inputSchema", t.inputSchema()


                    )).toList()
            )));
            case "tools/call" -> handleToolCall(req, principal);
            default -> ResponseEntity.ok(JsonRpcResponse.err(req.id(), -32601, "Method npt found", Map.of("method", req.method())));
        };
    }

    private ResponseEntity<JsonRpcResponse> handleToolCall(JsonRpcRequest req, Principal principal){
        Map<String, Object>p = req.params() == null ? Map.of() : req.params();
        String name = (String) p.get("name");
        Map<String, Object> arguments = (Map<String, Object>) p.getOrDefault("arguments", Map.of());

        if (name == null || name.isBlank()){
            return ResponseEntity.ok(JsonRpcResponse.err(req.id(), -32602, "Missing tool name", null));
        }
        try {
            McpTool tool = toolRegistry.require(name);
            Object toolResult = tool.call(arguments, principal);

            Map<String, Object> result = Map.of(
                    "content", List.of(
                            Map.of("type","json","json", toolResult)),
                    "isError",false
            );
            return ResponseEntity.ok(JsonRpcResponse.ok(req.id(), result));
        }catch (Exception e){
            Map<String, Object> result = Map.of(
                    "content", List.of(Map.of("type", "text","text","Tool error: " + e.getMessage())),
                    "isError", true
            );
            return ResponseEntity.ok(JsonRpcResponse.ok(req.id(),result));
        }
    }

    private String toJsonString(Object obj){
        try{
            return objectMapper.writeValueAsString(obj);
        }catch (Exception e){
            throw new RuntimeException("Failed to serialize tool result to JSON", e);
        }
    }
}
