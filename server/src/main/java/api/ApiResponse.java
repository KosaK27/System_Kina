package api;

public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    private ApiResponse(boolean success, String message, Object data) {
        this.success = success; this.message = message; this.data = data;
    }

    public static ApiResponse ok(Object data) { return new ApiResponse(true, null, data); }
    public static ApiResponse ok() { return new ApiResponse(true, null, null); }
    public static ApiResponse error(String m) { return new ApiResponse(false, m, null); }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}