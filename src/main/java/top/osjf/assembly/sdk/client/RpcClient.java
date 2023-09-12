package top.osjf.assembly.sdk.client;

import top.osjf.assembly.sdk.process.Response;

/**
 * RPC based request client and now no support , if necessary, support will be added in the future.
 *
 * @author zpf
 * @since 1.1.0
 */
public class RpcClient<T extends Response> extends AbstractClient<T> {

    private static final long serialVersionUID = 8405526950849385906L;

    public RpcClient(String url) {
        super(url);
    }

    @Override
    public T request() {
        throw new UnsupportedOperationException("There is as yet no via RPC framework to call API, " +
                "if there is a will give support here");
    }
}
