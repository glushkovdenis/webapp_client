public class User {
    private Integer id = null;
    private String name = "";
    private String surname = "";
    private String patronymic = "";
    private String birth = "";

    public void setId(Integer id) { this.id = id;}
    public void setName(String name) {
        this.name = name;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }
    public void setBirth(String birth) { this.birth = birth; }

    public Integer getId() { return id;}
    public String getName() {
        return name;
    }
    public String getSurname() {
        return surname;
    }
    public String getPatronymic() {
        return patronymic;
    }
    public String getBirth() {
        return birth;
    }
}
