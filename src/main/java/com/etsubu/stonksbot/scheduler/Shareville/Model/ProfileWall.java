package com.etsubu.stonksbot.scheduler.Shareville.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
public class ProfileWall {
    private final List<WallEntry> results;
}
