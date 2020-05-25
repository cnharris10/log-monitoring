package Models;

public class Record {

    protected Integer date;
    protected Integer status;

    protected Record(){ }

    protected Record(Integer date, Integer status) {
        setDate(date);
        setStatus(status);
    }

    public void setDate(String date) {
        this.date = Integer.parseInt(date);
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getDate() {
        return date;
    }

    public void setStatus(String status) {
        this.status = Integer.parseInt(status);
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

}
