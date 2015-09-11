package com.vtence.molecule.session;

public interface SessionPoolListener {

    SessionPoolListener NONE = new SessionPoolListener() {
        public void sessionCreated(String sid) {}

        public void sessionLoaded(String sid) {}

        public void sessionSaved(String sid) {}

        public void sessionDropped(String sid) {}
    };

    void sessionLoaded(String sid);

    void sessionCreated(String sid);

    void sessionSaved(String sid);

    void sessionDropped(String sid);
}