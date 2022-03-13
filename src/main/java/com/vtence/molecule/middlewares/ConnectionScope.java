package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionScope implements Middleware {

    private final DataSource dataSource;

    public ConnectionScope(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Application then(Application next) {
        return request -> {
            Connection connection = dataSource.getConnection();
            var ref = new Reference(request);

            ref.set(connection);
            try {
                return next.handle(request).whenComplete((result, error) -> dispose(ref));
            } catch (Throwable e) {
                dispose(ref);
                throw e;
            }
        };
    }

    private void dispose(Reference ref) {
        close(ref.get());
        ref.unset();
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    public static class Reference {
        private final Request request;

        public Reference(Request request) {
            this.request = request;
        }

        public Connection get() {
            return request.attribute(Connection.class);
        }

        private void set(Connection connection) {
            request.attribute(Connection.class, connection);
        }

        public void unset() {
            request.removeAttribute(Connection.class);
        }
    }
}