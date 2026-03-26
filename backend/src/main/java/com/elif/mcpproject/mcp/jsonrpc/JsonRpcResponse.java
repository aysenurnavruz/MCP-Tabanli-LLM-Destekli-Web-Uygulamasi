package com.elif.mcpproject.mcp.jsonrpc;

public record JsonRpcResponse(
        String jsonrpc,
        Object id,
        Object result,
        JsonRpcError error
) {
    public static JsonRpcResponse ok(Object id, Object result){
        return new JsonRpcResponse("2.0",id,result,null);
    }

    public static JsonRpcResponse err(Object id, int code,String message, Object data){
        return new JsonRpcResponse("2.0",id,null,new JsonRpcError(code,message,data));
    }
}
