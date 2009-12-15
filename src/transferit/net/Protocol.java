package transferit.net;

/**
 * The Protocol interface defines two enums containing the commands 
 * used by the client and server. Commands sent by the client is specified 
 * in the ClientCommand enum and commands sent by the server is specified 
 * in the ServerCommand enum.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public interface Protocol {

    public enum ClientCommands {
        
        ABORT,
        GETFILE,
        GETFILELIST,
        GETRECURSIVEFILELIST,
        CREATEDIR,
        DELETE,
        SENDFILE,
        SENDAUTH,
        SENDMSG,
        SENDQUIT,
        NOVALUE;
    }

    public enum ServerCommands {

        AUTHED,
        AUTHWRITE,
        AUTHNOTWRITE,
        SENDMSG,
        SENDQUIT,
        NOVALUE;
    }
}
