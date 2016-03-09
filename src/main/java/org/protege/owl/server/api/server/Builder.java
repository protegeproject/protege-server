package org.protege.owl.server.api.server;

@Deprecated
public interface Builder {

    @Deprecated
    void initialise(ServerConfiguration configuration);

    @Deprecated
    void addServerComponentFactory(ServerComponentFactory factory);

    @Deprecated
    void removeServerComponentFactory(ServerComponentFactory factory);

    @Deprecated
    void deactivate();

    @Deprecated
    boolean isUp();
}
