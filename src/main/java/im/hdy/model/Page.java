package im.hdy.model;

/**
 * Created by hdy on 04/01/2018.
 * 用于存放信息
 */
public class Page {
    private String title;
    private String text;

    public Page(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Page{" +
                "title='" + title + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
