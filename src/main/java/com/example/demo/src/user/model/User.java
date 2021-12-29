package com.example.demo.src.user.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    @Column(length = 100, nullable = false, unique = true)
    private String id;
    @Column(length = 255, nullable = false)
    private String password;
    @Column(length = 255, nullable = true)
    private String nickname;
    private String status = "N";
    private Integer level = 1;
    @Column(length = 2000, nullable = true)
    private String imgurl;
    @Column(length = 45, nullable = true)
    private String title;
    @Transient
    private String jwt;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Pet> pets = new HashSet<>();

    @Builder
    public User(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public void addPet(Pet pet){
        this.getPets().add(pet);
        pet.setUser(this);
    }

    public void delPet(Pet pet){
        this.getPets().remove(pet);
        pet.setUser(null);
        pet.setStatus("D");
    }

}
