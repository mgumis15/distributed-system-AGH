package zadanie.client;

public class Message {
    private final String nick;
    private final String text;

    public Message(String nick, String text) {
        this.nick = nick;
        this.text = text;
    }

    public String getNick() {
        return nick;
    }

    public String getText() {
        return text;
    }
}
