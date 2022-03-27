package xyz.xcye.enums;

/**
 * @author qsyyke
 */

public enum ResultCode {

    //----------------------成功相关的响应码
    SUCCESS(20000,"成功"),


    //----------------------参数相关的响应码
    PARAM_TYPE_ERROR(31000,"参数类型错误"),
    PARAM_IS_INVALID(32000,"参数无效"),
    PARAM_IS_BLANK(33000,"参数为空"),
    PARAM_NOT_COMPLETE(34000,"参数缺失"),


    //----------------------异常相关的响应码
    EXCEPTION_FILE_FAIL_CREATE(11100,"文件写入失败"),
    EXCEPTION_FILE_PERMISSION(11200,"没有权限操作文件"),
    EXCEPTION_FILE_NOT_FOUND(11300,"未发现该文件"),
    EXCEPTION_FILE_ALREADY_EXIST(11400,"已经存在该文件"),


    //----------------------异常相关的响应码--------未知异常
    EXCEPTION_UNKNOWN(12000,"未知异常"),


    //----------------------异常相关的响应码--------邮件相关异常
    EXCEPTION_EMAIL_SEND_PASSWORD_MISTAKE(13110,"发件者邮箱密码错误"),
    EXCEPTION_EMAIL_SEND_HOST_MISTAKE(13120,"发件者邮箱主机错误"),
    EXCEPTION_EMAIL_SEND_PROTOCOL_MISTAKE(13130,"发件者邮箱协议错误"),
    EXCEPTION_EMAIL_RECEIVE_SEND_FAILURE(13210,"邮件发送失败"),
    EXCEPTION_EMAIL_SEND_CONTENT_TO_LONG(13220,"发送的邮件内容太长"),


    //----------------------异常相关的响应码--------超时异常
    EXCEPTION_TIMEOUT(14000,"超时啦♪(^∇^*)"),


    //----------------------权限相关的响应码
    PERMISSION_DENIED(41000,"用户权限不足"),
    PERMISSION_UNAUTHORIZED(42000,"用户未认证"),
    PERMISSION_TOKEN_CREATE_FAILURE(43100,"token创建失败"),
    PERMISSION_TOKEN_EXPIRATION(43200,"token过期"),
    PERMISSION_USER_NOT_LOGIN(44100,"未登录"),
    PERMISSION_USER_MISTAKE(44200,"账号不存在或者密码错误"),
    PERMISSION_USER_IS_DISABLE(44300,"账户已被禁用"),
    PERMISSION_USER_NOT_EXIST(44400,"用户不存在"),
    PERMISSION_USER_EXIST(44500,"用户已存在"),
    PERMISSION_USER_NOT_EMAIL_UNAUTHORIZED(44610,"邮箱未验证"),


    //----------------------未知错误相关的响应码
    UNKNOWN(50000,"未知错误");

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应码描述
     */
    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}