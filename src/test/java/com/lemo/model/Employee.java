package com.lemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author 王兴岭
 * @create 2017-08-17 20:13
 */
public class Employee {

  private Integer id;
  @JsonProperty("first_name")
  private String firstName;
  @JsonProperty("last_name")
  private String lastName;
  private int age;
  private String about;
  private String[] interests;

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String[] getInterests() {
    return interests;
  }

  public void setInterests(String[] interests) {
    this.interests = interests;
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
}
