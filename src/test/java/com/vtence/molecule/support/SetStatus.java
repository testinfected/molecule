package com.vtence.molecule.support;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Response;

public class SetStatus implements Action {
    public static Action setStatus(HttpStatus status) {
        return new SetStatus(status);
    }

    private HttpStatus status;

    public SetStatus(HttpStatus status) {
        this.status = status;
    }

    public Object invoke(Invocation invocation) throws Throwable {
        Response response = (Response) invocation.getParameter(1);
        response.status(status);
        return null;
    }

    public void describeTo(Description description) {
        description.appendText("sets status ").appendValue(status.code);
    }
}
