package com.github.jlangch.venice.util.servlet;


public enum HttpStatus {

	/* 2XX: generally "OK" */

    HTTP_OK(200, "OK"),

    HTTP_CREATED(201,"Created"),

    HTTP_ACCEPTED(202, "Accepted"),

    HTTP_NOT_AUTHORITATIVE(203, "Non-Authoritative Information"),

    HTTP_NO_CONTENT(204, "No Content"),

    HTTP_RESET(205, "Reset Content"),

    HTTP_PARTIAL(206, "Partial Content"),


    /* 3XX: relocation/redirect */

    HTTP_MULT_CHOICE(300, "Multiple Choices"),

    HTTP_MOVED_PERM(301, "Moved Permanently"),

    HTTP_MOVED_TEMP(302, "Temporary Redirect"),

    HTTP_SEE_OTHER(303, "See Other"),

    HTTP_NOT_MODIFIED(304, "Not Modified"),

    HTTP_USE_PROXY(305, "Use Proxy"),


    /* 4XX: client error */

    HTTP_BAD_REQUEST(400, "Bad Request"),

    HTTP_UNAUTHORIZED(401, "Unauthorized"),

    HTTP_PAYMENT_REQUIRED(402,"Payment Required"),

    HTTP_FORBIDDEN(403, "Forbidden"),

    HTTP_NOT_FOUND(404, "Not Found"),

    HTTP_BAD_METHOD(405, "Method Not Allowed"),

    HTTP_NOT_ACCEPTABLE(406, "Not Acceptable"),

    HTTP_PROXY_AUTH(407, "Proxy Authentication Required"),

    HTTP_CLIENT_TIMEOUT(408, "Request Time-Out"),

    HTTP_CONFLICT(409, "Conflict"),

    HTTP_GONE(410, "Gone"),

    HTTP_LENGTH_REQUIRED(411, "Length Required"),

    HTTP_PRECON_FAILED(412, "Precondition Failed"),

    HTTP_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),

    HTTP_REQ_TOO_LONG(414, "Request-URI Too Large"),

    HTTP_UNSUPPORTED_TYPE(415, "Unsupported Media Type"),


    /* 5XX: server error */

    HTTP_INTERNAL_ERROR(500, "Internal Server Error"),

    HTTP_NOT_IMPLEMENTED(501, "Not Implemented"),

    HTTP_BAD_GATEWAY(502, "Bad Gateway"),

    HTTP_UNAVAILABLE(503, "Service Unavailable"),

    HTTP_GATEWAY_TIMEOUT(504, "Gateway Timeout"),

    HTTP_VERSION(505, "HTTP Version Not Supported");




    public int code() {
		return code;
	}

	public String description() {
		return description;
	}

	public static HttpStatus of(final int code) {
		for (HttpStatus s : values()) {
			if (s.code() == code) {
				return s;
			}
		}

		throw new RuntimeException("Invalid HttpStatus code " + code);
	}


	@Override
    public String toString() {
        return name();
    }


    private HttpStatus(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    private final int code;
    private final String description;
}
