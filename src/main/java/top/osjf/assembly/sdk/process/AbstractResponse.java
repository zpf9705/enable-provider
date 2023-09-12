package top.osjf.assembly.sdk.process;


import org.apache.http.HttpStatus;

/**
 * Response abstract node class, used to define common states, unknown error messages, success plans, etc.
 * <p>
 * You can check the example code:
 * <pre>
 * {@code
 * public class TestR extends AbstractResponse {
 *
 *     private Boolean success;
 *
 *     private Integer code;
 *
 *     private String message;
 *
 *     private Object errors;
 *
 *     private List<Supplier> data;
 * }}
 * </pre>
 *
 * @author zpf
 * @since 1.1.0
 */
public abstract class AbstractResponse implements Response {

    private static final long serialVersionUID = 6922151145018976148L;

    public static final Integer DATA_ERROR_CODE = 600558;

    private static final Integer UNKNOWN_ERROR_CODE = 500358;

    private static final boolean DEFAULT_IS_SUCCESS = false;

    private static final String DEFAULT_MESSAGE = "Please inherited [AbstractResponse]";

    @Override
    public boolean isSuccess() {
        return DEFAULT_IS_SUCCESS;
    }

    @Override
    public String getMessage() {
        return buildUnknownResponse(DEFAULT_MESSAGE).getMessage();
    }

    public static DefaultResponse buildResponse(String message) {
        return new DefaultResponse(HttpStatus.SC_BAD_REQUEST, message);
    }

    public static DefaultResponse buildUnknownResponse(String message) {
        return new DefaultResponse(UNKNOWN_ERROR_CODE, String
                .format("happen unknown exception,message=[%s]", message));
    }

    public static DefaultResponse buildDataErrorResponse(String message) {
        return new DefaultResponse(DATA_ERROR_CODE, String
                .format("happen data_error exception,message=[%s]", message));
    }
}
