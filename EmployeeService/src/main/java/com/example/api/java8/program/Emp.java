package com.example.api.java8.program;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Emp {
    private Integer empId;
    private String name;
    private String depart;
    private Integer salary;

    @Override
    public String toString() {
        return "Emp{" +
                "empId=" + empId +
                ", name='" + name + '\'' +
                ", depart='" + depart + '\'' +
                ", salary=" + salary +
                '}';
    }
}
