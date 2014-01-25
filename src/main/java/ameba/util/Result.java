package ameba.util;

/**
 * @author: ICode
 * @since: 13-8-21 上午3:28
 */
public class Result {
    private boolean success;
    private String  message;
    private Integer code;

    public Result(boolean success) {
        this.success = success;
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Result(boolean success, String message, Integer code) {
        this.success = success;
        this.message = message;
        this.code = code;
    }

    public static Result success() {
        return new Result(true);
    }

    public static Result failure() {
        return new Result(false);
    }

    public static Result success(String message) {
        return new Result(true, message);
    }

    public static Result failure(String message) {
        return new Result(false, message);
    }

    public static Result success(String message, Integer code) {
        return new Result(true, message, code);
    }

    public static Result failure(String message, Integer code) {
        return new Result(false, message, code);
    }

    public Integer getCode() {
        return code;
    }

    public Result setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public Result setSuccess(boolean success) {
        this.success = success;
        return this;
    }
}
