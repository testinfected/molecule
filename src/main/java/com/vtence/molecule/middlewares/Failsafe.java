package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.vtence.molecule.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static com.vtence.molecule.http.MimeTypes.HTML;

public class Failsafe extends AbstractMiddleware {

    public Failsafe() {}

    public void handle(Request request, Response response) throws Exception {
        try {
            forward(request, response);
        } catch (Throwable error) {
            failsafeResponse(error, response);
        }
    }

    private void failsafeResponse(Throwable error, Response response) throws IOException {
        response.status(INTERNAL_SERVER_ERROR);
        response.contentType(HTML + "; charset=utf-8");
        response.body(formatAsHtml(error));
    }

    private String formatAsHtml(Throwable error) {
        StringWriter html = new StringWriter();
        PrintWriter buffer = new PrintWriter(html);
        buffer.println("<h1>Oups!</h1>");
        buffer.println("<h2>Sorry, an internal error occurred</h2>");
        startPrintingError(buffer, error);

        return html.toString();
    }

    private void startPrintingError(PrintWriter buffer, Throwable error) {
        printErrorAndCause(buffer, error, false);
    }

    private void printErrorAndCause(PrintWriter buffer, Throwable error, boolean addCausedBy) {
        buffer.println("<p>");
        buffer.printf("  <strong>%s%s</strong>", addCausedBy ? "Caused by: " : "", error).println();
        buffer.println("  <ul>");
        for (StackTraceElement stackTraceElement : error.getStackTrace()) {
            buffer.println("    <li>" + stackTraceElement.toString() + "</li>");
        }
        buffer.println("  </ul>");
        buffer.println("</p>");
        if (error.getCause() != null) {
            buffer.println();
            printErrorAndCause(buffer, error.getCause(), true);
        }
    }
}