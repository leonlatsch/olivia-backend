package de.leonlatsch.oliviabackend.dto.rabbitmq;

public class RMQPermissions {

    private String configure;

    private String write;

    private String read;

    public RMQPermissions() {
    }

    public RMQPermissions(String configure, String write, String read) {
        this.configure = configure;
        this.write = write;
        this.read = read;
    }

    public String getConfigure() {
        return configure;
    }

    public void setConfigure(String configure) {
        this.configure = configure;
    }

    public String getWrite() {
        return write;
    }

    public void setWrite(String write) {
        this.write = write;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}
