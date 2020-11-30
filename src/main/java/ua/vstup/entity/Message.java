package ua.vstup.entity;

import ua.vstup.annotation.Entity;
import ua.vstup.annotation.Id;

@Entity
public class Message {
    @Id
    private Long id;

    private String text;
    private String tag;
}
