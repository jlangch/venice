/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.util.servlet;


/**
 * Defines the HTTP status codes
 */
public enum HttpStatus {

    /* 1XX:  */

    HTTP_CONTINUE(100, "Continue"),

    HTTP_SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    HTTP_PROCESSING(102, "Processing"),


    /* 2XX: generally "OK" */

    HTTP_OK(200, "OK"),

    HTTP_CREATED(201, "Created"),

    HTTP_ACCEPTED(202, "Accepted"),

    HTTP_NOT_AUTHORITATIVE(203, "Non-Authoritative Information"),

    HTTP_NO_CONTENT(204, "No Content"),

    HTTP_RESET(205, "Reset Content"),

    HTTP_PARTIAL(206, "Partial Content"),

    HTTP_MULTI_STATUSL(207, "Multi-Status"),

    HTTP_ALREADY_REPORTED(208, "Already Reported"),

    HTTP_IM_USED(226, "IM Used"),


    /* 3XX: relocation/redirect */

    HTTP_MULT_CHOICE(300, "Multiple Choices"),

    HTTP_MOVED_PERM(301, "Moved Permanently"),

    HTTP_MOVED_TEMP(302, "Temporary Redirect"),

    HTTP_SEE_OTHER(303, "See Other"),

    HTTP_NOT_MODIFIED(304, "Not Modified"),

    HTTP_USE_PROXY(305, "Use Proxy"),

    HTTP_TEMPORARY_REDIRECT(307, "Temporary Redirect"),


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

    HTTP_REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),

    HTTP_EXPECTATION_FAILED(417, "Expectation Failed"),

    HTTP_I_AM_A_TEAPOT(418, "I'm a teapot"),

    HTTP_INSUFFICIENT_SPACE_ON_RESOURCE(419, "Insufficient Space on Resource"),

    HTTP_METHOD_FAILURE(420, "Method Failure"),

    HTTP_DESTINATION_LOCKED(421, "Destination Locked"),

    HTTP_UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),

    HTTP_LOCKED(423, "Locked"),

    HTTP_FAILED_DEPENDENCY(424, "Failed Dependency"),

    HTTP_UPGRADE_REQUIRED(426, "Upgrade Required"),

    HTTP_PRECONDITION_REQUIRED(428, "Precondition Required"),

    HTTP_TOO_MANY_REQUESTS(429, "Too Many Requests"),

    HTTP_REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),


    /* 5XX: server error */

    HTTP_INTERNAL_ERROR(500, "Internal Server Error"),

    HTTP_NOT_IMPLEMENTED(501, "Not Implemented"),

    HTTP_BAD_GATEWAY(502, "Bad Gateway"),

    HTTP_UNAVAILABLE(503, "Service Unavailable"),

    HTTP_GATEWAY_TIMEOUT(504, "Gateway Timeout"),

    HTTP_VERSION(505, "HTTP Version Not Supported"),

	HTTP_VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),

    HTTP_INSUFFICIENT_STORAGE(507, "Insufficient Storage"),

    HTTP_LOOP_DETECTED(508, "Loop Detected"),

    HTTP_NOT_EXTENDED(510, "Not Extended");



    /**
     * @return the HTTP status code (integer)
     */
    public int code() {
        return code;
    }

    /**
     * @return the HTTP status short description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the HTTP status enum based on an integer value
     *
     * @param code A HTTP status code integer value
     * @return a HTTP status enum
     */
    public static HttpStatus of(final int code) {
        for (HttpStatus s : values()) {
            if (s.code() == code) {
                return s;
            }
        }

        throw new RuntimeException("Invalid HttpStatus code " + code);
    }


    /**
     * Checks if the passed HTTP status code is in the range
     * of the OK codes (200 ... 299)
     *
     * @param code A HTTP status code integer value
     * @return The corresponding HTTP status enum
     */
    public static boolean isOkRange(final int code) {
        return code >= 200 && code <= 299;
    }

    /**
     * Checks if the passed HTTP status code is in the range
     * of the REDIRECT codes (300 ... 399)
     *
     * @param code A HTTP status code integer value
     * @return The corresponding HTTP status enum
     */
    public static boolean isRedirectRange(final int code) {
        return code >= 300 && code <= 399;
    }

    /**
     * Checks if the passed HTTP status code is in the range
     * of the CLIENT codes (400 ... 499)
     *
     * @param code A HTTP status code integer value
     * @return The corresponding HTTP status enum
     */
    public static boolean isClientRange(final int code) {
        return code >= 400 && code <= 499;
    }

    /**
     * Checks if the passed HTTP status code is in the range
     * of the SERVER ERROR codes (500 ... 599)
     *
     * @param code A HTTP status code integer value
     * @return The corresponding HTTP status enum
     */
    public static boolean isServerErrorRange(final int code) {
        return code >= 500 && code <= 599;
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
