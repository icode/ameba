package ameba.message;

import ameba.core.Application;
import ameba.util.Result;
import com.google.common.collect.Lists;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.spi.ResponseErrorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

/**
 * <p>RootExceptionMapper class.</p>
 *
 * @author icode
 * @since 13-8-17 下午2:00
 */
@Priority(Priorities.ENTITY_CODER)
@Singleton
public class RootExceptionMapper implements ExceptionMapper<Throwable>, ResponseErrorMapper {

    public static final String DEFAULT_ERROR_MSG = "系统错误";

    public static final String ERROR_400_MSG = "错误的请求";
    public static final String ERROR_501_MSG = "未实现的请求";
    public static final String ERROR_401_MSG = "未授权的请求";
    public static final String ERROR_403_MSG = "服务器拒绝请求";
    public static final String ERROR_404_MSG = "请求地址不存在";
    public static final String ERROR_405_MSG = "不支持的请求方法";
    public static final String ERROR_406_MSG = "无法满足请求条件";
    public static final String ERROR_415_MSG = "不支持的媒体类型";

    public static final String ERROR_501_DESC = "服务器不具备完成请求的功能";
    public static final String ERROR_400_DESC = "服务器不理解请求的语法";
    public static final String ERROR_401_DESC = "请求要求身份验证，请先进行授权";
    public static final String ERROR_403_DESC = "由于一些原因服务器拒绝了该请求";
    public static final String ERROR_404_DESC = "服务器不存在所请求的资源";
    public static final String ERROR_405_DESC = "您访问的资源不支持该 HTTP Method 方法";
    public static final String ERROR_406_DESC =
            "请求的资源的内容特性无法满足请求头中的条件，因而无法生成响应实体";
    public static final String ERROR_415_DESC =
            "对于当前请求的方法和所请求的资源，请求中提交的实体并不是服务器中所支持的格式，因此请求被拒绝";


    private static final Logger logger = LoggerFactory.getLogger(RootExceptionMapper.class);

    @Inject
    private Application application;

    protected int getHttpStatus(Throwable exception) {
        int status = 500;
        if (exception instanceof InternalServerErrorException) {
            if (exception.getCause() instanceof MessageBodyProviderNotFoundException) {
                MessageBodyProviderNotFoundException e = (MessageBodyProviderNotFoundException) exception.getCause();
                if (e.getMessage().startsWith("MessageBodyReader")) {
                    status = 415;
                } else if (e.getMessage().startsWith("MessageBodyWriter")) {
                    status = 406;
                }
            }
        } else if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        }
        return status;
    }

    protected String parseMessage(Throwable exception, int status) {
        String msg = DEFAULT_ERROR_MSG;
        if (status < 500) {
            switch (status) {
                case 401:
                    msg = ERROR_401_MSG;
                    break;
                case 403:
                    msg = ERROR_403_MSG;
                    break;
                case 404:
                    msg = ERROR_404_MSG;
                    break;
                case 405:
                    msg = ERROR_405_MSG;
                    break;
                case 406:
                    msg = ERROR_406_MSG;
                    break;
                case 415:
                    msg = ERROR_415_MSG;
                    break;
                default:
                    msg = ERROR_400_MSG;
            }
        } else {
            switch (status) {
                case 501:
                    msg = ERROR_501_MSG;
                    break;
            }
        }

        return msg;
    }

    protected String parseDescription(Throwable exception, int status) {
        String desc = exception.getLocalizedMessage();
        if (status < 500) {
            switch (status) {
                case 401:
                    desc = ERROR_401_DESC;
                    break;
                case 403:
                    desc = ERROR_403_DESC;
                    break;
                case 404:
                    desc = ERROR_404_DESC;
                    break;
                case 405:
                    desc = ERROR_405_DESC;
                    break;
                case 406:
                    desc = ERROR_406_DESC;
                    break;
                case 415:
                    desc = ERROR_415_DESC;
                    break;
                default:
                    desc = ERROR_400_DESC;
            }
        } else {
            switch (status) {
                case 501:
                    desc = ERROR_501_DESC;
                    break;
            }
        }

        return desc;
    }

    protected List<Result.Error> parseErrors(Throwable exception, int status) {
        List<Result.Error> errors = Lists.newArrayList();

        Throwable cause = exception;
        while (cause != null) {
            StackTraceElement[] stackTraceElements = cause.getStackTrace();
            if (stackTraceElements != null && stackTraceElements.length > 0) {
                StackTraceElement stackTraceElement = stackTraceElements[0];
                String source = stackTraceElement.toString();
                Result.Error error = new Result.Error(
                        cause.getClass().getCanonicalName().hashCode(),
                        cause.getMessage());

                StringBuilder descBuilder = new StringBuilder();
                for (StackTraceElement element : stackTraceElements) {
                    descBuilder
                            .append(element.toString())
                            .append("\n");
                }

                error.setDescription(descBuilder.toString());
                error.setSource(source);

                errors.add(error);
            }
            cause = cause.getCause();
        }

        return errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(Throwable exception) {
        int status = getHttpStatus(exception);

        ErrorMessage message = new ErrorMessage(false);

        if (exception instanceof MappableException
                && exception.getCause() != null) {
            exception = exception.getCause();
        }

        message.setCode(exception.getClass().getCanonicalName().hashCode());
        message.setStatus(status);
        message.setThrowable(exception);
        message.setMessage(parseMessage(exception, status));
        message.setDescription(parseDescription(exception, status));
        if (application.getMode().isDev()) {
            message.setErrors(parseErrors(exception, status));
        }

        if (status == 500) {
            logger.error("系统发生错误", exception);
        }

        return Response.status(status).entity(message).build();
    }
}
