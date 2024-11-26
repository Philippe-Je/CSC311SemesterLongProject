package model;

public class Person {
    private Integer id;
    private String firstName;
    private String lastName;
    private String department;
    private Double performanceRating;
    private String email;
    private String imageURL;

    public Person() {
    }

    public Person(String firstName, String lastName, String department, Double performanceRating, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.performanceRating = performanceRating;
        this.email = email;
        this.imageURL = imageURL;
    }

    public Person(Integer id, String firstName, String lastName, String department, Double performanceRating, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.performanceRating = performanceRating;
        this.email = email;
        this.imageURL = imageURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public void setPerformanceRating(Double performanceRating) {
        this.performanceRating = performanceRating;
    }

    public Double getPerformanceRating() {
        return performanceRating;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }


    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", performanceRating=" + performanceRating +
                ", email='" + email + '\'' +
                '}';
    }
}