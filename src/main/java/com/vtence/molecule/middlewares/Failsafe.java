package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.vtence.molecule.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static com.vtence.molecule.http.MimeTypes.HTML;

public class Failsafe extends AbstractMiddleware {

    public Failsafe() {}

    public void handle(Request request, Response response) throws Exception {
        try {
            forward(request, response).rescue(this::failsafeResponse);
        } catch (Throwable error) {
            failsafeResponse(response, error);
        }
    }

    private void failsafeResponse(Response response, Throwable error) {
        response.status(INTERNAL_SERVER_ERROR);
        response.contentType(HTML + "; charset=utf-8");
        response.body(formatAsHtml(error));
    }

    private String formatAsHtml(Throwable error) {
        StringWriter html = new StringWriter();
        PrintWriter buffer = new PrintWriter(html);
        buffer.println("<html>");
        buffer.println("<body>");
        buffer.println("<h1>Oups!</h1>");
        buffer.println("<h2>Sorry, an internal error occurred</h2>");
        printFullStackTrace(buffer, error);
        buffer.println("</body>");
        buffer.println("</html>");

        return html.toString();
    }

    private void printFullStackTrace(PrintWriter buffer, Throwable error) {
        printErrorAndCause(buffer, error, false);
    }

    private void printErrorAndCause(PrintWriter buffer, Throwable error, boolean isCause) {
        buffer.println("<p>");
        buffer.printf("  <strong>%s%s</strong>", isCause ? "Caused by: " : "", error).println();
        buffer.println("</p>");
        buffer.println("<ul>");
        for (StackTraceElement stackTraceElement : error.getStackTrace()) {
            buffer.println("  <li>" + stackTraceElement.toString() + "</li>");
        }
        buffer.println("</ul>");
        if (error.getCause() != null) {
            buffer.println();
            printErrorAndCause(buffer, error.getCause(), true);
        }
    }
}