package com.vtz.cbsbasic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private String name;
    private Location startLocation;
    private Location goalLocation;
}
