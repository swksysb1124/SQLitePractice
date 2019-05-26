package crop.computer.askey.sqlitepractice.model;

public class News {

    public String title;
    public String subtitle;

    public News(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public String toString() {
        return "[title: "+title +", subtitle: "+subtitle+"]";
    }
}
