package ua.vstup.entity;

import ua.vstup.annotation.Entity;
import ua.vstup.annotation.Id;

@Entity
public class Test {
    @Id
    private Long id;

    private String name;
}
